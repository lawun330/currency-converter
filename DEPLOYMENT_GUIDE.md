# Deployment Guide

This guide explains how to deploy the Currency Converter application:
- **Backend (Java)**: Render (using Docker)
- **Frontend (React)**: Vercel

---

## Why Docker?

**Docker** is needed to deploy the Java backend on Render. The `Dockerfile` uses `eclipse-temurin:17-jdk` which provides Java 17 JDK (Eclipse Temurin is the official OpenJDK distribution).

---

## Step 1: Deploy Backend to Render

### 1.1 Create a Render Web Service

1. Go to https://dashboard.render.com
2. Click **"New +"** → **"Web Service"**
3. Connect the GitHub repository
4. Select the repository

### 1.2 Configure Render Service - Manual Settings

- **Name**: `currency-converter-backend` (or preferred name)
- **Environment**: `Docker`
- **Region**: Choose closest region
- **Branch**: `MJR`
- **Root Directory:** Leave empty (default root)

### 1.3 Set Environment Variables in Render

Go to **Environment Variables** tab and add:
```bash
CURRENCY_API_URL=https://api.exchangerate-api.com/v6/latest
```

Render automatically sets `PORT` environment variable.

### 1.4 Deploy

1. Click **"Create Web Service"**
2. Render will:
   - Build the Docker image using the `Dockerfile`
   - Compile the Java code (`javac Converter.java`)
   - Create a container with Java 17 runtime
   - Start the application (`java Converter`)
3. Wait for deployment to complete (usually 2-5 minutes)
4. Copy the service URL (e.g., `https://example-name.onrender.com`)

**Important**: Note down the Render backend URL - required for frontend configuration

---

## Step 2: Deploy Frontend to Vercel

### 2.1 Import Project to Vercel

1. Go to https://vercel.com
2. Click **"Add New..."** → **"Project"**
3. Import the GitHub repository
4. Select the repository

### 2.2 Configure Vercel Project - Manual Settings

- **Framework Preset**: `Vite`
- **Root Directory**: `frontend` (React app is in the frontend folder)
- **Build Command**: `npm run build`
- **Output Directory**: `dist` (Vite outputs to `dist`, not `public`)
- **Install Command**: `npm install`

### 2.3 Set Environment Variables in Vercel

Go to **Environment Variables** tab and add:
```bash
# Use actual Render backend URL from Step 1.4
VITE_API_BASE=https://example-name.onrender.com
```

**Important**: 
- Do NOT include `/api/convert` in the URL
- Use `https://` (not `http://`)
- This variable is used by the React app to call the backend API

### 2.4 Deploy

1. Click **"Deploy"**
2. Vercel installs dependencies, builds the React app, and deploys the `dist` folder
3. Wait for build to complete (usually 1-2 minutes)
4. The app will be live at `https://example-project-name.vercel.app`

---

## Environment Variables Summary

### Render (Backend):

**Required:**
- `CURRENCY_API_URL` - Exchange rate API URL
  - Set to: `https://api.exchangerate-api.com/v6/latest`
  - Used by Java backend to fetch currency rates

**Automatic (set by Render):**
- `PORT` - Port number automatically set by Render
  - Java code reads this from system environment

### Vercel (Frontend):

**Required:**
- `VITE_API_BASE` - Render backend URL
  - Example: `https://example-name.onrender.com`
  - Used by React app to call the Java API

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
     v                                         v
  User Browser                         Exchange Rate API
```

**Flow:**
1. User visits Vercel frontend URL
2. React app calls Render backend via `VITE_API_BASE`
3. Java backend fetches rates from Exchange Rate API
4. Backend converts currency and returns JSON
5. Frontend displays result to user

---