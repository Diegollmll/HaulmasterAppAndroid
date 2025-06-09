
# NEXT SPRINT PLANNING - API OPTIMIZATION & MULTITENANCY

## Sprint Goal: Optimize API Architecture & Implement Multitenancy

### TASK 1: API REFACTORING (MockAPI → GO Platform Pattern)
**Goal**: Replace multiple API calls with single optimized calls (like VehicleListScreen pattern)

| Task ID | Task Description | Current Pattern Issue | New Pattern Goal | Priority | Estimation (hours) | Status |
|---------|------------------|----------------------|------------------|----------|-------------------|---------|
| API-001 | IncidentListScreen API Optimization | Multiple calls: getIncidents() + getIncidentsByUserId() + individual incident details | Single call with all incident data + related entities | High | 4 | Not Started |
| API-002 | AdminDashboard API Optimization | Multiple calls: sessions + incidents + checks + operators + vehicles | Single dashboard API call with all related data | High | 3 | Not Started |
| API-003 | Dashboard (Operator) API Optimization | Multiple calls for user data + sessions + incidents + checks | Single operator dashboard API call | High | 5 | Not Started |
| API-004 | AllChecklistScreen API Optimization | Multiple calls: getAllChecks() + individual check details + user data | Single call with checklist data + related entities | High | 4 | Not Started |
| API-005 | VehicleProfile API Optimization | Multiple calls: vehicle data + sessions + checks + incidents + user data | Single vehicle profile API with all related data | High | 2 | Not Started |
| API-006 | OperatorsList API Optimization | Multiple calls: users + sessions + certifications + performance data | Single operators API with all related data | Medium | 2 | Not Started |
| API-007 | Identify & Refactor Other Screens | Audit remaining screens for old MockAPI pattern | Apply optimized pattern consistently | Medium | 8 | Not Started |

**API Pattern Comparison:**
- **OLD**: 1 + N*5 API calls (1 main + 5 calls per item)
- **NEW**: 1 + unique_entities calls (like VehicleListScreen: 1 vehicle call + N unique user calls)

## SUBTAREAS DETALLADAS - REFACTOR DE APIs

### API-001: IncidentListScreen (4h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| API-001a | Crear endpoint `GET /api/incidents/with-related-data` con filtros por user/business | GO Platform | 1.5h | Not Started |
| API-001b | Optimizar queries para traer incidents + users + vehicles en una sola llamada | GO Platform | 1h | Not Started |
| API-001c | Actualizar IncidentRepository para usar nuevo endpoint optimizado | App | 1h | Not Started |
| API-001d | Refactorizar IncidentListViewModel para manejar datos optimizados | App | 0.5h | Not Started |

### API-002: AdminDashboard (3h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| API-002a | Crear endpoint `GET /api/admin/dashboard-data` con todos los datos del dashboard | GO Platform | 1.5h | Not Started |
| API-002b | Agregar queries para sessions + incidents + checks + operators en una llamada | GO Platform | 0.5h | Not Started |
| API-002c | Actualizar AdminDashboardViewModel para usar endpoint único | App | 1h | Not Started |

### API-003: Dashboard Operador (5h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| API-003a | Crear endpoint `GET /api/operator/dashboard-data` con datos del operador | GO Platform | 2h | Not Started |
| API-003b | Incluir user data + sessions + incidents + checks para el operador actual | GO Platform | 1h | Not Started |
| API-003c | Refactorizar DashboardViewModel para operador | App | 1.5h | Not Started |
| API-003d | Actualizar UI para manejar datos optimizados | App | 0.5h | Not Started |

### API-004: AllChecklistScreen (4h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| API-004a | Crear endpoint `GET /api/checklists/with-related-data` | GO Platform | 1.5h | Not Started |
| API-004b | Optimizar queries para incluir checklist + vehicle + user + answers | GO Platform | 1h | Not Started |
| API-004c | Actualizar ChecklistRepository para nuevo patrón | App | 1h | Not Started |
| API-004d | Refactorizar ChecklistListViewModel | App | 0.5h | Not Started |

