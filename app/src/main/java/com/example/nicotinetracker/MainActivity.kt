package com.example.nicotinetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.nicotinetracker.ui.theme.NicotineTrackerTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel

// Launching MainScreen here, which contains the main UI - I decided to this,
// in case I want to add more screens later
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NicotineTrackerTheme {
                      MainScreen()
                }
            }
        }
    }

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    // collect persisted events from Room
    val events by viewModel.useEvents.collectAsState(initial = emptyList())

    // current time state that updates every second - stores time in millis
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // update currentTime every second
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    // derive nextAllowedTime from the most-recent event
    val nextAllowedTime = events.firstOrNull()?.nextAt

    // determines if the button should be enabled
    val buttonEnabled by remember(nextAllowedTime, currentTime) {
        derivedStateOf { nextAllowedTime?.let { currentTime >= it } ?: true }
    }

    // toggle to stop the incrementing of wait time after each use
    val incrementEnabledState = remember { androidx.compose.runtime.mutableStateOf(true)}

    // transform events to the same string pairs used by UI
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val useTimes = remember(events) {
        events.map { e ->
            Pair(formatter.format(java.util.Date(e.usedAt)), formatter.format(java.util.Date(e.nextAt)))
        }.toMutableList()
    }

    // reserved spacing values
    val buttonHeight = 48.dp
    val buttonBottomMargin = 16.dp
    val reservedBottom = buttonHeight + buttonBottomMargin + 8.dp

    // Scaffold for basic screen structure
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        // Box for stacking elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .systemBarsPadding()
        ) {
            // Header
            Header(modifier = Modifier.align(Alignment.TopCenter),
                nextAllowedTime = nextAllowedTime,
                currentTime = currentTime)

            // Scrollable column for usage history
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp, bottom = reservedBottom)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Showing each usage event
                useTimes.forEach { (used, next) ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(3.dp, MaterialTheme.colorScheme.primary)
                            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Row to separate use time and next allowed time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Used: $used",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = "Next: $next",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                UseButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    enabled = buttonEnabled,
                    onClick = {
                        viewModel.onUseClicked(incrementEnabledState.value)
                    }
                )
                Button(
                    onClick = {
                        incrementEnabledState.value = !incrementEnabledState.value
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = if (incrementEnabledState.value) "Stop Increment" else "Resume Increment",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}


// Helper function to format remaining time into HH:MM:SS or "Ready to use"
private fun formatRemainingTime(remainingMs: Long?): String {
    if (remainingMs == null || remainingMs <= 0L) {
        return "Ready to use"
    }
    val totalSeconds = remainingMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(),
        "Next %02d:%02d:%02d", hours, minutes, seconds)

}

// Header composable showing app title and countdown timer
@Composable
fun Header(
    modifier: Modifier = Modifier,
    nextAllowedTime: Long? = null,
    currentTime: Long = System.currentTimeMillis()
) {
    val remainingMs = nextAllowedTime?.let { it - currentTime }
    val timerText = formatRemainingTime(remainingMs)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Velo Tracker",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = timerText,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            textAlign = TextAlign.End
        )
    }
}

// UseButton composable for the "Use" button
@Composable
fun UseButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
){
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth(0.4f)
            .height(48.dp),
        shape = RoundedCornerShape(24.dp)
    ){
        Text(
            text = "Use",
            style = MaterialTheme.typography.labelLarge
        )
    }
}