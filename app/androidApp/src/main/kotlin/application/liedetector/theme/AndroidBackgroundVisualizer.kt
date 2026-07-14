package application.liedetector.theme

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import application.liedetector.uicore.theme.BackgroundState
import application.liedetector.uicore.theme.BackgroundVisualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidBackgroundVisualizer(context: Context) : BackgroundVisualizer, SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) 
                  ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _state = MutableStateFlow(BackgroundState())
    override val state: StateFlow<BackgroundState> = _state.asStateFlow()

    private var baseTiltX: Float? = null
    private var baseTiltY: Float? = null
    
    // Smooth filters for two different "weights"
    private var fastX = 0f
    private var fastY = 0f
    private var slowX = 0f
    private var slowY = 0f

    init {
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun setIntensity(value: Float) {}
    override fun onTap(x: Float, y: Float) {
        baseTiltX = null 
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        
        val rawX: Float
        val rawY: Float

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            rawX = event.values[0] * 8.0f
            rawY = event.values[1] * 8.0f
        } else {
            rawX = -event.values[0] / 9.81f
            rawY = event.values[1] / 9.81f
        }

        if (baseTiltX == null) {
            baseTiltX = rawX
            baseTiltY = rawY
        }

        // INVERTING AXES TO MATCH USER PERSPECTIVE
        val targetX = (rawX - (baseTiltX ?: 0f))
        val targetY = (rawY - (baseTiltY ?: 0f))

        // Fast pupil movement (low damping)
        fastX += (targetX - fastX) * 0.15f
        fastY += (targetY - fastY) * 0.15f

        // Heavy eye body movement (high damping)
        slowX += (targetX - slowX) * 0.02f
        slowY += (targetY - slowY) * 0.02f

        _state.value = _state.value.copy(
            tiltX = fastX.coerceIn(-1.5f, 1.5f), // Pupil
            tiltY = fastY.coerceIn(-1.5f, 1.5f),
            // We use intensity to pass the "Slow" heavy offset to UI
            intensity = slowX.coerceIn(-0.5f, 0.5f) // Representing slow horizontal shift
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
