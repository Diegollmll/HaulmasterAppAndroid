# ForkU Android Project Context

## Project Overview
ForkU is an Android application built with modern Android development practices and Clean Architecture principles. The app appears to be focused on vehicle management, incident tracking, and user authentication systems.

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
- Login/Register functionality
- Session management
- User role management

### 2. Vehicle Management
- Vehicle tracking
- Vehicle information management

### 3. Incident Tracking
- Incident reporting
- Incident management

### 4. Checklist System
- Task checklists
- Checklist management

### 5. QR Scanner
- QR code scanning functionality
- Scanner integration

### 6. Tour/Onboarding
- User onboarding flow
- Feature introduction

## Architecture Components

### 1. Presentation Layer (MVVM)
- ViewModels for state management
- Composable UI components
- Screen-based navigation
- Shared UI components

### 2. Domain Layer
- Business logic encapsulation
- Use case pattern implementation
- Clean Architecture principles
- Repository interfaces

### 3. Data Layer
- Repository implementations
- Data source abstractions
- DTO mappings
- Local/Remote data handling

## Common Patterns

### 1. State Management
- UI state handling in ViewModels
- Event handling patterns
- State preservation

### 2. Navigation
- Compose Navigation
- Deep linking support
- Screen route management

### 3. Error Handling
- Repository error management
- UI error presentation
- Network error handling

### 4. Data Storage
- DataStore for preferences
- Room Database for structured data
- Caching strategies

## Important Notes
- The project follows Material Design principles
- Uses modern Android development practices
- Implements clean architecture patterns
- Focuses on maintainable and testable code

## Recent Changes/Focus Areas
- User authentication improvements
- Tour screen animations
- Login/Register flow enhancements
- Error handling in repositories

---
*Note: This context file should be updated as the project evolves.* 