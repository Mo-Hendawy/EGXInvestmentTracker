# Quick Publish Guide - Step by Step

## ⚠️ IMPORTANT: Save Your Keystore Password!
You'll need this password for ALL future app updates. Write it down securely!

---

## Step 1: Create Release Keystore (REQUIRED - One Time Only)

**Run this command in PowerShell (in the project folder):**

```powershell
cd "C:\New folder\EGXPortfolioTracker"
keytool -genkey -v -keystore keystore/egx-portfolio-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias egx_portfolio_key
```

**You'll be asked for:**
1. **Keystore password**: Create a STRONG password (save it!)
2. **Re-enter password**: Type it again
3. **First and last name**: Your name or company name
4. **Organizational unit**: (can be blank, press Enter)
5. **Organization**: Your company/name
6. **City**: Your city
7. **State/Province**: Your state
8. **Country code**: EG (for Egypt) or your country code
9. **Confirm**: Type "yes"
10. **Key password**: Press Enter to use same as keystore password (recommended)

**After this completes, tell me and I'll help with the next steps!**

---

## Step 2: Create keystore.properties File

After you create the keystore, I'll help you create the properties file with your passwords.

---

## Step 3: Build Release AAB

Once keystore is ready, I'll build the Android App Bundle for you.

---

## Step 4: Google Play Console Setup

1. Go to https://play.google.com/console
2. Pay $25 one-time registration fee
3. Create new app
4. Upload the AAB file

---

**Ready? Start with Step 1 above!**


