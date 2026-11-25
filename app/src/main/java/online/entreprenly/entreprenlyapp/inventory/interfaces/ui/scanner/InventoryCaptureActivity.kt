package online.entreprenly.entreprenlyapp.inventory.interfaces.ui.scanner

import android.os.Bundle
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView

/**
 * Inventory camera scanner. Same screen as the library's, but it also reads package barcodes
 * held sideways (see [RotatingDecoderFactory]).
 */
class InventoryCaptureActivity : CaptureActivity() {

    private lateinit var scannerView: DecoratedBarcodeView

    override fun initializeContent(): DecoratedBarcodeView =
        super.initializeContent().also { scannerView = it }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The library sets its decoder factory from the scan options in super.onCreate;
        // the camera (and decoding) only starts in onResume, so wrapping it here applies to every frame.
        scannerView.decoderFactory = RotatingDecoderFactory(scannerView.decoderFactory)
    }
}
