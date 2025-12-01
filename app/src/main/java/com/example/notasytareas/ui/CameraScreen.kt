package com.example.notasytareas.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.await
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

enum class CameraMode {
    PHOTO, VIDEO
}

@Composable
fun CameraScreen(onImageCaptured: (Uri) -> Unit, onVideoCaptured: (Uri) -> Unit) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasPermission = permissions.values.all { it }
        }
    )

    LaunchedEffect(key1 = true) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        launcher.launch(permissionsToRequest.toTypedArray())
    }

    if (hasPermission) {
        CameraView(context, onImageCaptured, onVideoCaptured)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Requesting permissions...")
        }
    }
}

@Composable
private fun CameraView(
    context: Context,
    onImageCaptured: (Uri) -> Unit,
    onVideoCaptured: (Uri) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }

    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var mode by remember { mutableStateOf(CameraMode.PHOTO) }

    var recording: Recording? by remember { mutableStateOf(null) }
    var isRecording by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }


    val imageCapture = remember { ImageCapture.Builder().build() }
    val recorder = remember { Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build() }
    val videoCapture: VideoCapture<Recorder> = remember { VideoCapture.withOutput(recorder) }

    LaunchedEffect(Unit) {
        cameraProvider = context.getCameraProvider()
    }

    val previewView = remember { PreviewView(context) }
    LaunchedEffect(cameraProvider, lensFacing, mode) {
        cameraProvider?.let { provider ->
            try {
                provider.unbindAll()
                val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                if (mode == CameraMode.PHOTO) {
                    provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                } else {
                    provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, videoCapture)
                }
            } catch (e: Exception) {
                Log.e("CameraView", "Failed to bind camera use cases", e)
            }
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (true) {
                delay(1000)
                recordingSeconds++
            }
        } else {
            recordingSeconds = 0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

        if (isRecording) {
            Text(
                text = formatDuration(recordingSeconds),
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        CameraControls(
            mode = mode,
            isRecording = isRecording,
            onModeChange = { newMode -> if (!isRecording) mode = newMode },
            onFlipCamera = {
                if (!isRecording) {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else {
                        CameraSelector.LENS_FACING_BACK
                    }
                }
            },
            onCapture = {
                if (mode == CameraMode.PHOTO) {
                    takePhoto(context, imageCapture, onImageCaptured)
                } else {
                    if (isRecording) {
                        recording?.stop()
                    } else {
                        recording = startRecording(context, videoCapture) { event ->
                            when (event) {
                                is VideoRecordEvent.Start -> {
                                    isRecording = true
                                }
                                is VideoRecordEvent.Finalize -> {
                                    isRecording = false
                                    if (!event.hasError()) {
                                        onVideoCaptured(event.outputResults.outputUri)
                                    } else {
                                        recording = null
                                        Log.e("CameraView", "Video capture error: ${event.error}")
                                    }
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CameraControls(
    mode: CameraMode,
    isRecording: Boolean,
    onModeChange: (CameraMode) -> Unit,
    onFlipCamera: () -> Unit,
    onCapture: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ModeSelector(text = "PHOTO", isSelected = mode == CameraMode.PHOTO, onClick = { onModeChange(CameraMode.PHOTO) })
            ModeSelector(text = "VIDEO", isSelected = mode == CameraMode.VIDEO, onClick = { onModeChange(CameraMode.VIDEO) })
        }

        Row(
            modifier = Modifier.padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Spacer(modifier = Modifier.size(64.dp))
            CaptureButton(
                mode = mode,
                isRecording = isRecording,
                onClick = onCapture
            )
            IconButton(onClick = onFlipCamera, modifier = Modifier.size(64.dp)) {
                Icon(
                    imageVector = Icons.Default.FlipCameraAndroid,
                    contentDescription = "Flip camera",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}

@Composable
private fun CaptureButton(
    mode: CameraMode,
    isRecording: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(80.dp)
    ) {
        if (mode == CameraMode.PHOTO) {
            Icon(
                imageVector = Icons.Default.PhotoCamera,
                contentDescription = "Take photo",
                tint = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .border(4.dp, Color.White, CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) Color.White else Color.Red),
                contentAlignment = Alignment.Center
            ) {
                if (isRecording) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop recording",
                        tint = Color.Red,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeSelector(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text = text,
        color = if (isSelected) Color.Yellow else Color.White,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        fontSize = 16.sp,
        modifier = Modifier.clickable(onClick = onClick)
    )
}


private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            continuation.resume(future.get())
        }, ContextCompat.getMainExecutor(this))
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit
) {
    // Crear un nombre de archivo único basado en la fecha y hora
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "IMG_$timeStamp.jpg"

    // Usar DIRECTORY_PICTURES dentro de los archivos de la app (Persistente)
    // se guarda en: /Android/data/com.example.notasytareas/files/Pictures/
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    val file = File(storageDir, fileName)

    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                // Usamos Uri.fromFile para asegurar que guardamos la ruta absoluta correcta
                val savedUri = Uri.fromFile(file)
                onImageCaptured(savedUri)
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "Photo capture failed: ${exception.message}", exception)
            }
        }
    )
}

private fun startRecording(
    context: Context,
    videoCapture: VideoCapture<Recorder>,
    onRecordEvent: (VideoRecordEvent) -> Unit
): Recording {
    // Nombre único
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "VID_$timeStamp.mp4"

    // Usar DIRECTORY_MOVIES (Persistente)
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)

    val file = File(storageDir, fileName)

    val outputOptions = FileOutputOptions.Builder(file).build()

    return videoCapture.output.prepareRecording(context, outputOptions)
        .withAudioEnabled()
        .start(ContextCompat.getMainExecutor(context), onRecordEvent)
}

private fun formatDuration(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
}
