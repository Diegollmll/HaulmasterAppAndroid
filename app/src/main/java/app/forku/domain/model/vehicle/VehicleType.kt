package app.forku.domain.model.vehicle

enum class VehicleType(
    val id: String,
    val displayName: String,
    val requiresCertification: Boolean
) {
    COUNTERBALANCE_FORKLIFT("counterbalance_forklift", "Counterbalance Forklift", true),
    REACH_FORKLIFT("reach_forklift", "Reach Forklift", true),
    SIDE_LOADER("side_loader", "Side Loader", true),
    TELESCOPIC_HANDLER("telescopic_handler", "Telescopic Handler", true),
    ORDER_PICKER("order_picker", "Order Picker", true),
    ROUGH_TERRAIN_FORKLIFT("rough_terrain_forklift", "Rough Terrain Forklift", true),
    INDUSTRIAL_FORKLIFT("industrial_forklift", "Industrial Forklift", true),
    ARTICULATED_FORKLIFT("articulated_forklift", "Articulated Forklift", true),
    TURRET_TRUCK("turret_truck", "Turret Truck", true),
    PALLET_JACK("pallet_jack", "Pallet Jack", false),
    OTHER("other", "Other", true),
    ALL("all", "All", false);

    companion object {
        fun fromId(id: String) = values().find { it.id == id } ?: OTHER
        fun fromName(name: String) = try {
            // Intenta encontrar una coincidencia directa primero
            values().find { it.displayName.equals(name, ignoreCase = true) }
                // Si no encuentra coincidencia directa, intenta con el nombre enum
                ?: valueOf(name.uppercase().replace(" ", "_"))
                // Si aún no encuentra, devuelve OTHER
                ?: OTHER
        } catch (e: IllegalArgumentException) {
            android.util.Log.w("VehicleType", "Unknown vehicle type: $name, using OTHER type")
            OTHER
        }
    }
}
