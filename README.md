# StockFlow — Clothing Inventory Management (Android)

StockFlow is a native Android app for managing a clothing inventory, built for the
FNI 421 Phase 1 project with **Kotlin**, **Jetpack Compose** and a **Room** database.

## Features

| Screen | What it does |
|---|---|
| Home | Category chips (All, T-Shirts, Shirts, Hoodies, Pants, Jackets, Accessories), product cards (name, category, color, size, SKU, quantity, price), **Add Product** button |
| Search | Search by product name or SKU, results count, empty state |
| Favorites | Products marked with the heart icon (stored in Room) |
| Settings | Theme (System Default / Light / Dark), Room statistics, About section |
| Add / Edit Product | Validated form; quantity and price cannot be negative; SKU must be unique; optional photo |
| Product Details | All fields, Favorite, Edit, Share (`Intent.ACTION_SEND`), Delete with confirmation, Stock In / Stock Out (never below zero) |

## How the requirements are covered

| Requirement | Where |
|---|---|
| Jetpack Compose | All screens in `ui/screens`, components in `ui/components` |
| Intent | `util/ShareHelper.kt` — `Intent.ACTION_SEND` + `Intent.createChooser` |
| Responsive UI | `ProductGrid` uses `GridCells.Adaptive`, details and form are width-limited on tablets |
| Kotlin lambdas, `val` / `var` | Lambda callbacks such as `onProductClick: (Int) -> Unit`, `saveProduct(onSuccess, onError)`; `var ... by rememberSaveable` for form state, `val` everywhere else |
| Room + CRUD | `data/Product.kt` (`@Entity`), `data/ProductDao.kt` (`@Insert`, `@Query`, `@Update`, `@Delete`), `data/AppDatabase.kt` |
| Two-page ACM abstract | `docs/StockFlow_ACM_Abstract.tex` (source) and `docs/StockFlow_ACM_Abstract.pdf` |

## Project structure

```
app/src/main/java/com/stockflow/app/
├── data/          Product (entity), ProductDao, AppDatabase, ThemePreferences (DataStore)
├── repository/    ProductRepository
├── viewmodel/     ProductViewModel, SettingsViewModel
├── navigation/    Screen routes, BottomNavBar, StockFlowNavHost
├── ui/screens/    Home, Search, Favorites, Settings, ProductDetails, ProductForm
├── ui/components/ ProductCard, ProductGrid, CategoryFilterRow, StockBadge, EmptyState, ProductImage
├── ui/theme/      Colors, typography, StockFlowTheme
├── util/          ShareHelper (Intent), ImageStorage, price formatting
├── MainActivity.kt
└── StockFlowApplication.kt
```

## Build and run

1. Install a recent **Android Studio** (it includes the JDK and Android SDK).
2. **File → Open** and choose the `StockFlow` folder. Let Gradle sync finish
   (it downloads Gradle 8.14.3 and the libraries the first time).
   If Studio asks to install Android SDK Platform 36, accept.
3. Pick an emulator or a phone (Android 8.0 / API 26 or newer) and press **Run**.

Command line alternative: `./gradlew assembleDebug` (needs `ANDROID_HOME` or a `local.properties` with `sdk.dir`).

Main versions: AGP 8.11.1, Kotlin 2.1.21, KSP 2.1.21-2.0.1, Compose BOM 2025.06.01,
Room 2.7.2, Navigation Compose 2.9.1 (see `gradle/libs.versions.toml`).

## Before you submit

- Put your team names in `app/src/main/res/values/strings.xml` (`about_team`) and in
  the `\author{...}` blocks of `docs/StockFlow_ACM_Abstract.tex`.
- Rebuild the abstract PDF: upload the `.tex` and `references.bib` to Overleaf
  (it has the ACM `acmart` template) and download the PDF, or run
  `pdflatex`, `bibtex`, `pdflatex`, `pdflatex` locally.

## Upload to GitHub

The folder is already a Git repository with one commit. To publish it:

1. Create an empty repository on GitHub (no README, no .gitignore).
2. In the `StockFlow` folder run:
   ```
   git remote add origin https://github.com/<user>/<repo>.git
   git branch -M main
   git push -u origin main
   ```
3. Add teammates as collaborators. Each member should commit and push their own
   part so that individual contributions are visible.
