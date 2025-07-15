package app.forku.presentation.vehicle.profile

import app.forku.domain.repository.user.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VehicleProfileEntryPoint {
    fun userPreferencesRepository(): UserPreferencesRepository
} 