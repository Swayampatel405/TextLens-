package com.appvantage.imagetospeech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.appvantage.imagetospeech.ui.ImageToTextApp
import com.appvantage.imagetospeech.ui.theme.ImageToSpeechTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ImageToSpeechTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ImageToTextApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

