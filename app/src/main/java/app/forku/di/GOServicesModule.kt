package app.forku.di

import app.forku.data.api.GOServicesApi
import app.forku.data.api.GOSecurityProviderApi
import app.forku.data.datastore.AuthDataStore
import app.forku.data.repository.GOServicesRepository
import app.forku.data.repository.GOSecurityProviderRepository
import app.forku.data.service.GOServicesManager
import app.forku.domain.repository.IGOServicesRepository
import app.forku.domain.repository.IGOSecurityProviderRepository
import app.forku.domain.usecase.security.*
import app.forku.core.auth.HeaderManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object GOServicesModule {

    @Provides
    @Singleton
    fun provideGOServicesApi(@Named("baseRetrofit") retrofit: Retrofit): GOServicesApi {
        return retrofit.create(GOServicesApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGOSecurityProviderApi(@Named("baseRetrofit") retrofit: Retrofit): GOSecurityProviderApi {
        return retrofit.create(GOSecurityProviderApi::class.java)
    }

    @Provides
    @Singleton
    fun provideGOServicesRepository(
        api: GOServicesApi,
        authDataStore: AuthDataStore
    ): IGOServicesRepository {
        return GOServicesRepository(api, authDataStore)
    }

    @Provides
    @Singleton
    fun provideGOSecurityProviderRepository(
        api: GOSecurityProviderApi,
        authDataStore: AuthDataStore,
        goServicesManager: GOServicesManager,
        userRepository: app.forku.domain.repository.user.UserRepository,
        headerManager: app.forku.core.auth.HeaderManager
    ): IGOSecurityProviderRepository {
        return GOSecurityProviderRepository(api, authDataStore, goServicesManager, userRepository, headerManager)
    }

    @Provides
    @Singleton
    fun provideGOServicesManager(
        repository: IGOServicesRepository,
        authDataStore: AuthDataStore
    ): GOServicesManager {
        return GOServicesManager(repository, authDataStore)
    }

    @Provides
    @Singleton
    fun provideHeaderManager(
        authDataStore: AuthDataStore,
        goServicesManager: GOServicesManager
    ): HeaderManager {
        return HeaderManager(authDataStore, goServicesManager)
    }

    @Provides
    @Singleton
    fun provideRegisterUserUseCase(
        repository: IGOSecurityProviderRepository
    ): RegisterUserUseCase = RegisterUserUseCase(repository)

    @Provides
    @Singleton
    fun provideLostPasswordUseCase(
        repository: IGOSecurityProviderRepository
    ): LostPasswordUseCase = LostPasswordUseCase(repository)

    @Provides
    @Singleton
    fun provideResetPasswordUseCase(
        repository: IGOSecurityProviderRepository
    ): ResetPasswordUseCase = ResetPasswordUseCase(repository)

    @Provides
    @Singleton
    fun provideChangePasswordUseCase(
        repository: IGOSecurityProviderRepository
    ): ChangePasswordUseCase = ChangePasswordUseCase(repository)

    @Provides
    @Singleton
    fun provideBlockUserUseCase(
        repository: IGOSecurityProviderRepository
    ): BlockUserUseCase = BlockUserUseCase(repository)

    @Provides
    @Singleton
    fun provideApproveUserUseCase(
        repository: IGOSecurityProviderRepository
    ): ApproveUserUseCase = ApproveUserUseCase(repository)

    @Provides
    @Singleton
    fun provideValidateRegistrationUseCase(
        repository: IGOSecurityProviderRepository
    ): ValidateRegistrationUseCase = ValidateRegistrationUseCase(repository)

    @Provides
    @Singleton
    fun provideKeepAliveUseCase(
        repository: IGOSecurityProviderRepository
    ): KeepAliveUseCase = KeepAliveUseCase(repository)

    @Provides
    @Singleton
    fun provideRegisterByEmailUseCase(
        repository: IGOSecurityProviderRepository
    ): RegisterByEmailUseCase = RegisterByEmailUseCase(repository)

    @Provides
    @Singleton
    fun provideRegisterFullUseCase(
        repository: IGOSecurityProviderRepository
    ): RegisterFullUseCase = RegisterFullUseCase(repository)

    @Provides
    @Singleton
    fun provideEmailChangeValidationUseCase(
        repository: IGOSecurityProviderRepository
    ): EmailChangeValidationUseCase = EmailChangeValidationUseCase(repository)

    @Provides
    @Singleton
    fun provideUnregisterUseCase(
        repository: IGOSecurityProviderRepository
    ): UnregisterUseCase = UnregisterUseCase(repository)
} 