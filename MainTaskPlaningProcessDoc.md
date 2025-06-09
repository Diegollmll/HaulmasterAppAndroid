# 📋 PLANIFICACIÓN DE TAREAS - FORKU PROJECT

=================================================================================

## 🎯 **PLAN ORIGINAL - 228h (5.7 semanas)**

### **BACKEND TASKS - PLAN ORIGINAL:**

#### ✅ **REFACTOR APIs - Total: 5h**
- API-001b: Optimizar queries incidents + users + vehicles (0.5h) ✅ SI
- API-002b: Queries sessions + incidents + checks + operators (0.5h) ✅ SI  
- API-003b: User data + sessions + incidents + checks operador (0.5h) ✅ SI
- API-004b: Queries checklist + vehicle + user + answers (0.5h) ✅ SI
- API-007b: Adaptaciones adicionales endpoints (1h) ✅ SI

#### ✅ **MULTITENANCY - Total: 28h**
- MT-001a: Agregar businessId a DTOs (4h) ✅ SI
- MT-001c: Formularios y grillas multitenancy (3h) ✅ SI
- MT-002a: Agregar siteId a entidades (2h) ✅ SI
- MT-002b: Entity para sitios por negocio (2h) ✅ SI
- MT-002c: Relacionar sites con filtros (2h) ✅ SI
- MT-003a: Autenticación business/site selection (3h) ✅ SI
- MT-004a: Forms/grids businessId/siteId (4h) ✅ SI
- MT-004b: Endpoints respeten business/site (3h) ✅ SI
- MT-004c: Endpoints respeten contexto usuario (2h) ✅ SI
- MT-005a: Admins múltiples negocios (3h) ✅ SI

#### ✅ **KEEPALIVE & SESSION - Total: 6h**
- KA-001a: Endpoint keepalive renovación (2h) ✅ SI
- KA-001b: Lógica renovación tokens/sesión (2h) ✅ SI
- KA-001c: Validación sesión activa endpoints (2h) ✅ SI

#### ✅ **NUEVAS FUNCIONALIDADES - Total: 74h**
- US-2.3a: Soporte video/audio multimedia (4h) ✅ SI
- US-2.3b: Endpoints upload/download videos/audios (3h) ✅ SI
- US-17a: Servicio agregación reportes (8h) ✅ SI
- US-17b: Generación reportes PDF (12h) ✅ SI
- US-17c: Exportación CSV (6h) ✅ SI
- US-17d: Endpoints tipos reportes (8h) ✅ SI
- US-2.5c: Endpoints preguntas por negocio (3h) ✅ SI
- US-2.6a: Modelo datos tipos activos (4h) ✅ SI
- US-2.6b: Sistema categorías activos (4h) ✅ SI
- US-2.6c: Checklists tipos activos (4h) ✅ SI
- US-2.7a: Campos horómetro/odómetro (2h) ✅ SI
- US-2.7b: Endpoints tracking horas (3h) ✅ SI
- US-2.7c: Cálculo automático horas (3h) ✅ SI
- US-24a: Modelo certificaciones operadores (3h) ✅ SI
- US-24b: CRUD certificaciones (4h) ✅ SI
- US-24c: Validación certificaciones check-in (3h) ✅ SI

**BACKEND TOTAL ORIGINAL: 115h**

### **APP TASKS - PLAN ORIGINAL:**

#### ✅ **REFACTOR APIs - Total: 13h**
- API-001c: IncidentRepository endpoint optimizado (1h) (SI)
- API-001d: IncidentListViewModel datos optimizados (0.5h) (SI)
- API-002c: AdminDashboardViewModel endpoint único (1h) (SI)
- API-003c: DashboardViewModel operador (1.5h) (SI)
- API-003d: UI datos optimizados (0.5h) (SI)
- API-004c: ChecklistRepository nuevo patrón (1h) (SI)
- API-004d: ChecklistListViewModel (0.5h) (SI)
- API-005b: VehicleProfileViewModel endpoint único (1h) (SI)
- API-006b: OperatorsViewModel patrón optimizado (1h) (SI)
- API-007a: Auditar pantallas patrón MockAPI (2h) (SI)
- API-007c: Refactorizar pantallas restantes (3h) (SI)

