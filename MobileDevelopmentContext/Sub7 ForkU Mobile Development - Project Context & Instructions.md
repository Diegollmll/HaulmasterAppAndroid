# Sub7 ForkU Mobile Development Instructions

## Current Project Context

### Project Status
- GO Platform entity relationships completed
- Ready for auto-create application
- Moving to Android implementation phase
- MVP features defined and scoped

### MVP Core Stories
1. Story 1.1: Basic Authentication
   - Simple username/password login
   - Basic error handling
   - Clear user feedback

2. Story 1.2: Pre-Shift Vehicle Check
   - QR code vehicle identification
   - Basic YES/NO questions
   - Critical safety items only

3. Story 1.3: Vehicle Session Management
   - Simple session start/end
   - Basic status tracking
   - Essential time recording

4. Story 1.4: Basic Incident Reporting
   - Basic incident types
   - Simple description field
   - Submit to supervisor

### Timeline Context
- One month for MVP completion
- Focus on implementation
- Rapid development cycle
- MVP features only

## Development Guidelines

### 1. MVP First Approach
MUST:
- Keep features minimal
- Focus on core functionality
- Implement basic flows
- Ensure critical safety features

AVOID:
- Offline capabilities
- Complex features
- Advanced security
- Detailed analytics

### 2. Android Implementation Focus
Key Points:
- Kotlin development
- MVVM + Clean Architecture
- Jetpack Compose for UI
- Basic GO Platform integration

### 3. Technical Requirements

#### Core Technologies
- Kotlin for development
- Jetpack Compose for UI
- Retrofit for networking
- Room for persistence
- Hilt for dependency injection
- Coroutines for async operations

#### Architecture
- MVVM + Clean Architecture
- Repository pattern
- Use cases for business logic
- Simple view hierarchies
- Basic error handling

#### API Integration
- REST communication
- Basic authentication
- Simple data models
- Essential error handling

## Implementation Priorities

### Immediate Next Steps
1. Complete auto-create application with GO Platform
2. Set up Android project structure
3. Implement core MVVM architecture
4. Create authentication flow
5. Build basic UI components

### Short-term Goals
1. Basic authentication working
2. Vehicle check implementation
3. Session management
4. Simple incident reporting
5. Essential testing coverage

## Development Process

### 1. Project Setup
- Initialize Android project
- Configure MVVM structure
- Set up dependency injection
- Add core dependencies

### 2. Feature Implementation Order
1. Authentication (Story 1.1)
   - Login screen
   - Basic API integration
   - Error handling

2. Vehicle Check (Story 1.2)
   - QR scanning
   - Basic checklist
   - Submit results

3. Session Management (Story 1.3)
   - Start/end tracking
   - Status display
   - Time recording

4. Incident Reporting (Story 1.4)
   - Basic form
   - Type selection
   - Simple submission

### 3. Testing Approach
- Core functionality tests
- Basic user flows
- Essential error cases
- Critical safety validations

## Quality Standards

### Code Quality
- Follow Kotlin style guide
- Clear documentation
- Basic error handling
- Essential testing

### User Experience
- Simple, focused UI
- Clear feedback
- Essential flows only
- Basic error messages

### Safety Requirements
- Critical checks validation
- Basic safety features
- Essential compliance
- Simple reporting

## Communication & Documentation

### Team Communication
- Regular progress updates
- Clear blocking issues
- Implementation questions
- Technical challenges

### Documentation Needs
- Basic architecture docs
- Essential API docs
- Core user flows
- Setup instructions

## GO Platform Integration

### API Integration
- Basic REST endpoints
- Simple authentication
- Essential data sync
- Clear error states

### Data Flow
- Simple CRUD operations
- Basic data models
- Essential validations
- Error handling

## Risk Mitigation

### Technical Risks
- Test core features
- Validate API integration
- Check safety features
- Monitor performance

### Timeline Risks
- Focus on MVP
- Avoid scope creep
- Regular checkpoints
- Clear communication

## Support Resources

### Technical Reference
- MVP story documentation
- GO Platform API docs
- Mobile stack definition
- Android guidelines

### Team Support
- GO Platform team
- Project management
- Development team
- QA support

### Development Environment
- Android Studio
- Kotlin & Compose setup
- GO Platform access
- Testing frameworks