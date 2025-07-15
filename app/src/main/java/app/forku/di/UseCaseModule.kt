package app.forku.di

import app.forku.domain.repository.ICollisionIncidentRepository
import app.forku.domain.repository.vehicle.VehicleRepository
import app.forku.domain.usecase.checklist.GetChecklistUseCase
import app.forku.domain.usecase.vehicle.GetVehicleUseCase
import app.forku.domain.usecase.vehicle.GetVehiclesUseCase
import app.forku.domain.usecase.checklist.SubmitChecklistUseCase
import app.forku.domain.repository.incident.IncidentRepository
import app.forku.domain.repository.session.VehicleSessionRepository
import app.forku.domain.usecase.incident.ReportIncidentUseCase

import app.forku.domain.usecase.vehicle.GetVehicleStatusUseCase
import app.forku.domain.repository.user.UserRepository
import app.forku.domain.repository.checklist.ChecklistRepository
import app.forku.domain.usecase.checklist.GetLastPreShiftCheckByVehicleUseCase
import app.forku.domain.repository.vehicle.VehicleStatusRepository
import app.forku.domain.usecase.checklist.ValidateChecklistUseCase

import app.forku.domain.usecase.user.LoginUseCase
import app.forku.domain.usecase.vehicle.GetVehicleActiveSessionUseCase
import app.forku.domain.repository.gogroup.*
import app.forku.domain.usecase.collision_incident.DeleteCollisionIncidentUseCase
import app.forku.domain.usecase.collision_incident.GetCollisionIncidentByIdUseCase
import app.forku.domain.usecase.collision_incident.GetCollisionIncidentCountUseCase
import app.forku.domain.usecase.collision_incident.GetCollisionIncidentListUseCase
import app.forku.domain.usecase.collision_incident.SaveCollisionIncidentUseCase
import app.forku.domain.usecase.gogroup.group.*
import app.forku.domain.usecase.gogroup.role.*
import app.forku.domain.usecase.gogroup.file.*
import app.forku.domain.usecase.nearmiss_incident.SaveNearMissIncidentUseCase
import app.forku.data.repository.NearMissIncidentRepository
import app.forku.domain.usecase.vehiclefail_incident.SaveVehicleFailIncidentUseCase
import app.forku.domain.repository.incident.VehicleFailIncidentRepository
import app.forku.core.business.BusinessContextManager
import app.forku.domain.repository.IGOSecurityProviderRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    @Singleton
    fun provideGetChecklistUseCase(repository: ChecklistRepository): GetChecklistUseCase {
        return GetChecklistUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSubmitChecklistUseCase(repository: ChecklistRepository): SubmitChecklistUseCase {
        return SubmitChecklistUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetVehicleUseCase(
        repository: VehicleRepository,
        userRepository: UserRepository,
        businessContextManager: BusinessContextManager
    ): GetVehicleUseCase {
        return GetVehicleUseCase(repository, userRepository, businessContextManager)
    }

    @Provides
    @Singleton
    fun provideGetVehiclesUseCase(
        repository: VehicleRepository,
        userRepository: UserRepository
    ): GetVehiclesUseCase {
        return GetVehiclesUseCase(repository, userRepository)
    }

    @Provides
    @Singleton
    fun provideReportIncidentUseCase(
        repository: IncidentRepository,
        userRepository: UserRepository,
        vehicleSessionRepository: VehicleSessionRepository,
        businessContextManager: BusinessContextManager
    ): ReportIncidentUseCase {
        return ReportIncidentUseCase(repository, userRepository, vehicleSessionRepository, businessContextManager)
    }

    @Provides
    @Singleton
    fun provideGetVehicleStatusUseCase(
        vehicleStatusRepository: VehicleStatusRepository,
        userRepository: UserRepository
    ): GetVehicleStatusUseCase {
        return GetVehicleStatusUseCase(vehicleStatusRepository, userRepository)
    }

    @Provides
    @Singleton
    fun provideGetLastPreShiftCheckByVehicleUseCase(
        repository: ChecklistRepository
    ): GetLastPreShiftCheckByVehicleUseCase {
        return GetLastPreShiftCheckByVehicleUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetVehicleActiveSessionUseCase(
        vehicleSessionRepository: VehicleSessionRepository,
        userRepository: UserRepository,
        vehicleRepository: VehicleRepository
    ): GetVehicleActiveSessionUseCase {
        return GetVehicleActiveSessionUseCase(vehicleSessionRepository, userRepository, vehicleRepository)
    }

    @Provides
    @Singleton
    fun provideValidateChecklistUseCase(): ValidateChecklistUseCase {
        return ValidateChecklistUseCase()
    }

    @Provides
    @Singleton
    fun provideLoginUseCase(
        goSecurityProviderRepository: IGOSecurityProviderRepository
    ): LoginUseCase {
        return LoginUseCase(goSecurityProviderRepository)
    }

    @Provides
    @Singleton
    fun provideGetGroupsUseCase(
        repository: GOGroupRepository
    ): GetGroupsUseCase = GetGroupsUseCase(repository)

    @Provides
    @Singleton
    fun provideManageGroupUseCase(
        repository: GOGroupRepository
    ): ManageGroupUseCase = ManageGroupUseCase(repository)

    @Provides
    @Singleton
    fun provideGetGroupRolesUseCase(
        repository: GOGroupRoleRepository
    ): GetGroupRolesUseCase = GetGroupRolesUseCase(repository)

    @Provides
    @Singleton
    fun provideManageGroupRoleUseCase(
        repository: GOGroupRoleRepository
    ): ManageGroupRoleUseCase = ManageGroupRoleUseCase(repository)

    @Provides
    @Singleton
    fun provideUploadFileUseCase(
        repository: GOFileUploaderRepository
    ): UploadFileUseCase = UploadFileUseCase(repository)

    @Provides
    @Singleton
    fun provideGetCollisionIncidentByIdUseCase(
        repository: ICollisionIncidentRepository
    ): GetCollisionIncidentByIdUseCase {
        return GetCollisionIncidentByIdUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetCollisionIncidentListUseCase(
        repository: ICollisionIncidentRepository
    ): GetCollisionIncidentListUseCase {
        return GetCollisionIncidentListUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSaveCollisionIncidentUseCase(
        repository: ICollisionIncidentRepository
    ): SaveCollisionIncidentUseCase {
        return SaveCollisionIncidentUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteCollisionIncidentUseCase(
        repository: ICollisionIncidentRepository
    ): DeleteCollisionIncidentUseCase {
        return DeleteCollisionIncidentUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetCollisionIncidentCountUseCase(
        repository: ICollisionIncidentRepository
    ): GetCollisionIncidentCountUseCase {
        return GetCollisionIncidentCountUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSaveNearMissIncidentUseCase(
        repository: NearMissIncidentRepository
    ): SaveNearMissIncidentUseCase {
        return SaveNearMissIncidentUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSaveVehicleFailIncidentUseCase(
        repository: VehicleFailIncidentRepository
    ): SaveVehicleFailIncidentUseCase {
        return SaveVehicleFailIncidentUseCase(repository)
    }
} 