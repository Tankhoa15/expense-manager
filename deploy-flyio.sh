#!/bin/bash

# Fly.io Deployment Script for Expense Manager
# Run this script to deploy backend + database + redis

set -e

echo "🚀 Deploying Expense Manager to Fly.io..."
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if flyctl is installed
if ! command -v flyctl &> /dev/null; then
    echo "❌ Fly CLI not found. Please install it first:"
    echo "   curl -L https://fly.io/install.sh | sh"
    exit 1
fi

# Check if logged in
if ! flyctl auth whoami &> /dev/null; then
    echo "❌ Not logged in to Fly.io. Please run:"
    echo "   flyctl auth login"
    exit 1
fi

echo -e "${BLUE}Step 1: Create Fly.io app${NC}"
flyctl apps create expense-manager-backend --org personal || echo "App already exists"

echo ""
echo -e "${BLUE}Step 2: Create PostgreSQL database${NC}"
flyctl postgres create \
  --name expense-manager-db \
  --region sin \
  --initial-cluster-size 1 \
  --vm-size shared-cpu-1x \
  --volume-size 1 \
  || echo "Database already exists"

echo ""
echo -e "${BLUE}Step 3: Attach database to app${NC}"
flyctl postgres attach expense-manager-db --app expense-manager-backend || echo "Already attached"

echo ""
echo -e "${BLUE}Step 4: Setup Upstash Redis${NC}"
echo "⚠️  Go to https://console.upstash.com/ and create a free Redis database"
echo "    Region: ap-southeast-1 (Singapore)"
echo ""
read -p "Press Enter when you have Redis URL ready..."

echo ""
echo -e "${BLUE}Step 5: Set secrets${NC}"
echo "Enter the following secrets:"

# Generate JWT secret
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
echo -e "${GREEN}✓ Generated JWT_SECRET${NC}"

# Prompt for other secrets
read -p "CORS_ORIGINS (e.g., https://your-frontend.vercel.app): " CORS_ORIGINS
read -p "Redis Host (from Upstash): " REDIS_HOST
read -p "Redis Port (default 6379): " REDIS_PORT
REDIS_PORT=${REDIS_PORT:-6379}
read -p "Redis Password (from Upstash): " REDIS_PASSWORD
read -p "Gmail address (MAIL_USERNAME): " MAIL_USERNAME
read -s -p "Gmail App Password (MAIL_PASSWORD): " MAIL_PASSWORD
echo ""
read -p "Email FROM address (MAIL_FROM): " MAIL_FROM

# Set all secrets at once
flyctl secrets set \
  JWT_SECRET="$JWT_SECRET" \
  CORS_ORIGINS="$CORS_ORIGINS" \
  REDIS_HOST="$REDIS_HOST" \
  REDIS_PORT="$REDIS_PORT" \
  REDIS_PASSWORD="$REDIS_PASSWORD" \
  MAIL_USERNAME="$MAIL_USERNAME" \
  MAIL_PASSWORD="$MAIL_PASSWORD" \
  MAIL_FROM="$MAIL_FROM" \
  --app expense-manager-backend

echo ""
echo -e "${BLUE}Step 6: Deploy application${NC}"
flyctl deploy --app expense-manager-backend

echo ""
echo -e "${GREEN}✅ Deployment complete!${NC}"
echo ""
echo "Backend URL: https://expense-manager-backend.fly.dev"
echo "API Base: https://expense-manager-backend.fly.dev/api/v1"
echo ""
echo "Test it:"
echo "  curl https://expense-manager-backend.fly.dev/api/v1/health"
echo ""
echo -e "${YELLOW}Next: Deploy frontend to Vercel with this backend URL${NC}"