### API-005: VehicleProfile (2h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| API-005a | Crear endpoint `GET /api/vehicles/{id}/profile-data` con todos los datos del vehículo | GO Platform | 1h | Not Started |
| API-005b | Actualizar VehicleProfileViewModel para usar endpoint único | App | 1h | Not Started |

### API-006: OperatorsList (2h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| API-006a | Crear endpoint `GET /api/operators/with-related-data` | GO Platform | 1h | Not Started |
| API-006b | Actualizar OperatorsViewModel para patrón optimizado | App | 1h | Not Started |

### API-007: Auditoría y Otras Pantallas (8h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| API-007a | Auditar todas las pantallas para identificar patrón MockAPI | App | 2h | Not Started |
| API-007b | Crear endpoints adicionales según necesidades encontradas | GO Platform | 3h | Not Started |
| API-007c | Refactorizar pantallas restantes (estimado 3-4 pantallas) | App | 3h | Not Started |

## RESUMEN POR PLATAFORMA

| Platform | Total Horas | Pantallas Afectadas |
|----------|-------------|---------------------|
| **GO Platform (Backend)** | 12h | Nuevos endpoints optimizados |
| **App (Frontend)** | 16h | Refactor de ViewModels y Repositories |
| **TOTAL** | **28h** | 6 pantallas principales + auditoría |

**Dependencias críticas:**
- Las subtareas GO Platform deben completarse antes que las subtareas App correspondientes
- API-007a (auditoría) debe hacerse primero para identificar trabajo adicional
- Patrón VehicleListScreen debe usarse como referencia para todas las implementaciones

## SUBTAREAS DETALLADAS - MULTITENANCY

### MT-001: Business Context Management (16h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| MT-001a | Agregar businessId a todos los modelos de datos (DTOs) | GO Platform | 3h | Not Started |
| MT-001b | Modificar todos los endpoints para incluir filtro por businessId | GO Platform | 4h | Not Started |
| MT-001c | Actualizar queries de base de datos para manejar multitenancy | GO Platform | 3h | Not Started |
| MT-001d | Actualizar todos los Repository para pasar businessId en llamadas API | App | 3h | Not Started |
| MT-001e | Modificar ViewModels para manejar contexto de negocio | App | 2h | Not Started |
| MT-001f | Actualizar SharedPreferences/DataStore para guardar businessId actual | App | 1h | Not Started |

### MT-002: Site Context Management (12h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| MT-002a | Agregar siteId a entidades relevantes (vehicles, operators, incidents) | GO Platform | 2h | Not Started |
| MT-002b | Crear endpoints para manejo de sitios por negocio | GO Platform | 2h | Not Started |
| MT-002c | Actualizar filtros de datos para incluir siteId donde corresponda | GO Platform | 2h | Not Started |
| MT-002d | Actualizar modelos de dominio con siteId | App | 2h | Not Started |
| MT-002e | Implementar selector de sitio en UI | App | 3h | Not Started |
| MT-002f | Actualizar filtros locales por sitio | App | 1h | Not Started |

### MT-003: User Session Context (10h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| MT-003a | Modificar autenticación para incluir business/site selection | GO Platform | 3h | Not Started |
| MT-003b | Crear endpoint para obtener businesses/sites disponibles por usuario | GO Platform | 2h | Not Started |
| MT-003c | Actualizar AuthRepository para manejar contexto multitenant | App | 2h | Not Started |
| MT-003d | Crear pantalla de selección de negocio/sitio | App | 2h | Not Started |
| MT-003e | Actualizar flujo de login con selección de contexto | App | 1h | Not Started |

### MT-004: Data Filtering by Context (14h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| MT-004a | Implementar middleware de filtrado automático por businessId/siteId | GO Platform | 4h | Not Started |
| MT-004b | Actualizar todos los controllers para usar filtrado automático | GO Platform | 3h | Not Started |
| MT-004c | Validar que todos los endpoints respeten el contexto del usuario | GO Platform | 2h | Not Started |
| MT-004d | Actualizar todas las pantallas para filtrar por contexto actual | App | 3h | Not Started |
| MT-004e | Implementar validación de contexto en operaciones CRUD | App | 2h | Not Started |

