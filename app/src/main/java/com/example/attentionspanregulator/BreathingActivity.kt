package com.example.attentionspanregulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.attentionspanregulator.ui.theme.AttentionSpanRegulatorTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BreathingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AttentionSpanRegulatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.8f)
                ) {
                    BreathingScreen {
                        finish()
                    }
                }
            }
        }
    }
}

@Composable
fun BreathingScreen(onFinished: () -> Unit) {
    var instruction by remember { mutableStateOf("Get Ready...") }
    val size = remember { Animatable(100f) }

    // This coroutine controls the animation and text, ensuring they are in sync.
    LaunchedEffect(key1 = Unit) {
        val animationJob = launch {
            delay(1000) // Initial pause
            while (isActive) {
                instruction = "Breathe In"
                size.animateTo(250f, animationSpec = tween(4000, easing = FastOutSlowInEasing))
                instruction = "Hold"
                delay(1500)
                instruction = "Breathe Out"
                size.animateTo(100f, animationSpec = tween(4000, easing = FastOutSlowInEasing))
                instruction = "Hold"
                delay(1500)
            }
        }
        // This coroutine acts as a 30-second timeout for the whole exercise.
        launch {
            delay(30000)
            if (animationJob.isActive) {
                animationJob.cancel()
                onFinished()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // A fixed-size canvas to draw the animated circle in.
        Canvas(modifier = Modifier.size(250.dp)) {
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = size.value / 2, // The radius is animated.
                style = Stroke(width = 8f),
                center = this.center // Always draw in the center of the Canvas.
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = instruction,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(64.dp))

        // The "Done" button to allow the user to exit early.
        Button(
            onClick = { onFinished() },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White.copy(alpha = 0.2f),
                contentColor = Color.White
            )
        ) {
            Text("Done", modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
        }
    }
}
