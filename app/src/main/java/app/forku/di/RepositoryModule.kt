package app.forku.di

import app.forku.data.api.Sub7Api
import app.forku.data.datastore.AuthDataStore
import app.forku.data.repository.vehicle.VehicleRepositoryImpl
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.data.repository.incident.IncidentRepositoryImpl
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.data.repository.checklist.ChecklistRepositoryImpl
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.google.gson.Gson
import com.google.gson.GsonBuilder

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideVehicleRepository(
        vehicleRepositoryImpl: VehicleRepositoryImpl
    ): VehicleRepository = vehicleRepositoryImpl

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideIncidentRepository(
        api: Sub7Api,
        authDataStore: AuthDataStore
    ): IncidentRepository {
        return IncidentRepositoryImpl(api, authDataStore)
    }

    @Provides
    @Singleton
    fun provideChecklistRepository(
        api: Sub7Api,
        authDataStore: AuthDataStore,
        validateChecklistUseCase: ValidateChecklistUseCase
    ): ChecklistRepository {
        return ChecklistRepositoryImpl(api, authDataStore, validateChecklistUseCase)
    }
}