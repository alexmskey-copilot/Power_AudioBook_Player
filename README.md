# Power AudioBook Player  
Player for audiobooks with large icons and clear audio controls

🏆 **Best Audiobook App in RuStore** (Russia's official app store)  
📱 Also available on **Huawei AppGallery**, **Xiaomi GetApps**, and **Samsung Galaxy Store**

A lightweight, user-focused Android audiobook player built for seamless listening, battery efficiency, and full offline access.  
Chose Android’s built-in **MediaPlayer** for simplicity, stability, and minimal footprint — fully sufficient for audiobook playback needs.  
Future roadmap includes migration to ExoPlayer for advanced features (e.g., gapless playback, custom decoders), but the current implementation meets all user requirements with optimal resource usage.

## 📊 Key Achievements
- **50,000+ organic downloads** with **zero marketing**
- Ranked **#1 in "Books" and "Entertainment" categories** in RuStore
- Published in **Chinese app ecosystems** (Huawei, Xiaomi)
- Published in **South Korean app ecosystem** (Samsung Galaxy Store)

## 🛠️ Technical Highlights
- **Language**: Java  
- **Architecture**: Modular design with 29 specialized components (Activities/Services), ensuring separation of concerns  
- **Key Features**:  
  - Offline playback  
  - Sleep timer  
  - Playback speed control  
  - Library management  
  - Bookmarking  
- **Media Engine**: Android MediaPlayer  

## 🧩 Architecture Overview
The app follows a **modular architecture**, where each feature is encapsulated in a dedicated component:
- `MainActivity` — central navigation hub  
- `PlayerService` — background audio playback with MediaPlayer, sleep timer, and speed control  
- `SoundScreen` — local audiobook discovery and library browsing  
- `LoadCovers` — manually cover art fetching  
- `Bookmarks` — bookmarks management  
- `CustomizingScreen` — user preferences and UI customization  

This approach ensures **maintainability, testability, and scalability** — even in a solo-developed project.

> This project demonstrates full-cycle mobile development — from concept to a top-ranked app across **Russian, Chinese, and Korean** app stores.

## 📸 Screenshots
<img width="216" height="400" alt="image" src="https://github.com/user-attachments/assets/70c58422-75d8-4b75-b169-20016db0d237" />
<img width="216" height="400" alt="image" src="https://github.com/user-attachments/assets/b8426cf4-50dd-4c22-bf59-e88ac4fc06fd" />
<img width="216" height="400" alt="image" src="https://github.com/user-attachments/assets/d4ba065e-c6b4-42f8-94d3-bdcae031ba40" />


