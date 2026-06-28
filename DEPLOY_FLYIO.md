# Deploy to Fly.io - Complete Guide

## Prerequisites

1. **Install Fly CLI:**
```bash
curl -L https://fly.io/install.sh | sh
```

2. **Login:**
```bash
fly auth login
```

3. **Sign up for Upstash Redis (Free):**
   - Go to https://console.upstash.com/
   - Create account
   - Create Redis database in `ap-southeast-1` (Singapore)
   - Note the `REDIS_HOST` and `REDIS_PASSWORD`

---

## 🚀 Quick Deploy (Automated)

```bash
# Run the deploy script
./deploy-flyio.sh
```

The script will:
1. Create Fly.io app
2. Create PostgreSQL database
3. Attach database
4. Ask you for secrets (Redis, Email, CORS)
5. Deploy the application

---

## 📝 Manual Deploy (Step by Step)

### Step 1: Create Fly.io App

```bash
fly apps create expense-manager-backend --org personal
```

### Step 2: Create PostgreSQL Database

```bash
fly postgres create \
  --name expense-manager-db \
  --region sin \
  --initial-cluster-size 1 \
  --vm-size shared-cpu-1x \
  --volume-size 1
```

This creates a 1GB PostgreSQL database (free tier).

### Step 3: Attach Database to App

```bash
fly postgres attach expense-manager-db --app expense-manager-backend
```

This automatically sets `DATABASE_URL` secret.

### Step 4: Get Database Connection Info

```bash
fly postgres connect -a expense-manager-db
```

Inside PostgreSQL shell:
```sql
-- Create production database
CREATE DATABASE expense_prod_db;

-- Verify
\l
\q
```

### Step 5: Setup Upstash Redis

**Why Upstash?** Fly.io doesn't have free Redis, but Upstash has 10k requests/day free.

1. Go to https://console.upstash.com/
2. Create Redis Database
3. Select region: **ap-southeast-1** (Singapore - gần VN)
4. Copy:
   - `UPSTASH_REDIS_REST_URL` 
   - `UPSTASH_REDIS_REST_TOKEN`

### Step 6: Set Secrets

```bash
# Generate JWT secret
JWT_SECRET=$(openssl rand -base64 64)

# Set all secrets
fly secrets set \
  JWT_SECRET="$JWT_SECRET" \
  CORS_ORIGINS="https://your-frontend.vercel.app,https://your-domain.com" \
  REDIS_HOST="<upstash-host>.upstash.io" \
  REDIS_PORT="6379" \
  REDIS_PASSWORD="<upstash-password>" \
  MAIL_USERNAME="<your-email@gmail.com>" \
  MAIL_PASSWORD="<gmail-app-password>" \
  MAIL_FROM="noreply@your-domain.com" \
  --app expense-manager-backend
```

### Step 7: Update Database URL Format

Fly.io sets `DATABASE_URL` automatically, but we need to extract parts:

```bash
# Get database URL
fly secrets list -a expense-manager-backend | grep DATABASE_URL

# Set individual DB vars (extract from DATABASE_URL)
fly secrets set \
  DB_URL="jdbc:postgresql://<host>:5432/expense_prod_db" \
  DB_USERNAME="<username>" \
  DB_PASSWORD="<password>" \
  --app expense-manager-backend
```

### Step 8: Deploy!

```bash
fly deploy --app expense-manager-backend
```

First deploy takes 5-10 minutes.

### Step 9: Check Status

```bash
# Check app status
fly status -a expense-manager-backend

# View logs
fly logs -a expense-manager-backend

# Test health endpoint
curl https://expense-manager-backend.fly.dev/api/v1/health
```

---

## 🎨 Deploy Frontend to Vercel

### Step 1: Update Frontend API URL

```bash
cd frontend

# Create .env.production
cat > .env.production << 'EOF'
VITE_API_URL=https://expense-manager-backend.fly.dev/api/v1
EOF
```

### Step 2: Update `src/services/api.js`

```javascript
const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1';
```

### Step 3: Deploy to Vercel

**Via Vercel CLI:**
```bash
npm i -g vercel
cd frontend
vercel --prod
```

**Via Vercel Dashboard:**
1. Go to https://vercel.com/
2. Import GitHub repository
3. Set Root Directory: `frontend`
4. Framework Preset: `Vite`
5. Build Command: `npm run build`
6. Output Directory: `dist`
7. Environment Variables:
   - `VITE_API_URL`: `https://expense-manager-backend.fly.dev/api/v1`
8. Deploy!

---

## 🔧 Post-Deployment Configuration

### Update CORS Origins

After frontend is deployed, update CORS:

```bash
fly secrets set \
  CORS_ORIGINS="https://your-frontend.vercel.app,https://expense-manager-backend.fly.dev" \
  --app expense-manager-backend
```

### Scale (If Needed)

```bash
# Check current scale
fly scale show -a expense-manager-backend

# Scale up memory (if needed)
fly scale memory 512 -a expense-manager-backend

# Add more instances
fly scale count 2 -a expense-manager-backend
```

---

## 💰 Fly.io Free Tier Limits

- **3 shared-cpu VMs** (256MB RAM each)
- **3GB persistent volume storage**
- **160GB outbound data transfer**

Our setup uses:
- ✅ 1 VM for backend (256MB)
- ✅ 1 VM for PostgreSQL (256MB)
- ✅ 1GB storage for database

**Still have 1 free VM left!**

---

## 🐛 Troubleshooting

### Check Logs
```bash
fly logs -a expense-manager-backend
```

### SSH into Machine
```bash
fly ssh console -a expense-manager-backend
```

### Check Database Connection
```bash
fly postgres connect -a expense-manager-db
```

### Restart App
```bash
fly apps restart expense-manager-backend
```

### Check Health
```bash
curl https://expense-manager-backend.fly.dev/api/v1/health
```

### Common Issues

**1. "Connection refused" - Database not ready**
```bash
# Check database status
fly status -a expense-manager-db

# Check database logs
fly logs -a expense-manager-db
```

**2. "Out of memory"**
```bash
# Scale up memory
fly scale memory 512 -a expense-manager-backend
```

**3. "Flyway migration failed"**
```bash
# Check logs for SQL errors
fly logs -a expense-manager-backend | grep -i flyway

# Connect to DB and check manually
fly postgres connect -a expense-manager-db
\c expense_prod_db
\dt
```

---

## 📊 Monitoring

```bash
# View metrics
fly dashboard -a expense-manager-backend

# Check machine status
fly machine list -a expense-manager-backend

# View app info
fly info -a expense-manager-backend
```

---

## 🔄 Update / Redeploy

```bash
# Pull latest code
git pull

# Deploy
fly deploy --app expense-manager-backend
```

---

## 🗑️ Cleanup (If Needed)

```bash
# Delete app
fly apps destroy expense-manager-backend

# Delete database
fly apps destroy expense-manager-db
```

---

## 📝 Summary

**Your URLs:**
- Backend: `https://expense-manager-backend.fly.dev`
- API Base: `https://expense-manager-backend.fly.dev/api/v1`
- Frontend: `https://your-app.vercel.app`

**Test the stack:**
```bash
# 1. Health check
curl https://expense-manager-backend.fly.dev/api/v1/health

# 2. Register
curl -X POST https://expense-manager-backend.fly.dev/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!","fullName":"Test User"}'

# 3. Login
curl -X POST https://expense-manager-backend.fly.dev/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}'
```

---

## 🎉 Done!

Your Expense Manager is now live on Fly.io! 🚀