#### ✅ **MULTITENANCY - Total: 30h**
- MT-001d: Repository pasar businessId (3h) (SI)
- MT-001e: ViewModels contexto negocio (2h) (SI)
- MT-001f: SharedPreferences businessId (1h) (SI)
- MT-002d: Modelos dominio siteId (2h) (SI)
- MT-002e: Selector sitio UI (3h) (SI)
- MT-002f: Filtros locales sitio (1h) (SI)
- MT-003c: AuthRepository contexto multitenant (2h) (SI)
- MT-003d: Pantalla selección negocio/sitio (2h) (SI)
- MT-003e: Flujo login con contexto (1h) (SI)
- MT-004d: Pantallas filtrar contexto (3h) (SI)
- MT-004e: Validación contexto CRUD (2h) (SI)
- MT-005c: Switcher negocio/sitio admins (3h) (SI)
- MT-005d: Dashboards admin multi-negocio (3h) (SI)
- MT-006c: Room database nuevos campos (1h) (SI)
- MT-006d: Migrar datos contexto multitenant (1h) (SI)

#### ✅ **KEEPALIVE & SESSION - Total: 8h**
- KA-002a: Cliente keepalive automático (2h) (SI)
- KA-002b: Keepalive flujos críticos (3h) (SI)
- KA-002c: Reconexión automática (2h) (SI)
- KA-002d: Notificaciones sesión (1h) (SI)

#### ✅ **NUEVAS FUNCIONALIDADES - Total: 61h**
- US-2.3c: Grabación video app (4h) (SI)
- US-2.3d: Grabación audio app (3h) (SI)
- US-2.3e: UI captura evidencias video/audio (2h) (SI)
- US-17e: Plantillas reportes (6h) (SI)
- US-17f: UI selección reportes (8h) (SI)
- US-17g: Descarga compartir reportes (4h) (SI)
- US-17i: Reportes dashboard admin (6h) (SI)
- US-2.5d: UI preguntas personalizadas (6h) (SI)
- US-2.5e: Preguntas checklists (3h) (SI)
- US-2.6d: UI selección tipo activo (4h) (SI)
- US-2.6e: Formularios checklist tipo activo (4h) (SI)
- US-2.6f: Reportes múltiples activos (4h) (SI)
- US-2.7d: Captura horómetro check-in/out (3h) (SI)
- US-2.7e: UI estadísticas uso activos (3h) (SI)
- US-2.7f: Alertas mantenimiento horas (1h) (SI)
- US-24d: UI gestión certificaciones (3h) (SI)
- US-24e: Validación certificaciones check-in (2h) (SI)
- US-24f: Notificaciones certificaciones vencidas (1h) (SI)

**APP TOTAL ORIGINAL: 112h**

### **📊 RESUMEN PLAN ORIGINAL**
- Backend: 115h
- App: 112h  
- **TOTAL: 227h (5.7 semanas)**

=================================================================================

## 🚀 **PLAN OPTIMIZADO - 120h (3 semanas)**

### **BACKEND TASKS - PLAN OPTIMIZADO:**

#### ✅ **REFACTOR APIs - Total: 7h**
- API-001b: Optimizar queries incidents + users + vehicles (1h) ✅ SI
- API-002b: Queries sessions + incidents + checks + operators (1h) ✅ SI
- API-003b: User data + sessions + incidents + checks operador (1h) ✅ SI
- API-004b: Queries checklist + vehicle + user + answers (1h) ✅ SI
- API-007b: Adaptaciones adicionales endpoints (3h) ✅ SI

#### ✅ **MULTITENANCY - Total: 19h** *(Solo businessId)*
- MT-001a: Agregar businessId a DTOs (4h) ✅ SI
- MT-001c: Formularios y grillas multitenancy (3h) ✅ SI
- MT-003a: Autenticación business selection (3h) ✅ SI
- MT-004a: Forms/grids businessId (4h) ✅ SI
- MT-004b: Endpoints respeten business (3h) ✅ SI
- MT-005a: Admins múltiples negocios (2h) ✅ SI

#### ✅ **KEEPALIVE & SESSION - Total: 6h**
- KA-001a: Endpoint keepalive renovación (2h) ✅ SI
- KA-001b: Lógica renovación tokens/sesión (2h) ✅ SI
- KA-001c: Validación sesión activa endpoints (2h) ✅ SI

#### ✅ **NUEVAS FUNCIONALIDADES - Total: 25h** *(Core reducido)*
- US-2.3a: Soporte video multimedia (3h) ✅ SI
- US-17a: Servicio reportes básicos (4h) ✅ SI
- US-17b: 2 reportes básicos PDF (6h) ✅ SI
- US-2.6a: Modelo 2 tipos activos (2h) ✅ SI
- US-2.6b: Categorías básicas (2h) ✅ SI
- US-2.7a: Horómetro básico (2h) ✅ SI
- US-2.7b: Endpoints tracking básico (2h) ✅ SI
- US-24a: Certificaciones básicas (2h) ✅ SI
- US-24b: CRUD básico certificaciones (2h) ✅ SI

