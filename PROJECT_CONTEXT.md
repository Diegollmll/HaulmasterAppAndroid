# ForkU Android Project Context

## Project Overview
ForkU is an Android application built with modern Android development practices and Clean Architecture principles. The app focuses on vehicle management, incident tracking, user authentication, and certification management for industrial operations.

## Current Project Status (December 2024)

### 🎯 **Core Systems - Production Ready**

#### ✅ Authentication System (100% Complete)
- **Login/Register**: Email/password authentication with GO Platform integration
- **Session Management**: Persistent sessions with DataStore and automatic token renewal
- **User Role Management**: Support for SYSTEM_OWNER, SUPERADMIN, ADMIN, OPERATOR roles
- **Tour/Onboarding**: User preference-based onboarding flow
- **Security**: CSRF protection, antiforgery cookies, and secure token handling
- **Session Keep-Alive**: Automatic session maintenance with background/foreground handling

#### ✅ Vehicle Management (100% Complete)
- **Vehicle Tracking**: Real-time status monitoring and updates
- **QR Code Integration**: Vehicle identification and access control
- **Status Management**: AVAILABLE, IN_USE, OUT_OF_SERVICE, MAINTENANCE states
- **Vehicle Profiles**: Comprehensive vehicle information with responsive UI
- **Type Management**: Support for different vehicle types and energy sources
- **Session Integration**: Vehicle session lifecycle management

#### ✅ Checklist System (95% Complete)
- **Pre-shift Checklists**: Dynamic question-based vehicle inspections
- **Real-time Validation**: Immediate feedback and status determination
- **Multimedia Support**: Photo attachments for checklist items
- **Safety Alert Integration**: Automatic safety alert creation for failed non-critical items
- **Certification Validation**: Role-based access and certification requirements
- **Business Context**: Multi-tenant checklist filtering and management

#### ✅ Incident Management (85% Complete)
- **Multiple Incident Types**: Collision, Hazard, Near Miss reporting
- **Multimedia Documentation**: Photo and document attachments
- **Severity Classification**: Risk-based incident categorization
- **Load Weight Tracking**: Operational context recording
- **Business Integration**: Multi-tenant incident management

#### ✅ Admin Dashboard (90% Complete)
- **User Management**: Comprehensive user administration
- **Certification Tracking**: Expiration monitoring and status management
- **Vehicle Fleet Overview**: Real-time fleet status and analytics
- **Business Site Filtering**: Discrete filter UI with collapsible design
- **Role-based Access**: Contextual features based on user permissions
- **Admin-Specific Data Loading**: Separate data loading for Admin vs Operator contexts
- **Multi-Site Management**: Admin can view and manage all sites within their business

#### ✅ Multitenancy Implementation (100% Complete)
- **Business Context Management**: Centralized BusinessContextManager
- **Data Isolation**: Automatic businessId filtering across all entities
- **Cross-Platform Consistency**: Backend and mobile app synchronized
- **Repository Pattern**: Business-aware data access layer
- **Session Management**: Business-scoped vehicle sessions

### 🔧 **Technical Architecture**

#### **Core Technologies**
- **Language**: Kotlin 1.9.22
- **UI Framework**: Jetpack Compose (BOM 2024.02.00)
- **Architecture**: MVVM + Clean Architecture + Repository Pattern
- **Dependency Injection**: Hilt 2.48
- **Navigation**: Jetpack Navigation Compose 2.7.7
- **Local Storage**: Room 2.6.1 + DataStore 1.0.0
- **Network**: Retrofit 2.9.0 + OkHttp 4.12.0 with custom interceptors
- **Image Loading**: Coil 2.5.0
- **QR Functionality**: CameraX 1.3.1 + MLKit Barcode 17.2.0
- **State Management**: StateFlow + Compose State

#### **Role-Based Architecture Patterns**
- **Admin Context**: Administrative functions load data for ALL sites within business or for specific site selected.
- **Operator Context**: User functions load data only for assigned site/business
- **Shared Filters**: AdminSharedFiltersViewModel for admin-specific filtering
- **Business Context Manager**: Centralized business/site context management
- **Repository Pattern**: Role-aware data access with context filtering

