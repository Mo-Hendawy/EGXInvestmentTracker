# Google Play Store Publishing Guide

## Step 1: Create Signing Key (One-time setup)

**IMPORTANT:** Keep this key safe! You'll need it for all future updates.

1. Open PowerShell/Command Prompt in the project root
2. Run this command (replace passwords with your own):

```powershell
keytool -genkey -v -keystore keystore/egx-portfolio-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias egx_portfolio_key
```

You'll be asked for:
- **Keystore password**: (create a strong password, save it!)
- **Key password**: (can be same as keystore password)
- **Name, Organization, etc.**: (fill in your details)

3. Copy `keystore.properties.example` to `keystore.properties`:
```powershell
Copy-Item keystore.properties.example keystore.properties
```

4. Edit `keystore.properties` and fill in your passwords:
```
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=egx_portfolio_key
storeFile=keystore/egx-portfolio-release.jks
```

5. **Add to .gitignore** (if using git):
```
keystore.properties
keystore/*.jks
```

## Step 2: Build Release AAB (Android App Bundle)

Google Play requires AAB format (not APK).

```powershell
cd "C:\New folder\EGXPortfolioTracker"
.\gradlew.bat bundleRelease
```

The AAB will be at:
```
app\build\outputs\bundle\release\app-release.aab
```

## Step 3: Create Google Play Console Account

1. Go to https://play.google.com/console
2. Pay the **$25 one-time registration fee** (lifetime)
3. Complete account setup

## Step 4: Create New App

1. In Google Play Console, click **"Create app"**
2. Fill in:
   - **App name**: "EGX Portfolio Tracker"
   - **Default language**: English
   - **App or game**: App
   - **Free or paid**: Free
   - **Declarations**: Check all that apply

## Step 5: Complete Store Listing

### Required Information:

1. **App details**:
   - Short description (80 chars): "Track your EGX stock investments with real-time prices, charts, and performance analytics"
   - Full description (4000 chars): Write about features, benefits, etc.

2. **Graphics**:
   - **App icon**: 512x512 PNG (no transparency)
   - **Feature graphic**: 1024x500 PNG (for Play Store banner)
   - **Screenshots**: 
     - Phone: At least 2 screenshots (1080x1920 or 1440x2560)
     - Tablet (optional): 1200x1920
   - **Promo video** (optional): YouTube link

3. **Categorization**:
   - App category: Finance
   - Tags: Investment, Stocks, Portfolio, Finance

4. **Privacy Policy** (REQUIRED):
   - You need a privacy policy URL
   - Can use free services like:
     - https://www.privacypolicygenerator.info/
     - https://www.freeprivacypolicy.com/
   - Must mention:
     - Data collection (if any)
     - Third-party APIs (TradingView, CNBC)
     - Local data storage

## Step 6: Set Up App Content

1. **Content rating**: Complete questionnaire
2. **Target audience**: Select appropriate age group
3. **Data safety**: 
   - Declare what data you collect/use
   - For this app: Local storage only, no personal data sent

## Step 7: Upload AAB

1. Go to **"Production"** → **"Create new release"**
2. Upload your `app-release.aab` file
3. **Release name**: "1.0.0" (matches versionName)
4. **Release notes**: 
   ```
   Initial release
   - Track EGX stock investments
   - Real-time price updates
   - Portfolio analytics and charts
   - Dividend tracking
   - Performance analysis
   ```

## Step 8: Review and Submit

1. Review all sections (green checkmarks)
2. Click **"Start rollout to Production"**
3. Wait for review (usually 1-3 days)

## Step 9: After Approval

- App will be live on Google Play
- Share the Play Store link
- Monitor reviews and ratings

---

## Important Notes:

### Version Updates:
- **versionCode**: Increment by 1 for each release (1, 2, 3...)
- **versionName**: User-visible version (1.0.0, 1.0.1, 1.1.0...)

### Future Releases:
1. Update versionCode and versionName in `build.gradle.kts`
2. Build new AAB: `.\gradlew.bat bundleRelease`
3. Upload to Play Console → Production → New release

### Testing Before Release:
- Test the release build thoroughly
- Install release AAB on your device to verify

---

## Quick Commands Reference:

```powershell
# Build release AAB
.\gradlew.bat bundleRelease

# Build release APK (for testing)
.\gradlew.bat assembleRelease

# Check AAB file
bundletool.jar (if you have it)
```

---

## Need Help?

- Google Play Console Help: https://support.google.com/googleplay/android-developer
- Android App Bundle Guide: https://developer.android.com/guide/app-bundle


