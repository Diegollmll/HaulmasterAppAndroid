package app.forku.data.api.auth

import android.util.Base64
import android.util.Log
import app.forku.core.auth.UserRoleManager
import app.forku.domain.model.user.UserRole
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class TokenParser {
    companion object {
        fun parseJwtToken(token: String): JwtTokenClaims {
            val parts = token.split(".")
            if (parts.size != 3) {
                throw IllegalArgumentException("Invalid JWT token format")
            }
            
            val payload = parts[1]
            val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
            val decodedString = String(decodedBytes, StandardCharsets.UTF_8)
            val jsonObject = JSONObject(decodedString)

            Log.d("appflow", "TokenParser parseJwtToken:  ${jsonObject}")
            
            val roleString = jsonObject.optString("role", "")
            Log.d("appflow", "TokenParser parseJwtToken role:  $roleString")
            
            // Handle multiple roles separated by commas (e.g., "Administrator,User")
            val effectiveRole = if (roleString.contains(",")) {
                val roles = roleString.split(",").map { it.trim() }
                Log.d("appflow", "TokenParser multiple roles found: $roles")
                
                // Use UserRoleManager to determine highest priority role
                UserRoleManager.getHighestPriorityRole(roles)
            } else {
                UserRoleManager.fromString(roleString)
            }
            
            Log.d("appflow", "TokenParser effective role: $effectiveRole")

            return JwtTokenClaims(
                userId = jsonObject.optString("UserId", ""),
                username = jsonObject.optString("unique_name", ""),
                email = jsonObject.optString("email", "").takeIf { it.isNotBlank() }, // ✅ NEW: Extract email from token
                familyName = jsonObject.optString("family_name", ""),
                is2FAEnabled = jsonObject.optString("Is2FAEnabled", "false").toBoolean(),
                role = effectiveRole,
                expiration = jsonObject.optLong("exp", 0)
            )
        }
    }
}

data class JwtTokenClaims(
    val userId: String,
    val username: String,
    val email: String?,
    val familyName: String,
    val is2FAEnabled: Boolean,
    val role: UserRole,
    val expiration: Long
) 