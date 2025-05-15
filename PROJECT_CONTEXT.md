# ForkU Android Project Context

## Project Overview
ForkU is an Android application built with modern Android development practices and Clean Architecture principles. The app is focused on vehicle management, incident tracking, user authentication systems, and certification management.

## Current Development Status

### 1. Core Systems Progress

#### Authentication System (95% Complete)
- Login/Register functionality with email/password
- Session management with DataStore
- User role management
- Tour/Onboarding preferences
- **Remaining Time**: 1 day
- **Pending Tasks**:
  * ProfileScreen integration (4-6 hours)
  * Session management enhancements (2-3 hours)
  * Security features implementation (4-6 hours)

#### Vehicle Management (90% Complete)
- Vehicle tracking and status monitoring
- Vehicle information management
- Vehicle status validation
- Vehicle session management
- Real-time status updates
- **Remaining Time**: 1-2 days
- **Pending Tasks**:
  * Vehicle state management (60% complete)
  * Component tracking and maintenance
  * Vehicle profile photo management

#### Checklist System (40% Complete)
- Pre-shift checklist management
- Checklist validation
- Checklist status notifications
- Vehicle status determination based on checks
- **Remaining Time**: 3-4 days
- **Pending Tasks**:
  * Basic checklist flow (50% complete)
  * Session management (20% complete)
  * History and reports (10% complete)

#### Incident Management (30% Complete)
- Incident reporting with multiple types
- Incident management with severity levels
- Support for different vehicle types
- Load weight tracking
- Incident status tracking
- **Remaining Time**: 3-4 days
- **Pending Tasks**:
  * Incident reporting (30% complete)
  * Incident history (15% complete)
  * Basic reports (10% complete)

#### Admin Management Features (35% Complete)
- User certification tracking
- Certification creation and updates
- Certification deletion
- Certification status monitoring
- **Remaining Time**: 4-5 days
- **Pending Tasks**:
  * User management (30% complete)
  * Vehicle management (20% complete)
  * Checklist monitoring (25% complete)
  * Reports and analytics (15% complete)

#### MockAPI Migration (50% Complete)
- ProfileScreen
  * User profile management
  * Personal settings
  * Role information
- **Remaining Time**: 5-6 days
- **Pending Tasks**:
  * ProfileScreen (50% complete)
  * Basic reports (30% complete)
  * Advanced features (20% complete)

### 2. Optimized Sprint Plan

#### Week 1 (7 days)
- Day 1-2: Complete Authentication & Vehicle Management
- Day 3-5: Core Checklist System
- Day 6-7: Basic Incident Management

#### Week 2 (7 days)
- Day 1-3: Complete Checklist & Incident Systems
- Day 4-5: Essential Admin Features
- Day 6-7: Critical MockAPI Migration

#### Week 3 (7 days)
- Day 1-3: Complete Admin Features
- Day 4-7: Finish MockAPI Migration & Testing

### 3. Key Optimizations

1. **Parallel Development**
   - Run API integration and feature development in parallel
   - Focus on completed APIs first

2. **Prioritization**
   - Complete core features before enhancements
   - Focus on high-impact, low-effort tasks first

3. **Resource Allocation**
   - Dedicate more resources to Checklist System
   - Prioritize Incident Management integration

### 4. Risk Mitigation

1. **Technical Risks**
   - Start with completed APIs
   - Implement feature flags for gradual rollout
   - Regular testing cycles

2. **Dependencies**
   - Clear API documentation
   - Regular backend sync
   - Early testing of critical paths

### 2. Current Sprint Focus

#### Primary Objectives
1. Complete Checklist System Integration
   - Basic checklist flow completion
   - Session management implementation
   - History and reports setup

2. Implement Basic Incident Reporting
   - Report creation and submission
   - Basic history view
   - Simple reporting features

3. Complete ProfileScreen Migration
   - Profile management
   - User preferences
   - Settings implementation

4. Set Up Basic Reporting System
   - Incident reports
   - Checklist reports
   - CICO history

#### Role-Specific Implementation

