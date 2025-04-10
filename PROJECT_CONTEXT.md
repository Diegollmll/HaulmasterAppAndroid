# ForkU Android Project Context

## Project Overview
ForkU is an Android application built with modern Android development practices and Clean Architecture principles. The app appears to be focused on vehicle management, incident tracking, user authentication systems, and certification management.

## Tech Stack & Architecture

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
hilt = "2.48"
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

## MockAPI Integration

### API Base URL
```
https://67ed9e4e4387d9117bbe2e16.mockapi.io/forku/api/v1
```

### API's Data Structure
```
user (7)
vehicle-category (6)
    └── vehicle-type (26)
experience-level (0)
energy-source (0)

questionary-checklist-item-category (9)
    └── questionary-checklist-item-subcategory (15)
questionary-checklist (0)
    └── questionary-checklist-item (0)
feedback (1)
notification (0)
timezone (0)
country (50)
    └── country-state (100)
business (3)
    └── site (0)
        └── site-configuration (0)
        └── incident (0)
        └── vehicle (1)
            └── checklist (0)
            └── safety-alert (0)
            └── vehicle-session (0)
        └── geofence (0)
        └── business-configuration (0)
configuration (0)
certification (0)
subscription (0)
```
The numbers in parentheses indicate the count of records in each collection.
We can access to any entity like this for example: "/business/1/vehicle" (all vehicles of that business) or "/vehicle" (for all vehicles)

### Naming Convention
- All API endpoints and response fields use camelCase
- Example: `businessId`, `vehicleType`, `nextService` (not `business_id`, `vehicle_type`, `next_service`)

### Response Handling
- When no results are found (404), the API returns a string: "Not found"
- Success responses return JSON objects/arrays
- Error responses follow standard HTTP status codes

### Filtering
Filtering is implemented using query parameters:

| Parameter | Type | Example | Description |
|-----------|------|---------|-------------|
| search | String | search=hello | Get items matching string in any field |
| filter | String | filter=hello | Get items matching string in any field |
| fieldName | String | status=suspended | Get items matching specific field value |

Example:
```kotlin
// Get businesses with suspended status
api.getAllBusinesses(status = "SUSPENDED")

// Get businesses for a specific superAdmin
api.getAllBusinesses(superAdminId = "123")
```

### Pagination
Pagination parameters:

| Parameter | Type | Example | Description |
|-----------|------|---------|-------------|
| page | Number | page=1 | Get specific page |
| limit | Number | limit=10 | Items per page |

### Sorting
Sorting parameters:

| Parameter | Type | Example | Description |
|-----------|------|---------|-------------|
| sortBy | field_name | sortBy=name | Sort by field |
| order | asc/desc | order=desc | Sort direction |

### API Endpoints

#### Businesses
- GET `/business` - Get all businesses (supports filtering)
- GET `/business/{id}` - Get business by ID
- POST `/business` - Create new business
- PUT `/business/{id}` - Update business
- DELETE `/business/{id}` - Delete business

#### Users
- GET `/user` - Get all users (supports filtering)
- GET `/user/{id}` - Get user by ID
- POST `/user` - Create new user
- PUT `/user/{id}` - Update user
- DELETE `/user/{id}` - Delete user

### Error Handling
- All API calls should handle 404 "Not found" responses by returning empty lists/null objects
- Other error codes should be propagated to the UI layer for user feedback

---
*Note: This context file should be updated as the project evolves.* 