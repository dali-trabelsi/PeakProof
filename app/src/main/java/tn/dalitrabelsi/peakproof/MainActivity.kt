package tn.dalitrabelsi.peakproof

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.provider.Settings
import android.widget.Button
import tn.dalitrabelsi.peakproof.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startServiceButton = findViewById<Button>(R.id.startServiceButton)
        val stopServiceButton = findViewById<Button>(R.id.stopServiceButton)

        // Start the PeekProofService to show the floating rectangles
        startServiceButton.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivity(intent)
            } else {
                startService(Intent(this, PeekProofService::class.java))
            }
        }

        // Stop the PeekProofService and remove the floating rectangles
        stopServiceButton.setOnClickListener {
            stopService(Intent(this, PeekProofService::class.java))
        }
    }
}