1. ADMIN Role (Primary Focus)
   - Management Capabilities:
     * Vehicle Management
       - View and monitor vehicles
       - Track vehicle status
       - Monitor components
     * User Management
       - View operator list
       - Monitor operator activity
     * Checklist Management
       - View all checklists
       - Monitor completion
     * Incident Management
       - View all incidents
       - Basic incident reports
     * Reports & Analytics
       - Basic incident reports
       - Basic checklist reports
       - CICO history
   
   - Operational Capabilities:
     * Vehicle Operations
       - View vehicle list
       - View vehicle details
       - Complete checklists
       - Manage sessions
     * Checklist System
       - Complete daily checklists
       - View history
     * Incident Reporting
       - Report incidents
       - View history
     * Profile & History
       - View and edit profile
       - View personal history

2. OPERATOR Role
   - Core Functionalities:
     * Vehicle Management
       - View vehicle list
       - View vehicle details
       - Complete pre-shift checklists
       - Start/end vehicle sessions
     * Checklist System
       - Complete daily checklists
       - View checklist history
     * Incident Reporting
       - Report incidents
       - View own incident history

### 3. API Integration Status

#### Core APIs
1. Authentication & User Management
   - GOSecurityProviderApi (Complete)
   - GOUserRoleApi (In Progress)
   - UserApi (Pending)

2. Vehicle Management
   - VehicleApi (Complete)
   - VehicleTypeApi (Complete)
   - VehicleComponentApi (Complete)
   - VehicleSessionApi (In Progress)

3. Checklist System
   - ChecklistApi (In Progress)
   - ChecklistItemApi (In Progress)
   - UserChecklistApi (Pending)
   - AnsweredChecklistItemApi (Pending)

4. Incident Management
   - IncidentApi (In Progress)
   - IncidentMultimediaApi (Pending)

#### Backend-Only Operations
The following operations are handled exclusively by the GO backend platform:
- Vehicle CRUD operations
- Vehicle Type management
- Vehicle Category management
- Type-Category relationships
- Component definitions and requirements

### 4. Technical Stack & Architecture

### Core Technologies
- **Language**: Kotlin 1.9.22
- **UI Framework**: Jetpack Compose (BOM 2024.02.00)
- **Architecture Pattern**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt 2.48
- **Navigation**: Jetpack Navigation Compose 2.7.7
- **Local Storage**: Room 2.6.1 + DataStore 1.0.0
- **Network**: Retrofit 2.9.0 + OkHttp 4.12.0
- **Firebase Integration**: Firebase BOM 32.7.2
- **Image Loading**: Coil 2.5.0
- **QR Functionality**: CameraX 1.3.1 + MLKit Barcode 17.2.0
- **Build System**: Gradle with Version Catalog
- **JSON Parsing**: Gson

### Dependency Management
- Uses Gradle Version Catalog (`libs.versions.toml`) for centralized dependency management
- All library versions and dependencies are declared in `gradle/libs.versions.toml`
- Dependencies are referenced using type-safe accessors: `libs.{group}.{artifact}`

Key aspects of dependency management:
1. **Version Declaration**:
```toml
[versions]
kotlin = "1.9.22"
compose-bom = "2024.02.00"
hilt = "2024.02.00"
```

2. **Library Declaration**:
```toml
[libraries]
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
```

