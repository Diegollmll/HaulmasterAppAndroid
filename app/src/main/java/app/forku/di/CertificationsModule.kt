package app.forku.di

import app.forku.data.api.CertificationApi
import app.forku.data.repository.certification.CertificationRepositoryImpl
import app.forku.domain.repository.certification.CertificationRepository
import app.forku.domain.usecase.certification.*
import app.forku.data.datastore.AuthDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CertificationsModule {

    @Provides
    @Singleton
    fun provideCertificationRepository(
        api: CertificationApi,
        authDataStore: AuthDataStore
    ): CertificationRepository {
        return CertificationRepositoryImpl(api, authDataStore)
    }

    @Provides
    @Singleton
    fun provideGetUserCertificationsUseCase(
        repository: CertificationRepository
    ): GetUserCertificationsUseCase {
        return GetUserCertificationsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetCertificationByIdUseCase(
        repository: CertificationRepository
    ): GetCertificationByIdUseCase {
        return GetCertificationByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideCreateCertificationUseCase(
        repository: CertificationRepository
    ): CreateCertificationUseCase {
        return CreateCertificationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideUpdateCertificationUseCase(
        repository: CertificationRepository
    ): UpdateCertificationUseCase {
        return UpdateCertificationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteCertificationUseCase(
        repository: CertificationRepository
    ): DeleteCertificationUseCase {
        return DeleteCertificationUseCase(repository)
    }
} 