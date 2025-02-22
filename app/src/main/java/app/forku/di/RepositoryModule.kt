package app.forku.di

import app.forku.data.api.Sub7Api
import app.forku.data.repository.AuthRepositoryImpl
import app.forku.domain.repository.AuthRepository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(api: Sub7Api): AuthRepository {
        return AuthRepositoryImpl(api)

    }
}