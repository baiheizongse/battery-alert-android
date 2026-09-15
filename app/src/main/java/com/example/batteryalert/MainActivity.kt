package com.example.batteryalert

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val batteryPercent = mutableIntStateOf(-1)
    private var tts: TextToSpeech? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
            batteryPercent.intValue = if (scale > 0) level * 100 / scale else -1
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.JAPANESE
            }
        }

        setContent {
            MaterialTheme {
                BatteryAlertScreen(
                    batteryPercent = batteryPercent,
                    onStart = { t -> BatteryMonitorService.start(this, t) },
                    onStop = { BatteryMonitorService.stop(this) },
                    onTest = { testNotification() }
                )
            }
        }
    }

    private fun testNotification() {
        BatteryMonitorService.postTestNotification(this)
        speak("テストです。通知と音声が正しく動作しています。")
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "battery_test")
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onPause() {
        super.onPause()
        runCatching { unregisterReceiver(batteryReceiver) }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun BatteryAlertScreen(
    batteryPercent: MutableIntState,
    onStart: (Int) -> Unit,
    onStop: () -> Unit,
    onTest: () -> Unit
) {
    var thresholdText by remember { mutableStateOf("20") }
    var monitoring by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    val threshold = thresholdText.toIntOrNull() ?: 20

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("現在のバッテリー残量", style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (batteryPercent.intValue >= 0) "${batteryPercent.intValue}%" else "取得中…",
                style = MaterialTheme.typography.displayMedium
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = thresholdText,
                onValueChange = { thresholdText = it },
                label = { Text("通知する残量（%）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("監視をON", modifier = Modifier.weight(1f))
                Switch(
                    checked = monitoring,
                    onCheckedChange = { on ->
                        monitoring = on
                        if (on) onStart(threshold) else onStop()
                    }
                )
            }

            Button(onClick = onTest, modifier = Modifier.fillMaxWidth()) {
                Text("テスト通知・音声を再生")
            }

            OutlinedButton(onClick = { showAbout = true }, modifier = Modifier.fillMaxWidth()) {
                Text("このアプリについて")
            }

            Text(
                text = "指定した%に達すると、通知＋音声でお知らせします（上がっても下がっても）。",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("このアプリについて") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("バッテリー通知 v1.0.1")
                    Text(
                        "バッテリー残量が指定した%に達すると、音声でお知らせするアプリです。",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(12.dp))
                    Text("オープンソースライセンス", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "本アプリは以下のオープンソースソフトウェアを使用しています。",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(8.dp))
                    Text("・AndroidX — Apache License 2.0")
                    Text("・Jetpack Compose — Apache License 2.0")
                    Text("・Kotlin — Apache License 2.0")

                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Apache License 2.0 の全文:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        "https://www.apache.org/licenses/LICENSE-2.0",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(12.dp))
                    Text(
                        "本アプリ自体は MIT License で公開しています。",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("閉じる") }
            }
        )
    }
}
