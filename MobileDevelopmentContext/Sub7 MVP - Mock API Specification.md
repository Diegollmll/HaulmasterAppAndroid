# Sub7 MVP - Mock API Specification

## Overview

This document provides API specifications for implementing mock endpoints while GO Platform integration is pending. Based on the ERD and MVP requirements.

## Base Configuration

```kotlin
baseUrl = "https://api.sub7.mock/v1"
headers = {
    "Content-Type": "application/json",
    "Accept": "application/json"
}
```

## Authentication API (Story 1.1)

### Login
```
POST /auth/login
Request:
{
    "username": string,
    "password": string
}

Response (200):
{
    "token": string,
    "refreshToken": string,
    "user": {
        "id": string,
        "username": string,
        "role": "OPERATOR" | "ADMIN",
        "permissions": string[],
        "certifications": [{
            "vehicleTypeId": string,
            "isValid": boolean,
            "expiresAt": string
        }]
    }
}

Error (401):
{
    "error": "INVALID_CREDENTIALS",
    "message": "Invalid username or password"
}
```

## Vehicle API (Story 1.2)

### Get Vehicle by QR Code
```
GET /vehicles/qr/{code}
Response (200):
{
    "id": string,
    "type": {
        "id": string,
        "name": string,
        "requiresCertification": boolean
    },
    "status": "AVAILABLE" | "IN_USE" | "MAINTENANCE",
    "lastCheck": {
        "timestamp": string,
        "status": "PASS" | "FAIL"
    }
}

Error (404):
{
    "error": "VEHICLE_NOT_FOUND",
    "message": "Vehicle not found"
}
```

### Submit Pre-shift Check
```
POST /vehicles/{id}/checks
Request:
{
    "checkItems": [{
        "id": string,
        "response": boolean,
        "isCritical": boolean
    }],
    "timestamp": string,
    "notes": string?
}

Response (200):
{
    "id": string,
    "status": "PASS" | "FAIL",
    "failedCritical": boolean,
    "timestamp": string
}
```

## Session API (Story 1.3)

### Start Session
```
POST /sessions/start
Request:
{
    "vehicleId": string,
    "checkId": string,
    "timestamp": string
}

Response (200):
{
    "id": string,
    "status": "ACTIVE",
    "startTime": string,
    "vehicle": {
        "id": string,
        "type": string
    }
}

Error (403):
{
    "error": "SESSION_BLOCKED",
    "message": "Cannot start session - failed critical checks"
}
```

### End Session
```
POST /sessions/{id}/end
Request:
{
    "timestamp": string,
    "notes": string?
}

Response (200):
{
    "id": string,
    "status": "COMPLETED",
    "startTime": string,
    "endTime": string,
    "duration": number // minutes
}
```

## Incident API (Story 1.4)

### Report Incident
```
POST /incidents
Request:
{
    "type": "COLLISION" | "NEAR_MISS" | "HAZARD",
    "description": string,
    "vehicleId": string?,
    "sessionId": string?,
    "timestamp": string
}

Response (200):
{
    "id": string,
    "status": "REPORTED",
    "reportTimestamp": string,
    "assignedTo": string?
}
```

## Mock Data Structure

### Users
```kotlin
val mockUsers = listOf(
    User(
        id = "op1",
        username = "operator1",
        password = "test123", // Only in mock
        role = "OPERATOR",
        permissions = listOf("OPERATE_VEHICLE", "REPORT_INCIDENT")
    ),
    User(
        id = "admin1",
        username = "admin1",
        password = "admin123", // Only in mock
        role = "ADMIN",
        permissions = listOf("MANAGE_VEHICLES", "MANAGE_USERS", "VIEW_REPORTS")
    )
)
```

### Vehicles
```kotlin
val mockVehicles = listOf(
    Vehicle(
        id = "v1",
        qrCode = "VEH001",
        type = VehicleType(
            id = "vt1",
            name = "Standard Forklift",
            requiresCertification = true
        ),
        status = "AVAILABLE"
    )
)
```

### Check Items
```kotlin
val mockCheckItems = listOf(
    CheckItem(
        id = "ci1",
        question = "Are the forks in good condition?",
        isCritical = true
    ),
    CheckItem(
        id = "ci2",
        question = "Is the horn working?",
        isCritical = true
    ),
    CheckItem(
        id = "ci3",
        question = "Are the tires in good condition?",
        isCritical = true
    ),
    CheckItem(
        id = "ci4",
        question = "Are all lights functioning?",
        isCritical = false
    )
)
```

## Implementation Notes

1. Error Handling
   - Use consistent error response format
   - Include HTTP status codes
   - Provide meaningful error messages
   - Simulate network delays

2. Authentication
   - Validate tokens
   - Check permissions
   - Handle token expiration
   - Manage refresh flow

3. Data Persistence
   - Use Room database
   - Mirror API structure
   - Support offline data
   - Handle sync conflicts

4. Testing
   - Mock network responses
   - Test error scenarios
   - Validate data flow
   - Check edge cases

## Android Implementation

### MockWebServer Setup
```kotlin
fun setupMockWebServer() {
    val server = MockWebServer()
    server.dispatcher = object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            return when (request.path) {
                "/auth/login" -> handleLogin(request)
                "/vehicles/qr/${request.path?.split("/")?.last()}" -> handleGetVehicle(request)
                // ... other endpoints
                else -> MockResponse().setResponseCode(404)
            }
        }
    }
}
```

### API Interface
```kotlin
interface Sub7Api {
    @POST("auth/login")
    suspend fun login(@Body credentials: LoginRequest): Response<LoginResponse>

    @GET("vehicles/qr/{code}")
    suspend fun getVehicleByQr(@Path("code") code: String): Response<VehicleResponse>

    @POST("vehicles/{id}/checks")
    suspend fun submitCheck(
        @Path("id") vehicleId: String,
        @Body check: CheckRequest
    ): Response<CheckResponse>

    // ... other endpoints
}
```

### Repository Implementation
```kotlin
class Sub7Repository(
    private val api: Sub7Api,
    private val db: AppDatabase
) {
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(ApiException(response.code()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ... other repository methods
}
```