### MT-005: Admin Multi-Business Views (12h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| MT-005a | Crear endpoints para admins que manejen múltiples negocios | GO Platform | 3h | Not Started |
| MT-005b | Implementar permisos y roles por negocio/sitio | GO Platform | 3h | Not Started |
| MT-005c | Crear switcher de negocio/sitio para admins | App | 3h | Not Started |
| MT-005d | Actualizar dashboards de admin para multi-negocio | App | 3h | Not Started |

### MT-006: Database Schema Updates (8h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| MT-006a | Migrar esquemas de base de datos para incluir businessId/siteId | GO Platform | 4h | Not Started |
| MT-006b | Actualizar índices de base de datos para performance multitenant | GO Platform | 2h | Not Started |
| MT-006c | Actualizar Room database local con nuevos campos | App | 1h | Not Started |
| MT-006d | Migrar datos existentes para incluir contexto multitenant | App | 1h | Not Started |

## SUBTAREAS DETALLADAS - NUEVAS FUNCIONALIDADES

### US-2.3: Media Capture Enhancement (16h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| US-2.3a | Agregar soporte para video y audio en endpoints de multimedia | GO Platform | 4h | Not Started |
| US-2.3b | Crear endpoints para upload/download de videos y audios | GO Platform | 3h | Not Started |
| US-2.3c | Implementar grabación de video en app | App | 4h | Not Started |
| US-2.3d | Implementar grabación de audio en app | App | 3h | Not Started |
| US-2.3e | Actualizar UI de captura de evidencias con video/audio | App | 2h | Not Started |

### US-17: Reports PDF/CSV (64h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| US-17a | Crear servicio de agregación de datos para reportes | GO Platform | 8h | Not Started |
| US-17b | Implementar generación de reportes en PDF (backend) | GO Platform | 12h | Not Started |
| US-17c | Implementar exportación a CSV (backend) | GO Platform | 6h | Not Started |
| US-17d | Crear endpoints para diferentes tipos de reportes | GO Platform | 8h | Not Started |
| US-17e | Diseñar plantillas de reportes (Vehicle, Operator, Safety) | App | 6h | Not Started |
| US-17f | Implementar UI para selección y configuración de reportes | App | 8h | Not Started |
| US-17g | Implementar descarga y compartir reportes | App | 4h | Not Started |
| US-17h | Crear sistema de reportes automáticos/programados | GO Platform | 6h | Not Started |
| US-17i | Integrar reportes con dashboard de admin | App | 6h | Not Started |

### US-2.5: Custom Questions (20h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| US-2.5a | Crear modelo de datos para preguntas personalizadas | GO Platform | 3h | Not Started |
| US-2.5b | Implementar CRUD de preguntas personalizadas (backend) | GO Platform | 5h | Not Started |
| US-2.5c | Crear endpoints para gestión de preguntas por negocio | GO Platform | 3h | Not Started |
| US-2.5d | Implementar UI para crear/editar preguntas personalizadas | App | 6h | Not Started |
| US-2.5e | Integrar preguntas personalizadas en checklists existentes | App | 3h | Not Started |

### US-2.6: Multi-Asset System (24h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| US-2.6a | Crear modelo de datos para diferentes tipos de activos | GO Platform | 4h | Not Started |
| US-2.6b | Implementar sistema de categorías de activos (cranes, lifts, trucks) | GO Platform | 4h | Not Started |
| US-2.6c | Adaptar checklists para diferentes tipos de activos | GO Platform | 4h | Not Started |
| US-2.6d | Crear UI para selección de tipo de activo | App | 4h | Not Started |
| US-2.6e | Adaptar formularios de checklist por tipo de activo | App | 4h | Not Started |
| US-2.6f | Actualizar reportes para incluir múltiples tipos de activos | App | 4h | Not Started |

