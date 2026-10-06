# Coding Challenge: Ressourcen-Buchung

Ein lauffähiges Spring-Boot-/React-Projekt als Ausgangspunkt für unsere Programmieraufgabe. Backend
und Frontend werden zu **einem** Jar gebaut und laufen in **einem** Container.

👉 **Die Aufgabenstellung steht in [AUFGABE.md](AUFGABE.md) — lies die zuerst.**

## Was schon drin ist

| Bereich       | Stand                                                                        |
| ------------- | ---------------------------------------------------------------------------- |
| **Backend**   | Spring Boot 3.5 auf Java 21 — Spring Web, Spring Data JPA, Bean Validation    |
| **Datenbank** | H2 in-memory, per `data.sql` mit Beispiel-Ressourcen gefüllt                  |
| **Frontend**  | React 19 + TypeScript, MUI 7, Vite 7                                         |
| **Build**     | Gradle (Groovy DSL) — baut das Frontend mit und packt es in das Jar           |
| **Tests**     | JUnit 5 + MockMvc im Backend, Vitest + Testing Library im Frontend            |
| **Container** | Multi-Stage-`Dockerfile` plus `compose.yaml`                                  |
| **CI**        | GitHub-Actions-Workflow in `.github/workflows/build.yml`                      |

Als Wegweiser ist ein **Beispiel-Feature** komplett durchgezogen: die Anzeige der buchbaren
Ressourcen — Entity, Repository, Controller, Fehlerbehandlung, API-Client, MUI-Tabelle und Tests auf
beiden Seiten. Du kannst dich daran orientieren, musst es aber nicht: siehe
[AUFGABE.md](AUFGABE.md).

## Voraussetzungen

- **JDK 21 oder neuer.** Der Build kompiliert gegen Java 21 und lädt sich notfalls selbst ein
  passendes JDK herunter.
- **Docker** (oder Podman) für den Container-Teil.
- **Node.js musst du nicht installieren** — der Gradle-Build lädt sich Node selbst. Für den
  Dev-Loop unten ist ein lokales Node 22+ trotzdem bequemer.
- Netzwerkzugang beim ersten Build (Gradle-Distribution, Maven Central, npm-Registry). Der erste
  Durchlauf dauert deshalb ein paar Minuten, danach ist alles im Cache.

## Schnellstart

```bash
docker compose up --build
```

→ http://localhost:8080

Ohne Container, direkt als Jar:

```bash
./gradlew bootJar
java -jar build/libs/booking-challenge.jar
```

→ ebenfalls http://localhost:8080

## Dev-Loop

Für die tägliche Arbeit sind zwei Terminals angenehmer — dann musst du für eine
Frontend-Änderung nicht das Backend neu bauen:

```bash
# Terminal 1 — Backend auf Port 8080
./gradlew bootRun

# Terminal 2 — Vite-Dev-Server mit Hot Reload auf Port 5173
cd frontend && npm install && npm run dev
```

Arbeite dann auf **http://localhost:5173**. Der Vite-Dev-Server leitet alle Requests auf `/api` an
das Backend auf Port 8080 weiter (`server.proxy` in `frontend/vite.config.ts`), deshalb ist kein
CORS-Setup nötig. `./gradlew bootRun` baut das Frontend absichtlich **nicht** mit.

## Nützliche Befehle

| Befehl                             | Wirkung                                                      |
| ---------------------------------- | ------------------------------------------------------------ |
| `./gradlew build`                  | Alles: kompilieren, Backend-Tests, Frontend-Tests, Jar bauen  |
| `./gradlew test`                   | nur die Backend-Tests                                        |
| `./gradlew npmTestFrontend`        | nur die Frontend-Tests                                       |
| `./gradlew build -PskipFrontend`   | Build ohne npm — praktisch für reine Backend-Iterationen      |
| `./gradlew bootRun`                | Backend starten (ohne Frontend-Build)                        |
| `./gradlew bootJar`                | das vollständige Jar inklusive SPA                           |
| `npm run dev` (in `frontend/`)     | Vite-Dev-Server mit Hot Reload                                |
| `npm test` (in `frontend/`)        | Vitest im Watch-Modus                                        |
| `npm run lint` (in `frontend/`)    | ESLint — als Hilfe gedacht, nicht als Abgabekriterium         |

