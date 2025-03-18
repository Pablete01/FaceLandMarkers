package com.app.facelandmarkers

import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
import androidx.camera.core.ExperimentalMirrorMode
import androidx.camera.core.ImageProxy
import androidx.camera.core.MirrorMode.MIRROR_MODE_ON
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.facelandmarkers.faceLandmarkerHelper.FaceLandmarkerHelper
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.KoinApplication.Companion.init

class MainViewModel(
      private val faceLandmarkerHelper: FaceLandmarkerHelper
): ViewModel(){

    private var _delegate: Int = FaceLandmarkerHelper.DELEGATE_CPU
    private var _minFaceDetectionConfidence: Float =
        FaceLandmarkerHelper.DEFAULT_FACE_DETECTION_CONFIDENCE
    private var _minFaceTrackingConfidence: Float = FaceLandmarkerHelper
        .DEFAULT_FACE_TRACKING_CONFIDENCE
    private var _minFacePresenceConfidence: Float = FaceLandmarkerHelper
        .DEFAULT_FACE_PRESENCE_CONFIDENCE
    private var _maxFaces: Int = FaceLandmarkerHelper.DEFAULT_NUM_FACES


    // Used to set up a link between the Camera and your UI.
    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest

    private val cameraPreviewUseCase = Preview.Builder()
        .build().apply {
        setSurfaceProvider { newSurfaceRequest ->
            _surfaceRequest.update { newSurfaceRequest }
        }
    }

    private val _cameraBound = MutableStateFlow(false) // Estado para indicar si la cámara está vinculada
    val cameraBound: StateFlow<Boolean> get() = _cameraBound

    private val _showMesh = MutableStateFlow(false)
    val showMesh: StateFlow<Boolean> get() = _showMesh

    fun onShowMesh(value: Boolean){
        _showMesh.value = value
    }



    // Suspend function to bind the camera
    suspend fun bindToCamera(lifecycleOwner: LifecycleOwner, cameraProvider: ProcessCameraProvider) {
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            DEFAULT_FRONT_CAMERA,
            cameraPreviewUseCase
        )
        _cameraBound.value = true
        // Ensure cancellation logic
        try {
            awaitCancellation()
        } finally {
            cameraProvider.unbindAll()
        }
    }

    private var categories: MutableList<Category?> = MutableList(52) { null }

    private val _categoriesState = MutableStateFlow<List<Category?>>(emptyList())
    val categoriesState: StateFlow<List<Category?>> = _categoriesState

    fun updateResults(faceLandmarkerResult: FaceLandmarkerResult? = null) {
        categories = MutableList(52) { null }
        if (faceLandmarkerResult != null && faceLandmarkerResult.faceBlendshapes().isPresent) {
            val faceBlendshapes = faceLandmarkerResult.faceBlendshapes().get()
            val sortedCategories = faceBlendshapes[0].sortedByDescending { it.score() }
            val min = kotlin.math.min(sortedCategories.size, categories.size)
            for (i in 0 until min) {
                categories[i] = sortedCategories[i]
            }
        }
        _categoriesState.value = categories.filterNotNull()
        detectGestures(categories)

    }

    private val gestosConfigurados = mapOf(
        "mouthSmileLeft" to GestoConfig("Sonrisa Izquierda", 0.5f),  //ajustar valor de sensibilidad
        "mouthSmileRight" to GestoConfig("Sonrisa Derecha", 0.5f),
        "eyeBlinkRight" to GestoConfig("Parpadeo Izquierdo", 0.5f),
        "eyeBlinkLeft" to GestoConfig("Parpadeo Derecho", 0.5f),
        "mouthShrugLower" to GestoConfig("Encogimiento Inferior de Boca", 1.8f),
        "mouthShrugUpper" to GestoConfig("Encogimiento Superior de Boca", 1.8f),
        "browDownLeft" to GestoConfig("Cejas Abajo Izquierda", 0.5f),
        "browDownRight" to GestoConfig("Cejas Abajo Derecha", 0.5f),
        "eyeLookDownRight" to GestoConfig("Mirada Abajo Derecha", 1.8f),
        "eyeLookDownLeft" to GestoConfig("Mirada Abajo Izquierda", 1.8f),
        "eyeLookOutLeft" to GestoConfig("Mirada Afuera Izquierda", 1.8f),
        "eyeLookOutRight" to GestoConfig("Mirada Afuera Derecha", 1.8f),
        "eyeLookInLeft" to GestoConfig("Mirada Adentro Izquierda", 1.8f),
        "eyeLookInRight" to GestoConfig("Mirada Adentro Derecha", 1.8f),
        "eyeLookUpLeft" to GestoConfig("Mirada Arriba Izquierda", 1.8f),
        "eyeLookUpRight" to GestoConfig("Mirada Arriba Derecha", 1.8f),
        "mouthUpperUpLeft" to GestoConfig("Boca Superior Arriba Izquierda", 0.5f),
        "mouthUpperUpRight" to GestoConfig("Boca Superior Arriba Derecha", 0.5f),
        "mouthPressLeft" to GestoConfig("Presión Boca Izquierda", 0.5f),
        "mouthPressRight" to GestoConfig("Presión Boca Derecha", 0.5f),
        "mouthFrownLeft" to GestoConfig("Boca Fruncida Izquierda", 0.5f),
        "mouthFrownRight" to GestoConfig("Boca Fruncida Derecha", 0.5f),
        "mouthStretchLeft" to GestoConfig("Estiramiento Boca Izquierda", 0.5f),
        "mouthStretchRight" to GestoConfig("Estiramiento Boca Derecha", 0.5f),
        "mouthDimpleLeft" to GestoConfig("Hoyuelo Boca Izquierda", 0.5f),
        "mouthDimpleRight" to GestoConfig("Hoyuelo Boca Derecha", 0.5f),
        "mouthRollLower" to GestoConfig("Rodillo Inferior de Boca", 0.5f),
        "mouthRollUpper" to GestoConfig("Rodillo Superior de Boca", 0.5f),
        "mouthPucker" to GestoConfig("Boca Fruncida", 0.5f),
        "mouthFunnel" to GestoConfig("Boca en Embudo", 0.5f),
        "mouthClose" to GestoConfig("Boca Cerrada", 0.5f),
        "mouthOpen" to GestoConfig("Boca Abierta", 0.5f),
        "jawOpen" to GestoConfig("Mandíbula Abierta", 0.5f),
        "jawRight" to GestoConfig("Mandíbula Derecha", 0.5f),
        "jawLeft" to GestoConfig("Mandíbula Izquierda", 0.5f),
        "jawForward" to GestoConfig("Mandíbula Adelante", 0.5f),
        "cheekPuff" to GestoConfig("Mejillas Infladas", 0.5f),
        "cheekSquintLeft" to GestoConfig("Ojo Entrecerrado Izquierdo", 1.8f),
        "cheekSquintRight" to GestoConfig("Ojo Entrecerrado Derecho", 1.8f),
        "eyeSquintLeft" to GestoConfig("Ojo entrecerrado Izquierdo", 1.8f),
        "eyeSquintRight" to GestoConfig("Ojo entrecerrado Derecho", 1.8f),
        "eyeWideLeft" to GestoConfig("Ojo Abierto Izquierdo", 1.8f),
        "eyeWideRight" to GestoConfig("Ojo Abierto Derecho", 1.8f),
        "browOuterUpLeft" to GestoConfig("Ceja Externa Arriba Izquierda", 1.8f),
        "browOuterUpRight" to GestoConfig("Ceja Externa Arriba Derecha", 1.8f),
        "browInnerUp" to GestoConfig("Cejas Internas Arriba", 1.8f),
        "noseSneerLeft" to GestoConfig("Nariz Arrugada Izquierda", 0.5f),
        "noseSneerRight" to GestoConfig("Nariz Arrugada Derecha", 0.5f),
        "_neutral" to GestoConfig("Neutral", 0.5f)
    )


    private val _gesto = MutableStateFlow<List<String>>(emptyList())
    val gesto: StateFlow<List<String>> = _gesto

    // Función para detectar gestos
    private fun detectGestures(categories: List<Category?>) {
        val gestoDetectado = categories.firstNotNullOfOrNull { category ->
            category?.let {
                val config = gestosConfigurados[it.categoryName()]
                println("Detectando: ${it.categoryName()} con score: ${it.score()}") // Debug
                if (config != null && it.score() > config.threshold) {
                    config.textoSalida
                } else {
                    null
                }
            }
        }
        // Solo actualizamos el StateFlow si realmente detectamos un gesto
        gestoDetectado?.let { nuevoGesto ->
            _gesto.value = (_gesto.value + nuevoGesto).takeLast(4)
            println("Gesto detectado: $_gesto") // Debug
        }
    }

    private val _faceResults = MutableLiveData<FaceLandmarkerHelper.ResultBundle?>()
    val faceResults: LiveData<FaceLandmarkerHelper.ResultBundle?> = _faceResults

    init {
        faceLandmarkerHelper.faceLandmarkerHelperListener = object : FaceLandmarkerHelper.LandmarkerListener {
            override fun onResults(resultBundle: FaceLandmarkerHelper.ResultBundle) {
                // Aquí recibimos los resultados y los almacenamos en LiveData
                _faceResults.postValue(resultBundle)
                updateResults(resultBundle.result)
            }

            override fun onError(error: String, errorCode: Int) {
                // Manejo de errores
                Log.e("FaceLandmarker", "Error: $error")
            }

            override fun onEmpty() {
                // Si no hay caras detectadas
                Log.d("FaceLandmarker", "No faces detected")
            }
        }
    }

    fun processImage(imageProxy: ImageProxy, isFrontCamera: Boolean) {
        faceLandmarkerHelper.detectLiveStream(imageProxy, isFrontCamera)
    }

}