package app.forku.di

import app.forku.data.api.QuestionaryChecklistApi
import app.forku.data.repository.QuestionaryChecklistRepositoryImpl
import app.forku.domain.repository.QuestionaryChecklistRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object QuestionaryChecklistModule {
    
    @Provides
    @Singleton
    fun provideQuestionaryChecklistApi(
        @Named("authenticatedRetrofit") retrofit: Retrofit
    ): QuestionaryChecklistApi {
        return retrofit.create(QuestionaryChecklistApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideQuestionaryChecklistRepository(
        api: QuestionaryChecklistApi
    ): QuestionaryChecklistRepository {
        return QuestionaryChecklistRepositoryImpl(api)
    }
} 