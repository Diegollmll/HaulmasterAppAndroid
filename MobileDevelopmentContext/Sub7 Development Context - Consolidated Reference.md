# Sub7 Development Context - Consolidated Reference

## Project Overview & Timeline

### Phase 1: Project Setup & Initial Analysis (Late January 2025)
- Completed GO Platform environment setup
- Analyzed MVP requirements and OSHA compliance needs
- Created base entity structure and relationships
- Established core relationships for MVP functionality

### Phase 2: Core Implementation (Early February 2025)
- Evaluated development approaches
- Created initial MVP user stories
- Refined based on stakeholder feedback
- Implemented core domain structures

### Phase 3: Feature Development & Integration (Mid February 2025)
- Focus on essential MVP features
- Basic safety features implementation
- Core training system development
- Platform integration planning

## Key Technical Decisions

### Mobile Development Strategy
1. Initial Focus
   - Start with Android development
   - iOS development to follow
   - Use mock APIs while GO Platform issues are resolved

2. Architecture & Stack
   - Kotlin for Android
   - MVVM + Clean Architecture
   - Retrofit + OkHttp for networking
   - Room for local storage
   - Jetpack Compose for UI
   - Hilt for dependency injection

3. API Integration
   - REST API communication with GO Platform
   - Implement mock APIs initially
   - Plan for offline capabilities
   - Handle sync requirements

### GO Platform Integration

1. Known Issues
   - Auto-create application feature currently not working
   - Server reset required every 2 relationships
   - Walter to debug and notify when fixed

2. Workarounds
   - Use mock APIs for initial development
   - Document relationships carefully
   - Wait for platform fixes before final integration

## MVP Feature Prioritization

### High Priority
1. Authentication (basic operator/admin)
2. Vehicle registration system
3. Check-in/out functionality
4. Pre-shift safety checks

### Medium Priority
1. Incident reporting
2. Basic admin dashboard
3. Safety alerts
4. Basic compliance tracking

### Low Priority
1. Offline capabilities
2. Advanced profile management
3. Location tracking
4. Social features

## Business Rules & Logic

### User Roles & Permissions
1. Basic User Types:
   - Learner Operator (without forklift certification)
   - Certified Operator (with forklift certification)
   - Admin (Non-Certified) - admin rights without certification
   - Certified Admin - admin rights with certification

2. Certification Rules:
   - Operators are certified only for specific activities they are trained in
   - Can be certified for individual tasks within a training category
   - Can operate vehicles only for certified tasks
   - Certification tracking per task, not just per category

3. Authorization Flow:
   - Users start as basic users
   - Become operators after completing required training/certification
   - Admin assignment is separate from operator certification
   - Certification status affects vehicle access permissions

### Vehicle Session Management
1. Session Rules:
   - One vehicle per active session
   - Sessions require successful pre-shift check
   - Clear start and end times must be recorded
   - Vehicle status must be tracked (in-use, available)

2. Vehicle Access:
   - Operators can only use vehicles they're certified for
   - Pre-shift check must be completed before session start
   - Critical safety check failures prevent vehicle use
   - Vehicle-specific certifications must be validated

3. Session States:
   - INITIATED (after pre-shift check)
   - ACTIVE (during operation)
   - PAUSED (temporary stops)
   - COMPLETED (normal end)
   - TERMINATED (abnormal end)
   - INCIDENT_REPORTED (when incident occurs)

### Incident Reporting
1. Report Types:
   - Collisions
   - Near-misses
   - Hazards
   - Vehicle failures

2. Incident Attributes:
   - Multi-select fields for most categories
   - Single-select for injuries and load weight
   - Automatic capture of session context if during session
   - Can report incidents without active vehicle session

3. Vehicle Failures:
   - Can be reported during pre-shift check
   - Can be reported during active session
   - Requires immediate action flag for critical issues
   - Must capture failure type and severity

