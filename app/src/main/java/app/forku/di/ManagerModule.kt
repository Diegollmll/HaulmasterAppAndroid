package app.forku.di

import app.forku.data.manager.VehicleSessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagerModule {
    
    @Provides
    @Singleton
    fun provideVehicleSessionManager(): VehicleSessionManager {
        return VehicleSessionManager()
    }
} 