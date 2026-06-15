// ──────────────────────────────────────────────────
//  PRODUCTION environment — committed to git
//  No secrets here — just the public backend URL.
//  After deploying the backend on Render.com:
//    1. Copy the backend service URL from Render
//    2. Replace YOUR-BACKEND-NAME below with it
//    3. git commit & push → CI auto-deploys
// ──────────────────────────────────────────────────
export const environment = {
  production: true,
  apiUrl: 'https://YOUR-BACKEND-NAME.onrender.com/api',
  wsUrl: 'https://YOUR-BACKEND-NAME.onrender.com/ws'
};
