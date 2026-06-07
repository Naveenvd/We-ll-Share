# 🚗 We'll Share — Full Stack Application

> **Share the Journey. Share the Cost.**
> India's carpooling & parcel delivery platform.

---

## 📁 Repository Structure

```
We-ll-Share/
├── ridemate-frontend/     ← Angular 17 (UI)
├── ridemate-backend/      ← Spring Boot 3 (REST API)
├── ridemate_schema.sql    ← MySQL database schema
└── README.md
```

---

## 🛠️ Tech Stack

| Layer      | Technology                        |
|------------|-----------------------------------|
| Frontend   | Angular 17, Angular Material, SCSS |
| Backend    | Spring Boot 3.2, Java 17          |
| Database   | MySQL 8                           |
| Auth       | JWT (Bearer Token)                |
| Maps       | Leaflet.js (Nominatim geocoding)  |
| File Store | Local disk (`/uploads`)           |

---

## 🚀 Local Setup Guide

### Prerequisites
- Java 17+
- Node.js 18+ & Angular CLI (`npm i -g @angular/cli`)
- MySQL 8 running locally
- Git

---

### 1. Clone the repository
```bash
git clone https://github.com/Naveenvd/We-ll-Share.git
cd We-ll-Share
```

---

### 2. Set up the Database
Open MySQL and run:
```sql
CREATE DATABASE ridemate;
```
Then import the schema:
```bash
mysql -u root -p ridemate < ridemate_schema.sql
```

---

### 3. Set up the Backend

```bash
cd ridemate-backend
```

Copy the config template and fill in your values:
```bash
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

Open `application.properties` and update:
```properties
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
app.jwt.secret=YOUR_OWN_SECRET_KEY_MIN_32_CHARS
spring.mail.username=your@gmail.com
spring.mail.password=your_gmail_app_password
```

Run the backend:
```bash
./mvnw spring-boot:run
```
Backend starts at → **http://localhost:8081**

---

### 4. Set up the Frontend

```bash
cd ridemate-frontend
npm install
```

Copy the environment template:
```bash
cp src/environments/environment.example.ts \
   src/environments/environment.ts
```

Run the frontend:
```bash
ng serve
```
App opens at → **http://localhost:4200**

---

## ✨ Features

| Feature | Description |
|---|---|
| 🔍 Ride Search | Filter by route, date, seats, price, women-only |
| 🚘 Post Rides | Drivers post trips and accept/reject bookings |
| 📦 Parcel Delivery | Send parcels with drivers on the same route |
| 🆘 SOS Alerts | Emergency button sends GPS to admin & contacts |
| 🔒 Aadhaar Verification | Admin verifies every user's identity |
| 📍 Live Tracking | Shareable public trip tracking link |
| 💬 In-App Chat | Rider ↔ Driver messaging per booking/parcel |
| ⭐ Reviews | Ratings and reviews after every trip |
| 🌙 Dark Mode | Full light/dark theme support |
| 📱 Responsive | Works on mobile, tablet, and desktop |

---

## 🌿 Branch Strategy

```
main   ← stable, production-ready code
dev    ← integration branch (all PRs merge here)
  ├── feature/your-feature
  └── fix/your-bugfix
```

**Daily workflow:**
```bash
git checkout dev && git pull origin dev
git checkout -b feature/my-feature
# ... code ...
git add . && git commit -m "feat: what you did"
git push origin feature/my-feature
# Open Pull Request → dev on GitHub
```

---

## 👥 Team

| Name | Role | GitHub |
|------|------|--------|
| Naveen | Lead Developer | [@Naveenvd](https://github.com/Naveenvd) |
| — | Backend Dev | — |
| — | Frontend Dev | — |

---

> ⚠️ **Never commit `application.properties`** — it contains database passwords and secrets.
> Use `application.properties.example` as the template.