Die H2-Konsole liegt unter http://localhost:8080/h2-console (JDBC-URL `jdbc:h2:mem:booking`,
Benutzer `sa`, kein Passwort).

## Projektstruktur

```
├─ AUFGABE.md                      ← die Aufgabenstellung
├─ build.gradle                    Backend-Build + Frontend-Integration
├─ Dockerfile / compose.yaml       ein Image, ein Container
├─ src/main/java/de/example/booking/
│   ├─ BookingApplication.java
│   ├─ common/                     NotFoundException, ApiExceptionHandler (ProblemDetail)
│   ├─ config/SpaWebConfig.java    liefert die SPA aus und macht Deeplinks reload-fest
│   └─ resource/                   ← das Beispiel-Feature
├─ src/main/resources/
│   ├─ application.yaml
│   └─ data.sql                    die buchbaren Ressourcen
├─ src/test/java/…/ResourceControllerTest.java
└─ frontend/
    ├─ vite.config.ts              Dev-Proxy auf /api + Vitest-Konfiguration
    └─ src/
        ├─ App.tsx  main.tsx  theme.ts
        ├─ api/client.ts           fetch-Wrapper, liest ProblemDetail-Fehler aus
        └─ features/resources/     ← das Beispiel-Feature im Frontend
```

## Wie das Frontend ins Jar kommt

`./gradlew bootJar` ruft den npm-Build auf und kopiert `frontend/dist` als statische Ressourcen in
das Jar. Zur Laufzeit liefert Spring Boot die SPA aus, `SpaWebConfig` fällt für unbekannte Pfade auf
`index.html` zurück (damit ein Reload auf einem Deeplink funktioniert) — außer unter `/api`, dort
bleibt ein 404 ein 404.

Der Container macht dasselbe: das `Dockerfile` ruft im Build-Stage `./gradlew bootJar` auf. Damit
können `docker build` und der lokale Gradle-Build nicht auseinanderlaufen.

## Einreichung

1. Oben auf **„Use this template" → „Create a new repository"** klicken und als Sichtbarkeit
   **Private** wählen. (Alternativ: Repo als ZIP herunterladen und lokal `git init`.)
2. Die Aufgabe lösen — in nachvollziehbaren Commits, wir schauen uns die Historie an.
3. Wenn du fertig bist: **Settings ▸ Collaborators ▸ Add people** und
   `red-impag, rene.dietze@imp-ag.de` als Collaborator einladen.
4. Eine kurze Mail an rene.dietze@imp-ag.de mit dem Link zum Repository.

Bitte lade deine Lösung **nicht** in ein öffentliches Repository und öffne keinen Pull Request auf dieses Template, das wäre für alle anderen Bewerber einsehbar.

## Wenn etwas nicht läuft

- **Port 8080 ist belegt:** `./gradlew bootRun --args='--server.port=8081'`, oder im
  `compose.yaml` das Port-Mapping auf `"8081:8080"` ändern.
- **Der erste Build dauert lange:** Gradle, Maven Central und npm werden einmal komplett geladen.
- **Es hakt am Frontend:** `./gradlew build -PskipFrontend` prüft das Backend isoliert.
- **Node-Probleme im Gradle-Build:** `./gradlew clean` entfernt auch das heruntergeladene Node
  (es liegt unter `build/nodejs`).
- **Sonst:** melde dich einfach. Ein Setup-Problem soll dich keine Zeit von der eigentlichen
  Aufgabe kosten.


## Test coverage ###

npm --prefix frontend run test:coverage
./gradlew jacocoTestReport
