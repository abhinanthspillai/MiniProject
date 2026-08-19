# Netraze Emulator Working Model

This branch is the controlled emulator milestone for the Phase 1 Android application. It intentionally focuses on the application workflow that can be validated reliably in Android Studio Emulator before returning to physical-device Wi-Fi field validation.

## Scope

The emulator working model covers:

- Login and secure session restoration
- Administrator-authorized Create User
- Administrator-authorized password reset
- Home / Surveys / Locations / Account app shell
- Compact Survey Area selection
- Survey creation with Android-generated canonical UUID and Room-first persistence
- Unified Survey reopening
- Room-backed offline persistence and cached hierarchy fallback
- Emulator-safe Survey Workspace
- Backend connectivity through the Android Emulator host alias

Real Wi-Fi field scanning remains a physical-device concern. The emulator workspace must not fabricate Wi-Fi observations.

## Emulator backend address

The Android app detects an emulator and uses:

`http://10.0.2.2:8000/`

`10.0.2.2` is the Android Emulator alias for the development laptop.

The physical-device debug path remains separate and continues to use the existing localhost/ADB-reverse approach.

## Start the backend

From the repository root on Windows PowerShell:

```powershell
cd backend
python -m uvicorn app.main:app --host 127.0.0.1 --port 8000
```

Verify on the laptop:

`http://127.0.0.1:8000/health`

Then verify from the emulator browser:

`http://10.0.2.2:8000/health`

## Start the Android emulator

1. Open the project in Android Studio.
2. Open Device Manager.
3. Start a phone emulator using a supported Android image.
4. Select that emulator as the deployment target.
5. Run the app, or use:

```powershell
.\gradlew installDebug
```

## Required emulator walkthrough

1. Launch Netraze.
2. Verify Login, Create User, and Reset Password navigation.
3. Sign in with a valid account.
4. Verify the main shell: Home, Surveys, Locations, Account.
5. Tap Start Survey.
6. Select Project -> Building -> Floor -> Survey Area in the compact selector.
7. Enter a title and select a Survey mode.
8. For the current emulator slice, Location Survey Mode is the valid startable path because it requires no floor-plan or Simple Map artifact reference.
9. Create the Survey and verify it opens in Survey Workspace.
10. Return to Surveys and reopen the Survey directly.
11. Stop FastAPI and verify previously cached hierarchy and Room Surveys remain available where applicable.
12. Create a Location Survey while offline and verify it remains `pending` locally.
13. Force-stop and relaunch the app; verify Room data and valid auth session survive.
14. Logout and verify authenticated screens cannot be reopened with Back.

## Why Floor Plan and Simple Map creation are guarded in this emulator slice

The frozen PostgreSQL schema requires:

- `floor_plan` Survey -> non-null `floor_plan_id`
- `simple_map` Survey -> non-null `simple_map_id`
- `location_survey` -> both references null

The current Application API does not yet expose artifact-selection routes for Floor Plan / Simple Map setup in the emulator flow. The UI therefore keeps all three approved modes visible but prevents starting an invalid Floor Plan or Simple Map Survey without its required artifact. This preserves the frozen schema instead of inserting invalid/fake references.

## Hard boundary

This emulator milestone does not validate:

- surrounding Wi-Fi radio scans
- real RSSI/BSSID evidence
- Android scan throttling
- ScanAttempt/ScanCycle field behavior
- physical spatial sampling

Those remain physical-device validation tasks.
