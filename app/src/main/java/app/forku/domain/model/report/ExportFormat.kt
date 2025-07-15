package app.forku.domain.model.report

/**
 * Available export formats for reports
 */
enum class ExportFormat(
    val displayName: String,
    val fileExtension: String,
    val mimeType: String
) {
    CSV(
        displayName = "CSV (Excel)",
        fileExtension = "csv",
        mimeType = "text/csv"
    ),
    JSON(
        displayName = "JSON (Data)",
        fileExtension = "json", 
        mimeType = "application/json"
    )
} 