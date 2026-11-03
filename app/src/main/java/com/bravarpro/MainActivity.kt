package com.bravarpro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BravarProApp()
        }
    }
}

@Composable
fun BravarProApp() {
    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("BRAVAR PRO", style = MaterialTheme.typography.headlineLarge)
            Text("Bravarski kalkulator")

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {}) {
                Text("Izračun profila i cijevi")
            }
            Button(onClick = {}) {
                Text("Izračun težine materijala")
            }
            Button(onClick = {}) {
                Text("Plan rezanja")
            }
            Button(onClick = {}) {
                Text("Izračun cijene")
            }
        }
    }
}
