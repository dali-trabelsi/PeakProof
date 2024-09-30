package tn.dalitrabelsi.peakproof

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import tn.dalitrabelsi.peakproof.R

class MainActivity : AppCompatActivity() {

    private lateinit var mainLayout: LinearLayout
    private var isPeekProofEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainLayout = findViewById(R.id.mainLayout)

        // Set the default background to "disabled" on app start
        updateBackground(false, false)

        // Add click listener to the entire layout to toggle PeekProof mode
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

    // Toggle the PeekProof mode and background
    private fun togglePeekProofMode() {
        isPeekProofEnabled = !isPeekProofEnabled

        if (isPeekProofEnabled) {
            // Start the PeekProofService and switch to enabled background
            startService(Intent(this, PeekProofService::class.java))
            updateBackground(true, true)
        } else {
            // Stop the PeekProofService and switch to disabled background
            stopService(Intent(this, PeekProofService::class.java))
            updateBackground(false, true)
        }
    }

    // Function to switch between the two background images with optional fade animation
    private fun updateBackground(isEnabled: Boolean, animate: Boolean) {
        val targetBackground = if (isEnabled) R.drawable.enabled else R.drawable.disabled

        if (animate) {
            // Simultaneously fade out the old background and fade in the new one
            val fadeOut = ObjectAnimator.ofFloat(mainLayout, "alpha", 1f, 0f)
            val fadeIn = ObjectAnimator.ofFloat(mainLayout, "alpha", 0f, 1f)

            fadeOut.duration = 500 // 0.5s duration
            fadeIn.duration = 500 // 0.5s duration

            fadeOut.interpolator = AccelerateDecelerateInterpolator()
            fadeIn.interpolator = AccelerateDecelerateInterpolator()

            // Start fade-out animation
            fadeOut.start()

            // Listener to change background after fade-out
            fadeOut.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // Once fade-out is complete, change the background
                    mainLayout.setBackgroundResource(targetBackground)

                    // Start fade-in animation after changing the background
                    fadeIn.start()
                }
            })
        } else {
            // Directly set background without animation (used on app start)
            mainLayout.setBackgroundResource(targetBackground)
            mainLayout.alpha = 1f  // Ensure it's fully visible
        }
    }
}
