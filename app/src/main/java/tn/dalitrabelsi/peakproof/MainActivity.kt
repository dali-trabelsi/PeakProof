package tn.dalitrabelsi.peakproof

import android.animation.ObjectAnimator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var mainLayout: View
    private lateinit var disabledBackground: ImageView
    private lateinit var enabledBackground: ImageView
    private var isPeekProofEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize background views
        mainLayout = findViewById(R.id.mainLayout)
        disabledBackground = findViewById(R.id.disabledBackground)
        enabledBackground = findViewById(R.id.enabledBackground)

        // Set click listener to toggle PeekProof mode
        mainLayout.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                // Request permission to draw overlays if not granted
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivity(intent)
            } else {
                togglePeekProofMode()  // Toggle Peek Proof mode on click
            }
        }
    }

    // Toggle the PeekProof mode and perform the crossfade
    private fun togglePeekProofMode() {
        isPeekProofEnabled = !isPeekProofEnabled

        if (isPeekProofEnabled) {
            // Start the PeekProofService and crossfade to enabled background
            startService(Intent(this, PeekProofService::class.java))
            crossfadeBackgrounds(enabledBackground, disabledBackground)
        } else {
            // Stop the PeekProofService and crossfade to disabled background
            stopService(Intent(this, PeekProofService::class.java))
            crossfadeBackgrounds(disabledBackground, enabledBackground)
        }
    }

    // Crossfade from one background to another
    private fun crossfadeBackgrounds(fadeInView: View, fadeOutView: View) {
        // Fade in the target background
        fadeInView.visibility = View.VISIBLE
        fadeInView.alpha = 0f
        val fadeIn = ObjectAnimator.ofFloat(fadeInView, "alpha", 0f, 1f)

        // Fade out the current background
        val fadeOut = ObjectAnimator.ofFloat(fadeOutView, "alpha", 1f, 0f)

        fadeIn.duration = 500 // 0.5s duration
        fadeOut.duration = 500 // 0.5s duration

        fadeIn.start()
        fadeOut.start()

        // After fade-out is complete, hide the view
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                fadeOutView.visibility = View.INVISIBLE
            }
        })
    }
}
