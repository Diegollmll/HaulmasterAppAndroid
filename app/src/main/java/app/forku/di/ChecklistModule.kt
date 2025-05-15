package app.forku.di

import app.forku.core.auth.HeaderManager
import app.forku.data.api.ChecklistItemCategoryApi
import app.forku.data.repository.checklist.ChecklistItemCategoryRepositoryImpl
import app.forku.domain.repository.checklist.ChecklistItemCategoryRepository
import app.forku.data.api.ChecklistAnswerApi
import app.forku.data.api.AnsweredChecklistItemApi
import app.forku.data.repository.checklist.ChecklistAnswerRepositoryImpl
import app.forku.data.repository.checklist.AnsweredChecklistItemRepositoryImpl
import app.forku.domain.repository.checklist.ChecklistAnswerRepository
import app.forku.domain.repository.checklist.AnsweredChecklistItemRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
import javax.inject.Named
import com.google.gson.Gson


@Module
@InstallIn(SingletonComponent::class)
object ChecklistModule {
    @Provides
    @Singleton
    fun provideChecklistItemCategoryApi(
        @Named("authenticatedRetrofit") retrofit: Retrofit
    ): ChecklistItemCategoryApi {
        return retrofit.create(ChecklistItemCategoryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChecklistItemCategoryRepository(
        api: ChecklistItemCategoryApi
    ): ChecklistItemCategoryRepository {
        return ChecklistItemCategoryRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideChecklistAnswerApi(
        @Named("authenticatedRetrofit") retrofit: Retrofit
    ): ChecklistAnswerApi {
        return retrofit.create(ChecklistAnswerApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChecklistAnswerRepository(
        api: ChecklistAnswerApi,
        gson: Gson,
        headerManager: HeaderManager
    ): ChecklistAnswerRepository {
        return ChecklistAnswerRepositoryImpl(api, gson, headerManager)
    }

    @Provides
    @Singleton
    fun provideAnsweredChecklistItemApi(
        @Named("authenticatedRetrofit") retrofit: Retrofit
    ): AnsweredChecklistItemApi {
        return retrofit.create(AnsweredChecklistItemApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAnsweredChecklistItemRepository(
        api: AnsweredChecklistItemApi,
        gson: Gson,
        headerManager: HeaderManager
    ): AnsweredChecklistItemRepository {
        return AnsweredChecklistItemRepositoryImpl(api, gson, headerManager)
    }
    // Other providers can be added here if needed
} 