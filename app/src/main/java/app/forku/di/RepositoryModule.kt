package app.forku.di

import app.forku.core.business.BusinessContextManager
import app.forku.core.location.LocationManager
import app.forku.data.api.*
import app.forku.data.datastore.AuthDataStore
import app.forku.data.repository.business.BusinessRepositoryImpl
import app.forku.data.repository.checklist.ChecklistRepositoryImpl
import app.forku.data.repository.checklist.ChecklistStatusNotifierImpl
import app.forku.data.repository.cico.CicoHistoryRepositoryImpl
import app.forku.data.repository.country.CountryRepositoryImpl
import app.forku.data.repository.country.StateRepositoryImpl
import app.forku.data.repository.incident.IncidentRepositoryImpl
import app.forku.data.repository.notification.NotificationRepositoryImpl
import app.forku.data.repository.site.SiteRepositoryImpl
import app.forku.data.repository.user.UserRepositoryImpl
import app.forku.data.repository.vehicle.*
import app.forku.data.repository.vehicle_session.*
import app.forku.data.service.VehicleStatusDeterminerImpl
import app.forku.data.service.VehicleValidationServiceImpl
import app.forku.data.service.GOServicesManager
import app.forku.domain.repository.business.BusinessRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.checklist.ChecklistStatusNotifier
import app.forku.domain.repository.checklist.ChecklistQuestionVehicleTypeRepository
import app.forku.domain.repository.cico.CicoHistoryRepository
import app.forku.domain.repository.country.CountryRepository
import app.forku.domain.repository.country.StateRepository
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.domain.repository.notification.NotificationRepository
import app.forku.domain.repository.session.SessionStatusChecker
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.site.SiteRepository
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.vehicle.*
import app.forku.domain.service.VehicleStatusDeterminer
import app.forku.domain.service.VehicleValidationService
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import app.forku.data.repository.gogroup.*
import app.forku.domain.repository.gogroup.*
import app.forku.data.api.UserBusinessApi
import app.forku.data.repository.CollisionIncidentRepository
import app.forku.data.repository.NearMissIncidentRepository
import app.forku.data.repository.HazardIncidentRepository
import app.forku.data.repository.user.UserBusinessRepositoryImpl
import app.forku.domain.repository.user.UserBusinessRepository
import app.forku.data.repository.weather.WeatherRepositoryImpl
import app.forku.domain.repository.ICollisionIncidentRepository
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
import app.forku.domain.repository.weather.WeatherRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import javax.inject.Named
import app.forku.data.repository.VehicleFailIncidentRepositoryImpl
import app.forku.domain.repository.incident.VehicleFailIncidentRepository


