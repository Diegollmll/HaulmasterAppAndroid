package app.forku.domain.model.vehicle

data class VehicleType(
    val Id: String,
    val Name: String,
    val RequiresCertification: Boolean,
    val VehicleCategoryId: String,
    val IsMarkedForDeletion: Boolean,
    val InternalObjectId: Int
) {
    companion object {
        /**
         * Creates a placeholder VehicleType when the full data isn't available
         */
        fun createPlaceholder(
            Id: String,
            Name: String = "Unknown",
            RequiresCertification: Boolean = false,
            VehicleCategoryId: String = "",
            IsMarkedForDeletion: Boolean = false,
            InternalObjectId: Int = 0
        ): VehicleType = VehicleType(
            Id = Id,
            Name = Name,
            RequiresCertification = RequiresCertification,
            VehicleCategoryId = VehicleCategoryId,
            IsMarkedForDeletion = IsMarkedForDeletion,
            InternalObjectId = InternalObjectId
        )
    }
}
