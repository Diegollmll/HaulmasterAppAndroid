package app.forku.di

import app.forku.core.notification.PushNotificationService
import app.forku.data.repository.notification.LocalNotificationManagerImpl
import app.forku.data.repository.notification.NotificationRepositoryImpl
import app.forku.data.repository.notification.PushNotificationManagerImpl
import app.forku.data.service.fcm.FCMService
import app.forku.domain.repository.notification.LocalNotificationManager
import app.forku.domain.repository.notification.NotificationRepository
import app.forku.domain.repository.notification.PushNotificationManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    
    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindLocalNotificationManager(
        impl: LocalNotificationManagerImpl
    ): LocalNotificationManager

    @Binds
    @Singleton
    abstract fun bindPushNotificationManager(
        impl: PushNotificationManagerImpl
    ): PushNotificationManager

    @Binds
    @Singleton
    abstract fun bindPushNotificationService(
        impl: FCMService
    ): PushNotificationService
} 