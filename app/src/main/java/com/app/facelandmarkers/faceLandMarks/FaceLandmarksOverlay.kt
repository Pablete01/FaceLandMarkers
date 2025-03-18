package com.app.facelandmarkers.faceLandMarks

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.app.facelandmarkers.faceLandmarkerHelper.FaceLandmarkerHelper
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarker


@Composable
fun FaceLandmarkerOverlay(
    modifier: Modifier,
    faceLandmarkerResults: FaceLandmarkerHelper.ResultBundle?,
    imageWidth: Int,
    imageHeight: Int,
    runningMode: RunningMode = RunningMode.IMAGE
) {
    val scaleFactor = remember { mutableFloatStateOf(2.5f) }
    val colorPrimary = MaterialTheme.colorScheme.primary



    Canvas(
        modifier = modifier
    ) {
        faceLandmarkerResults?.let { result ->
            val scaledImageWidth = imageWidth * scaleFactor.floatValue
            val scaledImageHeight = imageHeight * scaleFactor.floatValue

            val offsetX = (size.width - scaledImageWidth) / 2f
            val offsetY = (size.height - scaledImageHeight) / 2f

            result.result.faceLandmarks().forEach { faceLandmarks ->
                // Dibujar puntos de los landmarks
                drawFaceLandmarks(faceLandmarks, offsetX, offsetY, imageWidth, imageHeight, scaleFactor.floatValue)

                // Dibujar conectores entre landmarks
                drawFaceConnectors(faceLandmarks, offsetX, offsetY, imageWidth, imageHeight, scaleFactor.floatValue)
            }
        }
    }
}

private fun DrawScope.drawFaceLandmarks(
    faceLandmarks: List<NormalizedLandmark>,
    offsetX: Float,
    offsetY: Float,
    imageWidth: Int,
    imageHeight: Int,
    scaleFactor: Float
) {
    faceLandmarks.forEach { landmark ->
        val x = landmark.x() * imageWidth * scaleFactor + offsetX
        val y = landmark.y() * imageHeight * scaleFactor  + offsetY

        drawCircle(
            color = Color.Yellow,
            radius = 3f,
            center = Offset(x, y)
        )
    }
}

private fun DrawScope.drawFaceConnectors(
    faceLandmarks: List<NormalizedLandmark>,
    offsetX: Float,
    offsetY: Float,
    imageWidth: Int,
    imageHeight: Int,
    scaleFactor: Float,
) {
    FaceLandmarker.FACE_LANDMARKS_CONNECTORS.filterNotNull().forEach { connector ->
        val startLandmark = faceLandmarks.getOrNull(connector.start())
        val endLandmark = faceLandmarks.getOrNull(connector.end())

        if (startLandmark != null && endLandmark != null) {
            val startX = startLandmark.x() * imageWidth * scaleFactor + offsetX
            val startY = startLandmark.y() * imageHeight * scaleFactor + offsetY
            val endX = endLandmark.x() * imageWidth * scaleFactor + offsetX
            val endY = endLandmark.y() * imageHeight * scaleFactor + offsetY

            drawLine(
                color = Color.DarkGray,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 2f
            )
        }
    }
}
