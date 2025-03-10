package app.forku.di

import app.forku.domain.repository.vehicle.VehicleStatusChecker
import app.forku.data.api.GeneralApi
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
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.repository.vehicle.VehicleStatusUpdater
import app.forku.data.repository.vehicle.VehicleStatusUpdaterImpl
import app.forku.domain.repository.session.SessionStatusChecker
import app.forku.data.repository.vehicle_session.VehicleSessionStatusCheckerImpl
import app.forku.domain.repository.checklist.ChecklistStatusNotifier
import app.forku.data.repository.checklist.ChecklistStatusNotifierImpl
import app.forku.domain.service.VehicleValidationService
import app.forku.data.service.VehicleValidationServiceImpl
import app.forku.domain.service.VehicleStatusDeterminer
import app.forku.data.service.VehicleStatusDeterminerImpl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideVehicleStatusUpdater(
        api: GeneralApi
    ): VehicleStatusUpdater {
        return VehicleStatusUpdaterImpl(api)
    }

    @Provides
    @Singleton
    fun provideSessionStatusChecker(
        api: GeneralApi
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
        vehicleStatusDeterminer: VehicleStatusDeterminer
    ): ChecklistStatusNotifier {
        return ChecklistStatusNotifierImpl(vehicleStatusUpdater, vehicleStatusDeterminer)
    }

    @Provides
    @Singleton
    fun provideVehicleValidationService(
        api: GeneralApi,
        sessionStatusChecker: SessionStatusChecker,
        checklistRepository: ChecklistRepository,
        vehicleStatusDeterminer: VehicleStatusDeterminer
    ): VehicleValidationService {
        return VehicleValidationServiceImpl(
            api, 
            sessionStatusChecker, 
            checklistRepository,
            vehicleStatusDeterminer
        )
    }

    @Provides
    @Singleton
    fun provideChecklistRepository(
        api: GeneralApi,
        authDataStore: AuthDataStore,
        validateChecklistUseCase: ValidateChecklistUseCase,
        checklistStatusNotifier: ChecklistStatusNotifier
    ): ChecklistRepository {
        return ChecklistRepositoryImpl(
            api, 
            authDataStore, 
            validateChecklistUseCase,
            checklistStatusNotifier
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
        api: GeneralApi,
        authDataStore: AuthDataStore,
        vehicleStatusRepository: VehicleStatusRepository,
        checklistRepository: ChecklistRepository
    ): SessionRepository {
        return VehicleSessionRepositoryImpl(api, authDataStore, vehicleStatusRepository, checklistRepository)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideIncidentRepository(
        api: GeneralApi,
        authDataStore: AuthDataStore
    ): IncidentRepository {
        return IncidentRepositoryImpl(api, authDataStore)
    }

    @Provides
    @Singleton
    fun provideVehicleRepository(
        api: GeneralApi,
        authDataStore: AuthDataStore,
        validateChecklistUseCase: ValidateChecklistUseCase,
        vehicleStatusRepository: VehicleStatusRepository
    ): VehicleRepository {
        return VehicleRepositoryImpl(api, authDataStore, validateChecklistUseCase, vehicleStatusRepository)
    }

    @Provides
    @Singleton
    fun provideVehicleStatusChecker(
        vehicleStatusRepository: VehicleStatusRepository
    ): VehicleStatusChecker {
        return vehicleStatusRepository
    }
}