4. Hazard Types (Multi-select):
   - Uneven Surface
   - Poor Visibility
   - Obstructed Path
   - Equipment Defect
   - Unsafe Loading Area
   - Spills
   - Other

### Training & Certification Management
1. Training Rules:
   - Training tasks can be completed individually
   - Certification granted per completed task
   - Video evidence required for training assessment
   - One video recording per assessment

2. Quiz Requirements:
   - Experience quiz required for onboarding
   - Success criteria based on points
   - Vehicle certification follows quiz completion
   - Training category and details required

3. Certification Triggers:
   - After accidents/unsafe use observed
   - New equipment introduction
   - Assignment to different forklift type
   - Regular renewal periods

## Key Development Decisions

### User Authentication
- Basic username/password authentication
- No social media integration for MVP
- Focus on core operator and admin roles
- Simple role-based access control

### Vehicle Management
- QR code scanning for vehicle identification
- Basic pre-shift checks
- Simple session tracking
- Critical safety validations only

### Safety Features
- Basic incident reporting
- Simple hazard reporting
- Essential safety checks
- Critical alerts only

### Training & Certification
- Basic certification tracking
- Simple training verification
- Essential compliance checks
- Core OSHA requirements

## Platform Requirements

### GO Platform Specifics
1. Entity Creation
   - Use systematic approach
   - Document relationships
   - Consider server reset requirements
   - Follow naming conventions

2. Data Model
   - Integer primary keys
   - Consistent audit fields
   - Mobile-optimized structure
   - Clear hierarchy definitions

3. API Integration
   - REST-based communication
   - Token authentication
   - Basic error handling
   - Simple data sync

## Implementation Guidelines

### Development Process
1. Set up core architecture
2. Implement authentication
3. Add vehicle management
4. Build safety features
5. Integrate with GO Platform
6. Comprehensive testing
7. User acceptance testing

### Testing Strategy
1. Unit tests for core logic
2. Integration tests for APIs
3. UI testing with Compose
4. Mock API testing
5. Offline capability testing
6. Performance validation

### Documentation Requirements
1. Technical specifications
2. API documentation
3. User guides
4. Testing documentation
5. Deployment guides

## Next Steps & Action Items

### Immediate Priorities
1. Complete Android project setup
2. Implement mock APIs
3. Build authentication flow
4. Create vehicle check system
5. Test core functionality

### Platform Integration
1. Wait for GO Platform fixes
2. Document API requirements
3. Plan integration testing
4. Prepare deployment strategy

### Future Considerations
1. iOS development planning
2. Advanced feature roadmap
3. Performance optimization
4. Enhanced security measures

## Notes from Key Meetings

### Gamification Discussion (January 31, 2025)
- Badges and levels system planned
- Points system for tracking progress
- Quiz system for training/certification
- Critical vs non-critical checklist items

### Vehicle Management (February 3, 2025)
- Check-in/check-out system requirements
- Edge cases for vehicle usage
- Session tracking needs
- Safety considerations

### GO Platform Development (February 6, 2025)
- User roles and hierarchy clarification
- Training process integration
- Point system implementation
- Short-term priorities defined

### Development Sync (February 14, 2025)
- Data model refinement
- Auto-create application issues
- Mobile app development strategy
- Documentation requirements

## Risk Mitigation

### Technical Risks
1. GO Platform integration delays
   - Use mock APIs
   - Document requirements
   - Regular communication with platform team

2. Offline capabilities
   - Implement basic offline storage
   - Plan sync strategy
   - Test edge cases

3. Performance concerns
   - Regular performance testing
   - Optimize resource usage
   - Monitor battery impact

### Process Risks
1. Development timeline
   - Focus on MVP features
   - Regular progress reviews
   - Clear prioritization

2. Integration challenges
   - Early testing with mocks
   - Clear documentation
   - Regular sync meetings

3. User adoption
   - Early user feedback
   - Regular testing
   - Clear documentation