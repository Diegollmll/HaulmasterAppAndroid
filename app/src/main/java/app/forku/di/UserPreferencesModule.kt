package app.forku.di

import app.forku.data.api.UserPreferencesApi
import app.forku.data.repository.user.UserPreferencesRepositoryImpl
import app.forku.domain.repository.user.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserPreferencesModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    companion object {
        @Provides
        @Singleton
        fun provideUserPreferencesApi(@Named("authenticatedRetrofit") retrofit: Retrofit): UserPreferencesApi {
            return retrofit.create(UserPreferencesApi::class.java)
        }
    }
} 