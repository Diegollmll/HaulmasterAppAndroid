package app.forku.di

import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.usecase.checklist.GetChecklistUseCase
import app.forku.domain.usecase.vehicle.GetVehicleUseCase
import app.forku.domain.usecase.vehicle.GetVehiclesUseCase
import app.forku.domain.usecase.checklist.SubmitChecklistUseCase
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

} 