package app.forku.di

import app.forku.data.api.Sub7Api
import app.forku.data.datastore.AuthDataStore
import app.forku.data.repository.vehicle.VehicleRepositoryImpl
import app.forku.domain.repository.vehicle.VehicleRepository
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

}