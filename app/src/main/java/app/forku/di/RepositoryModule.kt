package app.forku.di

import app.forku.core.location.LocationManager
import app.forku.domain.repository.vehicle.VehicleStatusChecker
import app.forku.data.datastore.AuthDataStore
import app.forku.data.repository.vehicle.VehicleRepositoryImpl
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.data.repository.incident.IncidentRepositoryImpl
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.data.repository.checklist.ChecklistRepositoryImpl
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase
import app.forku.domain.repository.vehicle.VehicleStatusRepository
import app.forku.data.repository.vehicle.VehicleStatusRepositoryImpl
import app.forku.data.repository.vehicle_session.VehicleSessionRepositoryImpl
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.repository.vehicle.VehicleStatusUpdater
import app.forku.data.repository.vehicle.VehicleStatusUpdaterImpl
import app.forku.domain.repository.session.SessionStatusChecker
import app.forku.data.repository.vehicle_session.VehicleSessionStatusCheckerImpl
import app.forku.domain.repository.checklist.ChecklistStatusNotifier
import app.forku.data.repository.checklist.ChecklistStatusNotifierImpl
import app.forku.data.repository.cico.CicoHistoryRepositoryImpl
import app.forku.domain.service.VehicleValidationService
import app.forku.data.service.VehicleValidationServiceImpl
import app.forku.domain.service.VehicleStatusDeterminer
import app.forku.data.service.VehicleStatusDeterminerImpl
import app.forku.data.repository.notification.NotificationRepositoryImpl
import app.forku.domain.repository.notification.NotificationRepository
import app.forku.domain.repository.cico.CicoHistoryRepository
import app.forku.data.api.VehicleApi
import app.forku.data.api.ChecklistApi
import app.forku.data.api.VehicleSessionApi
import app.forku.data.api.IncidentApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideVehicleStatusUpdater(
        api: VehicleApi
    ): VehicleStatusUpdater {
        return VehicleStatusUpdaterImpl(api)
    }

    @Provides
    @Singleton
    fun provideSessionStatusChecker(
        api: VehicleSessionApi
    ): SessionStatusChecker {
        return VehicleSessionStatusCheckerImpl(api)
    }

    @Provides
    @Singleton
    fun provideVehicleStatusDeterminer(): VehicleStatusDeterminer {
        return VehicleStatusDeterminerImpl()
    }

    @Provides
    @Singleton
    fun provideChecklistStatusNotifier(
        vehicleStatusUpdater: VehicleStatusUpdater,
        vehicleStatusDeterminer: VehicleStatusDeterminer,
        authDataStore: AuthDataStore
    ): ChecklistStatusNotifier {
        return ChecklistStatusNotifierImpl(
            vehicleStatusUpdater = vehicleStatusUpdater,
            vehicleStatusDeterminer = vehicleStatusDeterminer,
            authDataStore = authDataStore
        )
    }

    @Provides
    @Singleton
    fun provideChecklistRepository(
        api: ChecklistApi,
        authDataStore: AuthDataStore,
        validateChecklistUseCase: ValidateChecklistUseCase,
        checklistStatusNotifier: ChecklistStatusNotifier,
        locationManager: LocationManager
    ): ChecklistRepository {
        return ChecklistRepositoryImpl(
            api = api,
            authDataStore = authDataStore,
            validateChecklistUseCase = validateChecklistUseCase,
            checklistStatusNotifier = checklistStatusNotifier,
            locationManager = locationManager
        )
    }

    @Provides
    @Singleton
    fun provideVehicleValidationService(
        sessionStatusChecker: SessionStatusChecker,
        checklistRepository: ChecklistRepository,
        vehicleStatusDeterminer: VehicleStatusDeterminer
    ): VehicleValidationService {
        return VehicleValidationServiceImpl(
            sessionStatusChecker = sessionStatusChecker,
            checklistRepository = checklistRepository,
            vehicleStatusDeterminer = vehicleStatusDeterminer
        )
    }

    @Provides
    @Singleton
    fun provideVehicleStatusRepository(
        vehicleValidationService: VehicleValidationService,
        vehicleStatusUpdater: VehicleStatusUpdater
    ): VehicleStatusRepository {
        return VehicleStatusRepositoryImpl(vehicleValidationService, vehicleStatusUpdater)
    }

    @Provides
    @Singleton
    fun provideSessionRepository(
        api: VehicleSessionApi,
        authDataStore: AuthDataStore,
        vehicleStatusRepository: VehicleStatusRepository,
        checklistRepository: ChecklistRepository,
        locationManager: LocationManager
    ): VehicleSessionRepository {
        return VehicleSessionRepositoryImpl(
            api = api,
            authDataStore = authDataStore,
            vehicleStatusRepository = vehicleStatusRepository,
            checklistRepository = checklistRepository,
            locationManager = locationManager
        )
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideIncidentRepository(
        api: IncidentApi,
        authDataStore: AuthDataStore
    ): IncidentRepository {
        return IncidentRepositoryImpl(api, authDataStore)
    }

    @Provides
    @Singleton
    fun provideVehicleRepository(
        api: VehicleApi,
        authDataStore: AuthDataStore,
        validateChecklistUseCase: ValidateChecklistUseCase,
        vehicleStatusRepository: VehicleStatusRepository
    ): VehicleRepository {
        return VehicleRepositoryImpl(
            api = api,
            authDataStore = authDataStore,
            validateChecklistUseCase = validateChecklistUseCase,
            vehicleStatusRepository = vehicleStatusRepository
        )
    }

    @Provides
    @Singleton
    fun provideVehicleStatusChecker(
        vehicleStatusRepository: VehicleStatusRepository
    ): VehicleStatusChecker {
        return vehicleStatusRepository
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingsModule {
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        notificationRepositoryImpl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    abstract fun bindCicoHistoryRepository(
        repository: CicoHistoryRepositoryImpl
    ): CicoHistoryRepository
}