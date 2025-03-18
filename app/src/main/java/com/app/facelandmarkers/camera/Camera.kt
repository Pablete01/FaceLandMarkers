package com.app.facelandmarkers.camera

import androidx.annotation.OptIn
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA
import androidx.camera.core.ExperimentalMirrorMode
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.facelandmarkers.MainViewModel
import com.app.facelandmarkers.faceLandMarks.FaceLandmarkerOverlay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMirrorMode::class)
@Composable
fun Camera(
    mainViewModel: MainViewModel,
    paddingValues: PaddingValues
){

    val faceResults by mainViewModel.faceResults.observeAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraBopund by mainViewModel.cameraBound.collectAsState()
    val categories by mainViewModel.categoriesState.collectAsState()
    val gesto by mainViewModel.gesto.collectAsState()
    val showMesh by mainViewModel.showMesh.collectAsState()

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)

    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ){
            CameraPreviewContent(
                viewModel = mainViewModel,
                modifier = Modifier,
                lifecycleOwner = lifecycleOwner
            )
            if(showMesh) {
                FaceLandmarkerOverlay(
                    modifier = Modifier.fillMaxSize(),
                       // .wrapContentSize(Alignment.BottomCenter),
                    faceLandmarkerResults = faceResults,
                    480,
                    640,
                )
            }
        }
        Box (modifier = Modifier
            .fillMaxWidth()
            .weight(1f)) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Checkbox(
                        checked = showMesh,
                        onCheckedChange = { checked ->
                            mainViewModel.onShowMesh(checked)
                        }
                    )
                    Text(
                        text = "Mostrar malla",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp),
                        fontSize = 14.sp
                    )
                }
                LazyColumn {
                    items(gesto) { category ->
                        Text(
                            text = category,
                            fontSize = 10.sp
                        )
                    }
                }
            }

        }
    }
}


@OptIn(ExperimentalMirrorMode::class)
@Composable
fun CameraPreviewContent(
    viewModel: MainViewModel,
    modifier: Modifier,
    lifecycleOwner: LifecycleOwner,
){
    val surfaceRequest by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val cameraProviderFuture =  remember { ProcessCameraProvider.getInstance(context) }


    // Esta función solo la necesitamos si estamos creando un flujo de imágenes desde la cámara
    LaunchedEffect(cameraProviderFuture) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            // Aquí configuramos el caso de uso de la cámara para obtener imágenes
            val imageAnalysis = ImageAnalysis.Builder().build()

            // Análisis de imagen: Captura imágenes para enviar a FaceLandmarker
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                // Procesamos la imagen
                viewModel.processImage(imageProxy, isFrontCamera = true)

                // Luego de procesar la imagen, debemos cerrar el imageProxy para evitar bloqueos
                imageProxy.close()
            }

            // Vinculamos los casos de uso con el ciclo de vida
            cameraProvider.bindToLifecycle(
                lifecycleOwner, DEFAULT_FRONT_CAMERA, imageAnalysis
            )
        }, ContextCompat.getMainExecutor(context))
    }

    // Asynchronously get the camera provider using LaunchedEffect
    val cameraProvider = remember {
        mutableStateOf<ProcessCameraProvider?>(null)
    }
    LaunchedEffect(context) {
        val provider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider.value = provider
    }

    // Bind the camera when the provider is available
    cameraProvider.value?.let { provider ->
        LaunchedEffect(provider) {
            viewModel.bindToCamera(lifecycleOwner, provider)
        }
    }

    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = modifier.fillMaxSize()
        )
    }
}
