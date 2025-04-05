package app.forku.di

import app.forku.data.api.FeedbackApi
import app.forku.data.repository.FeedbackRepositoryImpl
import app.forku.domain.repository.feedback.FeedbackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FeedbackModule {
    @Provides
    @Singleton
    fun provideFeedbackRepository(api: FeedbackApi): FeedbackRepository =
        FeedbackRepositoryImpl(api)
} 