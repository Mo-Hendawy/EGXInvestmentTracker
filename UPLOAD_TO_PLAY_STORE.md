# Step-by-Step: Upload AAB to Google Play Console

## 📋 Prerequisites Checklist
- [x] AAB file created: `app-release.aab` (3.93 MB)
- [ ] Google Play Console account created
- [ ] $25 registration fee paid
- [ ] App created in Play Console

---

## 🚀 Step-by-Step Upload Process

### **STEP 1: Access Google Play Console**

1. Go to **https://play.google.com/console**
2. Sign in with your Google account
3. If you haven't paid the $25 fee yet, you'll be prompted to pay it first

---

### **STEP 2: Create Your App (If Not Done Yet)**

1. Click the **"Create app"** button (top right or center)
2. Fill in the form:
   - **App name**: `EGX Portfolio Tracker`
   - **Default language**: `English (United States)`
   - **App or game**: Select **"App"**
   - **Free or paid**: Select **"Free"**
   - **Declarations**: Check the boxes that apply:
     - ☑ This app does not contain ads
     - ☑ This app does not use Google Play Payments
     - ☑ This app does not use Google Play Billing
   - Click **"Create app"**

---

### **STEP 3: Navigate to Production Release**

1. In the left sidebar, click **"Production"** (under "Release")
2. You'll see a page saying "No production releases yet"
3. Click the **"Create new release"** button

---

### **STEP 4: Upload Your AAB File**

