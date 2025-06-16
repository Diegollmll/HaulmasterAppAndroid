# 🏗️ MULTITENANCY PATTERN - ForkU Project

## 📋 **PATRÓN OBLIGATORIO PARA REPOSITORIES CON BUSINESSID**

### **✅ REGLA FUNDAMENTAL:**
> **TODOS los repositories que manejen entidades con `businessId` DEBEN seguir este patrón exacto.**

---

## 🔧 **1. REPOSITORY CONSTRUCTOR**

```kotlin
@Singleton
class YourEntityRepository @Inject constructor(
    private val api: YourEntityApi,
    private val gson: Gson,
    private val businessContextManager: BusinessContextManager // ✅ OBLIGATORIO
) : IYourEntityRepository {
```

---

## 📤 **2. SAVE/CREATE OPERATIONS**

```kotlin
override suspend fun saveYourEntity(
    entity: YourEntityDto,
    include: String? = null,
    dateformat: String? = null
): Flow<Result<YourEntityDto>> = flow {
    try {
        // ✅ PASO 1: Obtener businessId desde BusinessContextManager
        val businessId = businessContextManager.getCurrentBusinessId()
        
        // ✅ PASO 2: Logs de debugging obligatorios
        android.util.Log.d("YourEntityRepo", "=== YOUR ENTITY REPOSITORY DEBUG ===")
        android.util.Log.d("YourEntityRepo", "Original DTO businessId: '${entity.businessId}'")
        android.util.Log.d("YourEntityRepo", "BusinessId from BusinessContextManager: '$businessId'")
        
        // ✅ PASO 3: Asignar businessId al DTO
        val entityWithBusinessId = entity.copy(businessId = businessId)
        android.util.Log.d("YourEntityRepo", "Updated DTO businessId: '${entityWithBusinessId.businessId}'")
        
        // ✅ PASO 4: Serializar JSON con businessId
        val entityJson = gson.toJson(entityWithBusinessId)
        android.util.Log.d("YourEntityRepo", "JSON enviado a API: $entityJson")
        
        // ✅ PASO 5: Pasar businessId al API call
        android.util.Log.d("YourEntityRepo", "Calling API with businessId: '$businessId'")
        val result = api.save(
            entity = entityJson, 
            include = include, 
            dateformat = dateformat, 
            businessId = businessId // ✅ OBLIGATORIO como query parameter
        )
        
        android.util.Log.d("YourEntityRepo", "API response received successfully")
        android.util.Log.d("YourEntityRepo", "=====================================")
        emit(Result.success(result))
    } catch (e: Exception) {
        android.util.Log.e("YourEntityRepo", "Error saving entity: ${e.message}", e)
        emit(Result.failure(e))
    }
}
```

---

## 🔍 **3. QUERY/FILTER OPERATIONS**

```kotlin
override suspend fun getEntitiesByFilter(filter: String): Flow<Result<List<YourEntityDto>>> = flow {
    try {
        // ✅ Agregar filtro automático de businessId
        val businessId = businessContextManager.getCurrentBusinessId()
        val businessFilter = "BusinessId == Guid.Parse(\"$businessId\")"
        val finalFilter = if (filter.isNotBlank()) {
            "$filter && $businessFilter"
        } else {
            businessFilter
        }
        
        android.util.Log.d("YourEntityRepo", "Query filter with business context: $finalFilter")
        val result = api.getList(filter = finalFilter, include = "GOUser")
        emit(Result.success(result))
    } catch (e: Exception) {
        emit(Result.failure(e))
    }
}
```

---

## 🎯 **4. API INTERFACE REQUIREMENTS**

```kotlin
interface YourEntityApi {
    @FormUrlEncoded
    @POST("api/yourentity")
    suspend fun save(
        @Field("entity") entity: String,
        @Field("include") include: String? = null,
        @Field("dateformat") dateformat: String? = null,
        @Query("businessId") businessId: String? = null // ✅ OBLIGATORIO
    ): YourEntityDto
    
    @GET("api/yourentity/list")
    suspend fun getList(
        @Query("filter") filter: String? = null,
        @Query("include") include: String? = null
    ): List<YourEntityDto>
}
```

---

## 📋 **5. DTO REQUIREMENTS**

```kotlin
data class YourEntityDto(
    // ... otros campos ...
    @SerializedName("BusinessId")
    val businessId: String? = null // ✅ OBLIGATORIO
)
```

---

## 🧪 **6. DEPENDENCY INJECTION**

En el módulo DI correspondiente:

```kotlin
@Provides
@Singleton
fun provideYourEntityRepository(
    api: YourEntityApi,
    gson: Gson,
    businessContextManager: BusinessContextManager // ✅ OBLIGATORIO
): IYourEntityRepository {
    return YourEntityRepositoryImpl(api, gson, businessContextManager)
}
```

---

## 🚨 **CHECKLISTS DE VALIDACIÓN**

### **Antes de hacer commit:**
- [ ] ✅ Repository inyecta `BusinessContextManager`
- [ ] ✅ Save operations asignan `businessId` al DTO
- [ ] ✅ API calls incluyen `businessId` como query parameter
- [ ] ✅ Query operations filtran por `businessId`
- [ ] ✅ Logs de debugging incluidos
- [ ] ✅ DTO tiene campo `businessId`
- [ ] ✅ API interface acepta `businessId` parameter

### **Al revisar PRs:**
- [ ] 🔍 Verificar patrón completo implementado
- [ ] 🔍 Verificar logs de debugging presentes
- [ ] 🔍 Verificar consistency con otros repositories

---

## 🔥 **REPOSITORIES QUE NECESITAN ACTUALIZACIÓN:**

### **Incidentes Específicos:**
- [ ] ❌ `NearMissIncidentRepository`
- [ ] ❌ `HazardIncidentRepository` 
- [ ] ❌ `VehicleFailIncidentRepository`
- [x] ✅ `CollisionIncidentRepository` (COMPLETADO)

### **Auditoria Pendiente:**
- [ ] 🔍 Revisar otros repositories que manejen entidades con businessId
- [ ] 🔍 Verificar consistency en toda la aplicación

---

## 📚 **NOTAS IMPORTANTES:**

1. **Nunca usar patrones inconsistentes**: Si una entidad tiene businessId, DEBE seguir este patrón
2. **Logs obligatorios**: Ayudan con debugging y verificación
3. **Testing**: Verificar que businessId llegue correctamente al backend
4. **Consistency**: Todos los repositories de entidades similares deben funcionar igual

---

*Creado: $(date)  
Propósito: Prevenir inconsistencias en implementación de multitenancy* 