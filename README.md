# HyGrip
HyGrip is a small mod for Hytale that adds a basic robotic arm to the game. It's designed to make moving objects easier, especially when working on large structures. This is the first step in a larger project called HyFactory, but for now, it focuses on a single function: gripping, lifting, and placing.

## How to test the mod

### 1. Unit tests (no game required)
Run the tests locally to check crane state logic and phases:

```bash
./gradlew test
```

This runs JUnit 5 tests for `CranePhase` and `CraneStateComponent` (e.g. `setJob`, `isArmAt`). Use this after code changes to avoid regressions.

### 2. Dev server (in-game)
1. First time: `./gradlew setupServer`
2. Start the server: `./gradlew runServer` (or `devServer` if configured)
3. Connect with the Hytale client to `localhost` (or the address shown in the server window).

If you get "Server authentication unavailable", the client cannot reach Hytale’s session service (e.g. network, VPN, or account). You can try enabling **offline mode** in the dev server so it does not require auth: in `settings.gradle.kts` add inside `hytale { }`:

```kotlin
devserver {
    OfflineMode = true
}
```

Then run `./gradlew setupServer` again so the server config is regenerated.

### 3. Manual checklist (once connected)
- Run `/hygrip test` to spawn a test crane (base at 0,0,0; source 1,0,0; target -1,0,0).
- Place blocks with inventories at source and target.
- Confirm the arm moves and items transfer between them.

### 4. Build and test elsewhere
- Build the mod: `./gradlew build` (JAR under `build/libs/`).
- Copy the JAR to another server or mod folder and test with a full game client there.