3. **Plugin Management**:
```toml
[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt-android = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

When promt:
- Check the Codebase as much as possible to keep context

When adding new dependencies:
- Always add them to `libs.versions.toml` first
- Follow the existing versioning pattern
- Group related dependencies
- Use version references when possible
- Consider creating bundles for related dependencies
- Ensure version compatibility with existing dependencies


### Project Structure
```
app/src/main/java/app/forku/
├── presentation/           # UI Layer
│   ├── common/            # Shared UI components
│   ├── dashboard/         # Dashboard feature
│   ├── incident/          # Incident management
│   ├── tour/             # Onboarding/Tour feature
│   ├── scanner/          # QR Scanner functionality
│   ├── checklist/        # Checklists feature
│   ├── navigation/       # Navigation components
│   ├── debug/            # Debug utilities
│   ├── user/             # User management UI
│   ├── session/          # Session management
│   └── vehicle/          # Vehicle management
├── domain/               # Business Logic Layer
│   ├── model/           # Domain models
│   ├── repository/      # Repository interfaces
│   └── usecase/         # Use cases
├── data/                # Data Layer
│   ├── mapper/          # DTO <-> Domain mappers
│   ├── datastore/       # Preferences storage
│   ├── repository/      # Repository implementations
│   ├── local/           # Local data sources
│   ├── api/             # Remote API interfaces
│   ├── service/         # Background services
│   └── db/              # Database configuration
├── core/                # Core utilities
├── di/                  # Dependency injection modules
└── ForkUApplication.kt  # Application class
```

## Key Features

### 1. Authentication System
- Login/Register functionality with email/password
- Session management with DataStore
- User role management
- Tour/Onboarding preferences

### 2. Vehicle Management
- Vehicle tracking and status monitoring
- Vehicle information management
- Vehicle status validation
- Vehicle session management
- Real-time status updates

### 3. Incident Tracking
- Incident reporting with multiple types
- Incident management with severity levels
- Support for different vehicle types
- Load weight tracking
- Incident status tracking

### 4. Checklist System
- Pre-shift checklist management
- Checklist validation
- Checklist status notifications
- Vehicle status determination based on checks

### 5. Certification Management
- User certification tracking
- Certification creation and updates
- Certification deletion
- Certification status monitoring

### 6. QR Scanner
- QR code scanning functionality
- Scanner integration
- Vehicle validation through QR

### 7. Tour/Onboarding
- User onboarding flow
- Feature introduction
- Tour preferences management

## Architecture Components

### 1. Presentation Layer (MVVM)
- ViewModels for state management
- Composable UI components
- Screen-based navigation
- Shared UI components
- State handling with StateFlow
- Event-driven architecture

### 2. Domain Layer
- Business logic encapsulation
- Use case pattern implementation
- Clean Architecture principles
- Repository interfaces
- Domain models
- Validation logic

### 3. Data Layer
- Repository implementations
- Data source abstractions
- DTO mappings
- Local/Remote data handling
- API integration
- DataStore preferences

### 4. Service Layer
- Vehicle validation service
- Status determination service
- Session management service
- Checklist validation service

## Common Patterns

### 1. State Management
- UI state handling in ViewModels
- Event handling patterns
- State preservation
- MutableStateFlow for reactive updates
- State restoration

### 2. Navigation
- Compose Navigation
- Deep linking support
- Screen route management
- Navigation state preservation

### 3. Error Handling
- Repository error management
- UI error presentation
- Network error handling
- Graceful degradation
- Error state management

### 4. Data Storage
- DataStore for preferences
- Room Database for structured data
- Caching strategies
- Concurrent access handling
- Data synchronization

### 5. Dependency Injection
- Hilt modules for feature-specific DI
- Singleton components
- Repository bindings
- Use case providers
- Service implementations

## Important Notes
- The project follows Material Design principles
- Uses modern Android development practices
- Implements clean architecture patterns
- Focuses on maintainable and testable code
- Implements concurrent access patterns
- Uses coroutines for asynchronous operations
- Implements proper error handling and recovery

## Recent Changes/Focus Areas
- User authentication improvements
- Tour screen animations
- Login/Register flow enhancements
- Error handling in repositories
- Vehicle status management
- Certification system implementation
- Checklist validation system
- Session management improvements

## API Integration & Dependencies

### API Status & Mapping

1. **Core APIs (Complete)**
   - Authentication & User Management
     * GOSecurityProviderApi (100%)
       - Login/Register
       - Password management
       - Session handling
     * GOUserRoleApi (70%)
       - Role management
       - User permissions
     * UserApi (Pending)
       - Profile management
       - User preferences

   - Vehicle Management
     * VehicleApi (100%)
       - Vehicle listing
       - Status updates
       - Basic operations
     * VehicleTypeApi (100%)
       - Type information
       - Type validation
     * VehicleComponentApi (100%)
       - Component tracking
       - Maintenance status
     * VehicleSessionApi (60%)
       - Session management
       - State transitions

2. **In Progress APIs**
   - Checklist System
     * ChecklistApi (50%)
       - Checklist creation
       - Checklist management
     * ChecklistItemApi (50%)
       - Item management
       - Response handling
     * UserChecklistApi (Pending)
       - User-checklist association
       - Completion tracking
     * AnsweredChecklistItemApi (Pending)
       - Response storage
       - History tracking

   - Incident Management
     * IncidentApi (30%)
       - Incident reporting
       - Incident tracking
     * IncidentMultimediaApi (Pending)
       - Photo/document upload
       - Media management

3. **Pending APIs**
   - Business & Site Management
     * BusinessApi
     * BusinessConfigurationApi
     * SiteApi
     * UserBusinessApi
   - System & Utilities
     * NotificationApi
     * GOFileUploaderApi
     * EnergySourceApi
     * WeatherApi

### Role-Based Implementation

1. **ADMIN Role (Primary Focus)**
   - Management Capabilities
     * Vehicle Management
       - View and monitor vehicles
       - Track vehicle status
       - Monitor components
     * User Management
       - View operator list
       - Monitor operator activity
     * Checklist Management
       - View all checklists
       - Monitor completion
     * Incident Management
       - View all incidents
       - Basic incident reports
     * Reports & Analytics
       - Basic incident reports
       - Basic checklist reports
       - CICO history
   
   - Operational Capabilities
     * Vehicle Operations
       - View vehicle list
       - View vehicle details
       - Complete checklists
       - Manage sessions
     * Checklist System
       - Complete daily checklists
       - View checklist history
     * Incident Reporting
       - Report incidents
       - View history
     * Profile & History
       - View and edit profile
       - View personal history

2. **OPERATOR Role**
   - Core Functionalities
     * Vehicle Management
       - View vehicle list
       - View vehicle details
       - Complete pre-shift checklists
       - Start/end vehicle sessions
     * Checklist System
       - Complete daily checklists
       - View checklist history
     * Incident Reporting
       - Report incidents
       - View own incident history

### Testing & Quality Assurance

1. **Testing Requirements**
   - Unit Tests
     * API Integration Tests
       - Endpoint validation
       - Response handling
       - Error scenarios
     * Business Logic Tests
       - State transitions
       - Validation rules
       - Data processing

   - Integration Tests
     * Feature Flows
       - Complete user journeys
       - Cross-feature interactions
       - Data consistency
     * API Integration
       - End-to-end flows
       - Error handling
       - Performance

   - Performance Tests
     * Response Time
       - API calls < 2 seconds
       - UI updates < 100ms
       - Data loading < 1 second
     * Resource Usage
       - Memory management
       - Battery impact
       - Network efficiency

   - Security Tests
     * Authentication
       - Token management
       - Session handling
       - Access control
     * Data Protection
       - Encryption
       - Secure storage
       - Data privacy

2. **Success Criteria**
   - Functional Requirements
     * All core features working
     * Complete data migration
     * Proper error handling
     * Offline capabilities
   - Performance Requirements
     * Response time < 2 seconds
     * Smooth UI interactions
     * Efficient data sync
     * Low battery impact
   - Quality Requirements
     * Code coverage > 80%
     * No critical bugs
     * Proper documentation
     * User acceptance

## API Integration Status

### Total APIs: 38
1. Vehicle Management (4)
   - VehicleApi
   - VehicleTypeApi
   - VehicleCategoryApi
   - VehicleComponentApi
   - VehicleSessionApi

2. Checklist System (12)
   - ChecklistApi
   - ChecklistItemApi
   - ChecklistItemCategoryApi
   - ChecklistItemSubcategoryApi
   - ChecklistMetadataVehicleTypeApi
   - ChecklistRotationRulesItemCategoryApi
   - QuestionaryChecklistApi
   - QuestionaryChecklistItemApi
   - QuestionaryChecklistItemCategoryApi
   - QuestionaryChecklistItemSubcategoryApi
   - QuestionaryChecklistMetadataApi
   - QuestionaryChecklistRotationRulesApi

3. User & Authentication (6)
   - GOSecurityProviderApi
   - GOServicesApi
   - GOUserRoleApi
   - GOGroupRoleApi
   - GOGroupApi
   - SessionApi

4. Business & Site Management (4)
   - BusinessApi
   - BusinessConfigurationApi
   - SiteApi
   - UserBusinessApi

5. Incident & Safety (1)
   - IncidentApi

6. Certification & Feedback (2)
   - CertificationApi
   - FeedbackApi

7. Location & Weather (3)
   - CountryApi
   - CountryStateApi
   - WeatherApi

8. System & Utilities (6)
   - NotificationApi
   - GOFileUploaderApi
   - EnergySourceApi
   - UserChecklistApi
   - AnsweredChecklistItemApi
   - VehicleSessionApi

### Role-Specific Focus (Current Sprint)
1. ADMIN Role (Primary Focus)
   - Management Capabilities:
     * Vehicle Management
       - Full vehicle CRUD operations
       - Vehicle type/category management
       - Vehicle component management
     * User Management
       - View operator list
       - Manage operator access
     * Checklist Management
       - View all checklists
       - Monitor checklist completion
     * Incident Management
       - View all incidents
       - Manage incident reports
       - Generate safety reports
     * Reports & Analytics
       - Basic incident reports
       - Basic checklist reports
       - CICO (Check-in/Check-out) history
       - User activity reports
   
   - Operational Capabilities (Same as Operator):
     * Vehicle Operations
       - View vehicle list
       - View vehicle details
       - Complete pre-shift checklists
       - Start/end vehicle sessions
     * Checklist System
       - Complete daily checklists
       - View checklist history
     * Incident Reporting
       - Report incidents
       - View incident history
     * Profile & History
       - View and edit profile
       - View personal CICO history
       - View personal incident reports
       - View personal checklist history

2. OPERATOR Role
   - Core Functionalities:
     * Vehicle Management
       - View vehicle list
       - View vehicle details
       - Complete pre-shift checklists
       - Start/end vehicle sessions
     * Checklist System
       - Complete daily checklists
       - View checklist history
     * Incident Reporting
       - Report incidents
       - View own incident history

### Integration & Testing Status

#### ✅ Fully Integrated & Tested
1. Authentication & Onboarding
   - LoginScreen: GOSecurityProviderApi (Complete integration and testing)

2. Vehicle Management (Admin & Operator)
   - VehicleListScreen: VehicleApi, VehicleTypeApi (Complete integration and testing)
   - VehicleProfileScreen: VehicleApi, VehicleComponentApi (Complete integration and testing)

#### 🔄 In Progress (GO Platform Integration)
1. Checklist Management (Admin & Operator)
   - ChecklistScreen: 
     * Primary APIs: ChecklistApi, ChecklistItemApi
     * Secondary Flow: VehicleSessionApi (for session management after checklist completion)
     * Integration Status: 
       - GO Platform integration in progress
       - Need to test complete flow:
         1. Checklist completion
         2. Vehicle session initiation
         3. Session state management
     * Dependencies:
       - VehicleApi (for vehicle status updates)
       - VehicleSessionApi (for session management)
       - UserChecklistApi (for user-checklist association)

2. Admin Management Features
   - UserManagementScreen: GOUserRoleApi, GOGroupRoleApi
   - VehicleManagementScreen: VehicleApi, VehicleTypeApi, VehicleCategoryApi
   - IncidentManagementScreen: IncidentApi

#### ⏳ Pending Integration (Prioritized for Current Sprint)
1. Admin Operational Features
   - IncidentReportScreen: IncidentApi
   - ChecklistHistoryScreen: UserChecklistApi, AnsweredChecklistItemApi
   - VehicleSessionScreen: VehicleSessionApi
   - ProfileScreen: GOUserRoleApi, UserApi
   - CicoHistoryScreen: VehicleSessionApi

2. Admin Management Features
   - VehicleCategoriesScreen: VehicleCategoryApi
   - VehicleTypesScreen: VehicleTypeApi
   - VehicleComponentsScreen: VehicleComponentApi
   - OperatorsListScreen: GOUserRoleApi
   - SafetyAlertsScreen: IncidentApi
   - BasicReportsScreen: 
     * IncidentApi (for incident reports)
     * UserChecklistApi (for checklist reports)
     * VehicleSessionApi (for CICO history)

### Integration Notes
1. Authentication System
   - LoginScreen is fully integrated and tested with GOSecurityProviderApi
   - Register and Tour screens pending integration

2. Vehicle Management
   - Core vehicle listing and profile features are fully integrated
   - Vehicle creation, editing, and categorization features pending

3. Checklist System
   - Currently being integrated in GO platform
   - Complex flow implementation:
     1. Checklist completion and validation
     2. Vehicle session initiation
     3. Session state management
   - Key integration points:
     * ChecklistApi + ChecklistItemApi for checklist management
     * VehicleSessionApi for session handling
     * VehicleApi for status updates
     * UserChecklistApi for user associations
   - Testing priorities:
     1. Basic checklist completion
     2. Session initiation flow
     3. Session state management
     4. Vehicle status synchronization
     5. User-checklist associations

4. Next Integration Priorities (Current Sprint)
   - Complete checklist system integration with session management (Admin & Operator)
   - Implement incident reporting system (Admin & Operator)
   - Complete vehicle management features (Admin)
   - Set up operator management features (Admin)
   - Migrate profile and basic reports from MockAPI:
     * ProfileScreen functionality
     * Basic incident reports
     * Basic checklist reports
     * CICO history reports

### API Dependencies & Flows
1. Checklist Completion Flow
   ```
   ChecklistScreen
   ├── ChecklistApi (Get checklist data)
   ├── ChecklistItemApi (Get items)
   ├── UserChecklistApi (Associate with user)
   ├── VehicleSessionApi (Initiate session)
   └── VehicleApi (Update status)
   ```

2. Session Management Flow
   ```
   VehicleSessionApi
   ├── VehicleApi (Status updates)
   ├── UserChecklistApi (User association)
   └── ChecklistApi (Completion status)
   ```

3. Admin Workflow (Dual Role)
   ```
   Management Operations
   ├── User Management
   │   ├── GOUserRoleApi
   │   └── GOGroupRoleApi
   ├── Vehicle Management
   │   ├── VehicleApi
   │   ├── VehicleTypeApi
   │   └── VehicleCategoryApi
   ├── Checklist Monitoring
   │   ├── UserChecklistApi
   │   └── AnsweredChecklistItemApi
   ├── Incident Management
   │   └── IncidentApi
   └── Reports & Analytics
       ├── IncidentApi (Incident reports)
       ├── UserChecklistApi (Checklist reports)
       └── VehicleSessionApi (CICO history)

   Operational Activities
   ├── Vehicle Operations
   │   ├── VehicleApi
   │   └── VehicleSessionApi
   ├── Checklist Completion
   │   ├── ChecklistApi
   │   ├── ChecklistItemApi
   │   └── UserChecklistApi
   ├── Incident Reporting
   │   └── IncidentApi
   └── Profile & History
       ├── GOUserRoleApi (Profile management)
       ├── VehicleSessionApi (CICO history)
       └── UserChecklistApi (Personal checklist history)
   ```

4. Operator Workflow
   ```
   Daily Operations
   ├── Login (GOSecurityProviderApi)
   ├── Vehicle Selection (VehicleApi)
   ├── Checklist Completion
   │   ├── ChecklistApi
   │   ├── ChecklistItemApi
   │   └── UserChecklistApi
   ├── Session Management
   │   └── VehicleSessionApi
   └── Incident Reporting
       └── IncidentApi
   ```

## Naming Conventions

### API & JSON Fields
- Use camelCase for all API endpoints and JSON fields
- snake_case should be avoided unless dealing with external systems that strictly require it
- All DTOs should use camelCase in both property names and @SerializedName annotations
- Example:
  ```json
  {
    "id": "123",
    "typeId": "456",
    "vehicleName": "Forklift-1",
    "createdAt": "2024-04-08T10:00:00Z"
  }
  ```

The application has a rich set of screens organized by different user roles and functionalities. Here's the breakdown:

Authentication & Onboarding:
TourScreen - Initial onboarding screen
LoginScreen - User login
RegisterScreen - User registration
Dashboard Screens (Role-based):
DashboardScreen - Main dashboard for operators
AdminDashboardScreen - Dashboard for administrators
SuperAdminDashboardScreen - Dashboard for super admins
SystemOwnerDashboardScreen - Dashboard for system owners
Vehicle Management:
VehicleListScreen - List of vehicles
VehicleProfileScreen - Vehicle details
AddVehicleScreen - Add new vehicle
EditVehicleScreen - Edit vehicle details
VehicleCategoriesScreen - Vehicle categories management
VehicleTypesScreen - Vehicle types management
VehicleComponentsScreen - Vehicle components management
AdminVehiclesListScreen - Admin view of vehicles
AdminVehicleProfileScreen - Admin view of vehicle details
Checklist Management:
ChecklistScreen - Pre-shift checklist
AllChecklistScreen - All checklists view
CheckDetailScreen - Checklist details
QuestionaryChecklistScreen - Questionary checklist
QuestionarySelectionScreen - Questionary selection
QuestionaryChecklistItemScreen - Questionary items
ChecklistCategoriesScreen - Checklist categories
QuestionaryChecklistItemCategoryScreen - Questionary categories
QuestionaryChecklistItemSubcategoryScreen - Questionary subcategories
Incident & Safety:
IncidentReportScreen - Report incidents
IncidentListScreen - List of incidents
IncidentDetailScreen - Incident details
SafetyAlertsScreen - Safety alerts
PerformanceReportScreen - Performance reporting
User Management:
ProfileScreen - User profile
UserManagementScreen - User management
OperatorsListScreen - List of operators
CicoHistoryScreen - Check-in/Check-out history
Certification Management:
CertificationsScreen - List of certifications
CertificationDetailScreen - Certification details
CertificationScreen - Create/Edit certification
System Management:
SystemSettingsScreen - System settings
BusinessManagementScreen - Business management
SitesScreen - Sites management
TimeZonesScreen - Timezone management
CountriesScreen - Country management
EnergySourcesScreen - Energy sources management
Group Management:
GroupManagementScreen - Group management
GroupRoleManagementScreen - Group role management
Other Features:
QRScannerScreen - QR code scanner
NotificationScreen - Notifications
CountriesScreen - Country management

*Note: This context file should be updated as the project evolves.*

### MockAPI Migration vs API Integration Analysis

#### 1. Overall Status Comparison
- MockAPI Migration: 40% Complete
- API Integration: ~59% Complete (Average across all APIs)
- Gap Analysis: API Integration is ahead by 19%

#### 2. Feature-Specific Comparison

1. **Authentication & Profile**
   - MockAPI Status: 40% Complete
   - API Integration Status: 88% Complete
     * GOSecurityProviderApi (100% Complete) ✅
     * GOUserRoleApi (70% Complete) 🔄
     * UserApi (Pending) ⏳
     * SessionApi (95% Complete) ✅
   - Gap: +48% (API Integration is ahead)

2. **Reports & History**
   - MockAPI Status: 40% Complete
   - API Integration Status: 30% Complete
     * IncidentApi (30% Complete) 🔄
     * UserChecklistApi (Pending) ⏳
     * VehicleSessionApi (60% Complete) 🔄
   - Gap: -10% (MockAPI is ahead)

#### 3. Migration Priorities

1. **High Priority (APIs Ready)**
   - ProfileScreen Migration
     * Required APIs: GOSecurityProviderApi, GOUserRoleApi, SessionApi
     * Status: APIs 88% Complete
     * Action: Complete ProfileScreen migration

2. **Medium Priority (APIs In Progress)**
   - Basic Reports Migration
     * Required APIs: IncidentApi, VehicleSessionApi
     * Status: APIs 45% Complete
     * Action: Continue API integration while migrating reports

3. **Low Priority (APIs Pending)**
   - Advanced Features
     * Required APIs: UserChecklistApi, AnsweredChecklistItemApi
     * Status: APIs 0% Complete
     * Action: Focus on API integration first

#### 4. Recommendations

1. **Immediate Actions**
   - Complete ProfileScreen migration using available APIs
   - Start reports migration for IncidentApi and VehicleSessionApi
   - Begin UserChecklistApi integration

2. **Short-term Goals**
   - Reduce the gap between MockAPI and API integration
   - Complete basic reports functionality
   - Implement CICO history using VehicleSessionApi

3. **Long-term Strategy**
   - Parallel development of API integration and MockAPI migration
   - Focus on completing pending APIs
   - Maintain feature parity between MockAPI and new implementation

### 4. Offline Capabilities (Current Implementation)

#### Implemented Features
1. **Authentication & Session**
   - Session persistence using DataStore
   - Token management for offline authentication
   - Basic user profile caching

2. **Vehicle Management**
   - Vehicle list caching
   - Vehicle status tracking
   - Basic vehicle information storage

3. **Checklist System**
   - Offline checklist completion
   - Local storage of checklist responses
   - Basic sync when online

4. **Incident Management**
   - Basic offline incident reporting
   - Local storage of incident data
   - Simple media caching

#### Current Implementation Status
- Basic data persistence ✅
- Session management ✅
- Token storage ✅
- Simple caching ✅
- Offline-first workflow ✅
- Basic sync mechanism ✅

#### Success Criteria for Offline Capabilities
1. **Functional Requirements**
   - Core features work offline ✅
   - Basic data sync when online ✅
   - No data loss during transitions ✅
   - Simple conflict resolution ✅

2. **Performance Requirements**
   - Fast offline operations ✅
   - Basic sync process ✅
   - Minimal battery impact ✅
   - Efficient storage usage ✅

3. **User Experience**
   - Clear offline status ✅
   - Basic offline/online transitions ✅
   - Simple sync process ✅
   - Basic data consistency ✅

[Rest of the document remains unchanged...]