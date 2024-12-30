package com.flip.coinflip

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flip.coinflip.ui.theme.CoinflipTheme
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.ui.graphics.graphicsLayer

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoinflipTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("Flip the Coin") }, // Set the title here
                            actions = {
                                // Add any additional actions if needed (optional)
                            }
                        )
                    }
                ) { innerPadding ->
                    CoinFlipScreen(
                        Modifier.padding(innerPadding),
                        mediaPlayer = MediaPlayer.create(this, R.raw.coin_flip)
                    )
                }
            }
        }
    }
}

var headsCount = 0
var tailsCount = 0

fun flipCoin(): String {
    val result = if (Random.nextBoolean()) "Heads" else "Tails"

    if (result == "Heads") {
        headsCount++
    } else {
        tailsCount++
    }

    // Force "Tails" if heads appears too often
    if (headsCount >= 3) {
        headsCount = 0 // Reset heads count
        return "Tails"
    }

    return result
}

@Composable
fun CoinFlipScreen(modifier: Modifier = Modifier, mediaPlayer: MediaPlayer) {
    var result by remember { mutableStateOf("") }
    var isFlipping by remember { mutableStateOf(false) }
    var rotation by remember { mutableStateOf(0f) }
    var currentImage by remember { mutableStateOf(R.drawable.coin_heads) }
    var backgroundColor by remember { mutableStateOf(Brush.linearGradient(listOf(androidx.compose.ui.graphics.Color(0xFF00BCD4), androidx.compose.ui.graphics.Color(0xFF8E24AA)))) }

    val buttonScale by animateFloatAsState(
        targetValue = if (isFlipping) 0.9f else 1.0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    // Start rotation animation when flipping
    if (isFlipping) {
        LaunchedEffect(Unit) {
            mediaPlayer.start() // Play coin roll sound
            val duration = 5000L // Total duration: 5 seconds
            val flipInterval = 100L // Flip image every 100ms

            val endTime = System.currentTimeMillis() + duration
            while (System.currentTimeMillis() < endTime) {
                rotation += 45f
                currentImage = if (currentImage == R.drawable.coin_heads) {
                    R.drawable.coin_tails
                } else {
                    R.drawable.coin_heads
                }
                delay(flipInterval)
            }

            // End the flip with a balanced random result
            isFlipping = false
            result = flipCoin()
            mediaPlayer.stop()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(backgroundColor), // Background color change
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .rotate(rotation) // Apply rotation
        ) {
            Image(
                painter = painterResource(id = currentImage),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (!isFlipping) {
                    result = ""
                    isFlipping = true
                    mediaPlayer.reset()
                    mediaPlayer.start()
                    backgroundColor = Brush.linearGradient(listOf(androidx.compose.ui.graphics.Color(0xFF9C27B0), androidx.compose.ui.graphics.Color(0xFFFF9800))) // Change background during flip
                }
            },
            shape = CircleShape,
            modifier = Modifier.scale(buttonScale) // Scale animation for button
        ) {
            Text(text = if (isFlipping) "Flipping..." else "Flip Coin")
        }
        if (result.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Result: $result", style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoinFlipPreview() {
    CoinflipTheme {
        CoinFlipScreen(Modifier, MediaPlayer.create(null, R.raw.coin_flip))
    }
}
