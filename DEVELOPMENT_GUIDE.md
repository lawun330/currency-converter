# Development Guide

This guide explains how to set up and run the application locally for development.

---

## Prerequisites

Ensure the required software is installed (see `REQUIREMENTS.md`).

---

## Step 1: Clone and Setup

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd currency-converter
   ```

2. Create a `.env` file in the root directory

---

## Step 2: Configure Environment Variables

Create or edit `.env` in the root directory:

```bash
MONGODB_URI="mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/currency_converter?retryWrites=true&w=majority"
CURRENCY_API_URL=https://api.exchangerate-api.com/v6/latest
VITE_PORT=8080
VITE_API_BASE=http://localhost:${VITE_PORT}
```

Replace MongoDB connection string with the actual Atlas URI (see `DEPLOYMENT_GUIDE.md` Step 1.2).

**Note**: MongoDB is optional - conversions work without it, but history won't be saved.

---

## Step 3: Run Backend (Java)

### Option A: With Maven (Recommended)

```bash
mvn clean compile
mvn exec:java
```

### Option B: Without Maven (No MongoDB support)

```bash
javac src/main/java/Converter.java
java -cp src/main/java Converter
```

**Note**: Option B will fail if MongoDB imports are present. Use Option A for full functionality.

The backend starts on `http://localhost:8080` (or the port from `VITE_PORT`).

---

## Step 4: Run Frontend (React)

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:5173` (or another port if 5173 is busy).

---

## Step 5: Access the Application

1. Open a browser and go to `http://localhost:5173`
2. The React app calls the Java backend at `http://localhost:8080`
3. Make a currency conversion to test
4. If MongoDB is configured, check the history section for recent conversions

---

## Local Development URLs

- **Frontend**: `http://localhost:5173`
- **Backend API**: `http://localhost:8080`
- **Convert Endpoint**: `http://localhost:8080/api/convert?from=USD&to=MMK&amount=100`
- **History Endpoint**: `http://localhost:8080/api/history`

---

## Development Workflow

1. Make code changes
2. Backend: Restart `mvn exec:java` to see changes
3. Frontend: Vite hot-reloads automatically (no restart needed)
4. Test locally before pushing to deployment

---

## Notes

- The `.env` file is gitignored - never commit it with real credentials
- Both backend and frontend must be running simultaneously for full functionality
