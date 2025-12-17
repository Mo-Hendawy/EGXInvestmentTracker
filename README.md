# EGX Portfolio Tracker 📈

A modern Android app for tracking your Egyptian Stock Exchange (EGX) investments with beautiful charts, performance analytics, and smart insights.

## Features

### 📊 Portfolio Dashboard
- **Total Portfolio Value** - See your complete portfolio value at a glance
- **Profit/Loss Tracking** - Real-time P&L calculations with percentages
- **Visual Charts** - Donut charts for sector allocation and bar charts for role distribution
- **Top Performers** - Quickly identify your best and worst performing stocks

### 📱 Stock Management
- **70+ EGX Stocks** - Pre-loaded database with real EGX stock symbols and names (Arabic & English)
- **Easy Stock Selection** - Search and select from organized, sector-grouped stock list
- **Quick Price Updates** - Update current prices with one tap
- **Full CRUD Operations** - Add, edit, and delete holdings

### 🏷️ Smart Categorization
- **Roles**: Core, Income, Growth, Swing, Speculative
- **Status**: Hold, Add on Dips, Reduce, Exit, Review, Watch
- **Sectors**: Banking, Real Estate, Food & Beverage, Industrial, Healthcare, and more

### 📈 Analytics & Insights
- **Sector Performance** - See how each sector in your portfolio is performing
- **Portfolio Strategy Mix** - Visualize your investment strategy distribution
- **Action Insights** - Get notified about stocks needing review or exit
- **Performance Metrics** - Price change, percentage change, P/L per share

### 🎨 Beautiful UI
- **Modern Material 3 Design** - Clean, intuitive interface
- **Egyptian Gold Theme** - Inspired by Egyptian heritage
- **Dark Mode Support** - Easy on the eyes
- **Animated Charts** - Smooth, engaging visualizations

## Included EGX Stocks

The app comes pre-loaded with 70+ popular EGX stocks including:

| Symbol | Name | Sector |
|--------|------|--------|
| COMI | Commercial International Bank | Banking |
| TMGH | Talaat Moustafa Group | Real Estate |
| SWDY | El Sewedy Electric | Building Materials |
| JUFO | Juhayna Food Industries | Food & Beverage |
| EFID | EFG Hermes Holding | Financial Services |
| ETEL | Telecom Egypt | Telecommunications |
| ABUK | Abu Qir Fertilizers | Diversified |
| And many more... | | |

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Clean Architecture
- **Database**: Room
- **Dependency Injection**: Hilt
- **Navigation**: Jetpack Navigation Compose
- **Charts**: Custom Compose Canvas + Vico

## Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Installation

1. Clone the repository or copy the project folder
2. Open the project in Android Studio
3. Sync Gradle files
4. Run on emulator or physical device (API 26+)

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## Project Structure

```
app/
├── src/main/
│   ├── java/com/egx/portfoliotracker/
│   │   ├── data/
│   │   │   ├── local/          # Room database, DAOs
│   │   │   ├── model/          # Data classes (Stock, Holding, Transaction)
│   │   │   └── repository/     # Repository pattern
│   │   ├── di/                 # Hilt dependency injection
│   │   ├── navigation/         # Navigation setup
│   │   ├── ui/
│   │   │   ├── components/     # Reusable UI components
│   │   │   ├── screens/        # Dashboard, Portfolio, AddStock, StockDetail
│   │   │   └── theme/          # Colors, Typography, Theme
│   │   ├── viewmodel/          # ViewModels
│   │   ├── MainActivity.kt
│   │   └── PortfolioApp.kt
│   └── res/                    # Resources
└── build.gradle.kts
```

## Screenshots

The app features:
- 📊 Dashboard with portfolio overview and charts
- 📋 Portfolio list with filtering and sorting
- ➕ Stock picker with searchable EGX stocks
- 📈 Detailed stock view with edit capabilities

## Customization

### Adding More Stocks
Edit `Stock.kt` and add to the `EGXStocks.stocks` list:

```kotlin
Stock("SYMBOL", "English Name", "الاسم بالعربية", "Sector")
```

### Changing Colors
Edit `Color.kt` to customize the color scheme:
- Primary: Gold (#D4AF37)
- Secondary: Nile Blue (#1E5B8D)
- Profit: Green (#00C853)
- Loss: Red (#FF1744)

## Future Enhancements

- [ ] Live price fetching from EGX API
- [ ] Price alerts and notifications
- [ ] Dividend tracking
- [ ] Portfolio history and charts over time
- [ ] Export to CSV/Excel
- [ ] Backup/Restore functionality
- [ ] Watchlist feature
- [ ] News integration

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License.

---

**Made with ❤️ for Egyptian investors**
