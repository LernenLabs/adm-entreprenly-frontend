package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.components

/**
 * Codes the inventory scanner reads: QR codes plus the barcodes printed on packages.
 * The names match com.google.zxing.BarcodeFormat.
 */
val InventoryScanFormats: List<String> = listOf("QR_CODE", "EAN_13", "EAN_8", "UPC_A", "UPC_E", "CODE_128")
