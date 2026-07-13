# BEAM 🫙
> 감정을 유물처럼 아카이빙하는 감정 다이어리 앱

<p align="center">
  <img src="assets/logo.png" width="120"/>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/Jetpack Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white"/>
  <img src="https://img.shields.io/badge/FastAPI-009688?style=flat-square&logo=fastapi&logoColor=white"/>
</p>

---

## 📖 프로젝트 소개

**BEAM**은 오늘의 감정을 박물관 유물처럼 아카이빙하는 감정 다이어리 앱입니다.  
글을 작성하면 Groq AI가 감정을 자동 분류하고, 감정별로 기록을 보관해줍니다.  
매일 AI가 맞춤 질문을 던져 기록 습관을 만들어줍니다.

---

## ✨ 주요 기능

| 기능 | 설명 |
|------|------|
| 🔐 Google 로그인 | Credential Manager 기반 구글 소셜 로그인 |
| 🤖 AI 감정 분석 | Groq LLM(Llama 3.1)이 글을 읽고 감정 자동 분류 |
| 🗂 감정 아카이브 | 행복 / 우울 / 슬픔 / 짜증 / 외로움 5가지로 분류 보관 |
| 💬 오늘의 질문 | AI가 유저 감정 패턴 기반 맞춤 질문 생성 (하루 1회) |
| 📊 연대기 그래프 | 일별 감정 변화를 라인 그래프로 시각화 |
| 🌗 다크/라이트 모드 | 전체 화면 다크·라이트 테마 실시간 전환 |
| 🧩 홈 화면 위젯 | 오늘의 감정 or 기록 유도 문구 위젯 표시 |
| 🗑 기록 삭제 | 길게 눌러 스와이프로 기록 삭제 |

---

## 📱 스크린샷

<p align="center">
  <img src="assets/screen_login.png" width="180"/>
  <img src="assets/screen_home.png" width="180"/>
  <img src="assets/screen_record.png" width="180"/>
  <img src="assets/screen_archive.png" width="180"/>
</p>

---

## 🛠 기술 스택

### Android
| 기술 | 사용 목적 |
|------|-----------|
| Kotlin | 메인 언어 |
| Jetpack Compose | UI 구성 |
| MVVM Architecture | 아키텍처 패턴 |
| Retrofit2 + OkHttp | REST API 통신 |
| DataStore | 토큰 · 슬로건 로컬 저장 |
| Lottie | 감정별 애니메이션 |
| Glance AppWidget | 홈 화면 위젯 |
| Credential Manager | Google 로그인 |
| Coroutine + Flow | 비동기 처리 |

### Backend
| 기술 | 사용 목적 |
|------|-----------|
| FastAPI | REST API 서버 |
| MySQL | 데이터베이스 |
| SQLAlchemy | ORM |
| Groq API (Llama 3.1) | AI 감정 분석 · 질문 생성 |
| JWT | 인증 토큰 |

---

## 🏗 아키텍처