### US-2.7: Hour Meter/Odometer (16h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| US-2.7a | Agregar campos de horómetro/odómetro a modelo de activos | GO Platform | 2h | Not Started |
| US-2.7b | Crear endpoints para tracking de horas de uso | GO Platform | 3h | Not Started |
| US-2.7c | Implementar lógica de cálculo automático de horas | GO Platform | 3h | Not Started |
| US-2.7d | Agregar captura de horómetro en check-in/check-out | App | 3h | Not Started |
| US-2.7e | Crear UI para mostrar estadísticas de uso de activos | App | 3h | Not Started |
| US-2.7f | Integrar alertas de mantenimiento basadas en horas | App | 2h | Not Started |

### US-24: Operator Certification Control (18h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| US-24a | Crear modelo de datos para certificaciones de operadores | GO Platform | 3h | Not Started |
| US-24b | Implementar CRUD de certificaciones (backend) | GO Platform | 4h | Not Started |
| US-24c | Crear validación de certificaciones en check-in | GO Platform | 3h | Not Started |
| US-24d | Implementar UI para gestión de certificaciones | App | 4h | Not Started |
| US-24e | Agregar validación de certificaciones en flujo de check-in | App | 2h | Not Started |
| US-24f | Crear notificaciones para certificaciones vencidas | App | 2h | Not Started |

## RESUMEN TOTAL POR PLATAFORMA

| Platform | Refactor APIs | Multitenancy | Nuevas Funcionalidades | Total |
|----------|--------------|--------------|----------------------|--------|
| **GO Platform** | 12h | 36h | 84h | **132h** |
| **App** | 16h | 36h | 74h | **126h** |
| **TOTAL** | **28h** | **72h** | **158h** | **258h** |

**Duración estimada total**: ~6.5 semanas (40h/semana)

### TASK 2: MULTITENANCY IMPLEMENTATION
**Goal**: Handle Multibusiness and Multisite for all entities, flows, and app structure

| Task ID | Task Description | Scope | Priority | Estimation (hours) | Status |
|---------|------------------|-------|----------|-------------------|---------|
| MT-001 | Business Context Management | Add businessId to all API calls and data models | High | 16 | Not Started |
| MT-002 | Site Context Management | Add siteId to relevant entities (vehicles, operators, incidents) | High | 12 | Not Started |
| MT-003 | User Session Context | Update authentication to handle business/site selection | High | 10 | Not Started |
| MT-004 | Data Filtering by Context | Ensure all screens filter data by current business/site context | High | 14 | Not Started |
| MT-005 | Admin Multi-Business Views | Allow admins to switch between businesses/sites | Medium | 12 | Not Started |
| MT-006 | Database Schema Updates | Update local storage and caching for multitenancy | Medium | 8 | Not Started |

### TASK 3: REPORTS IMPLEMENTATION (US-17)
**Goal**: Admin reports in PDF/CSV format with easy-to-understand data

| Task ID | Task Description | Report Type | Priority | Estimation (hours) | Status |
|---------|------------------|-------------|----------|-------------------|---------|
| RPT-001 | Report Data Aggregation Service | Backend service to aggregate data for reports | High | 12 | Not Started |
| RPT-002 | Vehicle Activity Reports | Usage, sessions, checks, incidents per vehicle | High | 10 | Not Started |
| RPT-003 | Operator Performance Reports | Sessions, incidents, certifications per operator | High | 10 | Not Started |
| RPT-004 | Safety & Compliance Reports | Incidents, safety alerts, compliance status | High | 12 | Not Started |
| RPT-005 | PDF Export Functionality | Generate formatted PDF reports | Medium | 8 | Not Started |
| RPT-006 | CSV Export Functionality | Generate CSV data exports | Medium | 6 | Not Started |
| RPT-007 | Report Sharing & Distribution | Email, share, save reports locally | Low | 6 | Not Started |

## SUBTAREAS DETALLADAS - MULTITENANCY

