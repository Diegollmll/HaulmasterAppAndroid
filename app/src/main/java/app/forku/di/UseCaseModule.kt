package app.forku.di

import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.usecase.checklist.GetChecklistUseCase
import app.forku.domain.usecase.vehicle.GetVehicleUseCase
import app.forku.domain.usecase.vehicle.GetVehiclesUseCase
import app.forku.domain.usecase.checklist.SubmitChecklistUseCase
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.domain.repository.user.AuthRepository
import app.forku.domain.repository.session.SessionRepository
import app.forku.domain.usecase.incident.ReportIncidentUseCase

import app.forku.domain.usecase.vehicle.GetVehicleStatusUseCase
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.usecase.session.GetVehicleActiveSessionUseCase
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.usecase.checklist.GetLastPreShiftCheckByVehicleUseCase
import app.forku.domain.repository.vehicle.VehicleStatusRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideGetChecklistUseCase(repository: ChecklistRepository): GetChecklistUseCase {
        return GetChecklistUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSubmitChecklistUseCase(repository: ChecklistRepository): SubmitChecklistUseCase {
        return SubmitChecklistUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetVehicleUseCase(repository: VehicleRepository): GetVehicleUseCase {
        return GetVehicleUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetVehiclesUseCase(repository: VehicleRepository): GetVehiclesUseCase {
        return GetVehiclesUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideReportIncidentUseCase(
        repository: IncidentRepository,
        authRepository: AuthRepository,
        sessionRepository: SessionRepository
    ): ReportIncidentUseCase {
        return ReportIncidentUseCase(repository, authRepository, sessionRepository)
    }

    @Provides
    @Singleton
    fun provideGetVehicleStatusUseCase(
        vehicleStatusRepository: VehicleStatusRepository
    ): GetVehicleStatusUseCase {
        return GetVehicleStatusUseCase(vehicleStatusRepository)
    }

    @Provides
    @Singleton
    fun provideGetLastPreShiftCheckByVehicleUseCase(
        checklistRepository: ChecklistRepository
    ): GetLastPreShiftCheckByVehicleUseCase {
        return GetLastPreShiftCheckByVehicleUseCase(checklistRepository)
    }

    @Provides
    @Singleton
    fun provideGetVehicleActiveSessionUseCase(
        sessionRepository: SessionRepository,
        userRepository: UserRepository
    ): GetVehicleActiveSessionUseCase {
        return GetVehicleActiveSessionUseCase(sessionRepository, userRepository)
    }
} 