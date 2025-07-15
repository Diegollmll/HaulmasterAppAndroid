# SessionKeepAlive Diagnostics Guide

## 🔍 **Problema Reportado**
- No se ven logs de `SessionKeepAlive` en los logs
- No hay evidencia de que `keepalive` o `renewtoken` se estén ejecutando

## 📊 **Logging Implementado**

### **Logs de Inicio (MainActivity)**
```
🚀 User authenticated, starting session keep-alive
🛑 User not authenticated, stopping session keep-alive
```

### **Logs de SessionKeepAliveManager**
```
🚀 Starting session keep-alive service
✅ Keep-alive status set to active
🔄 Keep-alive coroutine started
⏰ Initial delay completed, starting keep-alive loop
🔄 Keep-alive loop iteration starting
🔄 Performing keep-alive ping...
✅ Keep-alive successful
⏳ Waiting 30000ms for next keep-alive
```

### **Logs de Status Diagnóstico**
```
📊 === SESSION KEEP-ALIVE STATUS ===
🔄 Keep-alive active: true/false
🔄 Keep-alive job active: true/false
🔄 Token renewal job active: true/false
🔄 Session scope active: true/false
⏰ Last keep-alive: Xs ago / Never
⏰ Last token renewal: Xs ago / Never
📈 Token renewal successes: X
📉 Token renewal failures: X
📊 === END STATUS ===
```

## 🔧 **Pasos de Diagnóstico**

### **1. Verificar Inicio del Servicio**
Busca en los logs:
```bash
# Buscar logs de MainActivity
grep "User authenticated" logs
grep "User not authenticated" logs

# Buscar logs de inicio del servicio
grep "Starting session keep-alive service" logs
grep "Keep-alive status set to active" logs
```

### **2. Verificar Estado de Corrutinas**
Busca:
```bash
# Verificar que las corrutinas se inicien
grep "Keep-alive coroutine started" logs
grep "Token renewal coroutine started" logs

# Verificar el estado de diagnóstico
grep "SESSION KEEP-ALIVE STATUS" logs
```

### **3. Verificar Autenticación**
El SessionKeepAlive solo se inicia cuando `isAuthenticated = true`. Verifica:
```bash
# Buscar logs de autenticación
grep "authenticated" logs
grep "token" logs | grep -i success
```

## 🚨 **Posibles Problemas**

### **1. Usuario No Autenticado**
- **Síntoma**: Solo ves logs de "User not authenticated"
- **Causa**: El usuario no está logueado correctamente
- **Solución**: Verificar que el login sea exitoso

### **2. Corrutinas No Se Inician**
- **Síntoma**: Ves "Starting session keep-alive" pero no "Keep-alive coroutine started"
- **Causa**: Problema con el CoroutineScope
- **Solución**: Verificar que sessionScope esté activo

### **3. Delay Inicial Muy Largo**
- **Síntoma**: Ves "Keep-alive coroutine started" pero no "Initial delay completed"
- **Causa**: Esperando 5 segundos de delay inicial
- **Solución**: Esperar o reducir INITIAL_DELAY_MS

### **4. Jobs Cancelados**
- **Síntoma**: Logs de inicio pero luego "Keep-alive loop ended"
- **Causa**: Jobs siendo cancelados prematuramente
- **Solución**: Verificar que no se llame stopKeepAlive() inesperadamente

## 📋 **Checklist de Verificación**

### **Al Iniciar la App**
- [ ] ¿Ves "User authenticated, starting session keep-alive"?
- [ ] ¿Ves "🚀 Starting session keep-alive service"?
- [ ] ¿Ves "✅ Keep-alive status set to active"?
- [ ] ¿Ves "🔄 Keep-alive coroutine started"?
- [ ] ¿Ves el status de diagnóstico después de 2 segundos?

### **Durante la Ejecución**
- [ ] ¿Ves "⏰ Initial delay completed" después de 5 segundos?
- [ ] ¿Ves "🔄 Keep-alive loop iteration starting"?
- [ ] ¿Ves "🔄 Performing keep-alive ping..."?
- [ ] ¿Ves "✅ Keep-alive successful" o errores?

### **Token Renewal (después de 2 minutos)**
- [ ] ¿Ves "🔄 Token renewal iteration starting"?
- [ ] ¿Ves "🔍 Token renewal headers (fresh CSRF)"?
- [ ] ¿Ves "✅ Token renewal successful" o errores?

## 🔧 **Comandos de Debug**

### **Filtrar Solo SessionKeepAlive**
```bash
# Ver solo logs de SessionKeepAlive
grep "SessionKeepAlive" logs
grep "MainActivity.*keep-alive" logs
```

### **Verificar Timing**
```bash
# Ver timestamps de keep-alive
grep -E "(keep-alive|Keep-alive)" logs | grep -E "\d{2}:\d{2}:\d{2}"
```

### **Verificar Errores**
```bash
# Buscar errores relacionados
grep -E "(Error|Exception|Failed)" logs | grep -i "keep"
grep -E "(Error|Exception|Failed)" logs | grep -i "renewal"
```

## 💡 **Soluciones Rápidas**

### **Si No Hay Logs de Inicio**
1. Verificar que el usuario esté autenticado
2. Verificar que MainActivity llame a startKeepAlive()
3. Verificar que no haya errores de dependencias

### **Si Se Inicia Pero Se Detiene**
1. Buscar logs de "Stopping session keep-alive"
2. Verificar que no se llame stopKeepAlive() inesperadamente
3. Verificar que las corrutinas no se cancelen

### **Si Hay Delays Largos**
1. Verificar INITIAL_DELAY_MS (5 segundos)
2. Verificar KEEP_ALIVE_INTERVAL_MS (30 segundos)
3. Verificar TOKEN_RENEWAL_INTERVAL_MS (2 minutos)

## 🎯 **Próximos Pasos**

1. **Ejecutar la app** con los nuevos logs
2. **Filtrar logs** usando los comandos de arriba
3. **Reportar hallazgos** específicos encontrados
4. **Ajustar configuración** según los resultados

Los logs detallados deberían revelar exactamente dónde está el problema en el flujo de SessionKeepAlive. 