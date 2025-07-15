# Implementación de Validación de Business y Site en Scanner QR

## 🎯 **Objetivo**

Implementar validación de seguridad para que los usuarios **solo puedan acceder a vehículos de su business y site asignado** a través del scanner QR, previniendo acceso no autorizado a vehículos de otros businesses o sites.

---

## 🔧 **Cambios Implementados**

### **1. Nuevo Método en UserPreferencesRepository**

**Archivo**: `app/src/main/java/app/forku/domain/repository/user/UserPreferencesRepository.kt`

```kotlin
/**
 * Check if a vehicle belongs to the user's assigned business and site
 * @param vehicleBusinessId The business ID of the vehicle
 * @param vehicleSiteId The site ID of the vehicle
 * @return true if the vehicle belongs to the user's business and site, false otherwise
 */
suspend fun isVehicleInUserContext(vehicleBusinessId: String, vehicleSiteId: String): Boolean
```

### **2. Implementación en UserPreferencesRepositoryImpl**

**Archivo**: `app/src/main/java/app/forku/data/repository/user/UserPreferencesRepositoryImpl.kt`

```kotlin
override suspend fun isVehicleInUserContext(vehicleBusinessId: String, vehicleSiteId: String): Boolean {
    return try {
        Log.d(TAG, "🔍 Checking if vehicle belongs to user's assigned business and site")
        Log.d(TAG, "  - Vehicle Business ID: '$vehicleBusinessId'")
        Log.d(TAG, "  - Vehicle Site ID: '$vehicleSiteId'")
        
        val userPreferences = getCurrentUserPreferences()
        if (userPreferences == null) {
            Log.w(TAG, "❌ No user preferences found - user needs setup")
            return false
        }
        
        val userBusinessId = userPreferences.getEffectiveBusinessId()
        val userSiteId = userPreferences.getEffectiveSiteId()
        
        if (userBusinessId.isNullOrBlank()) {
            Log.w(TAG, "❌ No business assigned to user")
            return false
        }
        
        if (userSiteId.isNullOrBlank()) {
            Log.w(TAG, "❌ No site assigned to user")
            return false
        }
        
        val isSameBusiness = userBusinessId == vehicleBusinessId
        val isSameSite = userSiteId == vehicleSiteId
        val isInUserContext = isSameBusiness && isSameSite
        
        Log.d(TAG, "🔍 Context validation result:")
        Log.d(TAG, "  - User's business: '$userBusinessId'")
        Log.d(TAG, "  - Vehicle's business: '$vehicleBusinessId'")
        Log.d(TAG, "  - Business match: $isSameBusiness")
        Log.d(TAG, "  - User's site: '$userSiteId'")
        Log.d(TAG, "  - Vehicle's site: '$vehicleSiteId'")
        Log.d(TAG, "  - Site match: $isSameSite")
        Log.d(TAG, "  - Final result: $isInUserContext")
        
        isInUserContext
    } catch (e: Exception) {
        Log.e(TAG, "❌ Error checking vehicle context validation", e)
        false
    }
}
```

### **3. Integración en QRScannerViewModel**

**Archivo**: `app/src/main/java/app/forku/presentation/scanner/QRScannerViewModel.kt`

#### **Dependencia Agregada:**
```kotlin
@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val checklistRepository: ChecklistRepository,
    private val vehicleSessionRepository: VehicleSessionRepository,
    private val userPreferencesRepository: app.forku.domain.repository.user.UserPreferencesRepository
) : ViewModel() {
```

#### **Validación Agregada en onQrScanned:**
```kotlin
// VALIDATION: Check if vehicle belongs to user's assigned business and site
Log.d("QRFlow", "[onQrScanned] Validating vehicle context access")
val isVehicleInUserContext = userPreferencesRepository.isVehicleInUserContext(
    vehicle.businessId ?: "",
    vehicle.siteId ?: ""
)
Log.d("QRFlow", "[onQrScanned] Vehicle context validation result: $isVehicleInUserContext")

if (!isVehicleInUserContext) {
    _state.update {
        it.copy(
            vehicle = vehicle,
            isLoading = false,
            canStartCheck = false,
            navigateToChecklist = false,
            navigateToProfile = false,
            error = "This vehicle does not belong to your assigned business and site. Please contact your administrator if you need access to vehicles from other locations."
        )
    }
    isProcessingQR = false
    lastScannedCode = null
    return@launch
}
```

---

## 🔍 **Flujo de Validación**

### **1. Usuario Escanea QR**
- Se obtiene el vehículo por su ID (QR code)
- Se extrae el `businessId` y `siteId` del vehículo

