# Deployment Guide

This guide explains how to deploy the Currency Converter application:
- **Backend (Java)**: Render (using Docker)
- **Frontend (React)**: Vercel

---

## Why Docker?

**Docker** is needed to deploy the Java backend on Render. The `Dockerfile` uses `eclipse-temurin:21-jdk` which provides Java 21 JDK (Eclipse Temurin is the official OpenJDK distribution).

---

## Step 1: MongoDB Atlas Setup

### 1.1 Create MongoDB Atlas Account and Cluster

1. Go to https://cloud.mongodb.com and sign up/login
2. Create a new cluster (free tier M0 is sufficient)

### 1.2 Get Connection String

1. Go to **Database** → **Connect** → **Drivers** (or "Connect application")
2. Select **Java** as the driver
3. If prompted, create a database user (set username and password, save these securely)
4. Copy the connection string and replace `<password>` with the actual database user password
5. Add the database name to the path: change `...mongodb.net/` to `...mongodb.net/currency_converter`

**Connection string format:**
```
mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/currency_converter?retryWrites=true&w=majority
```

### 1.3 Configure Network Access

1. Go to **Network Access** → **Add IP Address**
2. Click **"Allow Access from Anywhere"** (adds `0.0.0.0/0`)
3. Click **Confirm**

**Note**: MongoDB is optional - the application works without it, but conversion history won't be saved.

---

## Step 2: Deploy Backend to Render

### 2.1 Create a Render Web Service

1. Go to https://dashboard.render.com
2. Click **"New +"** → **"Web Service"**
3. Connect the GitHub repository
4. Select the repository

### 2.2 Configure Render Service

- **Name**: `currency-converter-backend` (or preferred name)
- **Environment**: `Docker`
- **Region**: Choose closest region
- **Branch**: `MJR`
- **Root Directory:** Leave empty (default root)

### 2.3 Set Environment Variables

Go to **Environment Variables** tab and add:
```bash
CURRENCY_API_URL=https://api.exchangerate-api.com/v6/latest
MONGODB_URI=mongodb+srv://username:password@cluster0.xxxxx.mongodb.net/currency_converter?retryWrites=true&w=majority
```

Replace `username`, `password`, and `cluster0.xxxxx.mongodb.net` with actual values from Step 1.2.

Render automatically sets `PORT` environment variable.

### 2.4 Deploy

1. Click **"Create Web Service"**
2. Render builds the Docker image, compiles Java code, and starts the application
3. Copy the service URL (e.g., `https://example-name.onrender.com`)

**Important**: Note down the Render backend URL - required for frontend configuration.

---

## Step 3: Deploy Frontend to Vercel

### 3.1 Import Project to Vercel

1. Go to https://vercel.com
2. Click **"Add New..."** → **"Project"**
3. Import the GitHub repository
4. Select the repository

### 3.2 Configure Vercel Project

- **Framework Preset**: `Vite`
- **Root Directory**: `frontend`
- **Build Command**: `npm run build`
- **Output Directory**: `dist`
- **Install Command**: `npm install`

### 3.3 Set Environment Variables

Go to **Environment Variables** tab and add:
```bash
VITE_API_BASE=https://example-name.onrender.com
```

Use the actual Render backend URL from Step 2.4. Do NOT include `/api/convert` in the URL.

### 3.4 Deploy

1. Click **"Deploy"**
2. Vercel installs dependencies, builds the React app, and deploys
3. The app will be live at `https://example-project-name.vercel.app`

---

## Environment Variables Summary

### Render (Backend):

- `CURRENCY_API_URL` - Set to: `https://api.exchangerate-api.com/v6/latest`
- `MONGODB_URI` - MongoDB Atlas connection string from Step 1.2
- `PORT` - Automatically set by Render

### Vercel (Frontend):

- `VITE_API_BASE` - Render backend URL from Step 2.4

---

## How Environment Variables Work

**Backend (Java):**
1. Checks system environment variables first (`System.getenv()`)
2. Falls back to `.env` file if system env vars not found
3. Throws error only if not found in both places

**Frontend (React/Vite):**
1. Vite only exposes variables prefixed with `VITE_`
2. Access via `import.meta.env.VITE_API_BASE`
3. Build-time replacement (not runtime)

---

## Updating Deployment

1. Make changes to code
2. Push changes to GitHub
3. Both Render and Vercel automatically detect changes, rebuild, and redeploy

---

## Architecture Overview

```
┌─────────────┐      HTTP/HTTPS       ┌──────────────┐
│   Vercel    │ ───────────────────>  │    Render    │
│  (Frontend) │                       │  (Backend)   │
│   React     │ <───────────────────  │  Java + API  │
│   App       │      JSON Response    │   Server     │
└─────────────┘                       └──────────────┘
     │                                         │
     │                                         │
     v                                         │
  User Browser                                 │
                                               │
                                               ├─────────────────┐
                                               │                 │
                                               │                 │
                                               v                 v
                                    ┌──────────────────┐  ┌──────────────┐
                                    │  MongoDB Atlas   │  │ Exchange Rate│
                                    │  (Database)      │  │     API      │
                                    └──────────────────┘  └──────────────┘
```

**Flow:**
1. User visits Vercel frontend URL
2. React app calls Render backend via `VITE_API_BASE`
3. Java backend fetches rates from Exchange Rate API
4. Backend converts currency and saves to MongoDB Atlas (if configured)
5. Backend returns conversion result to frontend
6. Backend retrieves conversion history from MongoDB Atlas and returns to frontend
7. Frontend displays result and conversion history
