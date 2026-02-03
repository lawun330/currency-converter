# Requirements

This document lists all software versions, dependencies, and tools required to build and run the application.

---

## Backend (Java)

**Java**: JDK 17 or higher
 - Used: Eclipse Temurin 17 JDK (as specified in Dockerfile)

**Maven**: Latest version (installed in Dockerfile)
 - Required for MongoDB Java Driver dependency
 - Configuration: `pom.xml`

**MongoDB Java Driver Sync**: 4.11.1
   - Required for MongoDB Atlas connection

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

**Local Development (without MongoDB):**
```bash
javac Converter.java
java Converter
```

**Local Development (with MongoDB):**
```bash
mvn clean compile
mvn exec:java
```

**Production Build:**
Maven is used in Dockerfile to compile and include MongoDB driver dependencies.

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

## Database

**MongoDB Atlas**
 - Cloud-hosted MongoDB databas used for storing conversion history
 - Connection string format: `mongodb+srv://username:password@cluster.mongodb.net/database?retryWrites=true&w=majority`
 - Database name: `currency_converter`
 - Collection name: `conversions`

**Note**: Application continues to work even if MongoDB is unavailable

---

## Summary

- Java standard library used for HTTP server and API calls
- MongoDB Java Driver (external dependency) is used for connecting to MongoDB Atlas
- Frontend dependencies are managed via npm and listed in `package.json`
- Docker is used for deployment and includes Maven for building
- Maven is required for MongoDB integration
