# Kotlin Music App (PN)

A modern Android music player built with Kotlin. This README is organized, detailed, and includes responsive screenshot placeholders (using your original image URLs) sized to fit the README cleanly. Replace the placeholders with updated screenshots when available.

- Language: Kotlin (100%)
- Platform: Android
- Status: Example / Template

---

Table of Contents
- About
- Key Features
- Screenshots & UI Details
- Project Structure
- Installation & Quick Start
- Usage & Notes
- Permissions & Manifest
- How to update screenshots
- Contributing
- License
- Contact

About
-----
Kotlin Music App (PN) is a compact reference implementation demonstrating standard patterns for an Android music player: background playback, MediaSession integration, playlist and library management, and common authentication screens. This README focuses on clarity, visual presentation, and practical guidance for contributors.

Key Features
------------
- Play local audio and streamed tracks
- Background playback with a foreground service
- MediaSession + notification + lock-screen controls
- Playlists, favorites, library browsing
- Search, discover, and contextual actions (add to playlist, download)
- Account flows: Sign In / Sign Up / Profile edit
- Clean architecture: MVVM + Repository; coroutines for async work
- Optional: ExoPlayer for playback engine and Room for persistence

Screenshots & UI Details
------------------------
Visual guidelines (recommended)
- Use PNG or high-quality JPG in portrait (mobile) aspect ratio.
- Keep safe area: place important UI inside a centered area; avoid status/navigation overlays.
- Export at device width ~1080px and scale down in README with max-width for consistent rendering.
- Filenames: use descriptive names (home.png, search.png, library.png, player.png, sign_in.png, sign_up.png, account.png, account_edit.png).
- Place files in /screenshots/ and update src paths if you prefer local images. Below we keep your original user-attachments URLs and use responsive styling with a smaller max-width (300px) for a cleaner look.

Home
- Description: Featured playlists, editorial banners, recommended tracks, and a persistent mini-player at the bottom showing artwork, title, progress, and play/pause/next controls.
- Screenshot (responsive placeholder; replace with your image if desired):
  <img src="https://github.com/user-attachments/assets/293c9a10-fef2-4eb3-86f7-f16513e7d920" alt="Home layout" style="max-width:300px; width:100%; height:auto; border-radius:8px; object-fit:cover; box-shadow:0 6px 18px rgba(0,0,0,0.10);" />

Search
- Description: Search bar at the top, recent queries, intelligent suggestions, and grouped results (Songs, Albums, Artists). Quick actions include play, add to queue, or add to playlist.
- Screenshot:
  <img src="https://github.com/user-attachments/assets/5ecaf11b-3b00-4110-9ea4-bd0ff383ad5e" alt="Search layout" style="max-width:300px; width:100%; height:auto; border-radius:8px; object-fit:cover; box-shadow:0 6px 18px rgba(0,0,0,0.10);" />
- Search using speech recognization by utilizing Google Services.
 <img src="https://github.com/user-attachments/assets/feb3a292-4d91-4360-aee1-ef547e203c92" alt="Search Speech Recognization" style="max-width:300px; width:100%; height:auto; border-radius:8px; object-fit:cover; box-shadow:0 6px 18px rgba(0,0,0,0.10);" />


Library
- Description: The user's library — playlists, downloaded songs, albums, and artists with quick filters and sorting options.
- Screenshot (library):
  <img src="https://github.com/user-attachments/assets/704104f7-0771-4af7-b3f8-32e405230514" alt="Library layout" style="max-width:300px; width:100%; height:auto; border-radius:8px; object-fit:cover; box-shadow:0 6px 18px rgba(0,0,0,0.10);" />

Playlist Detail
- Description: Playlist header with title/cover, Play / Shuffle buttons, and full track list with per-track context menus.
- Screenshot:
  <img src="https://github.com/user-attachments/assets/abd46a5b-dec8-448c-89ac-a5c39c56301c" alt="Playlist detail" style="max-width:300px; width:100%; height:auto; border-radius:8px; object-fit:cover; box-shadow:0 6px 18px rgba(0,0,0,0.10);" />

Favorites
- Description: Favorite tracks/albums view with quick play and manage actions.
- Screenshot:
  <img src="https://github.com/user-attachments/assets/d12a8a17-5f04-468e-ad33-77483ed7e7c0" alt="Favorites layout" style="max-width:300px; width:100%; height:auto; border-radius:8px; object-fit:cover; box-shadow:0 6px 18px rgba(0,0,0,0.10);" />

Account
- Description: User profile, subscription status, linked devices, and account actions (settings, payment, logout).
- Screenshot (profile):
  <img src="https://github.com/user-attachments/assets/3e152ec2-845d-4ccc-8410-8eab166414bd" alt="Account layout" style="max-width:300px; width:100%; height:auto; border-radius:8px; object-fit:cover; box-shadow:0 6px 18px rgba(0,0,0,0.10);" />

Account Edit
- Description: Edit display name, change avatar, and manage preferences.
- Screenshot:
  <img src="https://github.com/user-attachments/assets/0d49452f-166c-4208-a6e9-1b0ee8bd7835" alt="Account edit" style="max-width:300px; width:100%; height:auto; border-radius:8px; object-fit:cover; box-shadow:0 6px 18px rgba(0,0,0,0.10);" />

