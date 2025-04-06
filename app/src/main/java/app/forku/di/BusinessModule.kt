package app.forku.di

import app.forku.data.remote.api.BusinessApi
import app.forku.data.repository.business.BusinessRepositoryImpl
import app.forku.domain.repository.business.BusinessRepository
import app.forku.domain.repository.user.UserRepository
import com.google.gson.Gson
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
        gson: Gson,
        userRepository: UserRepository
    ): BusinessRepository {
        return BusinessRepositoryImpl(api, gson, userRepository)
    }
} 