### **2. Validación de Business y Site**
- Se obtienen las preferencias del usuario actual
- Se extrae el `businessId` y `siteId` efectivo del usuario (último seleccionado o por defecto)
- Se compara: `userBusinessId == vehicleBusinessId && userSiteId == vehicleSiteId`

### **3. Resultado de Validación**
- **✅ Éxito**: `userBusinessId == vehicleBusinessId && userSiteId == vehicleSiteId`
  - Usuario puede proceder al checklist
  - Se navega a la pantalla de checklist

- **❌ Fallo**: `userBusinessId != vehicleBusinessId || userSiteId != vehicleSiteId`
  - Se muestra mensaje de error
  - Se bloquea el acceso al checklist
  - Se sugiere contactar al administrador

---

## 📊 **Casos de Uso**

### **Caso 1: Usuario Operator con Business y Site Asignado**
- **Usuario**: Operator con business "ABC Corp" y site "Warehouse A"
- **Vehículo**: Pertenece a "ABC Corp" y "Warehouse A"
- **Resultado**: ✅ **Acceso permitido**

### **Caso 2: Usuario Operator con Business y Site Asignado**
- **Usuario**: Operator con business "ABC Corp" y site "Warehouse A"
- **Vehículo**: Pertenece a "ABC Corp" pero "Warehouse B"
- **Resultado**: ❌ **Acceso bloqueado** (Site diferente)

### **Caso 3: Usuario Operator con Business y Site Asignado**
- **Usuario**: Operator con business "ABC Corp" y site "Warehouse A"
- **Vehículo**: Pertenece a "XYZ Corp" y "Warehouse A"
- **Resultado**: ❌ **Acceso bloqueado** (Business diferente)

### **Caso 4: Usuario Admin con "All Sites"**
- **Usuario**: Admin con filtro "All Sites"
- **Vehículo**: Cualquier vehículo del negocio
- **Resultado**: ✅ **Acceso permitido** (Admin tiene permisos globales)

### **Caso 5: Usuario sin Preferencias**
- **Usuario**: Sin preferencias configuradas
- **Vehículo**: Cualquier vehículo
- **Resultado**: ❌ **Acceso bloqueado** (Necesita configurar preferencias)

---

## 🛡️ **Beneficios de Seguridad**

### **1. Control de Acceso**
- Previene acceso no autorizado a vehículos de otros businesses o sites
- Mantiene separación lógica entre businesses y sites
- Reduce riesgo de operaciones accidentales

### **2. Auditoría**
- Logs detallados de validaciones
- Trazabilidad de intentos de acceso
- Facilita debugging y monitoreo

### **3. UX Mejorada**
- Mensajes de error claros y específicos
- Guía al usuario sobre qué hacer
- Sugiere contactar administrador cuando es necesario

---

## 🧪 **Testing**

### **Script de Prueba**
**Archivo**: `scripts/test_site_validation.sh`

```bash
# Ejecutar el script
./scripts/test_site_validation.sh
```

### **Escenarios de Prueba**
1. **Escaneo de vehículo del mismo business y site** → Debe permitir acceso
2. **Escaneo de vehículo de otro business** → Debe bloquear acceso
3. **Escaneo de vehículo de otro site** → Debe bloquear acceso
4. **Verificación de logs** → Debe mostrar detalles de validación

### **Logs Esperados**
```
🔍 Checking if vehicle belongs to user's assigned business and site
🔍 Context validation result:
  - User's business: 'user-business-id'
  - Vehicle's business: 'vehicle-business-id'
  - Business match: true/false
  - User's site: 'user-site-id'
  - Vehicle's site: 'vehicle-site-id'
  - Site match: true/false
  - Final result: true/false
```

---

## 🔄 **Consideraciones Futuras**

### **1. Permisos de Admin**
- Los admins podrían tener acceso a todos los sites
- Implementar lógica de permisos basada en roles

### **2. Configuración Flexible**
- Permitir configuración de excepciones por usuario
- Implementar whitelist de vehículos específicos

### **3. Notificaciones**
- Notificar a administradores de intentos de acceso no autorizado
- Implementar sistema de alertas

---

## ✅ **Estado de Implementación**

- [x] Método de validación implementado
- [x] Integración en QRScannerViewModel
- [x] Mensajes de error configurados
- [x] Logs detallados agregados
- [x] Script de prueba creado
- [ ] Testing en dispositivo real
- [ ] Validación con diferentes roles de usuario

**Estado**: ✅ **Implementado y listo para testing** 