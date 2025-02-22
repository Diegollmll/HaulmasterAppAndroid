# Sub7 Mobile MVP - Consolidated User Stories

## Project Purpose & Objectives

The Sub7 platform aims to enhance workplace safety and ensure regulatory compliance in the material handling industry. For the MVP phase, we focus on four essential capabilities:

1. Safety First
   - Enable pre-shift safety checks
   - Support basic incident reporting
   - Track vehicle usage

2. Regulatory Compliance
   - Implement basic OSHA-compliant checks
   - Track operator certifications
   - Record vehicle sessions

3. Core Features
   - Basic authentication
   - Simple vehicle check-in/out
   - Essential safety reporting
   - Session management

4. Mobile-First Design
   - Simple, focused UI
   - Critical features accessible quickly
   - Clear error handling
   - Basic offline support

## Core MVP Stories

### 1. Authentication (Story 1.1)

**As a** forklift operator,  
**I want to** log into the app using my username and password,  
**So that** I can access my personalized app features securely.

#### Acceptance Criteria
- User can enter username and password
- Password field masks input
- Show/hide password toggle available
- Submit button enabled only with valid input
- Clear error messages for invalid credentials
- Loading indicator during authentication
- Basic error handling
- Store authentication token securely

#### Technical Requirements

API Integration:
- POST /api/auth/login
- Request: { username, password }
- Response: { token, refreshToken, user }
- Error handling for invalid credentials
- Token storage in secure storage

Data Models:
- AuthState { token, refreshToken, timestamp }
- UserProfile { id, name, role, permissions }
- LoginCredentials { username, password }

Error States:
1. Invalid credentials
2. Network unavailable 
3. Server error
4. Account locked

#### Not Included for MVP
- Password recovery
- Remember me
- Complex validation
- Biometrics
- Offline auth
- Multi-device handling
- Complex security features

### 2. Pre-Shift Vehicle Check (Story 1.2)

**As a** forklift operator,  
**I want to** complete a basic vehicle safety check before starting work,  
**So that** I can ensure the vehicle is safe to operate.

#### Acceptance Criteria
- Scan QR code to identify vehicle
- Show basic vehicle info (ID, type)
- Display simple YES/NO questions
- Critical safety items only
- Submit inspection results
- Prevent vehicle use if critical checks fail
- Clear submission status
- Basic error handling

#### Technical Requirements

API Integration:
- GET /api/vehicles/{qrCode}
- POST /api/checks/pre-shift
- Handle submission failures
- Store check results

Data Models:
- Vehicle { id, type, status }
- CheckItem { id, question, isCritical }
- CheckResult { itemId, response, timestamp }

#### Not Included for MVP
- Offline support
- Photo capture
- Complex validations
- Detailed vehicle info
- Check history
- Multiple checklists
- Time tracking
- Location tracking

### 3. Vehicle Session Management (Story 1.3)

**As a** forklift operator,  
**I want to** start and end my vehicle operation sessions,  
**So that** my work time and vehicle usage can be tracked.

#### Acceptance Criteria
- Start session after passing pre-shift check
- Record start time
- Show active session status
- End session with single tap
- Record end time
- Clear session status
- Basic error handling
- Show session duration

#### Technical Requirements

API Integration:
- POST /api/sessions/start
- POST /api/sessions/end
- Handle session state changes
- Track session times

Data Models:
- Session { id, vehicleId, startTime, endTime, status }
- SessionStatus { ACTIVE, ENDED }

#### Not Included for MVP
- Break tracking
- Location tracking
- Session history
- Detailed activity logging
- Automatic timeouts
- Multiple active sessions
- Complex state management
- Performance analytics

### 4. Basic Incident Reporting (Story 1.4)

**As a** forklift operator,  
**I want to** report basic incidents (collisions or near-misses),  
**So that** safety issues can be documented and addressed.

#### Acceptance Criteria
- Select incident type (collision, near-miss, hazard)
- Enter basic description
- Submit to supervisor
- Record time and reporter
- Optional vehicle association
- Clear form after submission
- Show submission status
- Basic error handling

#### Technical Requirements

API Integration:
- POST /api/incidents
- Handle submission failures
- Store draft reports

Data Models:
- Incident { type, description, timestamp, reporterId, vehicleId? }
- IncidentType { COLLISION, NEAR_MISS, HAZARD }

#### Not Included for MVP
- Photo/video uploads
- Detailed categories
- Location tracking
- Witness statements
- Injury reporting
- Complex workflows
- Root cause analysis
- Corrective actions
- Notification system

## Android Implementation Guidelines

### Architecture
- MVVM + Clean Architecture
- Repository pattern
- Use cases for business logic
- ViewModel for UI state management
- Single activity, multiple fragments
- Navigation component for routing

### Tech Stack
- Kotlin
- Jetpack Compose for UI
- Retrofit + OkHttp for networking
- Room for local storage
- Hilt for dependency injection
- Coroutines for async operations
- Flow for reactive streams
- MockWebServer for API mocks

### Testing Strategy
- Unit tests for use cases and ViewModels
- UI tests with Compose testing
- Integration tests for repositories
- Mock API responses during development
- Test error scenarios and edge cases

### Development Approach
1. Set up core architecture and dependencies
2. Implement authentication flow
3. Add vehicle check functionality
4. Build session management
5. Create incident reporting
6. Add error handling and loading states
7. Polish UI and UX
8. Comprehensive testing

### Mocking Strategy
While GO Platform API is unavailable:
1. Define mock API responses for each endpoint
2. Use MockWebServer to simulate network
3. Create realistic test data
4. Simulate various error conditions
5. Test sync behavior
6. Validate offline scenarios

### Key Considerations
- Handle configuration changes
- Manage process death
- Implement proper error handling
- Follow Material Design guidelines
- Consider accessibility
- Optimize battery usage
- Handle network changes
- Support different screen sizes