### MT-001: Business Context Management (16h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| MT-001a | Agregar businessId a todos los modelos de datos (DTOs) | GO Platform | 3h | Not Started |
| MT-001b | Modificar todos los endpoints para incluir filtro por businessId | GO Platform | 4h | Not Started |
| MT-001c | Actualizar queries de base de datos para manejar multitenancy | GO Platform | 3h | Not Started |
| MT-001d | Actualizar todos los Repository para pasar businessId en llamadas API | App | 3h | Not Started |
| MT-001e | Modificar ViewModels para manejar contexto de negocio | App | 2h | Not Started |
| MT-001f | Actualizar SharedPreferences/DataStore para guardar businessId actual | App | 1h | Not Started |

### MT-002: Site Context Management (12h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| MT-002a | Agregar siteId a entidades relevantes (vehicles, operators, incidents) | GO Platform | 2h | Not Started |
| MT-002b | Crear endpoints para manejo de sitios por negocio | GO Platform | 2h | Not Started |
| MT-002c | Actualizar filtros de datos para incluir siteId donde corresponda | GO Platform | 2h | Not Started |
| MT-002d | Actualizar modelos de dominio con siteId | App | 2h | Not Started |
| MT-002e | Implementar selector de sitio en UI | App | 3h | Not Started |
| MT-002f | Actualizar filtros locales por sitio | App | 1h | Not Started |

### MT-003: User Session Context (10h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| MT-003a | Modificar autenticación para incluir business/site selection | GO Platform | 3h | Not Started |
| MT-003b | Crear endpoint para obtener businesses/sites disponibles por usuario | GO Platform | 2h | Not Started |
| MT-003c | Actualizar AuthRepository para manejar contexto multitenant | App | 2h | Not Started |
| MT-003d | Crear pantalla de selección de negocio/sitio | App | 2h | Not Started |
| MT-003e | Actualizar flujo de login con selección de contexto | App | 1h | Not Started |

### MT-004: Data Filtering by Context (14h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| MT-004a | Implementar middleware de filtrado automático por businessId/siteId | GO Platform | 4h | Not Started |
| MT-004b | Actualizar todos los controllers para usar filtrado automático | GO Platform | 3h | Not Started |
| MT-004c | Validar que todos los endpoints respeten el contexto del usuario | GO Platform | 2h | Not Started |
| MT-004d | Actualizar todas las pantallas para filtrar por contexto actual | App | 3h | Not Started |
| MT-004e | Implementar validación de contexto en operaciones CRUD | App | 2h | Not Started |

### MT-005: Admin Multi-Business Views (12h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| MT-005a | Crear endpoints para admins que manejen múltiples negocios | GO Platform | 3h | Not Started |
| MT-005b | Implementar permisos y roles por negocio/sitio | GO Platform | 3h | Not Started |
| MT-005c | Crear switcher de negocio/sitio para admins | App | 3h | Not Started |
| MT-005d | Actualizar dashboards de admin para multi-negocio | App | 3h | Not Started |

### MT-006: Database Schema Updates (8h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| MT-006a | Migrar esquemas de base de datos para incluir businessId/siteId | GO Platform | 4h | Not Started |
| MT-006b | Actualizar índices de base de datos para performance multitenant | GO Platform | 2h | Not Started |
| MT-006c | Actualizar Room database local con nuevos campos | App | 1h | Not Started |
| MT-006d | Migrar datos existentes para incluir contexto multitenant | App | 1h | Not Started |

## SUBTAREAS DETALLADAS - NUEVAS FUNCIONALIDADES

### US-2.3: Media Capture Enhancement (16h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| US-2.3a | Agregar soporte para video y audio en endpoints de multimedia | GO Platform | 4h | Not Started |
| US-2.3b | Crear endpoints para upload/download de videos y audios | GO Platform | 3h | Not Started |
| US-2.3c | Implementar grabación de video en app | App | 4h | Not Started |
| US-2.3d | Implementar grabación de audio en app | App | 3h | Not Started |
| US-2.3e | Actualizar UI de captura de evidencias con video/audio | App | 2h | Not Started |

