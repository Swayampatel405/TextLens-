package com.appvantage.imagetospeech.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appvantage.imagetospeech.R
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToTextApp(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    var copiedText by remember { mutableStateOf("Extracted Text will be shown here") }

    var isEraseSelected by remember { mutableStateOf(false) }
    var isCameraSelected by remember { mutableStateOf(false) }
    var isSpeakSelected by remember { mutableStateOf(false) }
    var isCopySelected by remember { mutableStateOf(false) }

    //Image
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) } // Store the captured image

    //Clipboard Manager
    val clipboardManager = context.getSystemService(ClipboardManager::class.java)

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                capturedImage = bitmap // Store the captured image
                detectTextUsingML(bitmap){detectedText ->
                        copiedText = detectedText
                }
            } else {
                Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Oops! Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }

    //For Text To Read
    val textToSpeech = remember {mutableStateOf<TextToSpeech?>(null) }

    LaunchedEffect(Unit) {
        textToSpeech.value = TextToSpeech(context) { status ->
            if (status != TextToSpeech.ERROR) {
                textToSpeech.value?.language = java.util.Locale.US
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            textToSpeech.value?.stop()
            textToSpeech.value?.shutdown()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ImageToText", fontWeight = FontWeight.Bold)},
                colors = TopAppBarColors(
                    containerColor = Color.Gray,
                    titleContentColor = Color.White,
                    scrolledContainerColor = Color.Gray,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
                 },
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.Gray,
                contentColor = Color.White
            ){
                NavigationDrawerItem(
                    modifier = Modifier.weight(1f),
                    label = { Text("Erase") },
                    onClick = {
                        isCopySelected = false
                        isCameraSelected = false
                        isSpeakSelected = false
                        isEraseSelected = true

                        //erase
                        copiedText = "Extracted Text will be shown here" // Clear the text
                        capturedImage = null // Clear the image
                        isEraseSelected = false
                    },
                    selected = isEraseSelected,
                    icon = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_clear_24),
                                contentDescription = null
                            )
                            Text("Erase")
                        }
                    }
                )
                NavigationDrawerItem(
                    modifier = Modifier.weight(1f),
                    label = { Text("Scan") },
                    onClick = {
                        isCopySelected = false
                        isCameraSelected = true
                        isEraseSelected = false
                        isSpeakSelected = false

                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        if (intent.resolveActivity(context.packageManager) != null) {
                            cameraLauncher.launch(intent)
                        } else {
                            Toast.makeText(context, "No Camera App Found", Toast.LENGTH_SHORT).show()
                        }
                        isCameraSelected = false
                    },
                    selected = isCameraSelected,
                    icon = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_photo_camera_24),
                                contentDescription = null
                            )
                            Text("Scan")
                        }
                    }
                )
                NavigationDrawerItem(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        isCopySelected = false
                        isCameraSelected = false
                        isEraseSelected = false
                        isSpeakSelected = true

                        //To speak
                        textToSpeech.value?.speak(copiedText, TextToSpeech.QUEUE_FLUSH, null, null)

                        isSpeakSelected = false
                    },
                    selected = isSpeakSelected,
                    icon = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_mic_24),
                                contentDescription = null
                            )
                            Text("Speak")
                        }
                    },
                    label = { Text("") }
                )
                NavigationDrawerItem(
                        modifier = Modifier.weight(1f),
                label = { Text("Copy") },
                onClick = {
                    isCopySelected = true
                    isCameraSelected = false
                    isEraseSelected = false
                    isSpeakSelected = false

                    //copy
                    val clip = ClipData.newPlainText("Detected Text", copiedText)
                    clipboardManager.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to Clipboard", Toast.LENGTH_SHORT).show()
                    isCopySelected = false
                },
                selected = isCopySelected,
                icon = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_content_copy_24),
                            contentDescription = null
                        )
                        Text("Copy")
                    }
                }
                )
            }
        }

    ){innerPadding->
        ContentScreen(modifier= modifier.padding(innerPadding),capturedImage = capturedImage,copiedText)
    }

}

@Composable
fun ContentScreen(modifier: Modifier=Modifier,capturedImage: Bitmap?,copiedText:String){

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().background(color = Color.DarkGray).padding(15.dp)
    ){

        Text(text = copiedText, fontWeight = FontWeight.Bold)

        capturedImage?.let { bitmap ->
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Captured Image",
                modifier = Modifier
                    .size(200.dp) // Set the size of the image
                    .clip(RoundedCornerShape(12.dp)) // Rounded corners
            )
        }
    }

}


fun detectTextUsingML(bitmap: Bitmap, onTextDetected: (String) -> Unit){
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val image = InputImage.fromBitmap(bitmap, 0)

    val result = recognizer.process(image)
        .addOnSuccessListener { visionText ->
            // Task completed successfully
            onTextDetected(visionText.text)

        }
        .addOnFailureListener { e ->
            // Task failed with an exception
            onTextDetected("Text detection failed: ${e.message}")
        }

}