**BACKEND TOTAL OPTIMIZADO: 57h**

### **APP TASKS - PLAN OPTIMIZADO:**

#### ✅ **REFACTOR APIs - Total: 8h**
- API-001c: IncidentRepository endpoint optimizado (1h) (SI)
- API-002c: AdminDashboardViewModel endpoint único (1h) (SI)
- API-003c: DashboardViewModel operador (1.5h) (SI)
- API-004c: ChecklistRepository nuevo patrón (1h) (SI)
- API-005b: VehicleProfileViewModel endpoint único (1h) (SI)
- API-007a: Auditar pantallas críticas MockAPI (1.5h) (SI)
- API-007c: Refactorizar pantallas críticas (1h) (SI)

#### ✅ **MULTITENANCY - Total: 13h** *(Solo businessId)*
- MT-001d: Repository críticos businessId (2h) (SI)
- MT-001e: ViewModels críticos contexto (2h) (SI)
- MT-001f: SharedPreferences businessId (1h) (SI)
- MT-003c: AuthRepository contexto multitenant (2h) (SI)
- MT-003d: Pantalla básica selección negocio (2h) (SI)
- MT-004d: Pantallas críticas filtrar contexto (2h) (SI)
- MT-005c: Switcher básico negocio admins (2h) (SI)

#### ✅ **KEEPALIVE & SESSION - Total: 2h** *(Básico)*
- KA-002a: Cliente keepalive básico (2h) (SI)

#### ✅ **NUEVAS FUNCIONALIDADES - Total: 40h** *(Core)*
- US-2.3c: Grabación video básica (4h) (SI)
- US-2.3e: UI captura video (4h) (SI)
- US-17e: 2 plantillas básicas (3h) (SI)
- US-17f: UI básica reportes (5h) (SI)
- US-17g: Descarga básica reportes (4h) (SI)
- US-17i: Reportes básicos dashboard (3h) (SI)
- US-2.5d: UI básica preguntas personalizadas (3h) (SI)
- US-2.5e: Preguntas básicas checklists (2h) (SI)
- US-2.6d: UI básica 2 tipos activo (3h) (SI)
- US-2.6e: Formularios básicos tipo activo (3h) (SI)
- US-2.7d: Captura básica horómetro (2h) (SI)
- US-2.7e: UI básica estadísticas (1h) (SI)
- US-24d: UI básica certificaciones (2h) (SI)
- US-24e: Validación básica certificaciones (1h) (SI)

**APP TOTAL OPTIMIZADO: 63h**

### **📊 RESUMEN PLAN OPTIMIZADO**
- Backend: 57h
- App: 63h
- **TOTAL: 120h (3 semanas)**

### **📅 CRONOGRAMA OPTIMIZADO**
**Semana 1 (40h)**: APIs + Keepalive + Multitenancy base
**Semana 2 (40h)**: Media + Certificaciones + Reportes  
**Semana 3 (40h)**: Assets + Horómetro + Custom Questions

### **🔥 OPTIMIZACIONES APLICADAS**
1. Multitenancy: Solo businessId (siteId fase 2)
2. Reportes: Solo 2 básicos PDF (CSV fase 2)
3. Media: Solo video (audio fase 2)
4. Assets: Solo 2 tipos (expansión fase 2)
5. Keepalive: Básico en app
6. Testing: Integrado en desarrollo

=================================================================================

## ⚠️ **ANÁLISIS CRÍTICO DEL PLAN OPTIMIZADO**

### **🔍 PROBLEMAS IDENTIFICADOS:**

#### **1. REDUCCIONES DEMASIADO AGRESIVAS**
- **Multitenancy App**: 30h → 13h (-57%) 
  - **PROBLEMA**: Eliminar siteId NO reduce tanto las tareas de UI
  - **REALIDAD**: Aún necesitas todas las pantallas, validaciones y contexto
  - **ESTIMACIÓN REAL**: ~22h mínimo

- **Keepalive App**: 8h → 2h (-75%)
  - **PROBLEMA**: Solo "básico" no elimina la complejidad
  - **REALIDAD**: Reconexión y manejo de errores siguen siendo necesarios
  - **ESTIMACIÓN REAL**: ~6h mínimo

