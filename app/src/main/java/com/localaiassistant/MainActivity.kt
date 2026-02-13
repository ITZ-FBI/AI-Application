package com.localaiassistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.localaiassistant.data.AppDatabase
import com.localaiassistant.domain.LocalAssistantEngine
import com.localaiassistant.domain.MemoryRepository
import com.localaiassistant.domain.PermissionDecision
import com.localaiassistant.domain.PermissionManager
import com.localaiassistant.domain.PermissionRequest
import com.localaiassistant.security.CryptoManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "assistant.db").build()
        val memoryRepository = MemoryRepository(db.chatMemoryDao(), CryptoManager(applicationContext))
        val permissionManager = PermissionManager(db.permissionLogDao())
        val assistantEngine = LocalAssistantEngine(memoryRepository)

        setContent {
            MaterialTheme {
                val context = LocalContext.current
                val messages = remember { mutableStateListOf<String>() }
                var input by remember { mutableStateOf("") }
                var persona by remember { mutableStateOf("friendly") }
                var safeMode by remember { mutableStateOf(true) }
                val scope = rememberCoroutineScope()

                val micPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    scope.launch {
                        val decision = if (granted) PermissionDecision.GRANTED else PermissionDecision.DENIED
                        permissionManager.requestPermission(
                            request = PermissionRequest(
                                resource = "MICROPHONE",
                                rationale = "Needed only to convert voice to local text; never leaves device."
                            ),
                            decision = decision
                        )
                        messages.add("MICROPHONE permission ${decision.name}")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Text("Safe Mode")
                        Switch(checked = safeMode, onCheckedChange = {
                            safeMode = it
                            permissionManager.enableSafeMode(it)
                            messages.add(if (it) "Safe mode ON: actions blocked" else "Safe mode OFF")
                        })
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = {
                            messages.add("Permission request: MICROPHONE. Reason: local voice input.")
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (granted) {
                                scope.launch {
                                    permissionManager.requestPermission(
                                        PermissionRequest("MICROPHONE", "Needed only for local voice input."),
                                        PermissionDecision.GRANTED
                                    )
                                    messages.add("MICROPHONE permission GRANTED")
                                }
                            } else {
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }) { Text("Request Mic") }

                        Button(onClick = {
                            permissionManager.revokePermission("MICROPHONE")
                            messages.add("MICROPHONE permission REVOKED")
                        }) { Text("Revoke Mic") }
                    }

                    OutlinedTextField(
                        value = persona,
                        onValueChange = { persona = it },
                        label = { Text("Persona") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(messages) { msg -> Text(msg) }
                    }

                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        label = { Text("Ask locally") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = {
                        if (input.isBlank()) return@Button
                        val prompt = input
                        messages.add("You: $prompt")
                        input = ""
                        scope.launch {
                            val reply = assistantEngine.respond(prompt, persona)
                            messages.add("Assistant: $reply")
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("Send")
                    }
                }
            }
        }
    }
}
