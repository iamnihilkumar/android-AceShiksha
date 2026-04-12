# EduReach 📚🚀

**AI-Powered Learning Platform for Indian Schools (Classes 6–11)**
Bridging the education gap with intelligent tutoring, gamified learning, and teacher tools — built for Bharat 🇮🇳

---

## 🌟 Overview

EduReach1 is a free Android application designed to make quality education accessible to students and teachers, especially in regions with limited access to tutoring.

It combines:

* 📖 Structured lessons (subject & class-wise)
* 🤖 Gemini AI tutoring (instant explanations)
* 🧠 Teacher-managed quizzes & assessments
* 🎮 Gamified learning experiences
* 📊 Performance tracking for students & teachers

💡 Built entirely on **Firebase Spark (free tier)** — zero-cost at small scale.

---

## ⚙️ How It Works

```
STUDENT                          TEACHER
---------------------------------------------
View Lessons                     Create Quizzes
Ask AI                           Add Game Questions
Take Quiz                        View Attempts
Play Games                       Manage Settings
Leaderboard
```

### Flow:

* App opens → session check (Splash)
* Login/Register → role fetched from Firestore
* Student → MainActivity (Home, Subjects, Leaderboard, Profile)
* Teacher → Dashboard (quiz + game management)
* AI → Gemini API provides explanations & quiz generation

---

## ✨ Features

### 👩‍🎓 Students

* 📘 Browse lessons (Class 6–11)
* 🤖 Ask AI tutor (Gemini)
* 📝 Attempt quizzes
* 📄 PDF → AI-generated quiz
* 🏆 Leaderboard & badges

### 🎮 Games

* 🧩 Maze Game
* 🚗 Car Race
* 📜 History Timeline
* ⚡ Rapid Fire
* ⚔️ Quiz Battle

### 👨‍🏫 Teachers

* ➕ Create quizzes
* 🎮 Add game questions
* 📊 Track student performance
* ⚙️ Manage content

---

## 🛠 Tech Stack

| Layer        | Technology           |
| ------------ | -------------------- |
| Language     | Kotlin               |
| UI           | XML + View Binding   |
| Architecture | MVVM                 |
| State        | LiveData + ViewModel |
| Auth         | Firebase Auth        |
| Database     | Cloud Firestore      |
| AI           | Gemini 2.5 Flash     |
| Networking   | Retrofit             |
| Navigation   | Jetpack Navigation   |

---

## 🏗️ Architecture (MVVM)

```
UI (Activities/Fragments)
        ↓
ViewModel (State + Logic)
        ↓
Repository (Data Handling)
        ↓
Firebase / Gemini API
```

---

## 📁 Project Structure

* `activities/` → Screens (Login, Dashboard, Games)
* `fragments/` → UI sections (Home, Subjects, Profile)
* `models/` → Data classes
* `repositories/` → Data logic
* `viewmodels/` → State management
* `network/` → API calls (Gemini)
* `utils/` → Helpers & constants

---

## 🗄️ Firestore Schema (Simplified)

* `users` → student/teacher roles
* `subjects` → class-based subjects
* `lessons` → learning content
* `quizzes` → teacher-created quizzes
* `quiz_attempts` → student performance
* `game_questions` → game logic

---

## 🚀 Getting Started

### Prerequisites

* Android Studio Hedgehog+
* JDK 17+
* Firebase project
* Gemini API key

---

### 1. Clone Repo

```
git clone https://github.com/iamnihilkumar/android-Edureach.git
cd EduReach1
```

---

### 2. Firebase Setup

* Enable Authentication (Email/Password)
* Enable Firestore
* Add `google-services.json` → `app/`

---

### 3. Add API Key

```
local.properties
GEMINI_API_KEY=your_api_key_here
```

---

### 4. Run App

* Open in Android Studio
* Click ▶️ Run

---

## 🔐 Security Note

* API keys are NOT included
* `local.properties` is ignored
* Add your own credentials locally

---

## 🗺️ Roadmap

*  Phase 1–4: Core + AI
*  Phase 5–9: Quiz + Games + AI PDF
*  Phase 10: Reports & polish
*  Phase 11: Play Store release

---

## 📄 License

This project is licensed under the MIT License.
