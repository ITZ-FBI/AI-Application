# Local-Only Mobile AI Assistant (Android)

This repository contains a privacy-first Android app that runs fully on-device, stores memory encrypted locally, and enforces explicit user consent before any resource access or action.

## What is implemented

- **Offline-first local assistant app** with chat UI (Jetpack Compose).
- **Encrypted memory storage** for chat content via AES-GCM and Android Keystore.
- **Permission-aware core** with safe mode, revocable permissions, and persistent permission logs.
- **No automatic actions**: all protected actions are blocked unless explicitly granted.
- **Personalization hook** via persona mode and memory retrieval from prior conversations.

## Architecture Blueprint

1. **Interaction Layer (UI)**
   - Compose UI in `MainActivity` with:
     - Safe mode toggle
     - Persona selector
     - Chat interface
     - Explicit permission grant actions

2. **Policy & Control Layer**
   - `PermissionManager`:
     - Safe mode default ON
     - Tracks granted/revoked resources
     - Logs grant/deny events with rationale + timestamps
   - `ActionGate`:
     - Hard-stop guard that blocks protected operations without permissions

3. **Assistant Layer**
   - `LocalAssistantEngine`:
     - Learns from conversation history (stores all turns)
     - Retrieves recent context for personalized replies
     - Includes response reasoning string

4. **Memory Layer**
   - Room database (`AppDatabase`) tables:
     - `chat_memory` (encrypted chat + metadata + timestamp + vector)
     - `permission_log` (resource/rationale/decision/timestamp)
     - `preferences` (persona/profile preferences)

5. **Security Layer**
   - `CryptoManager` uses Android Keystore + AES/GCM.
   - Chat messages are encrypted before persistence.
   - `allowBackup=false` to reduce untrusted extraction risk.

## Model Recommendations (mobile optimized)

For production local inference, integrate one of:

- **Llama 3.2 1B/3B Instruct (4-bit GGUF)** for balanced quality/performance.
- **Qwen2.5 1.5B Instruct (4-bit)** for low-RAM devices.
- **Phi-3.5 mini instruct (quantized)** for stronger reasoning in constrained hardware.

Use execution backends such as:
- `llama.cpp` Android bindings (CPU/NNAPI where available), or
- MediaPipe LLM Inference / vendor NPU runtimes.

## Incremental Learning Strategy (on-device)

Use parameter-efficient adaptation in periodic local jobs:
- Collect user-approved training snippets from encrypted local memory.
- Fine-tune adapters only (LoRA/QLoRA) on-device when charging + idle.
- Keep base model immutable; swap encrypted adapter checkpoints.
- Always allow pause/disable and memory wipe.

Current code includes a **lightweight embedding + retrieval mechanism** to keep the app fully functional without cloud dependencies.

## Permission & Safety Plan

- Every sensitive capability requires:
  1. rationale explanation,
  2. explicit user grant/deny,
  3. immediate revocation support,
  4. immutable permission log entry.
- Safe mode blocks all actions until user grants access.
- Optional hardening extension:
  - failed integrity check -> lock assistant & offer local wipe.

## OS Integration Guidelines

When expanding integrations (contacts/calendar/files/SMS/mic/GPS/camera):
- Use Android runtime permissions + least-privilege scopes.
- Route all calls through `PermissionManager` + `ActionGate`.
- Provide per-action confirmation dialog before execution.
- Never run background automation without explicit session opt-in.

## Build

```bash
./gradlew test
./gradlew assembleDebug
```

> Note: Building Android APK requires Android SDK in your environment.
