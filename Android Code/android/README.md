# Saraswati Institute — Android App

Native Android app (Java) for Saraswati Institute coaching, built on the JDS 3.1 design language with a warm saffron accent.

## Project info

| Setting | Value |
|---|---|
| Language | Java |
| Min SDK | 24 (Android 7.0 Nougat) |
| Target SDK | 34 (Android 14) |
| Compile SDK | 34 |
| Build system | Gradle 8.2, AGP 8.2.0 |
| UI libraries | AndroidX, Material Components 1.11, ConstraintLayout 2.1, RecyclerView 1.3 |
| Architecture | Activities + Fragments (MVC). Add ViewModel/LiveData when wiring real data. |
| Package | `com.saraswati.institute` |

## How to open

1. Open Android Studio (Hedgehog or newer).
2. **File → Open** → select the `android/` folder.
3. Let Gradle sync. First sync downloads dependencies (~2 min).
4. Run on emulator (Pixel 6, API 33+ recommended) or physical device.

## What's implemented

Three fully-built screens demonstrate the patterns your team should replicate for the rest:

| Screen | Files | Demonstrates |
|---|---|---|
| **Login + OTP** | `LoginActivity.java`, `OtpActivity.java`, `activity_login.xml`, `activity_otp.xml` | Phone input, OTP entry with 6 boxes, Material text fields, primary button, country-code prefix |
| **Home (Variant A — Card-heavy)** | `HomeActivity.java`, `activity_home.xml`, `fragment_home.xml`, `LiveClassAdapter.java`, `SubjectAdapter.java` | BottomNavigationView, RecyclerView (horizontal + grid), CardView, custom ViewHolders, click handling |
| **Test attempt** | `TestAttemptActivity.java`, `activity_test_attempt.xml`, `OptionAdapter.java` | Timer (CountDownTimer), question pager, MCQ option selection, palette grid, submit dialog |

## Resources

All design tokens are codified in `app/src/main/res/values/`:

- **`colors.xml`** — JDS palette (Reliance Blue, saffron accent, surfaces, semantic).
- **`dimens.xml`** — Spacing scale (4/8/12/16/24/32/48), component heights, radii.
- **`strings.xml`** — All UI copy. English-only (per brief).
- **`styles.xml`** — Text appearances (Display, Headline, Title, Label, Body) + button styles.
- **`themes.xml`** — Material 3 theme overlay binding to JDS colors.
- **`font/jiotype_var.ttf`** — JioType variable font (axis 400-900). Fallback: Roboto.

## Remaining screens — build guide

Each remaining screen from the HTML mockup, with notes on how your team should implement:

### Course detail
- **Layout:** `CoordinatorLayout` + `CollapsingToolbarLayout` for the hero, `RecyclerView` for chapter list.
- **Adapter:** `ChapterAdapter` with expand/collapse rows (use `ExpandableLayout` or `RecyclerView` with view types).
- **Data:** `Course`, `Chapter`, `Lesson` POJOs.

### Video lecture player
- **Player:** ExoPlayer 2.19+ (`androidx.media3:media3-exoplayer`).
- **Layout:** `PlayerView` full-bleed + custom controller overlay (XML in `layout/player_controls.xml`).
- **Activity:** Set `requestedOrientation` to landscape on rotate; handle `onConfigurationChanged`.
- **Notes/transcript:** `BottomSheetBehavior` on a `NestedScrollView`.

### Live class player
- Same as video player + add chat panel (RecyclerView with `ChatMessage` items, polling/WebSocket for live updates).
- "Raise hand" toggle button posts to backend.

### Test results & analytics
- **Charts:** MPAndroidChart library (`com.github.PhilJay:MPAndroidChart:v3.1.0`).
- **Layout:** ScrollView with overall score card + per-subject breakdown + question review list.

### Doubt chat
- **List:** RecyclerView with two view types (user message, AI/teacher message).
- **Input:** Bottom-anchored EditText + send button. `adjustResize` in manifest.
- **Subject pills:** Horizontal RecyclerView at top.

### Schedule / calendar
- **Calendar:** Use `MaterialCalendarView` (com.prolificinteractive) or system DatePicker for date selection.
- **Day view:** RecyclerView of timed events. Color-code by subject.

### Fee payment
- ScrollView with payment summary card + payment method radio group.
- Integrate Razorpay/PhonePe SDK for actual payment (out of scope for this scaffold).
- Receipt screen: simple ScrollView with detail rows.

### Notifications inbox
- RecyclerView with `Notification` items. Group by date with sticky headers (use `RecyclerView.ItemDecoration`).
- Swipe-to-dismiss: `ItemTouchHelper`.

### Profile / settings
- `PreferenceFragmentCompat` for the settings list.
- Profile header: custom view at top of fragment.

### Parent dashboard (separate role)
- New activity `ParentHomeActivity` with its own bottom nav (Home / Reports / Fees / More).
- Shares fragments with student app where data overlaps (attendance, fee status).

### Teacher dashboard (separate role)
- New activity `TeacherHomeActivity`.
- Today's classes list, attendance marker (RecyclerView with checkboxes), assignment grader.

## Theming notes

- **Primary** is Reliance Blue `#1646BF` — use for CTAs, top app bar, links, active nav.
- **Secondary (accent)** is saffron `#E89A3C` — use for hero/promo cards, streaks, highlights, "live now" pulse. **One accent area per screen** (JDS rule).
- All cards: `cornerRadius = 16dp`, `elevation = 2dp`, no border + shadow combo.
- Buttons: 48dp default height, 8dp corner radius. Min touch target 44dp.
- All screens have a status-bar color matching the top app bar (set in `themes.xml`).

## Authentication API