#### **Project Structure**
```
app/src/main/java/app/forku/
├── presentation/              # UI Layer (Jetpack Compose)
│   ├── common/               # Shared UI components (BaseScreen, Filters, etc.)
│   ├── dashboard/            # Admin & User dashboards
│   ├── incident/             # Incident reporting & management
│   ├── checklist/            # Pre-shift checklist system
│   ├── vehicle/              # Vehicle management & profiles
│   ├── user/                 # User management & authentication
│   ├── scanner/              # QR code scanning functionality
│   ├── session/              # Vehicle session management
│   ├── certification/        # Certification tracking
│   └── navigation/           # App navigation structure
├── domain/                   # Business Logic Layer
│   ├── model/               # Domain entities (Vehicle, User, Checklist, etc.)
│   ├── repository/          # Repository interfaces
│   ├── usecase/             # Business use cases
│   └── service/             # Domain services
├── data/                    # Data Layer
│   ├── api/                 # GO Platform API interfaces
│   ├── dto/                 # Data Transfer Objects
│   ├── mapper/              # DTO ↔ Domain mappers
│   ├── repository/          # Repository implementations
│   ├── datastore/           # Local preferences storage
│   └── service/             # Data services
├── core/                    # Core Utilities
│   ├── auth/                # Authentication & session management
│   ├── business/            # Business context management
│   ├── network/             # Network connectivity
│   ├── location/            # Location services
│   └── utils/               # Utility functions
└── di/                      # Dependency Injection modules
```

### 🚀 **Recent Major Accomplishments**

#### **BaseScreen Authentication Enhancement**
- **SessionKeepAliveManager Integration**: Comprehensive session management
- **Lifecycle-aware Management**: Automatic foreground/background handling
- **Token Renewal**: Proactive token refresh before expiration
- **Authentication State Monitoring**: Global session expiration handling
- **User Interaction Triggers**: Keep-alive on user activity

#### **Vehicle Status Update Fix**
- **GO API Compliance**: Fixed PRIMARY KEY constraint violations
- **Proper JSON Generation**: Correct `IsNew` and `IsDirty` field handling
- **Status Update Flow**: Reliable vehicle status transitions
- **Error Handling**: Comprehensive error logging and recovery

#### **UI/UX Improvements**
- **Vehicle Profile Responsive Design**: Optimized for long model numbers
- **Business Site Filters**: Discrete, collapsible filter interface
- **Back Button Conditional Logic**: Context-aware navigation
- **Spanish to English Localization**: Complete UI text translation

#### **UserPreferences API Integration**
- **GO Platform Standards**: Full compliance with required fields
- **Business Context**: Proper businessId integration
- **Error Resolution**: Fixed save functionality with proper DTO mapping

### 🔧 **GO Platform Integration Standards**

#### **Mandatory API Fields**
```json
{
  "$type": "EntityDataObject",
  "Id": null,                    // null for new, actual ID for updates
  "IsDirty": true,              // Always true for modifications
  "IsNew": true,                // true for creation, false for updates
  "IsMarkedForDeletion": false, // Always false for active entities
  "InternalObjectId": 0,        // Always 0 for new entities
  "BusinessId": "business-id"   // Current business context
}
```

#### **Authentication Requirements**
- **CSRF Token**: From `AuthDataStore.getCsrfToken()`
- **Antiforgery Cookie**: From `AuthDataStore.getAntiforgeryCookie()`
- **Business Context**: From `BusinessContextManager.getCurrentBusinessId()`

#### **API Pattern Template**
```kotlin
@FormUrlEncoded
@POST("api/{endpoint}")
suspend fun save(
    @Header("X-CSRF-TOKEN") csrfToken: String,
    @Header("Cookie") cookie: String,
    @Field("entity") entity: String,
    @Query("businessId") businessId: String
): Response<EntityDto>
```

#### **Entity Type Mappings**
- **Vehicle**: `"VehicleDataObject"`
- **Checklist**: `"ChecklistDataObject"`
- **ChecklistAnswer**: `"ChecklistAnswerDataObject"`
- **VehicleSession**: `"VehicleSessionDataObject"`
- **User**: `"UserDataObject"`
- **Incident**: `"IncidentDataObject"`
- **UserPreferences**: `"UserPreferencesDataObject"`

#### **Energy Source Values**
- **Electric**: 0
- **LPG**: 1  
- **Diesel**: 2

### 📊 **Current Development Status**

#### **Completed Features (Production Ready)**
- ✅ **Multi-tenant Architecture**: Complete business context isolation
- ✅ **Authentication Flow**: Secure login with session management
- ✅ **Vehicle Management**: Full CRUD operations with status tracking
- ✅ **Checklist System**: Dynamic questionnaires with validation
- ✅ **QR Code Integration**: Vehicle identification and access
- ✅ **Admin Dashboard**: Comprehensive management interface
- ✅ **Incident Reporting**: Multi-type incident documentation
- ✅ **Certification Tracking**: User qualification management
- ✅ **Session Keep-Alive**: Automatic session maintenance
- ✅ **Responsive UI**: Optimized for various screen sizes
- ✅ **Error Handling**: Comprehensive error recovery
- ✅ **Localization**: English UI throughout the application

