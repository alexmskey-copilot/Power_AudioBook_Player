# Power AudioBook Player
Player for audiobooks with large icons and good sound

🏆 **Best Audiobook App in RuStore** (Russia's official app store)  
📱 Also available on **Huawei AppGallery** and **Xiaomi GetApps**

A lightweight, user-focused Android audiobook player built for seamless listening, battery efficiency, and offline access.
Chose Android’s built-in MediaPlayer for simplicity, stability, and minimal footprint — sufficient for audiobook playback needs.
Future roadmap includes migration to ExoPlayer for advanced features (e.g., gapless playback, custom decoders), but current implementation meets all user requirements with minimal resource usage.

## 📊 Key Achievements
- **50,000+ organic downloads** with **zero marketing**
- Ranked **Best in "Books" and "Entertaiment" categories** in RuStore
- Published in **Chinese app ecosystems** (Huawei, Xiaomi)
- Published in **South Korean app ecosystems** (Samsung)

## 🛠️ Technical Highlights
- **Language**: Java  
- **Architecture**: Clean architecture (data / domain / presentation layers)  
- **Key Features**:  
  - Offline playback  
  - Sleep timer  
  - Playback speed control  
  - Library management  
- **Media Engine**: mediaPlayer  

## 🧩 Architecture Overview
The app is built as a modular Android application with **29 specialized components** (Activities/Services), each responsible for a distinct feature:
- `MainActivity` — central coordinator and navigation hub  
- `PlayerService` — audio playback with MediaPlayer, sleep timer, speed control  
- `SoundScreen` — local book discovery
- 'LoadCovers' -  cover art searching
- 'Bookmarks' - work with bookmarks
- `CustomizingScreen` — user preferences and app configuration  

This design ensures **separation of concerns** and long-term maintainability.

## 📸 Screenshots
*(Optional: add 1–2 screenshots later if you want)*

> This project demonstrates full-cycle mobile development — from idea to top-ranked app in multiple app stores.
