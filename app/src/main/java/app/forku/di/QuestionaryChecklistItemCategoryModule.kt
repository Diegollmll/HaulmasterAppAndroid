package app.forku.di

import app.forku.data.api.QuestionaryChecklistItemCategoryApi
import app.forku.data.repository.QuestionaryChecklistItemCategoryRepositoryImpl
import app.forku.domain.repository.QuestionaryChecklistItemCategoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object QuestionaryChecklistItemCategoryModule {
    
    @Provides
    @Singleton
    fun provideQuestionaryChecklistItemCategoryApi(
        @Named("authenticatedRetrofit") retrofit: Retrofit
    ): QuestionaryChecklistItemCategoryApi {
        return retrofit.create(QuestionaryChecklistItemCategoryApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideQuestionaryChecklistItemCategoryRepository(
        api: QuestionaryChecklistItemCategoryApi
    ): QuestionaryChecklistItemCategoryRepository {
        return QuestionaryChecklistItemCategoryRepositoryImpl(api)
    }
} 