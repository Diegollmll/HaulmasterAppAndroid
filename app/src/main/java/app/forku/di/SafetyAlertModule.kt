package app.forku.di

import app.forku.data.api.SafetyAlertApi
import app.forku.core.auth.HeaderManager
import app.forku.core.business.BusinessContextManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
import javax.inject.Named
import app.forku.domain.repository.safetyalert.SafetyAlertRepository
import app.forku.data.repository.safetyalert.SafetyAlertRepositoryImpl
import app.forku.domain.usecase.safetyalert.CreateSafetyAlertUseCase
import com.google.gson.Gson

@Module
@InstallIn(SingletonComponent::class)
object SafetyAlertModule {
    @Provides
    @Singleton
    fun provideSafetyAlertApi(
        @Named("authenticatedRetrofit") retrofit: Retrofit
    ): SafetyAlertApi {
        return retrofit.create(SafetyAlertApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSafetyAlertRepository(
        api: SafetyAlertApi,
        headerManager: HeaderManager,
        gson: Gson,
        businessContextManager: BusinessContextManager
    ): SafetyAlertRepository {
        return SafetyAlertRepositoryImpl(api, headerManager, gson, businessContextManager)
    }

    @Provides
    @Singleton
    fun provideCreateSafetyAlertUseCase(
        safetyAlertRepository: SafetyAlertRepository
    ): CreateSafetyAlertUseCase {
        return CreateSafetyAlertUseCase(safetyAlertRepository)
    }
} 