@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindCicoHistoryRepository(
        repository: CicoHistoryRepositoryImpl
    ): CicoHistoryRepository

    @Binds
    @Singleton
    abstract fun bindCountryRepository(
        countryRepositoryImpl: CountryRepositoryImpl
    ): CountryRepository

    @Binds
    @Singleton
    abstract fun bindStateRepository(
        stateRepositoryImpl: StateRepositoryImpl
    ): StateRepository

    @Binds
    @Singleton
    abstract fun bindVehicleCategoryRepository(
        impl: VehicleCategoryRepositoryImpl
    ): VehicleCategoryRepository

    @Binds
    @Singleton
    abstract fun bindVehicleTypeRepository(
        repositoryImpl: VehicleTypeRepositoryImpl
    ): VehicleTypeRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(
        impl: VehicleRepositoryImpl
    ): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindSiteRepository(
        siteRepositoryImpl: SiteRepositoryImpl
    ): SiteRepository

    @Binds
    @Singleton
    abstract fun bindGOGroupRepository(
        repository: GOGroupRepositoryImpl
    ): GOGroupRepository

    @Binds
    @Singleton
    abstract fun bindGOGroupRoleRepository(
        repository: GOGroupRoleRepositoryImpl
    ): GOGroupRoleRepository

    @Binds
    @Singleton
    abstract fun bindGOFileUploaderRepository(
        repository: GOFileUploaderRepositoryImpl
    ): GOFileUploaderRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        repository: WeatherRepositoryImpl
    ): WeatherRepository

    // Incident repositories are now provided explicitly with BusinessContextManager
    // See RepositoryProvidersModule for @Provides methods
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryProvidersModule {
    @Provides
    @Singleton
    fun provideCollisionIncidentApi(@Named("authenticatedRetrofit") retrofit: Retrofit): CollisionIncidentApi {
        return retrofit.create(CollisionIncidentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHazardIncidentApi(@Named("authenticatedRetrofit") retrofit: Retrofit): HazardIncidentApi {
        return retrofit.create(HazardIncidentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNearMissIncidentApi(@Named("authenticatedRetrofit") retrofit: Retrofit): NearMissIncidentApi {
        return retrofit.create(NearMissIncidentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserBusinessRepository(
        userBusinessApi: UserBusinessApi,
        userRepository: UserRepository
    ): UserBusinessRepository {
        return UserBusinessRepositoryImpl(userBusinessApi, userRepository)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideVehicleStatusUpdater(
        api: VehicleApi,
        goServicesManager: GOServicesManager,
        authDataStore: AuthDataStore
    ): VehicleStatusUpdater = VehicleStatusUpdaterImpl(
        api = api,
        goServicesManager = goServicesManager,
        authDataStore = authDataStore
    )

    @Provides
    @Singleton
    fun provideSessionStatusChecker(
        api: VehicleSessionApi
    ): SessionStatusChecker = VehicleSessionStatusCheckerImpl(api)

    @Provides
    @Singleton
    fun provideVehicleStatusDeterminer(): VehicleStatusDeterminer = 
        VehicleStatusDeterminerImpl()

    @Provides
    @Singleton
    fun provideChecklistStatusNotifier(
        vehicleStatusUpdater: VehicleStatusUpdater,
        vehicleStatusDeterminer: VehicleStatusDeterminer,
        authDataStore: AuthDataStore
    ): ChecklistStatusNotifier = ChecklistStatusNotifierImpl(
        vehicleStatusUpdater = vehicleStatusUpdater,
        vehicleStatusDeterminer = vehicleStatusDeterminer,
        authDataStore = authDataStore
    )

    @Provides
    @Singleton
    fun provideChecklistRepository(
        api: ChecklistApi,
        authDataStore: AuthDataStore,
        validateChecklistUseCase: ValidateChecklistUseCase,
        checklistStatusNotifier: ChecklistStatusNotifier,
        locationManager: LocationManager,
        businessContextManager: BusinessContextManager,
        checklistQuestionVehicleTypeRepository: ChecklistQuestionVehicleTypeRepository,
        vehicleTypeRepository: VehicleTypeRepository
    ): ChecklistRepository = ChecklistRepositoryImpl(
        api = api,
        authDataStore = authDataStore,
        validateChecklistUseCase = validateChecklistUseCase,
        checklistStatusNotifier = checklistStatusNotifier,
        locationManager = locationManager,
        businessContextManager = businessContextManager,
        checklistQuestionVehicleTypeRepository = checklistQuestionVehicleTypeRepository,
        vehicleTypeRepository = vehicleTypeRepository
    )

    @Provides
    @Singleton
    fun provideVehicleValidationService(
        sessionStatusChecker: SessionStatusChecker,
        checklistRepository: ChecklistRepository,
        vehicleStatusDeterminer: VehicleStatusDeterminer
    ): VehicleValidationService = VehicleValidationServiceImpl(
        sessionStatusChecker = sessionStatusChecker,
        checklistRepository = checklistRepository,
        vehicleStatusDeterminer = vehicleStatusDeterminer
    )

    @Provides
    @Singleton
    fun provideVehicleStatusRepository(
        vehicleValidationService: VehicleValidationService,
        vehicleStatusUpdater: VehicleStatusUpdater
    ): VehicleStatusRepository = VehicleStatusRepositoryImpl(
        vehicleValidationService, 
        vehicleStatusUpdater
    )

    @Provides
    @Singleton
    fun provideSessionRepository(
        api: VehicleSessionApi,
        authDataStore: AuthDataStore,
        vehicleStatusRepository: VehicleStatusRepository,
        vehicleRepository: VehicleRepository,
        checklistAnswerRepository: ChecklistAnswerRepository,
        locationManager: LocationManager,
        businessContextManager: BusinessContextManager,
        validateUserCertificationUseCase: app.forku.domain.usecase.certification.ValidateUserCertificationUseCase
    ): VehicleSessionRepository = VehicleSessionRepositoryImpl(
        api = api,
        authDataStore = authDataStore,
        vehicleStatusRepository = vehicleStatusRepository,
        vehicleRepository = vehicleRepository,
        checklistAnswerRepository = checklistAnswerRepository,
        locationManager = locationManager,
        businessContextManager = businessContextManager,
        validateUserCertificationUseCase = validateUserCertificationUseCase
    )

    @Provides
    @Singleton
    fun provideIncidentRepository(
        api: IncidentApi,
        collisionApi: CollisionIncidentApi,
        nearMissIncidentApi: NearMissIncidentApi,
        hazardIncidentApi: HazardIncidentApi,
        vehicleFailIncidentApi: VehicleFailIncidentApi,
        authDataStore: AuthDataStore,
        gson: Gson,
        businessContextManager: BusinessContextManager
    ): IncidentRepository = IncidentRepositoryImpl(
        api,
        collisionApi,
        nearMissIncidentApi,
        hazardIncidentApi,
        vehicleFailIncidentApi,
        authDataStore,
        gson,
        businessContextManager
    )

    @Provides
    @Singleton
    fun provideVehicleStatusChecker(
        vehicleStatusRepository: VehicleStatusRepository
    ): VehicleStatusChecker = vehicleStatusRepository

    // Specific incident repository providers with BusinessContextManager
    @Provides
    @Singleton
    fun provideNearMissIncidentRepository(
        api: NearMissIncidentApi,
        gson: Gson,
        businessContextManager: BusinessContextManager
    ): NearMissIncidentRepository = NearMissIncidentRepository(
        api = api,
        gson = gson,
        businessContextManager = businessContextManager
    )

    @Provides
    @Singleton
    fun provideHazardIncidentRepository(
        api: HazardIncidentApi,
        gson: Gson,
        businessContextManager: BusinessContextManager
    ): HazardIncidentRepository = HazardIncidentRepository(
        api = api,
        gson = gson,
        businessContextManager = businessContextManager
    )

    @Provides
    @Singleton
    fun provideCollisionIncidentRepository(
        api: CollisionIncidentApi,
        gson: Gson,
        businessContextManager: BusinessContextManager
    ): ICollisionIncidentRepository = CollisionIncidentRepository(
        api = api,
        gson = gson,
        businessContextManager = businessContextManager
    )

    @Provides
    @Singleton
    fun provideVehicleFailIncidentRepositoryImpl(
        api: VehicleFailIncidentApi,
        gson: Gson,
        businessContextManager: BusinessContextManager
    ): VehicleFailIncidentRepository = VehicleFailIncidentRepositoryImpl(
        api = api,
        gson = gson,
        businessContextManager = businessContextManager
    )
}