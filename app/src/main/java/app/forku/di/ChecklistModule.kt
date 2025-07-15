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
import app.forku.core.business.BusinessContextManager
import app.forku.data.api.ChecklistApi
import app.forku.presentation.checklist.manage_checklist.ManageChecklistViewModel
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.repository.vehicle.VehicleTypeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
import javax.inject.Named
import com.google.gson.Gson
import app.forku.data.api.ChecklistItemApi
import app.forku.data.repository.checklist.ChecklistItemRepositoryImpl
import app.forku.domain.repository.checklist.ChecklistItemRepository
import app.forku.data.datastore.AuthDataStore
import app.forku.data.api.ChecklistItemSubcategoryApi
import app.forku.data.repository.checklist.ChecklistItemSubcategoryRepositoryImpl
import app.forku.domain.repository.checklist.ChecklistItemSubcategoryRepository
import app.forku.data.api.ChecklistChecklistItemCategoryApi
import app.forku.data.repository.checklist.ChecklistChecklistItemCategoryRepositoryImpl
import app.forku.domain.repository.checklist.ChecklistChecklistItemCategoryRepository
import app.forku.data.api.ChecklistVehicleTypeApi
import app.forku.data.repository.checklist.ChecklistVehicleTypeRepositoryImpl
import app.forku.domain.repository.checklist.ChecklistVehicleTypeRepository
import app.forku.data.api.ChecklistQuestionVehicleTypeApi
import app.forku.data.repository.checklist.ChecklistQuestionVehicleTypeRepositoryImpl
import app.forku.domain.repository.checklist.ChecklistQuestionVehicleTypeRepository


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
        headerManager: HeaderManager,
        businessContextManager: BusinessContextManager
    ): ChecklistAnswerRepository {
        return ChecklistAnswerRepositoryImpl(api, gson, headerManager, businessContextManager)
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
        headerManager: HeaderManager,
        businessContextManager: BusinessContextManager
    ): AnsweredChecklistItemRepository {
        return AnsweredChecklistItemRepositoryImpl(api, gson, headerManager, businessContextManager)
    }

    @Provides
    @Singleton
    fun provideChecklistItemApi(
        @Named("authenticatedRetrofit") retrofit: Retrofit
    ): ChecklistItemApi {
        return retrofit.create(ChecklistItemApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChecklistItemRepository(
        api: ChecklistItemApi,
        authDataStore: app.forku.data.datastore.AuthDataStore,
        businessContextManager: BusinessContextManager,
        userRepository: app.forku.domain.repository.user.UserRepository
    ): ChecklistItemRepository {
        return ChecklistItemRepositoryImpl(api, authDataStore, businessContextManager, userRepository)
    }

    @Provides
    @Singleton
    fun provideChecklistItemSubcategoryApi(
        @Named("authenticatedRetrofit") retrofit: Retrofit
    ): ChecklistItemSubcategoryApi {
        return retrofit.create(ChecklistItemSubcategoryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChecklistItemSubcategoryRepository(
        api: ChecklistItemSubcategoryApi
    ): ChecklistItemSubcategoryRepository {
        return ChecklistItemSubcategoryRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideChecklistChecklistItemCategoryApi(
        @Named("authenticatedRetrofit") retrofit: Retrofit
    ): ChecklistChecklistItemCategoryApi {
        return retrofit.create(ChecklistChecklistItemCategoryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChecklistChecklistItemCategoryRepository(
        api: ChecklistChecklistItemCategoryApi,
        authDataStore: AuthDataStore,
        businessContextManager: BusinessContextManager
    ): ChecklistChecklistItemCategoryRepository {
        return ChecklistChecklistItemCategoryRepositoryImpl(api, authDataStore, businessContextManager)
    }

    @Provides
    @Singleton
    fun provideChecklistVehicleTypeApi(
        @Named("authenticatedRetrofit") retrofit: Retrofit
    ): ChecklistVehicleTypeApi {
        return retrofit.create(ChecklistVehicleTypeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChecklistVehicleTypeRepository(
        api: ChecklistVehicleTypeApi,
        authDataStore: AuthDataStore,
        businessContextManager: BusinessContextManager
    ): ChecklistVehicleTypeRepository {
        return ChecklistVehicleTypeRepositoryImpl(api, authDataStore, businessContextManager)
    }

    @Provides
    @Singleton
    fun provideChecklistQuestionVehicleTypeApi(
        @Named("authenticatedRetrofit") retrofit: Retrofit
    ): ChecklistQuestionVehicleTypeApi {
        return retrofit.create(ChecklistQuestionVehicleTypeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChecklistQuestionVehicleTypeRepository(
        api: ChecklistQuestionVehicleTypeApi,
        authDataStore: AuthDataStore,
        businessContextManager: BusinessContextManager
    ): ChecklistQuestionVehicleTypeRepository {
        return ChecklistQuestionVehicleTypeRepositoryImpl(api, authDataStore, businessContextManager)
    }
    
    // Note: ChecklistApi is already provided in NetworkModule, so we don't need it here
    // ManageChecklistViewModel will be provided by Hilt automatically since it has @HiltViewModel
} 