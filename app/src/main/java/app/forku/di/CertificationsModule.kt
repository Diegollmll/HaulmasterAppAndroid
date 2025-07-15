package app.forku.di

import app.forku.data.api.CertificationApi
import app.forku.data.api.CertificationVehicleTypeApi
import app.forku.data.api.CertificationMultimediaApi
import app.forku.data.repository.certification.CertificationRepositoryImpl
import app.forku.data.repository.certification.CertificationVehicleTypeRepositoryImpl
import app.forku.data.repository.certification.CertificationMultimediaRepositoryImpl
import app.forku.domain.repository.certification.CertificationRepository
import app.forku.domain.repository.certification.CertificationVehicleTypeRepository
import app.forku.domain.repository.certification.CertificationMultimediaRepository
import app.forku.domain.repository.vehicle.VehicleTypeRepository
import app.forku.domain.usecase.certification.GetUserCertificationsUseCase
import app.forku.domain.usecase.certification.GetCertificationByIdUseCase
import app.forku.domain.usecase.certification.CreateCertificationUseCase
import app.forku.domain.usecase.certification.UpdateCertificationUseCase
import app.forku.domain.usecase.certification.DeleteCertificationUseCase
import app.forku.domain.usecase.certification.GetVehicleTypesUseCase
import app.forku.domain.usecase.certification.GetCertificationVehicleTypesUseCase
import app.forku.domain.usecase.certification.SaveCertificationVehicleTypesUseCase
import app.forku.domain.usecase.certification.DeleteCertificationVehicleTypesUseCase
import app.forku.domain.usecase.certification.AddCertificationMultimediaUseCase
import app.forku.domain.usecase.certification.GetCertificationMultimediaUseCase
import app.forku.data.datastore.AuthDataStore
import app.forku.core.business.BusinessContextManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CertificationsModule {

    @Provides
    @Singleton
    fun provideCertificationVehicleTypeApi(@Named("authenticatedRetrofit") retrofit: Retrofit): CertificationVehicleTypeApi {
        return retrofit.create(CertificationVehicleTypeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCertificationMultimediaApi(@Named("authenticatedRetrofit") retrofit: Retrofit): CertificationMultimediaApi {
        return retrofit.create(CertificationMultimediaApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCertificationVehicleTypeRepository(
        api: CertificationVehicleTypeApi,
        authDataStore: AuthDataStore,
        businessContextManager: BusinessContextManager
    ): CertificationVehicleTypeRepository {
        return CertificationVehicleTypeRepositoryImpl(api, authDataStore, businessContextManager)
    }

    @Provides
    @Singleton
    fun provideCertificationMultimediaRepository(
        api: CertificationMultimediaApi,
        businessContextManager: BusinessContextManager
    ): CertificationMultimediaRepository {
        return CertificationMultimediaRepositoryImpl(api, businessContextManager)
    }

    @Provides
    @Singleton
    fun provideCertificationRepository(
        api: CertificationApi,
        authDataStore: AuthDataStore,
        businessContextManager: BusinessContextManager,
        certificationVehicleTypeRepository: CertificationVehicleTypeRepository,
        vehicleTypeRepository: VehicleTypeRepository
    ): CertificationRepository {
        return CertificationRepositoryImpl(
            api, 
            authDataStore, 
            businessContextManager, 
            certificationVehicleTypeRepository, 
            vehicleTypeRepository
        )
    }

    @Provides
    @Singleton
    fun provideGetUserCertificationsUseCase(
        repository: CertificationRepository
    ): GetUserCertificationsUseCase {
        return GetUserCertificationsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetCertificationByIdUseCase(
        repository: CertificationRepository
    ): GetCertificationByIdUseCase {
        return GetCertificationByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCreateCertificationUseCase(
        repository: CertificationRepository
    ): CreateCertificationUseCase {
        return CreateCertificationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateCertificationUseCase(
        repository: CertificationRepository
    ): UpdateCertificationUseCase {
        return UpdateCertificationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteCertificationUseCase(
        repository: CertificationRepository
    ): DeleteCertificationUseCase {
        return DeleteCertificationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetVehicleTypesUseCase(
        vehicleTypeRepository: VehicleTypeRepository
    ): GetVehicleTypesUseCase {
        return GetVehicleTypesUseCase(vehicleTypeRepository)
    }

    @Provides
    @Singleton
    fun provideGetCertificationVehicleTypesUseCase(
        repository: CertificationVehicleTypeRepository
    ): app.forku.domain.usecase.certification.GetCertificationVehicleTypesUseCase {
        return app.forku.domain.usecase.certification.GetCertificationVehicleTypesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSaveCertificationVehicleTypesUseCase(
        repository: CertificationVehicleTypeRepository
    ): app.forku.domain.usecase.certification.SaveCertificationVehicleTypesUseCase {
        return app.forku.domain.usecase.certification.SaveCertificationVehicleTypesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteCertificationVehicleTypesUseCase(
        repository: CertificationVehicleTypeRepository
    ): app.forku.domain.usecase.certification.DeleteCertificationVehicleTypesUseCase {
        return app.forku.domain.usecase.certification.DeleteCertificationVehicleTypesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideValidateUserCertificationUseCase(
        certificationRepository: CertificationRepository,
        vehicleRepository: app.forku.domain.repository.vehicle.VehicleRepository,
        businessContextManager: app.forku.core.business.BusinessContextManager
    ): app.forku.domain.usecase.certification.ValidateUserCertificationUseCase {
        return app.forku.domain.usecase.certification.ValidateUserCertificationUseCase(
            certificationRepository, 
            vehicleRepository,
            businessContextManager
        )
    }

    @Provides
    @Singleton
    fun provideAddCertificationMultimediaUseCase(
        repository: app.forku.domain.repository.certification.CertificationMultimediaRepository
    ): AddCertificationMultimediaUseCase {
        return AddCertificationMultimediaUseCase(repository)
    }

    @Provides
    @Singleton  
    fun provideGetCertificationMultimediaUseCase(
        repository: app.forku.domain.repository.certification.CertificationMultimediaRepository
    ): GetCertificationMultimediaUseCase {
        return GetCertificationMultimediaUseCase(repository)
    }
} 