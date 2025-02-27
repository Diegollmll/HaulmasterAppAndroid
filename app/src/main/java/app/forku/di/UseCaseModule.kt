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
    fun provideGetVehicleUseCase(repository: VehicleRepository): GetVehicleUseCase {
        return GetVehicleUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetChecklistUseCase(repository: VehicleRepository): GetChecklistUseCase {
        return GetChecklistUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSubmitChecklistUseCase(repository: VehicleRepository): SubmitChecklistUseCase {
        return SubmitChecklistUseCase(repository)
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
        repository: VehicleRepository,
        getVehicleActiveSessionUseCase: GetVehicleActiveSessionUseCase
    ): GetVehicleStatusUseCase {
        return GetVehicleStatusUseCase(repository, getVehicleActiveSessionUseCase)
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