The app authenticates against the Spring Boot **auth-service** (MongoDB flavour). Swagger UI:
`https://3.108.167.166:8443/swagger-ui/index.html`.

| Setting | Value |
|---|---|
| Base URL | `https://3.108.167.166:8443/` (`network/ApiConfig.java`) |
| Public endpoints | `/api/v1/auth/**` |
| Authenticated endpoints | `/api/v1/users/me**`, bearer access token |
| Access token | JWT, HS256, 15 min |
| Refresh token | opaque, 7 days, **rotating** with reuse detection |
| Response shape | the DTO at the top level — there is no `success`/`data` envelope |
| Error shape | `{timestamp, status, error, message, path, fieldErrors}` |

### Endpoints bound in `AuthApiService`

| Method | Path | Body / Query | Returns |
|---|---|---|---|
| POST | `/api/v1/auth/register` | `AuthRequests.Register` | `RegistrationResponse` (201, **no tokens**) |
| POST | `/api/v1/auth/login` | `AuthRequests.Login` (`usernameOrEmail`) | `TokenResponse` |
| POST | `/api/v1/auth/refresh-token` | `AuthRequests.RefreshToken` | `TokenResponse` |
| POST | `/api/v1/auth/logout` | `AuthRequests.RefreshToken` | `MessageResponse` |
| POST | `/api/v1/auth/forgot-password` | `AuthRequests.Email` | `MessageResponse` (always 200) |
| POST | `/api/v1/auth/reset-password` | `AuthRequests.ResetPassword` | `MessageResponse` |
| GET | `/api/v1/auth/verify-email` | `?token=` | `MessageResponse` |
| POST | `/api/v1/auth/resend-verification` | `AuthRequests.Email` | `MessageResponse` |
| POST | `/api/v1/auth/validate-token` | `AuthRequests.ValidateToken` | `TokenValidationResponse` |
| GET | `/api/v1/users/me` | — | `UserResponse` |
| PUT | `/api/v1/users/me` | `AuthRequests.UpdateProfile` | `UserResponse` |
| POST | `/api/v1/users/me/change-password` | `AuthRequests.ChangePassword` | `MessageResponse` |
| POST | `/api/v1/users/me/logout-all` | — | `MessageResponse` |

### Flows

- **Sign in** — `LoginActivity` posts `usernameOrEmail` + `password`. 401 is shown on the password
  field, 423 (locked after 5 failed attempts) and 403 (disabled) as a dialog; a 403 about
  verification offers to resend the link.
- **Register** — `RegistrationActivity` creates the account, then signs in with the same
  credentials, because `register` returns a profile rather than tokens. If the service is run with
  `require-email-verification=true`, the sign-in answers 403 and the student is asked to confirm
  the address first.
- **Session renewal** — `TokenAuthenticator` exchanges the refresh token on a 401 and replays the
  request. Refresh tokens rotate, so the exchange is serialised and the new token is stored before
  the retry; if the exchange is rejected the session is cleared and `SaraswatiApp` returns to the
  login screen.
- **Password reset** — `forgot-password` emails
  `<app.frontend-url>/reset-password?token=…`; `ResetPasswordActivity` is registered for that link
  and also accepts a pasted code.
- **Start-up** — `SplashActivity` calls `users/me` once to confirm the session still stands. A
  network failure opens the app on the cached profile instead of signing the user out.

Tokens and the cached profile live in `SessionManager`, in an `EncryptedSharedPreferences` file
(falling back to a private `SharedPreferences` file if the keystore is unavailable).

### Transport security

`network/TlsConfig.java` offers **TLS 1.3 only, with TLS 1.2 as the floor** — there is no TLS 2.0;
1.3 (RFC 8446) is the current top of the protocol line. TLS 1.2 is limited to ECDHE + AEAD cipher
suites, so every session has forward secrecy. Conscrypt is installed as the first security provider
in `SaraswatiApp` because the platform cannot negotiate TLS 1.3 below API 29. Cleartext traffic is
disabled app-wide in `res/xml/network_security_config.xml`.

The deployment serves a **self-signed** certificate (`CN=localhost`, SAN `localhost`/`127.0.0.1`),
so:

- `res/raw/auth_service.pem` is trusted *in addition to* the system CAs — not instead of them, and
  never a trust-all manager. A later move to a CA-issued certificate needs no code change.
- Because the certificate carries no SAN for the server's IP address, the hostname verifier accepts
  `ApiConfig.HOST` **only** when the peer presents one of the pinned certificates. Every other host
  is verified strictly.

When the server's keystore is regenerated, refresh the bundled copy:

```bash
tools/fetch-server-cert.sh            # or: tools/fetch-server-cert.sh <host> <port>
```

### Contract test

`app/src/test/java/.../network/AuthContractTest.java` asserts the models against the exact JSON the
service emits — it was generated by serialising the service's own DTOs with Jackson configured the
way Spring Boot configures it, and the request bodies were fed back through the service's
`@JsonCreator` constructors. Run it with:

```bash
gradle :app:testDebugUnitTest
```

## Architecture next steps

The scaffold is intentionally simple (Activities + RecyclerView adapters). When you wire real data:

1. ~~Add **Retrofit 2** + **OkHttp** for API calls.~~ Done — see **Authentication API** above.
2. Add **Room** for offline cache (lessons, downloaded videos).
3. Add **Hilt** for DI.
4. Move screen logic into **ViewModels** with **LiveData**/**Flow**.
5. Use **Navigation Component** with a single-Activity architecture (replaces multiple Activities with Fragments + nav graph). Recommended for v2.

## Mock data

All RecyclerView adapters in this scaffold are populated from hardcoded lists in the Activity (see `HomeActivity.loadMockData()`). Replace with API calls.
