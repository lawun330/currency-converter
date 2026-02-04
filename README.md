# Currency Converter

A web-based currency converter application that supports conversion between 15 currencies with real-time exchange rates and conversion history tracking.

---

## Features

- Convert between **15 currencies**: USD, EUR, GBP, AUD, JPY, INR, SGD, THB, MMK, VND, KRW, MYR, KHR, LAK, AED
- Real-time exchange rates from Exchange Rate API
- Conversion history tracking (last 5 conversions) stored in MongoDB Atlas
- Responsive web interface built with React
- RESTful API backend built with Java

---

## Legacy System (Branch: `legacy-java-swing`)

The original implementation was a desktop application written in Java using:
- **JOptionPane** (Java Swing GUI) for user interface
- **Replit** development environment
- Simple dialog-based interaction:
  1. User selects source currency
  2. Program prompts for numerical amount
  3. User selects target currency
  4. Program displays converted amount in a GUI dialog

---

## Current System (Branch: `MJR`)

The application has been refactored into a modern web-based architecture:

**Backend (Java):**
- HTTP server using Java's `com.sun.net.httpserver.HttpServer`
- RESTful API endpoints (`/api/convert`, `/api/history`)
- MongoDB Atlas integration for conversion history storage
- Environment variable configuration for flexible deployment
- Maven for dependency management (MongoDB Java Driver)

**Frontend (React):**
- React 19 with Vite build tool
- Responsive design with mobile support
- Real-time currency conversion
- Conversion history display (last 5 conversions)
- Flag icons for visual currency selection

**Deployment:**
- Backend deployed on Render (Docker container)
- Frontend deployed on Vercel
- MongoDB Atlas for cloud database

---

## File Structure

```
currency-converter/
├── src/main/java/
│   └── Converter.java         # Java backend HTTP server
├── frontend/
│   ├── src/
│   │   ├── App.jsx            # React main component
│   │   ├── App.css            # Styles
│   │   └── currencies.js      # Currency definitions (15 currencies)
│   └── package.json           # Frontend dependencies
├── pom.xml                    # Maven configuration
├── Dockerfile                 # Docker build for deployment
├── .env                       # Environment variables (gitignored)
├── REQUIREMENTS.md            # Software versions and tools
├── DEVELOPMENT_GUIDE.md       # Local development setup
└── DEPLOYMENT_GUIDE.md        # Production deployment guide
```

---

## Documentation

- **[REQUIREMENTS.md](REQUIREMENTS.md)** - Software versions and tools required
- **[DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)** - Local development setup and commands
- **[DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md)** - Production deployment instructions

---

## Notes

This project serves as a learning exercise in full-stack web development, covering RESTful APIs, frontend-backend integration, database integration, and cloud deployment. Documentation traces the workflow from local development to production, including environment configuration, containerization, and refactoring from a desktop tool to a modern web application.
