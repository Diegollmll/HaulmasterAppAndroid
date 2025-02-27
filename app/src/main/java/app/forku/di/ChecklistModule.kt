package app.forku.di

import app.forku.domain.usecase.checklist.ValidateChecklistUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChecklistModule {
    
    @Provides
    @Singleton
    fun provideValidateChecklistUseCase(): ValidateChecklistUseCase {
        return ValidateChecklistUseCase()
    }
} 