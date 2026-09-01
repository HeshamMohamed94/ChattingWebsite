# ChattingWebsite

ChattingWebsite is the Kotlin/JS IR browser client for the Chatting platform. It ships registration, login, conversation-list, new-chat, and message-thread pages backed by the same shared KMP stores used by the Android and iOS clients.

## Prerequisites

- Eclipse Temurin JDK 21
- Node.js 24
- The `com.hesham.chatting:shared:0.1.0-SNAPSHOT` Kotlin Multiplatform artifact published to Maven Local for builds that resolve the shared SDK

Use the repository's Gradle wrapper for every build and run command. No global Gradle installation is required.

## Configuration

Create the local environment file from the committed template:

```powershell
Copy-Item .env.example .env
```

The template configures `API_BASE_URL` for the Ktor API and `WS_URL` for the real-time gateway. `.env` and `.env.local` are gitignored; never commit secrets.

By default, the website resolves `com.hesham.chatting:shared:0.1.0-SNAPSHOT` from Maven Local. Publish it from the sibling `ChattingMobileKMP` repository when needed:

```powershell
cd D:\ChattingProject\ChattingMobileKMP
.\gradlew :shared:publishToMavenLocal
```

For live shared-module development, create a gitignored `local.properties` in this repository:

```properties
sharedProjectPath=../ChattingMobileKMP
```

When that property is present, Gradle includes the shared repository as a composite build instead of requiring a republish after every change.

## Run and build

From `D:\ChattingProject\ChattingWebsite`:

```powershell
.\gradlew :webapp:jsBrowserDevelopmentRun --continuous
.\gradlew :webapp:jsTest
.\gradlew :webapp:jsBrowserDistribution
```

`jsTest` runs the full browser suite. For a filtered suite, use the concrete browser task, for example:

```powershell
.\gradlew :webapp:jsBrowserTest --tests "*RouterGuardTest*"
```

The production static bundle is written under `webapp/build/dist/js/productionExecutable/`.

## Ports

- Website webpack development server: `127.0.0.1:3000`
- Ktor backend consumed by the website: `127.0.0.1:8080`
- WebSocket gateway consumed by the website: `127.0.0.1:8081`

## Related repositories

- `ChattingBackEnd` owns the authoritative Ktor API, MongoDB persistence, and Node.js real-time gateway.
- `ChattingMobileKMP` owns the shared Kotlin Multiplatform SDK used by this website and the Android/iOS clients.
- `ChattingWebsite` is a consumer of that shared SDK and owns browser-specific DOM bindings and web assets.

See the [Architecture and Implementation Plan](https://github.com/HeshamMohamed94/ChattingBackEnd/blob/main/docs/ARCHITECTURE_AND_IMPLEMENTATION_PLAN.md) for the cross-repository design and milestone sequence.
