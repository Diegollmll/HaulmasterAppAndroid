package app.forku.di

import app.forku.data.api.QuestionaryChecklistItemApi
import app.forku.data.repository.QuestionaryChecklistItemRepositoryImpl
import app.forku.domain.repository.QuestionaryChecklistItemRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QuestionaryChecklistItemModule {
    
    @Provides
    @Singleton
    fun provideQuestionaryChecklistItemApi(retrofit: Retrofit): QuestionaryChecklistItemApi {
        return retrofit.create(QuestionaryChecklistItemApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideQuestionaryChecklistItemRepository(
        api: QuestionaryChecklistItemApi
    ): QuestionaryChecklistItemRepository {
        return QuestionaryChecklistItemRepositoryImpl(api)
    }
} 