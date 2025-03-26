package app.forku.di

import app.forku.domain.repository.user.UserRepository
import app.forku.domain.usecase.user.GetCurrentUserIdUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {
    @Provides
    @Singleton
    fun provideGetCurrentUserIdUseCase(
        userRepository: UserRepository
    ): GetCurrentUserIdUseCase = GetCurrentUserIdUseCase(userRepository)
} 