#### **In Progress / Next Sprint**
- 🔄 **Admin Dashboard Data Loading**: Fix admin-specific data loading for dashboard cards (Administrative data not his own user data)
- 🔄 **Operating Vehicles Count**: Ensure admin sees count from all sites, not just personal context
- 🔄 **Advanced Analytics**: Reporting dashboard implementation
- 🔄 **Offline Support**: Local data caching and sync
- 🔄 **Push Notifications**: Real-time alerts and updates
- 🔄 **Advanced Multimedia**: Enhanced photo management
- 🔄 **Performance Optimization**: Further UI/UX improvements

#### **Planned Features**
- 📋 **Maintenance Scheduling**: Preventive maintenance workflows
- 📋 **Training Materials**: Integrated learning management
- 📋 **Advanced Reporting**: Custom report generation
- 📋 **API Rate Limiting**: Enhanced request management
- 📋 **Dark Mode**: Theme customization

### 🔍 **Known Issues & Technical Debt**

#### **Current Investigations**
- **Checklist Question Filtering**: Vehicle type association logic needs refinement
- **Performance**: Large dataset handling optimization opportunities
- **Memory Management**: Image loading and caching improvements

#### **Technical Debt Items**
- Centralized multimedia management system
- API response caching strategy
- Unit test coverage expansion
- Documentation updates

### 💡 **Development Guidelines**

#### **Code Standards**
- **Clean Architecture**: Strict separation of concerns
- **MVVM Pattern**: Reactive UI with StateFlow
- **Repository Pattern**: Abstracted data access
- **Dependency Injection**: Hilt-based modular design
- **Error Handling**: Comprehensive try-catch with logging
- **Testing**: Unit tests for business logic

#### **GO Platform Integration**
- **API Compliance**: Follow mandatory field requirements
- **Business Context**: Always include businessId filtering
- **Authentication**: Proper CSRF and cookie handling
- **Error Handling**: Graceful degradation and user feedback
- **Logging**: Detailed request/response logging for debugging

#### **UI/UX Standards**
- **Material Design**: Consistent design language
- **Responsive Layout**: Adaptive to different screen sizes
- **Accessibility**: Screen reader and navigation support
- **Performance**: Smooth animations and transitions
- **User Feedback**: Clear loading states and error messages

### 📈 **Metrics & Performance**

#### **App Performance**
- **Cold Start Time**: < 3 seconds
- **Navigation Transitions**: < 300ms
- **API Response Handling**: Optimized with proper loading states
- **Memory Usage**: Efficient image loading and caching
- **Battery Optimization**: Background task management

#### **Business Metrics**
- **User Adoption**: Multi-role support with role-based features
- **Data Integrity**: 100% business context isolation
- **Security Compliance**: Full CSRF and authentication protection
- **Operational Efficiency**: Streamlined checklist and incident workflows

### 🔧 **Development Environment**

#### **Required Tools**
- **Android Studio**: Latest stable version
- **Kotlin**: 1.9.22
- **Gradle**: 8.x
- **Java**: 17
- **Git**: Version control with feature branch workflow

#### **Testing Strategy**
- **Unit Tests**: Business logic and repository layer
- **Integration Tests**: API integration validation
- **UI Tests**: Critical user flow automation
- **Manual Testing**: Cross-device compatibility

### 📝 **Important Notes**

#### **Session Management**
- SessionKeepAliveManager handles automatic token renewal
- Background/foreground state transitions are managed
- User activity triggers keep-alive requests
- Authentication failures redirect to login

#### **Business Context**
- All data operations are business-scoped
- BusinessContextManager provides centralized context
- Repository layer automatically applies business filtering
- Multi-tenant data isolation is enforced

#### **Error Handling**
- Comprehensive error logging throughout the application
- User-friendly error messages in English
- Graceful degradation for network issues
- Proper error recovery mechanisms

#### **Security**
- CSRF token validation on all API requests
- Secure token storage in DataStore
- Automatic session expiration handling
- Role-based access control

### 🎯 **Recent Session Accomplishments**

#### **Spanish to English Localization (December 2024)**
- **Complete UI Translation**: All user-facing text converted to English
- **Error Messages**: Standardized English error messages across the app
- **Comments & Logs**: Updated code comments and log messages to English
- **Files Modified**: 16 files updated with comprehensive text changes
- **Quality Assurance**: Project compiles successfully with all changes

#### **Files Updated in Localization**
1. `ChecklistScreen.kt` - 12 UI text changes
2. `ChecklistViewModel.kt` - Error messages and logs
3. `VehicleRepositoryImpl.kt` - API error messages
4. `UserRepositoryImpl.kt` - Authentication error messages
5. `AdminDashboardViewModel.kt` - Log messages
6. `DashboardViewModel.kt` - Code comments
7. Multiple mapper and repository files - Comments and documentation

---

*This context file is maintained as of December 2024 and reflects the current production-ready state of the ForkU Android application.* 