#### **2. TAREAS OMITIDAS CRÍTICAS**
- **Testing**: No está contemplado
- **Integración**: No hay tiempo buffer para bugs
- **UI Polish**: Se asume que "básico" = menos tiempo
- **Debugging**: No hay margen para problemas

#### **3. DEPENDENCIAS NO CONSIDERADAS**
- **Multitenancy**: Afecta TODAS las pantallas, no solo las "críticas"
- **API Changes**: Cualquier cambio en backend requiere ajustes en app
- **Data Migration**: Cambios de modelo requieren migración

### **🚨 RIESGOS DEL PLAN OPTIMIZADO**

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|--------------|---------|------------|
| **Scope Creep** | Alta | Alto | Las funciones "básicas" crecen |
| **Bugs Complejos** | Media | Alto | Sin tiempo para debugging |
| **Dependencies** | Alta | Medio | Backend-App sincronización |
| **Quality Issues** | Alta | Alto | Sin tiempo para polish |

### **📊 PLAN REALISTA - 160h (4 semanas)**

#### **BACKEND REALISTA: 75h**
- Refactor APIs: 7h
- Multitenancy: 22h (businessId + validaciones completas)
- Keepalive: 6h  
- Nuevas Funcionalidades: 35h (más buffer)
- Testing & Debug: 5h

#### **APP REALISTA: 85h**
- Refactor APIs: 10h (más tiempo para testing)
- Multitenancy: 22h (UI completa para businessId)
- Keepalive: 6h (implementación completa)
- Nuevas Funcionalidades: 42h (más tiempo para UI)
- Testing & Debug: 5h

### **📅 CRONOGRAMA REALISTA (4 semanas)**

#### **SEMANA 1 (40h)**: Fundaciones
- Backend: APIs (7h) + Keepalive (6h) + Multitenancy (22h) = 35h
- App: APIs (10h) + Keepalive (6h) + Multitenancy start (22h) = 38h

#### **SEMANA 2 (40h)**: Multitenancy & Media
- Backend: Nuevas Func. parte 1 (15h)
- App: Multitenancy finish + Media (25h)

#### **SEMANA 3 (40h)**: Features Core
- Backend: Nuevas Func. parte 2 (20h)
- App: Reportes + Certificaciones (25h)

#### **SEMANA 4 (40h)**: Features Final + Testing
- Backend: Testing & Debug (5h)
- App: Assets + Horómetro + Testing (17h)

**TOTAL REALISTA: 160h (4 semanas)**

=================================================================================

## 📊 **COMPARACIÓN DE LOS 3 PLANES**

| Aspecto | Original | Optimizado | **Realista** |
|---------|----------|------------|-------------|
| **Duración** | 5.7 sem | 3 sem | **4 sem** |
| **Horas** | 227h | 120h | **160h** |
| **Backend** | 115h | 57h | **75h** |
| **App** | 112h | 63h | **85h** |
| **Riesgo** | Bajo | Alto | **Medio** |
| **Calidad** | Alta | Baja | **Media-Alta** |
| **Viabilidad** | 95% | 60% | **85%** |

### **🎯 RECOMENDACIÓN FINAL**

**PLAN REALISTA (4 semanas)** es la mejor opción porque:
- ✅ **Tiempo razonable**: 4 semanas vs 5.7 original (-30%)
- ✅ **Riesgo controlado**: Buffer para debugging y testing
- ✅ **Funcionalidades core**: Mantiene lo esencial sin comprometer calidad
- ✅ **Ejecutable**: 85% de probabilidad de éxito vs 60% del optimizado

=================================================================================

## 📊 **COMPARACIÓN DE PLANES**

| Aspecto | Original | Optimizado | Ahorro |
|---------|----------|------------|--------|
| Duración | 5.7 sem | 3 sem | -47% |
| Horas | 227h | 120h | -47% |
| Backend | 115h | 57h | -50% |
| App | 112h | 63h | -44% |

**RECOMENDACIÓN**: Plan Optimizado para entrega rápida con funcionalidades core, Plan Original para implementación completa.

## 🎯 **PRÓXIMOS PASOS**

1. **Seleccionar plan**: Original (5.7 semanas) vs Optimizado (3 semanas) vs **Realista (4 semanas)**
2. **Evaluar tareas de APP** con criterio SI/NO (como se hizo con Backend)
3. **Definir secuencia de ejecución** por dependencias
4. **Asignar recursos** y comenzar ejecución

**¿Qué plan prefieres ejecutar?** 🚀
