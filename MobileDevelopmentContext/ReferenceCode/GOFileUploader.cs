using System.Net;
using System;
using System.Linq;
using System.Configuration;
using System.IO;
using System.Drawing;
using System.Threading.Tasks;
using System.Collections.Generic;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.AspNetCore.Http.Extensions;
using Microsoft.Identity.Client;
using Microsoft.Extensions.Configuration;
using Microsoft.AspNetCore.Http;

using GenerativeObjects.Practices.ORMSupportClasses;
using GenerativeObjects.Practices.LayerSupportClasses;
using GenerativeObjects.Practices.LayerSupportClasses.DataLayer;
using GenerativeObjects.Practices.ExceptionHandling;
using GenerativeObjects.Practices.LayerSupportClasses.Features.Threading;
using GenerativeObjects.Practices.LayerSupportClasses.BusinessLayer.Components;
using GenerativeObjects.Practices.LayerSupportClasses.Features.Storage.Common;
using GenerativeObjects.Practices.LayerSupportClasses.DataLayer.Extensions;
using GenerativeObjects.Practices.Settings;
using forkuapp.BusinessLayer.ORMSupportClasses;
using forkuapp.Data.DataObjects;
using GenerativeObjects.Practices.LayerSupportClasses.Features.Security.Common;


namespace forkuapp.BusinessLayer.Components.Server
{
	/// <summary>
	/// This is used in order to upload file on the server.
	/// </summary>
	public partial class GOFileUploader : BaseServerComponent, IGOFileUploader
		, IDataProviderExtension<UserMultimediaDataObject>
		, IDataProviderExtension<IncidentMultimediaDataObject>
		, IDataProviderExtension<VehicleDataObject>
		, IDataProviderExtension<BusinessConfigurationDataObject>
		, IDataProviderExtension<MultimediaDataObject>
		, IDataProviderExtension<VehicleMultimediaDataObject>
	{
		public GOFileUploader(IServiceProvider serviceProvider, IConfiguration configuration, ISettingsProvider settingsProvider, IDataFacade dataFacade, IThreadContext threadContext) : base(serviceProvider, configuration, dataFacade)
		{
			_threadContext = threadContext;
			_settingsProvider = settingsProvider;
		}

		private readonly IThreadContext _threadContext;
        private readonly ISettingsProvider _settingsProvider;
		protected IStorageProvider _storageProvider => _serviceProvider.GetServices<IStorageProvider>().Where(s => s.GetType().Name == _settingsProvider["StorageProvider"].ToString()).Single();

		/// <summary>
		/// Upload a file on a remote Server
		/// </summary>
		/// <returns>The Url of the file</returns>
		public Task<ComponentResponse<bool>> UploadFileAsync(Dictionary<string,object> parameters = null)
		{
			return Task.FromResult(new ComponentResponse<bool>(true));
		}

		// Get FileStream from Document url of the form ~/{entityname}/file/{entityid}/{documentFieldname}
		public async Task<Stream> GetDocumentFileStreamAsync<TEntity>(string documentUrl) 
			where TEntity : class, IDataObject
		{
			var fileUrl = await GetFullURLAsync<TEntity>(null, documentUrl);
			
			if(String.IsNullOrEmpty(fileUrl))
				throw new GOServerException($"No document field set for URL: '{documentUrl}'");

			return await _storageProvider.GetFileStream(fileUrl, System.IO.FileMode.Open);
		}

		// Get FileStream from Document url of the form ~/{entityname}/file/{entityid}/{documentFieldname}
		// This version is more efficient because if you've already got the instance, saves a trip to the db
		public async Task<Stream> GetDocumentFileStreamAsync<TEntity>(TEntity entity, string documentUrl) 
			where TEntity : class, IDataObject
		{
			var fileUrl = await GetFullURLAsync<TEntity>(entity, documentUrl);

			if(String.IsNullOrEmpty(fileUrl))
				throw new GOServerException($"No document field set for URL: '{documentUrl}'");

			return await _storageProvider.GetFileStream(fileUrl, System.IO.FileMode.Open);
		}

		// Given entity instance and document field name, get the full document URL
		public async Task<string> GetFullDocumentURLAsync<TEntity>(TEntity entity, string documentFieldName)
			where TEntity : class, IDataObject
		{
			string documentFieldUrlPropertyName = $"{documentFieldName}Url";
			var documentUrlProperty = entity.GetType().GetProperty(documentFieldUrlPropertyName);

			if (documentUrlProperty == null)
				throw new GOServerException($"Unknown property '{documentFieldUrlPropertyName}'");

			var url = documentUrlProperty.GetValue(entity);

			if (url == null)
				return null;

			if (!(url is String))
				throw new GOServerException($"Improper datatype for property {documentUrlProperty}");

			if (String.IsNullOrEmpty((string)url))
				return (string)url;

			return await GetFullURLAsync(entity, (string)url);
		}

		// Get FileStream from Document url of the form ~/{entityname}/file/{entityid}/{documentFieldname}
		public async Task<string> GetFullURLAsync<TEntity>(TEntity entity, string documentUrl) 
			where TEntity : class, IDataObject
		{
			if (String.IsNullOrEmpty(documentUrl))
				return documentUrl;
			
			// Get the file owning entity,
			// ~/{entityname}/file/{entityid}/{documentFieldname} 
			// Example document url:  api/reversefromexcel/file/b6e34ec0-d42c-457b-b9b6-7a9995647973/spreadsheet?dbKey=blaBlaBla 
			// pk starts at position 3, ends one before end (next item being the document field name)
			var urlParts = documentUrl.Split('?');
			var QueryParts = urlParts.First().Split('/').Skip(3);

			// if caller didn't provide entity, we need to fetch it
			if (entity == null)
			{
				var factory = _serviceProvider.GetRequiredService<IDataObjectFactory<TEntity>>();
				var provider = _serviceProvider.GetRequiredService<IDataProvider<TEntity>>();

				var pks = QueryParts.Take(QueryParts.Count() - 1);
				pks = pks.Select(pk => WebUtility.UrlDecode(pk));
				entity = await provider.GetAsync(factory.CreateDataObject(pks));

				if (entity == null)
					throw new ResourceNotFoundException();
			}

			// Get document internal 
			var documentInternalNameField = QueryParts.Last() + "InternalName";
			
			var documentInternalNameProperty = entity.GetType().GetProperty(documentInternalNameField);
			if (documentInternalNameProperty == null)
				throw new GOServerException("Unknown property " + documentInternalNameField);

			var documentInternalNameAsObject = documentInternalNameProperty.GetValue(entity, null);
			if(documentInternalNameAsObject == null) 
				return null;

			if (!(documentInternalNameAsObject is String))
				throw new GOServerException(String.Format("Unproper datatype for property {0}", documentInternalNameField));

			// Get document client name
			var documentClientNameField = QueryParts.Last();
			var documentClientNameFieldProperty = entity.GetType().GetProperty(documentClientNameField);

			if (documentClientNameFieldProperty == null)
				throw new GOServerException("Unknown property " + documentClientNameField);

			var documentClientNameAsObject = documentClientNameFieldProperty.GetValue(entity, null);
			if(documentClientNameAsObject == null) 
				return null;

			if (!(documentClientNameAsObject is String))
				throw new GOServerException(String.Format("Unproper datatype for property {0}", documentClientNameField));


			string documentInternalName = (string)documentInternalNameAsObject;
			string documentClientName = (string)documentClientNameAsObject;

			string storageContainer = String.IsNullOrEmpty(_configuration["StorageContainer"]) ? "files" : _configuration["StorageContainer"];

			return _threadContext.BaseUrl + storageContainer + "/" + documentInternalName;
		}

		#region image resizing

		public void Init(IDataProviderExtensionProvider dataProvider)
		{
			dataProvider.OnBeforeSaveDataSet += OnBeforeSaveDataSetAsync;
		}

		async Task OnBeforeSaveDataSetAsync(OnBeforeSaveDataSetEventArgs e)
		{
			// If delete in progress, no action required
			if (e.IsDeleteInProgress)
				return;

			if (e.Entity == null)
				return;

			string entityName = e.Entity.GetType().Name.Replace("DataObject", "");

			switch (entityName)
			{
				case "UserMultimedia":
					break;
				case "IncidentMultimedia":
					break;
				case "Vehicle":
					break;
				case "BusinessConfiguration":
					break;
				case "Multimedia":
					break;
				case "VehicleMultimedia":
					break;
				default:
					throw new GOServerException($"GOFileUploader, no handler found for entity type '{entityName}'");
			}
		}

		public async Task CheckImageDimensionsAsync<TEntity>(TEntity entity, string imageFieldName, int? maxWidth, int? maxHeight, bool canResize, bool cutToDimensions = false, bool stretchToMaxIfSmaller = false)
            where TEntity : class, IDataObject
        {
            // If no maxWidth or maxHeight, nothing to do
            if (maxWidth == null && maxHeight == null)
                return;

            maxWidth = maxWidth ?? int.MaxValue;
            maxHeight = maxHeight ?? int.MaxValue;
            string documentUrl = await GetFullDocumentURLAsync(entity, imageFieldName);

            if (String.IsNullOrEmpty(documentUrl))
                return;

            try
            {
                using (var rstream = await _storageProvider.GetFileStream(documentUrl, System.IO.FileMode.Open))
                {
                    using (var image = Image.FromStream(rstream))
                    {
						if(!canResize && (image.Width > maxWidth || image.Height > maxHeight)) 
                            throw new GOServerException("ImageTooLarge", $"Image dimensions ({image.Width} x {image.Height}) exceed the maximum allowed ({maxWidth} x {maxHeight})");

						// If resizing is allowed for this image, we proceed
                        using (var scaledImage = ScaleImage(image, (int)maxWidth, (int)maxHeight, cutToDimensions, stretchToMaxIfSmaller))
                        {
                            // Overwrite at same location we read image from => need to close rstream first
                            rstream.Close();
                            rstream.Dispose();

                            using (var wstream = await _storageProvider.GetFileStream(documentUrl, System.IO.FileMode.Create))
                            {
                                scaledImage.Save(wstream, image.RawFormat);
                                entity.GetType().GetProperty($"{imageFieldName}FileSize").SetValue(entity, (int)wstream.Length);
                            }
                        }
                    }
                }
            }
            catch (GOServerException)
            {
                // rethrow
                throw;
            }
            catch
            {
                // Eat all other errors, it's an internal operation - don't want to break apps because of problems resizing images
                // However TODO would want to log this error somewhere...
            }
        }

        /// <summary>
        /// Note : client side,  one can use the "object-fit : cover" css attribute to the img so that the same cropping is done client side as the one done server side with allowCutImage = true
        /// </summary>
        /// <param name="image"></param>
        /// <param name="maxWidth"></param>
        /// <param name="maxHeight"></param>
        /// <param name="cutToDimensions">
        /// When true, the output image should be exactly the maxWidth and maxHeight specified, cutting original image to these dimensions. 
        /// When false, output will be resized while keeping original image ratio.
        /// </param>
        /// <param name="stretchToMaxIfSmaller">When image is smaller than bounding box (max width/height), we stretch to fit the box if true, do nothing otherwise. This param has priority over "cutToDimensions". </param>
        /// <returns></returns>
		public Image ScaleImage(Image image, int maxWidth, int maxHeight, bool cutToDimensions, bool stretchToMaxIfSmaller)
        {
            // calculate x and y ration to fit maxWidth / maxHeight
            var ratioX = (double)maxWidth / image.Width;
            var ratioY = (double)maxHeight / image.Height;
            double ratio;
			
			int startX = 0;
			int startY = 0;
			double endX = image.Width;
			double endY = image.Height;
			int newWidth;
			int newHeight;
			int srcWidth = image.Width;
			int srcHeight = image.Height;

			if(!stretchToMaxIfSmaller) 
			{
				if (cutToDimensions)
				{
					// if we allow cut image, we use the max ratio, to keep image as big as possible before cutting
					ratio = Math.Max(ratioX, ratioY);
				}
				else
				{
					// otherwise, we use the min ratio, to have the image enter the maxWidth / maxHeight without losing the proportions
					ratio = Math.Min(ratioX, ratioY);
				}


				// if ratio x and y are different and we allow cutting, we need to define the rectangle to be cut inside original image
				if (ratioX != ratioY && cutToDimensions)
				{
					if (ratio > ratioX)
					{
						double extraWidth = ((image.Width * ratio) - maxWidth) / ratio;
						startX = (int)(extraWidth / 2);
						endX = image.Width - (extraWidth / 2);

						srcWidth = (int)(image.Width - extraWidth);
					}
					else
					{
						double extraHeight = ((image.Height * ratio) - maxHeight) / ratio;
						startY = (int)(extraHeight / 2);
						endY = image.Height - (extraHeight / 2);

						srcHeight = (int)(image.Height - extraHeight);
					}

					newWidth = maxWidth;
					newHeight = maxHeight;
				}
				// otherwise, we calculate the new size with calculated ratios
				// (in case of cut strategy, we are in the case where ratioX == ratioY so no cut needed)
				else
				{
					newWidth = (int)(image.Width * ratio);
					newHeight = (int)(image.Height * ratio);
				}
			}	
			else
			{
				newWidth = (int)(image.Width * ratioX);
				newHeight = (int)(image.Height * ratioY);
			}		

            // create new image and apply the ratio / cutting
            var newImage = new Bitmap(newWidth, newHeight);

            using (var graphics = Graphics.FromImage(newImage))
                graphics.DrawImage(image, new Rectangle(0, 0, newWidth, newHeight), startX, startY, srcWidth, srcHeight, GraphicsUnit.Pixel);

            return newImage;
        }


		#endregion
	}
}
