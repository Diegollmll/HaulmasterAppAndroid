package app.forku.di

import app.forku.data.api.QuestionaryChecklistItemSubcategoryApi
import app.forku.data.repository.QuestionaryChecklistItemSubcategoryRepositoryImpl
import app.forku.domain.repository.QuestionaryChecklistItemSubcategoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QuestionaryChecklistItemSubcategoryModule {
    
    @Provides
    @Singleton
    fun provideQuestionaryChecklistItemSubcategoryApi(retrofit: Retrofit): QuestionaryChecklistItemSubcategoryApi {
        return retrofit.create(QuestionaryChecklistItemSubcategoryApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideQuestionaryChecklistItemSubcategoryRepository(
        api: QuestionaryChecklistItemSubcategoryApi
    ): QuestionaryChecklistItemSubcategoryRepository {
        return QuestionaryChecklistItemSubcategoryRepositoryImpl(api)
    }
} 