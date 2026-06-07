# 🚗 We'll Share — Frontend

> **Share the Journey. Share the Cost.**  
> India's carpooling & parcel delivery platform built with Angular 17.

---

## 📋 Project Overview

We'll Share connects daily commuters with drivers who have empty seats — passengers save up to 70% on travel costs, drivers earn extra income. Drivers can also deliver parcels along their route for additional earnings.

**Key Features:**
- 🔍 Search & book rides by route, date, seats, price
- 🚘 Post rides and manage bookings as a driver
- 📦 Send & deliver parcels with passing drivers
- 🆘 SOS emergency alerts with live GPS
- 🔒 Aadhaar-verified users (Admin review)
- 👩 Women-only ride filter
- 📍 Real-time trip tracking (public shareable link)
- 💬 In-app chat (rider ↔ driver)
- ⭐ Ratings & reviews system

---

## 🛠️ Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Framework   | Angular 17 (Standalone Components)  |
| UI Library  | Angular Material (MDC)              |
| Styling     | SCSS + CSS Custom Properties        |
| Maps        | Leaflet.js                          |
| Auth        | JWT (sessionStorage)                |
| HTTP        | Angular HttpClient + Interceptors   |

---

## 🚀 Getting Started

### Prerequisites
- Node.js v18+
- Angular CLI v17: `npm install -g @angular/cli`
- Backend API running (Spring Boot — see backend repo)

### 1. Clone the repo
```bash
git clone https://github.com/Naveenvd/We-ll-Share.git
cd We-ll-Share
```

### 2. Install dependencies
```bash
npm install
```

### 3. Configure environment
```bash
# Copy the example file
cp src/environments/environment.example.ts src/environments/environment.ts
```
Then open `src/environments/environment.ts` and update the backend URL:
```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8081/api'  // ← your local backend port
};
```

### 4. Run the app
```bash
ng serve
```
Open **http://localhost:4200** in your browser.

---

## 📁 Project Structure

```
src/
├── app/
│   ├── core/
│   │   ├── guards/          # authGuard, adminGuard
│   │   ├── interceptors/    # JWT + error interceptors
│   │   ├── models/          # TypeScript interfaces
│   │   └── services/        # ApiService, AuthService, ThemeService
│   ├── features/
│   │   ├── landing/         # Public landing page
│   │   ├── auth/            # Login, Signup, OTP, Role Select
│   │   ├── dashboard/       # Main user dashboard
│   │   ├── rides/           # Search, Post, Detail
│   │   ├── bookings/        # My Bookings, Driver Bookings, Detail
│   │   ├── parcels/         # Post, My Parcels, Driver Parcels, Detail
│   │   ├── profile/         # Profile, Vehicles, Emergency Contacts
│   │   ├── history/         # Trip & transaction history
│   │   ├── admin/           # Admin dashboard, Verifications, Users
│   │   └── public/          # Trip tracking (no auth)
│   └── shared/
│       └── components/      # Reusable dialogs, SOS button, Star rating
├── environments/
│   ├── environment.ts        # Dev config (committed)
│   ├── environment.example.ts # Template for setup
│   └── environment.prod.ts   # Prod config (gitignored)
└── styles.scss               # Global styles + CSS theme tokens
```

---

## 🌿 Branch Strategy

```
main    ← stable production-ready code
dev     ← integration branch (merge PRs here)
  ├── feature/your-feature-name   ← your work
```

**Workflow:**
```bash
git checkout dev
git pull origin dev
git checkout -b feature/your-feature-name

# ... make your changes ...

git add .
git commit -m "feat: describe what you did"
git push origin feature/your-feature-name
# Then open a Pull Request → dev on GitHub
```

---

## 👥 Team

| Role        | GitHub            |
|-------------|-------------------|
| Lead        | @Naveenvd         |
| Teammate 1  | —                 |
| Teammate 2  | —                 |

---

## 🔗 Related

- **Backend Repo:** *(add link here)*
- **Live Demo:** *(add link here)*

---

> Built with ❤️ by the We'll Share team.
