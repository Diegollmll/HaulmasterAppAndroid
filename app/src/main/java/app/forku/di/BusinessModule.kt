package app.forku.di

import app.forku.data.remote.api.BusinessApi
import app.forku.data.repository.business.BusinessRepositoryImpl
import app.forku.domain.repository.business.BusinessRepository
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
    fun provideBusinessRepository(api: BusinessApi, gson: Gson): BusinessRepository {
        return BusinessRepositoryImpl(api, gson)
    }
} 