Sign In
- Description: Authentication screen with email/password fields, social login options, and clear validation/error states.
- Screenshot:
  <img src="https://github.com/user-attachments/assets/0daac130-fdf6-4715-b373-9ac959954826" alt="Sign in layout" style="max-width:300px; width:100%; height:auto; border-radius:8px; object-fit:cover; box-shadow:0 6px 18px rgba(0,0,0,0.10);" />

Sign Up
- Description: Registration screen with name, email, password (with strength indicator), and marketing consent toggle.
- Screenshot:
  <img src="https://github.com/user-attachments/assets/6486a003-00f8-4577-8221-0c8d7378d196" alt="Sign up layout" style="max-width:300px; width:100%; height:auto; border-radius:8px; object-fit:cover; box-shadow:0 6px 18px rgba(0,0,0,0.10);" />

Now Playing (Player)
- Description: Full-screen player with large album art, seek bar, playback controls (prev/play-pause/next), shuffle, repeat, queue, and contextual actions.
- Screenshot (player):
  <img src="https://github.com/user-attachments/assets/8d385816-3373-4354-b2d0-672a0a51dc74" alt="Now Playing" style="max-width:300px; width:100%; height:auto; border-radius:8px; object-fit:cover; box-shadow:0 6px 18px rgba(0,0,0,0.10);" />

Lyrics
- Description: Synchronized lyrics view with highlighted current line and manual scroll support.
- Screenshot:
  <img src="https://github.com/user-attachments/assets/47e368fe-06cf-4e29-bd72-9eae4827bd54" alt="Showing lyrics" style="max-width:300px; width:100%; height:auto; border-radius:8px; object-fit:cover; box-shadow:0 6px 18px rgba(0,0,0,0.10);" />

Comments / Track Interaction
- Description: Per-track comments and community notes, with add/edit/delete actions (if backend enabled).
- Screenshot:
  <img src="https://github.com/user-attachments/assets/f72fe50f-de8b-49e2-8da7-ea361b5eac65" alt="Comments on track" style="max-width:300px; width:100%; height:auto; border-radius:8px; object-fit:cover; box-shadow:0 6px 18px rgba(0,0,0,0.10);" />

Project Structure
-----------------
Keep this as a code block to avoid Markdown parsing issues:

```
.
├─ .github/
├─ app/
│  ├─ build.gradle.kts
│  ├─ src/
│  │  ├─ main/
│  │  │  ├─ AndroidManifest.xml
│  │  │  ├─ java/com/yourorg/musicapp/
│  │  │  │  ├─ ui/                (Activities & Fragments: Home, Search, Library, Account, Auth)
│  │  │  │  ├─ media/             (PlayerService, MediaSessionManager)
│  │  │  │  ├─ data/              (Repositories, Room entities, DAOs)
│  │  │  │  ├─ domain/            (Use-cases / business logic)
│  │  │  │  ├─ di/                (Hilt modules - optional)
│  │  │  │  └─ util/
│  │  │  └─ res/
├─ README.md
└─ screenshots/                    (Add your screenshots here)
```

Installation & Quick Start
--------------------------
Prerequisites
- Android Studio (Arctic Fox or later)
- JDK 11+
- Android SDK matching project's compile/target API

Quick setup
1. git clone https://github.com/minhnghi123/kotlin-music-app-PN.git
2. Open the project in Android Studio and let Gradle sync.
3. Add or update screenshots in /screenshots/ or keep using the user-attachment links.
4. Run on a device/emulator.

Permissions & Manifest
----------------------
Ensure runtime permissions and service declarations are present:

```xml
<!-- For older Android versions -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<!-- Android 13+ -->
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />

<!-- Example service declaration -->
<service
    android:name=".media.PlayerService"
    android:exported="false"
    android:foregroundServiceType="mediaPlayback">
    <intent-filter>
        <action android:name="android.media.browse.MediaBrowserService" />
    </intent-filter>
</service>
```

How to update screenshots (recommended workflow)
- Export screenshots from device/emulator at high resolution (e.g., 1080x2400).
- Save to /screenshots/ with suggested filenames.
- Update README image src to relative paths (e.g., `screenshots/home.png`) for fast local rendering:
  ```md
  <img src="screenshots/home.png" alt="Home" style="max-width:300px; width:100%; height:auto;" />
  ```
- Commit images and README in one PR to keep history consistent.

Contributing
------------
Contributions are welcome. Suggested workflow:
1. Fork the repo
2. Create a branch: git checkout -b feat/your-feature
3. Make focused commits with clear messages
4. Add/update screenshots where UI changes are made
5. Open a PR describing your change and include screenshots in the PR description

License
-------
Add a LICENSE file to the repository (MIT recommended) if you want the project to be reusable.

Contact
-------
Repo owner: @minhnghi123

Notes
-----
- I reduced the display size of screenshots to max-width:300px for a cleaner README layout.
- If you prefer another size (e.g., 260px or 320px), tell me and I’ll update the file.
- I can open a PR that updates README.md, and optionally add a /screenshots/ folder with resized images — tell me which action you want next.