1. You'll see a section called **"App bundles and APKs"**
2. Click **"Upload"** button
3. A file picker will open
4. Navigate to: `C:\New folder\EGXPortfolioTracker\app\build\outputs\bundle\release\`
5. Select **`app-release.aab`**
6. Click **"Open"**
7. Wait for upload to complete (you'll see a progress bar)
8. Once uploaded, you'll see the file listed with version "1.0.0 (1)"

---

### **STEP 5: Add Release Notes**

1. Scroll down to **"Release name"** field
2. Enter: `1.0.0`
3. Scroll to **"Release notes"** section
4. Click **"Add release notes"**
5. Select language: **"English (United States)"**
6. Enter the release notes:

```
Initial release
- Track EGX stock investments
- Real-time price updates from TradingView and CNBC
- Portfolio analytics and charts
- Dividend tracking
- Performance analysis (weekly, monthly, 2-months, 50-days)
- Cost history tracking
- Privacy blur feature
- Beautiful Material Design 3 UI
```

7. Click **"Save"**

---

### **STEP 6: Review and Save Release**

1. Scroll to the bottom of the page
2. Review everything:
   - ✅ AAB file uploaded (version 1.0.0)
   - ✅ Release name: 1.0.0
   - ✅ Release notes added
3. Click **"Save"** button (bottom right)
4. You'll see a confirmation: "Release saved"

---

### **STEP 7: Complete Required Store Listing (Before Publishing)**

Before you can publish, you MUST complete these sections:

#### **7.1: Store Presence → Main Store Listing**

1. Click **"Store presence"** in left sidebar
2. Click **"Main store listing"**
3. Fill in:

   **App details:**
   - **App name**: `EGX Portfolio Tracker`
   - **Short description** (80 chars max):
     ```
     Track your EGX stock investments with real-time prices, charts, and performance analytics
     ```
   - **Full description** (4000 chars max):
     ```
     EGX Portfolio Tracker is a comprehensive Android app designed to help you manage and track your investments in the Egyptian Stock Exchange (EGX).

     Features:
     • Track multiple EGX stocks with real-time price updates
     • Automatic price fetching from TradingView and CNBC APIs
     • Portfolio analytics with beautiful charts and graphs
     • Dividend tracking and history
     • Performance analysis (weekly, monthly, 2-months, 50-days)
     • Average cost history tracking
     • Profit/Loss breakdown (price appreciation vs dividends)
     • Portfolio allocation donut chart
     • Privacy blur feature to hide amounts
     • Material Design 3 UI

     Perfect for investors who want to monitor their EGX portfolio performance, track dividends, and make informed investment decisions.
     ```

4. Click **"Save"** at the bottom

#### **7.2: Graphics (REQUIRED)**

1. Still in **"Main store listing"**, scroll to **"Graphics"**
2. You need:
   - **App icon**: 512x512 PNG (no transparency)
   - **Feature graphic**: 1024x500 PNG (Play Store banner)
   - **Phone screenshots**: At least 2 screenshots
     - Size: 1080x1920 or 1440x2560 pixels
     - Take screenshots of your app running on a device
   - **Tablet screenshots** (optional): 1200x1920

**How to get screenshots:**
- Run the app on your device
- Take screenshots of:
  1. Dashboard screen (showing portfolio overview)
  2. Stock detail screen
  3. Portfolio list screen
  4. Charts/analytics screen

**How to create app icon:**
- Use any image editor (Photoshop, GIMP, Canva, etc.)
- Create 512x512 PNG
- Use your app's branding/logo

3. Upload all graphics
4. Click **"Save"**

#### **7.3: Categorization**

1. Still in **"Main store listing"**, scroll to **"Categorization"**
2. Fill in:
   - **App category**: `Finance`
   - **Tags**: `Investment`, `Stocks`, `Portfolio`, `Finance`, `EGX`
3. Click **"Save"**

#### **7.4: Privacy Policy (REQUIRED)**

1. In left sidebar, go to **"Policy"** → **"Privacy policy"**
2. You need a privacy policy URL
3. Options:
   - **Option A**: Use a free generator:
     - Go to https://www.privacypolicygenerator.info/
     - Fill in your app details
     - Generate and host it (GitHub Pages, your website, etc.)
   - **Option B**: Use the template in `PRIVACY_POLICY_TEMPLATE.md`
     - Copy the content
     - Host it somewhere (GitHub Pages, Google Sites, etc.)
4. Enter the privacy policy URL
5. Click **"Save"**

---

### **STEP 8: Complete App Content**

#### **8.1: Content Rating**

1. Click **"Policy"** → **"App content"**
2. Click **"Start questionnaire"** under Content rating
3. Answer the questions:
   - **Category**: Finance/Investment app
   - **Does your app contain user-generated content?** → No
   - **Does your app allow users to interact?** → No
   - Continue answering...
4. Submit questionnaire
5. Wait for rating (usually instant)

#### **8.2: Target Audience**

1. Still in **"App content"**
2. Scroll to **"Target audience"**
3. Select: **"Everyone"** or appropriate age group
4. Click **"Save"**

#### **8.3: Data Safety**

1. Still in **"App content"**
2. Scroll to **"Data safety"**
3. Click **"Start"** or **"Manage"**
4. Answer questions:
   - **Does your app collect or share user data?** → No
   - **Does your app use sensitive permissions?** → No (we only use INTERNET)
   - Continue...
5. Click **"Save"**

---

### **STEP 9: Review All Sections**

1. Go back to **"Dashboard"** (left sidebar)
2. Check that all sections have green checkmarks ✅:
   - ✅ Store listing
   - ✅ App content
   - ✅ Production release
   - ✅ Content rating
   - ✅ Data safety
   - ✅ Privacy policy

---

### **STEP 10: Submit for Review**

1. Go to **"Production"** → **"Releases"**
2. You should see your release (1.0.0) saved
3. Click **"Review release"** or **"Start rollout to Production"**
4. Review the summary
5. Check the box: "I understand that..."
6. Click **"Start rollout to Production"**
7. Confirm submission

---

### **STEP 11: Wait for Review**

1. Your app status will change to **"In review"**
2. Review usually takes **1-3 days**
3. You'll receive an email when:
   - ✅ App is approved and published
   - ❌ App is rejected (with reasons)

---

## 📱 After Approval

Once approved:
- Your app will be live on Google Play Store
- Share the Play Store link: `https://play.google.com/store/apps/details?id=com.egx.portfoliotracker`
- Monitor reviews and ratings
- Respond to user feedback

---

## 🔄 Future Updates

When you want to update the app:

1. Update `versionCode` and `versionName` in `app/build.gradle.kts`:
   ```kotlin
   versionCode = 2  // Increment by 1
   versionName = "1.0.1"  // Your version
   ```

2. Build new AAB:
   ```powershell
   .\gradlew.bat bundleRelease
   ```

3. Upload new AAB to Play Console → Production → New release

4. Add release notes for the update

5. Submit for review

---

## ⚠️ Important Notes

- **Keystore password**: `TempPass123!` (SAVE THIS! You'll need it for all updates)
- **Keep keystore file safe**: `keystore/egx-portfolio-release.jks` (if lost, you can't update the app)
- **First review takes longer**: 1-3 days
- **Subsequent updates**: Usually faster (hours to 1 day)

---

## 🆘 Troubleshooting

**Problem**: "Upload failed"
- **Solution**: Check file size (should be ~3.93 MB), ensure good internet connection

**Problem**: "Missing required sections"
- **Solution**: Complete all sections with red X marks in Dashboard

**Problem**: "Privacy policy required"
- **Solution**: Add privacy policy URL in Policy → Privacy policy

**Problem**: "Screenshots required"
- **Solution**: Upload at least 2 phone screenshots in Store presence → Main store listing

---

**Good luck with your app launch! 🚀**


