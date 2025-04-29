package app.forku.di

import app.forku.data.api.BusinessApi
import app.forku.data.api.BusinessConfigurationApi
import app.forku.data.api.UserBusinessApi
import app.forku.data.repository.business.BusinessRepositoryImpl
import app.forku.domain.repository.business.BusinessRepository
import app.forku.domain.repository.user.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BusinessModule {

    @Provides
    @Singleton
    fun provideBusinessRepository(
        api: BusinessApi,
        userBusinessApi: UserBusinessApi,
        userRepository: UserRepository,
        businessConfigurationApi: BusinessConfigurationApi
    ): BusinessRepository {
        return BusinessRepositoryImpl(api, userBusinessApi, userRepository, businessConfigurationApi)
    }
} 