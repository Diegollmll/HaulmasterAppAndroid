package app.forku.domain.model.vehicle

enum class VehicleType(
    val id: String,
    val displayName: String,
    val requiresCertification: Boolean
) {
    FORKLIFT("forklift_type", "Forklift", true),
    PALLET_JACK("pallet_jack_type", "Pallet Jack", false),
    REACH_TRUCK("reach_truck_type", "Reach Truck", true),
    ORDER_PICKER("order_picker_type", "Order Picker", true),
    TOW_TRACTOR("tow_tractor_type", "Tow Tractor", true),
    COUNTERBALANCE("counterbalance_type", "Counterbalance", true),
    ALL("all", "All", false);

    companion object {
        fun fromId(id: String) = values().find { it.id == id } ?: FORKLIFT
        fun fromName(name: String) = try {
            valueOf(name.uppercase())
        } catch (e: IllegalArgumentException) {
            android.util.Log.w("VehicleType", "Unknown vehicle type: $name")
            FORKLIFT
        }
    }
}