### US-17: Reports PDF/CSV (64h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| US-17a | Crear servicio de agregación de datos para reportes | GO Platform | 8h | Not Started |
| US-17b | Implementar generación de reportes en PDF (backend) | GO Platform | 12h | Not Started |
| US-17c | Implementar exportación a CSV (backend) | GO Platform | 6h | Not Started |
| US-17d | Crear endpoints para diferentes tipos de reportes | GO Platform | 8h | Not Started |
| US-17e | Diseñar plantillas de reportes (Vehicle, Operator, Safety) | App | 6h | Not Started |
| US-17f | Implementar UI para selección y configuración de reportes | App | 8h | Not Started |
| US-17g | Implementar descarga y compartir reportes | App | 4h | Not Started |
| US-17h | Crear sistema de reportes automáticos/programados | GO Platform | 6h | Not Started |
| US-17i | Integrar reportes con dashboard de admin | App | 6h | Not Started |

### US-2.5: Custom Questions (20h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| US-2.5a | Crear modelo de datos para preguntas personalizadas | GO Platform | 3h | Not Started |
| US-2.5b | Implementar CRUD de preguntas personalizadas (backend) | GO Platform | 5h | Not Started |
| US-2.5c | Crear endpoints para gestión de preguntas por negocio | GO Platform | 3h | Not Started |
| US-2.5d | Implementar UI para crear/editar preguntas personalizadas | App | 6h | Not Started |
| US-2.5e | Integrar preguntas personalizadas en checklists existentes | App | 3h | Not Started |

### US-2.6: Multi-Asset System (24h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| US-2.6a | Crear modelo de datos para diferentes tipos de activos | GO Platform | 4h | Not Started |
| US-2.6b | Implementar sistema de categorías de activos (cranes, lifts, trucks) | GO Platform | 4h | Not Started |
| US-2.6c | Adaptar checklists para diferentes tipos de activos | GO Platform | 4h | Not Started |
| US-2.6d | Crear UI para selección de tipo de activo | App | 4h | Not Started |
| US-2.6e | Adaptar formularios de checklist por tipo de activo | App | 4h | Not Started |
| US-2.6f | Actualizar reportes para incluir múltiples tipos de activos | App | 4h | Not Started |

### US-2.7: Hour Meter/Odometer (16h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| US-2.7a | Agregar campos de horómetro/odómetro a modelo de activos | GO Platform | 2h | Not Started |
| US-2.7b | Crear endpoints para tracking de horas de uso | GO Platform | 3h | Not Started |
| US-2.7c | Implementar lógica de cálculo automático de horas | GO Platform | 3h | Not Started |
| US-2.7d | Agregar captura de horómetro en check-in/check-out | App | 3h | Not Started |
| US-2.7e | Crear UI para mostrar estadísticas de uso de activos | App | 3h | Not Started |
| US-2.7f | Integrar alertas de mantenimiento basadas en horas | App | 2h | Not Started |

### US-24: Operator Certification Control (18h total)
| Subtarea | Descripción | Platform | Horas | Status |
|----------|-------------|----------|-------|---------|
| US-24a | Crear modelo de datos para certificaciones de operadores | GO Platform | 3h | Not Started |
| US-24b | Implementar CRUD de certificaciones (backend) | GO Platform | 4h | Not Started |
| US-24c | Crear validación de certificaciones en check-in | GO Platform | 3h | Not Started |
| US-24d | Implementar UI para gestión de certificaciones | App | 4h | Not Started |
| US-24e | Agregar validación de certificaciones en flujo de check-in | App | 2h | Not Started |
| US-24f | Crear notificaciones para certificaciones vencidas | App | 2h | Not Started |

## RESUMEN TOTAL POR PLATAFORMA

| Platform | Refactor APIs | Multitenancy | Nuevas Funcionalidades | Total |
|----------|--------------|--------------|----------------------|--------|
| **GO Platform** | 12h | 36h | 84h | **132h** |
| **App** | 16h | 36h | 74h | **126h** |
| **TOTAL** | **28h** | **72h** | **158h** | **258h** |

**Duración estimada total**: ~6.5 semanas (40h/semana)

**Risk Mitigation**:
- Start with one screen API optimization to establish pattern
- Test multitenancy thoroughly in development environment
- Create report templates early for stakeholder feedback