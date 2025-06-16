#!/bin/bash

# 🔍 MULTITENANCY PATTERN VALIDATOR
# Script para validar que todos los repositories siguen el patrón estándar

echo "🏗️ VALIDATING MULTITENANCY PATTERN..."
echo "====================================="

ERRORS=0
WARNINGS=0

# Colores para output
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_error() {
    echo -e "${RED}❌ ERROR: $1${NC}"
    ((ERRORS++))
}

print_warning() {
    echo -e "${YELLOW}⚠️  WARNING: $1${NC}"
    ((WARNINGS++))
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# 1. Buscar DTOs con BusinessId
echo -e "\n${BLUE}1. CHECKING DTOs WITH BUSINESSID...${NC}"
echo "--------------------------------"

DTO_FILES=$(find app/src/main/java -name "*Dto.kt" -exec grep -l "businessId\|BusinessId" {} \;)
DTO_COUNT=$(echo "$DTO_FILES" | wc -l)

if [ $DTO_COUNT -gt 0 ]; then
    print_info "Found $DTO_COUNT DTOs with BusinessId:"
    echo "$DTO_FILES" | while read file; do
        echo "  - $file"
    done
else
    print_warning "No DTOs with BusinessId found"
fi

# 2. Verificar repositories que manejan DTOs con BusinessId
echo -e "\n${BLUE}2. CHECKING REPOSITORIES...${NC}"
echo "-------------------------"

REPO_FILES=$(find app/src/main/java -name "*Repository*.kt" -path "*/repository/*")

echo "$REPO_FILES" | while read repo_file; do
    repo_name=$(basename "$repo_file")
    
    # Verificar si tiene BusinessContextManager
    has_business_context=$(grep -c "BusinessContextManager" "$repo_file")
    
    # Verificar si maneja entidades con BusinessId
    handles_business_entities=$(grep -c "businessId\|BusinessId" "$repo_file")
    
    if [ $handles_business_entities -gt 0 ]; then
        if [ $has_business_context -gt 0 ]; then
            print_success "$repo_name - Has BusinessContextManager ✓"
        else
            print_error "$repo_name - Handles business entities but NO BusinessContextManager!"
        fi
    fi
done

# 3. Verificar APIs que deberían tener businessId parameter
echo -e "\n${BLUE}3. CHECKING API INTERFACES...${NC}"
echo "----------------------------"

API_FILES=$(find app/src/main/java -name "*Api.kt" -path "*/api/*")

echo "$API_FILES" | while read api_file; do
    api_name=$(basename "$api_file")
    
    # Verificar si tiene métodos save/create
    has_save_method=$(grep -c "@POST\|@PUT" "$api_file")
    
    if [ $has_save_method -gt 0 ]; then
        # Verificar si tiene businessId parameter
        has_business_param=$(grep -c "@Query.*businessId" "$api_file")
        
        if [ $has_business_param -gt 0 ]; then
            print_success "$api_name - Has businessId query parameter ✓"
        else
            print_warning "$api_name - Has save methods but NO businessId parameter"
        fi
    fi
done

# 4. Verificar dependency injection
echo -e "\n${BLUE}4. CHECKING DEPENDENCY INJECTION...${NC}"
echo "--------------------------------"

MODULE_FILES=$(find app/src/main/java -name "*Module.kt" -path "*/di/*")

echo "$MODULE_FILES" | while read module_file; do
    module_name=$(basename "$module_file")
    
    # Verificar providers de repositories que deberían tener BusinessContextManager
    repo_providers=$(grep -A 10 "Repository.*=" "$module_file" | grep -c "businessContextManager\|BusinessContextManager")
    
    if [ $repo_providers -gt 0 ]; then
        print_success "$module_name - Repository providers include BusinessContextManager ✓"
    fi
done

# 5. Buscar logs de debugging obligatorios
echo -e "\n${BLUE}5. CHECKING DEBUG LOGS...${NC}"
echo "----------------------"

REPO_FILES_WITH_SAVE=$(find app/src/main/java -name "*Repository*.kt" -exec grep -l "save\|Save" {} \;)

echo "$REPO_FILES_WITH_SAVE" | while read repo_file; do
    repo_name=$(basename "$repo_file")
    
    # Verificar logs de debugging
    has_debug_logs=$(grep -c "android.util.Log.d.*businessId\|android.util.Log.d.*BusinessId" "$repo_file")
    
    if [ $has_debug_logs -gt 0 ]; then
        print_success "$repo_name - Has business debugging logs ✓"
    else
        handles_business=$(grep -c "businessId\|BusinessId" "$repo_file")
        if [ $handles_business -gt 0 ]; then
            print_warning "$repo_name - Handles business context but NO debugging logs"
        fi
    fi
done

# 6. Summary
echo -e "\n${BLUE}📊 VALIDATION SUMMARY${NC}"
echo "==================="

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    print_success "All multitenancy patterns are correctly implemented! 🎉"
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠️  Validation completed with $WARNINGS warnings${NC}"
    echo "Consider addressing warnings to ensure full compliance"
else
    echo -e "${RED}❌ Validation failed with $ERRORS errors and $WARNINGS warnings${NC}"
    echo "Please fix all errors before committing"
    exit 1
fi

echo -e "\n${BLUE}📋 NEXT STEPS:${NC}"
echo "1. Fix any errors shown above"
echo "2. Review warnings and improve where possible"  
echo "3. Run tests to verify functionality"
echo "4. Update MULTITENANCY_PATTERN.md if needed"

echo -e "\n✅ Validation complete!" 