package tn.dalitrabelsi.peakproof

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast

class PeekProofService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var topView: View
    private lateinit var bottomView: View
    private lateinit var leftView: View
    private lateinit var rightView: View
    private lateinit var closeButton: Button

    // Variables to track initial positions for resizing
    private var initialY = 0
    private var initialX = 0
    private var initialHeight = 0
    private var initialWidth = 0

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Add the four floating edges (rectangles)
        topView = addResizableEdge("top", WindowManager.LayoutParams.MATCH_PARENT, 100)
        bottomView = addResizableEdge("bottom", WindowManager.LayoutParams.MATCH_PARENT, 100)
        leftView = addResizableEdge("left", 100, WindowManager.LayoutParams.MATCH_PARENT)
        rightView = addResizableEdge("right", 100, WindowManager.LayoutParams.MATCH_PARENT)

        // Add the floating "X" button in the top-right corner
        addCloseButton()

        Log.d("PeekProofService", "Service started with floating views and close button.")
    }

    // Method to add and handle resizing logic for each edge
    private fun addResizableEdge(position: String, width: Int, height: Int): View {
        val params = WindowManager.LayoutParams(
            width, height,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        val edgeView = View(this)
        edgeView.setBackgroundColor(android.graphics.Color.BLACK) // Opaque black

        // Set the gravity for the position (top, bottom, left, right)
        when (position) {
            "top" -> params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            "bottom" -> params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            "left" -> params.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
            "right" -> params.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
        }

        // Add the edge view to the window manager
        windowManager.addView(edgeView, params)

        // Handle touch for resizing
        setTouchListener(edgeView, params, if (position == "top" || position == "bottom") "vertical" else "horizontal")

        return edgeView // Return the created view for later removal
    }

    // Set a touch listener to handle drag resizing
    private fun setTouchListener(view: View, params: WindowManager.LayoutParams, orientation: String) {
        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Capture initial positions
                    initialX = event.rawX.toInt()
                    initialY = event.rawY.toInt()
                    initialWidth = params.width
                    initialHeight = params.height
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Resize logic: update dimensions based on drag
                    val deltaX = event.rawX.toInt() - initialX
                    val deltaY = event.rawY.toInt() - initialY

                    // Handle vertical orientation resizing (top/bottom)
                    if (orientation == "vertical") {
                        if (view === bottomView) {
                            // Resize from the bottom, meaning we adjust height by deltaY
                            params.height = (initialHeight + deltaY).coerceAtLeast(50)
                        } else {
                            // Resize from the top
                            params.height = (initialHeight + deltaY).coerceAtLeast(50)
                        }
                    }
                    // Handle horizontal orientation resizing (left/right)
                    else {
                        if (view === rightView) {
                            // Resize from the right, meaning we adjust width by deltaX
                            params.width = (initialWidth + deltaX).coerceAtLeast(50)
                        } else {
                            // Resize from the left
                            params.width = (initialWidth + deltaX).coerceAtLeast(50)
                        }
                    }

                    // Update view with new dimensions
                    windowManager.updateViewLayout(view, params)
                    true
                }
                else -> false
            }
        }
    }

    // Add a floating close button to stop the service and remove the overlays
    private fun addCloseButton() {
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.RIGHT // Position the button in the top-right corner
        params.x = 0 // Set margins if necessary
        params.y = 0

        closeButton = Button(this).apply {
            text = "X"
            setBackgroundColor(android.graphics.Color.RED)
            setTextColor(android.graphics.Color.WHITE)
            setOnClickListener {
                Toast.makeText(this@PeekProofService, "Stopping overlay", Toast.LENGTH_SHORT).show()
                stopSelf() // Stop the service and remove all overlays
            }
        }

        windowManager.addView(closeButton, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove all views gracefully when the service is destroyed
        try {
            windowManager.removeView(topView)
            windowManager.removeView(bottomView)
            windowManager.removeView(leftView)
            windowManager.removeView(rightView)
            windowManager.removeView(closeButton)
            Log.d("PeekProofService", "All views removed, service stopped.")
        } catch (e: Exception) {
            Log.e("PeekProofService", "Error removing views: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not binding the service
    }
}
