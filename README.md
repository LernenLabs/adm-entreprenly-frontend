# Entreprenly – Android app

Native Android app (Kotlin + Jetpack Compose) for Entreprenly, organized with Domain-Driven Design.
Backend: `adm-entreprenly-backend` (Spring Boot, REST, JWT).

## Package structure

One package per bounded context under `online.entreprenly.entreprenlyapp`:

```
<context>/                     iam | profile | subscription | inventory | sales | chatbot
├── domain/                    model (aggregates, entities, valueobjects, commands, queries, events), repositories (ports), services
├── application/               commandservices, queryservices (interfaces) + internal/ (implementations, outbound services, ACL)
├── infrastructure/            remote/{api, repositories, assemblers}, local/ (persistence), acl/
└── interfaces/                rest/resources (DTOs), ui/{screens, components, viewmodels}
shared/                        Result/ApplicationError, Retrofit config, DI container, navigation, theme, common UI
```

`iam/` is the reference implementation: copy its layering for a new context.

## Conventions

- **Language:** code, comments, commits and the default UI language are English. Spanish lives in `res/values-es/strings.xml`.
- **No hardcoded UI text:** use `stringResource(R.string.…)`. ViewModels emit `UiText` (`shared/interfaces/ui`).
- **Look and feel:** use `MaterialTheme` and the shared components in `shared/interfaces/ui/components` (`TextInputField`, `EmailField`, `PasswordField`, `FormSubmit`). Brand colors are in `shared/interfaces/ui/theme`.
- **Wiring:** register services in `shared/infrastructure/di/AppContainer.kt` and screens in `shared/interfaces/navigation/AppNavigation.kt`.
- **Errors:** repositories return `Result<T>`; wrap Retrofit calls with `safeApiCall` / `safeApiCallWithBody`.
- **Git:** GitFlow (`feature/<context>` → `develop` → `main`), Conventional Commits in English. No AI authorship lines (see `CONTRIBUTING.md`).

## Running

1. Start the backend (port 8092) – see its README.
2. Both debug and release builds point to the deployed backend (`https://adm-entreprenly-backend.onrender.com/`). To use a local backend from the emulator, change `API_BASE_URL` in `app/build.gradle.kts` to `http://10.0.2.2:8092/`. The free Render instance sleeps when idle: the first request can take ~1 minute.
3. Run the `app` configuration from Android Studio.

## Subscription

The mobile subscription flow and backend integration details are documented in [docs/subscription.md](docs/subscription.md).
