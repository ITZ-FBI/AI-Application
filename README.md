# Local-Only Mobile AI Assistant (Android)

This repository contains a privacy-first Android app that runs fully on-device, stores memory encrypted locally, and enforces explicit user consent before any resource access or action.

## Implemented Features

- Offline-first local assistant chat app (Jetpack Compose).
- Encrypted local memory storage (Android Keystore + AES/GCM).
- Permission-aware policy with safe mode, grant/deny logging, and revocation.
- No automatic protected actions unless permission is explicitly granted.
- Persona customization with context-aware responses based on prior encrypted chat memory.

## Architecture Blueprint

1. **UI Layer** (`MainActivity`)
   - Safe mode toggle (default ON)
   - Persona input
   - Explicit microphone permission request + revoke controls
   - Chat timeline and local response generation

2. **Policy Layer**
   - `PermissionManager`: tracks active grants, enforces safe mode, logs permission decisions.
   - `ActionGate`: blocks protected actions when explicit access is not currently granted.

3. **Assistant Layer**
   - `LocalAssistantEngine`: stores every turn, retrieves recent context, returns reasoning-aware response.

4. **Storage Layer** (Room)
   - `chat_memory`: encrypted text + metadata + timestamp + vector.
   - `permission_log`: resource + rationale + decision + timestamp.
   - `preferences`: user profile preferences.

5. **Security Layer**
   - `CryptoManager`: Android Keystore-backed encryption/decryption.
   - `allowBackup=false` in manifest to reduce extraction risk.

## Mobile Model Recommendations

For production-quality local inference, integrate one quantized model:
- Llama 3.2 1B/3B Instruct (4-bit GGUF)
- Qwen2.5 1.5B Instruct (4-bit)
- Phi-3.5 mini instruct (quantized)

Recommended runtimes:
- `llama.cpp` Android bindings (CPU/NNAPI)
- MediaPipe LLM Inference

## Incremental Learning Plan

- Store user-approved memory snippets locally (encrypted).
- Build/update adapter weights (LoRA/QLoRA) on-device while charging + idle.
- Keep base model immutable; rotate encrypted adapter checkpoints.
- Expose user controls for pause/resume/wipe.

## Permission and Safety Rules

- Explain why access is needed **before** requesting permission.
- Always require explicit user approval for sensitive resource access.
- Permit revocation anytime; stop usage immediately on revoke.
- Keep append-only permission logs with timestamps.
- Safe mode blocks actions until explicitly allowed.

## Build APK and Install

### Prerequisites
- JDK 17-21 recommended (JDK 25 can break older Gradle/Kotlin tooling)
- Android SDK installed (`ANDROID_HOME` set)
- Gradle 8+

### Build
```bash
gradle assembleDebug
```

Output APK:
- `app/build/outputs/apk/debug/app-debug.apk`

### Build + install helper
```bash
./scripts/build_and_install_debug.sh
```

## Notes

- This project is local-only by design; no network inference is used.
- Expand OS integrations through runtime permissions + `PermissionManager` + `ActionGate` gates.
