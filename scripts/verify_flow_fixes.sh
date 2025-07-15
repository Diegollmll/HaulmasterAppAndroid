#!/bin/bash

echo "🔍 Verificando correcciones de Flow exception transparency..."
echo "=================================================="

# Verificar que no hay más emit(Result.failure(e)) sin safeEmitFailure
echo "📋 Buscando emit(Result.failure(e)) sin safeEmitFailure..."
if grep -r "emit(Result.failure(e))" app/src/main/java/ --include="*.kt" | grep -v "safeEmitFailure"; then
    echo "❌ ERROR: Se encontraron emit(Result.failure(e)) sin safeEmitFailure"
    exit 1
else
    echo "✅ No se encontraron emit(Result.failure(e)) sin safeEmitFailure"
fi

# Verificar que safeEmitFailure está siendo usado
echo ""
echo "📋 Verificando uso de safeEmitFailure..."
safe_emit_files=$(grep -r "safeEmitFailure" app/src/main/java/ --include="*.kt" | wc -l)
echo "✅ safeEmitFailure se usa en $safe_emit_files líneas"

# Verificar que FlowUtils.kt existe
echo ""
echo "📋 Verificando archivo FlowUtils.kt..."
if [ -f "app/src/main/java/app/forku/core/utils/FlowUtils.kt" ]; then
    echo "✅ FlowUtils.kt existe"
else
    echo "❌ ERROR: FlowUtils.kt no existe"
    exit 1
fi

# Verificar imports de safeEmitFailure
echo ""
echo "📋 Verificando imports de safeEmitFailure..."
import_files=$(grep -r "import app.forku.core.utils.safeEmitFailure" app/src/main/java/ --include="*.kt" | wc -l)
echo "✅ safeEmitFailure está importado en $import_files archivos"

# Listar archivos corregidos
echo ""
echo "📋 Archivos corregidos:"
grep -r "safeEmitFailure" app/src/main/java/ --include="*.kt" | cut -d: -f1 | sort | uniq

echo ""
echo "🎉 Verificación completada exitosamente!"
echo "Todos los archivos han sido corregidos para evitar Flow exception transparency violations." 