package app.forku.data.api.auth

import android.util.Base64
import android.util.Log
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
            Log.d("appflow", "TokenParser parseJwtToken role:  ${jsonObject.optString("role", "")}")

            return JwtTokenClaims(
                userId = jsonObject.optString("UserId", ""),
                username = jsonObject.optString("unique_name", ""),
                familyName = jsonObject.optString("family_name", ""),
                is2FAEnabled = jsonObject.optString("Is2FAEnabled", "false").toBoolean(),
                role = parseUserRole(jsonObject.optString("role", "")),
                expiration = jsonObject.optLong("exp", 0)
            )
        }
        
        private fun parseUserRole(roleString: String): UserRole {
            Log.d("appflow", "TokenParser parseUserRole roleString:  ${roleString}")
            return when (roleString.lowercase()) {
                "administrator" -> UserRole.SYSTEM_OWNER
                "admin" -> UserRole.ADMIN
                "operator" -> UserRole.OPERATOR
                "superadmin" -> UserRole.SUPERADMIN
                "systemowner", "system_owner" -> UserRole.SYSTEM_OWNER
                else -> UserRole.OPERATOR // Default role
            }
        }
    }
}

data class JwtTokenClaims(
    val userId: String,
    val username: String,
    val familyName: String,
    val is2FAEnabled: Boolean,
    val role: UserRole,
    val expiration: Long
) 