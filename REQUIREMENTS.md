# Requirements

This document lists all software versions, dependencies, and tools required to build and run the application.

---

## Backend (Java)

**Java**: JDK 17 or higher
 - Used: Eclipse Temurin 17 JDK (as specified in Dockerfile)

**No build tools (Maven/Gradle)**
 - No need for dependency management or complex build configuration
 - Docker handles the build process in production

---

## Frontend (React)

- **Node.js**: 16.x or higher (18.x recommended)
- **npm**: 7.x or higher (comes with Node.js)
- **Vite**: 7.2.4
  - Fast build tool and dev server
  - Handles React compilation and bundling
- **react**: ^19.2.0
- **react-dom**: ^19.2.0

---

## Installation

### Backend

No installation needed. Compile with:
```bash
javac Converter.java
java Converter
```

### Frontend

Install dependencies:
```bash
cd frontend
npm install
```

Run development server:
```bash
npm run dev
```

Build for production:
```bash
npm run build
```

---

## Notes

- All Java dependencies are part of the standard library - no external JAR files required
- Frontend dependencies are managed via npm and listed in `package.json`
- Docker is used for deployment but not required for local development
- The project intentionally avoids build tools like Maven/Gradle for simplicity