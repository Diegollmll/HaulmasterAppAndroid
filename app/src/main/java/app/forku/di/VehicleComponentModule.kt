package app.forku.di

import app.forku.data.api.VehicleComponentApi
import app.forku.data.repository.VehicleComponentRepositoryImpl
import app.forku.domain.repository.vehicle.VehicleComponentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VehicleComponentModule {
    
    @Provides
    @Singleton
    fun provideVehicleComponentRepository(
        api: VehicleComponentApi
    ): VehicleComponentRepository {
        return VehicleComponentRepositoryImpl(api)
    }
} 