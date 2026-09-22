Booka — Local Service Scheduling and Booking App
Slogan: Book local. Grow local.

Course: OPSC6312 

Portfolio of Evidence (PoE) — Part 2: App Prototype Development

Project Overview
Booka is a Kotlin-based mobile application designed to seamlessly connect local service providers (such as hairstylists, nail technicians, makeup artists and photographers) with local clients. The platform simplifies appointment scheduling, service browsing and booking management in one centralised hub.

Video Demonstration
YouTube: https://youtu.be/GZzkzFhPnRA?si=nOJL0PhjRhLD4pgZ 

Features
Authentication and Role-Based Navigation: Secure Sign In and Registration workflows with role distinction powered by Firebase Auth.

Database Integration: Hosted online database integration for active service items and real-time user session persistence.

Dependencies
Language: Kotlin

UI Framework: Jetpack Compose  and Material 3 Design

Database: Firebase Authentication & Cloud Firestore

Architecture: MVVM (Model-View-ViewModel)

CI/CD: GitHub Actions (.github/workflows/build.yml)

Target Emulator / Device: Android (Tested on BlueStacks / Samsung SM-S908E)

Automated Testing & Continuous Integration (CI/CD)
Automated build and testing workflows are configured via GitHub Actions. Every commit and push to the main branch triggers an automated build check to ensure project compilation and run integrity across environments.

AI Tool Usage Declaration
Overview & Intent
During the development of the Booka Android application, Google Gemini was used to assist with error diagnosis.

Debugging Attempt: During final testing, an issue arose where selecting the "Service Provider" role during registration did not correctly route to the provider screen. I initially attempted to resolve this by consulting YouTube video tutorials and online guides; however, this did not resolve the issue in time due to an unexpected crash near the final submission deadline. AI was subsequently used to analyze the AuthScreen.kt recomposition state and offer diagnostic steps.
