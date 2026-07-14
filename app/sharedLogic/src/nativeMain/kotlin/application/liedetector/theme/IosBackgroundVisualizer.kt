package application.liedetector.theme

import application.liedetector.uicore.theme.BackgroundState
import application.liedetector.uicore.theme.BackgroundVisualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue
import kotlinx.cinterop.useContents
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
class IosBackgroundVisualizer : BackgroundVisualizer {
    private val motionManager = CMMotionManager()
    private val _state = MutableStateFlow(BackgroundState())
    override val state: StateFlow<BackgroundState> = _state.asStateFlow()

    private var baseTiltX: Float? = null
    private var baseTiltY: Float? = null

    init {
        if (motionManager.isDeviceMotionAvailable()) {
            motionManager.deviceMotionUpdateInterval = 1.0 / 60.0
            motionManager.startDeviceMotionUpdatesToQueue(NSOperationQueue.mainQueue) { motion, _ ->
                motion?.gravity?.useContents {
                    val rawX = x.toFloat()
                    val rawY = y.toFloat()

                    if (baseTiltX == null) {
                        baseTiltX = rawX
                        baseTiltY = rawY
                    }

                    val tx = (rawX - (baseTiltX ?: 0f)) * 4.0f
                    val ty = (rawY - (baseTiltY ?: 0f)) * 4.0f

                    _state.value = _state.value.copy(
                        tiltX = tx.coerceIn(-1.5f, 1.5f),
                        tiltY = ty.coerceIn(-1.5f, 1.5f)
                    )
                }
            }
        }
    }

    override fun setIntensity(value: Float) {}
    override fun onTap(x: Float, y: Float) {
        baseTiltX = null
    }
}
