package com.localaiassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.unit.dp
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
                val messages = remember { mutableStateListOf<String>() }
                var input by remember { mutableStateOf("") }
                var persona by remember { mutableStateOf("friendly") }
                var safeMode by remember { mutableStateOf(true) }
                val scope = rememberCoroutineScope()

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
                        })
                        Button(onClick = {
                            scope.launch {
                                permissionManager.requestPermission(
                                    PermissionRequest(
                                        resource = "MICROPHONE",
                                        rationale = "Needed only to convert your voice into local text input."
                                    ),
                                    PermissionDecision.GRANTED
                                )
                                messages.add("Permission GRANTED for MICROPHONE")
                            }
                        }) { Text("Grant Mic") }
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
