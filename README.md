# Venkateshwara Photos – Shop Management System (Phase 1, offline)

Android app (Java, XML layouts, MVVM, Room, Material 3) for managing a retail photo / prasad / album shop.
Package: `com.venkateshwaraphotos.shop` · minSdk 24 · targetSdk/compileSdk 34 · Gradle Kotlin DSL · no Kotlin source, no Compose.

## 1. Open and run
1. Install **Android Studio** (Koala or newer) – it bundles JDK 17+ and the Android SDK.
2. `File > Open` and choose this folder (`VenkateshwaraPhotos`). Let Gradle sync (needs internet the first time).
   * `local.properties` is created automatically by Android Studio.
   * If it asks about the Gradle wrapper, accept. `gradle-wrapper.properties` points at Gradle 8.7 (required by AGP 8.5.2).
3. Run the `app` configuration on an emulator/device with Android 7.0 (API 24) or newer.

Command line: `./gradlew assembleDebug` (JDK 17, `ANDROID_HOME` set).

## 2. Demo logins (created on first launch with sample data)
| Role | Username | Password |
|---|---|---|
| Owner | `owner` | `owner123` |
| Employee | `ramesh` | `emp12345` |
| Employee | `suresh` | `emp12345` |

**Change these before real use** (menu ⋮ → *Change password*; owner can reset employee passwords in *Employees*).
Sample data = 5 categories, 6 products-worth of variants, 4 photo designs, opening stock, 3 sales and 2 expenses.
To start with an empty shop, uninstall the app, delete `SeedData.seedIfEmpty(...)` call in `ShopApplication`, and create your own owner account in code/DB (see Limitations).

## 3. What is implemented
| Area | Status |
|---|---|
| Login, roles (OWNER / EMPLOYEE), role guards on every screen and on every owner-only repository write | ✅ |
| Categories, products, variants (unlimited, nothing hard-coded; active/inactive) | ✅ owner |
| Variant fields: ID, name, size, frame type, cost, selling, minimum price, stock, active | ✅ |
| Two tracking modes per product: **variant-level** or **photo-design-level** stock (chosen at product creation, fixed afterwards) | ✅ |
| New Sale / cart: category → product → variant (→ design) → qty → cart → Cash/UPI/Other → complete | ✅ |
| Price rule: price editable but never below the variant's minimum (checked in UI, ViewModel and inside the DB transaction) | ✅ |
| Sale = ONE Room transaction: sale + items + stock deduction + stock movements + audit entry; any failure rolls back everything | ✅ |
| Historical snapshots of sale price and cost price per sale item; employee id + timestamp saved | ✅ |
| Insufficient stock blocks the sale (atomic `UPDATE ... WHERE quantity + delta >= 0` as a second line of defence) | ✅ |
| No printed bills / invoices | ✅ (none generated) |
| Purchases (owner): supplier, date, multiple items, cost, total – stock increased in one transaction | ✅ |
| Inventory: current stock, low-stock alerts (per-variant threshold), movement history, owner stock adjustments with mandatory reason | ✅ |
| Sales history: owner sees all (date + employee filter); employee sees only own; sale detail screen | ✅ |
| Expenses (owner): category, description, amount, date; monthly list; edit / delete | ✅ |
| Dashboard (owner): today's sales & gross profit, month expenses & net profit, total inventory (+value at cost), low-stock count, recent transactions | ✅ |
| Dashboard (employee): own sales today, low-stock count, own recent sales (no profit/cost anywhere) | ✅ |
| Reports (owner): daily / weekly (Mon–Sun) / monthly with prev/next, revenue, COGS, gross, expenses, net, payment-mode split, top 5 items | ✅ |
| Employee management (owner): create, rename, disable, reset password | ✅ |
| Audit log (written for every important action; owner can view latest 100 via ⋮ menu) | ✅ |
| Firebase | ❌ **not implemented** – only prepared (see §6) |

Formulas: `Gross profit = Revenue − COGS` (COGS from per-sale cost snapshots). `Net profit = Gross profit − Expenses`.
Purchases are stock, **not** expenses, so cost is never double counted.

Dashboard note: "Today" = sales and gross profit; "This month" = expenses and net profit (so expense totals are meaningful).

## 4. Architecture
```
Activity (XML)  →  ViewModel  →  Repository  →  DAO  →  Room (SQLite)
```
* `data/entity` – 14 Room entities (Users, Categories, Products, Variants, PhotoDesigns, VariantStock, DesignStock, Sales, SaleItems, Purchases, PurchaseItems, Expenses, StockMovements, AuditLogs) with foreign keys and indices.
* `data/dao` – 7 DAOs. `data/model` – query result POJOs.
* `data/repository` – business rules. `StockOps` is the only code that changes stock; `SaleProcessor` is the sale transaction; `SeedData` creates sample data.
* `util/AppExecutors` – single-thread `ExecutorService` for all DB work (serialises writes). Reads are Room `LiveData` or run on the executor. No main-thread DB access (`allowMainThreadQueries` is not used).
* `ui/` – activities, `RowAdapter` (one generic RecyclerView adapter used by all lists), `FormDialog` (code-built add/edit forms), `ItemSelector` (cascading drop-downs).
* Stock invariant: for design-tracked products `VariantStock.quantity` always equals the sum of that variant's `DesignStock` rows.

## 5. Folder structure
```
VenkateshwaraPhotos/
├─ build.gradle.kts, settings.gradle.kts, gradle.properties, gradlew(.bat), gradle/wrapper/
└─ app/
   ├─ build.gradle.kts, proguard-rules.pro
   └─ src/main/
      ├─ AndroidManifest.xml
      ├─ res/ (layout, drawable, menu, values)
      └─ java/com/venkateshwaraphotos/shop/
         ├─ ShopApplication.java
         ├─ data/ (Const, AppDatabase, entity/, dao/, model/, repository/)
         ├─ util/ (AppExecutors, Callback, SessionManager, PasswordUtil, DateUtil, Fmt, ShopException)
         ├─ viewmodel/ (10 ViewModels)
         └─ ui/ (13 activities + helpers, adapter/RowAdapter)
```

## 6. Firebase preparation (NOT implemented)
* The UI never touches DAOs; everything goes through the repository classes in `data/repository`.
* To add Firebase later: implement Firebase Auth inside `UserRepository.login/createEmployee`, and add Firestore sync inside the other repositories (write to Room first, then sync). Primary keys are `long`; map them to Firestore document ids when you design the sync.
* No Firebase dependency, config file or credentials are included, and cloud sync is **not** available.

## 7. Verification status (honest report)
Done in the build environment:
* All 83 Java files parse (syntax) with a Java parser; all imports of project classes resolve.
* All XML files are well-formed; every `R.id/layout/drawable/color/menu/string` reference, `@style`/`@drawable` reference in XML, every manifest activity and every Intent extra key was cross-checked by script.
* All 41 Room `@Query` statements were executed against a SQLite schema generated from the entity classes (catches wrong table/column names and SQL syntax errors).

**NOT done – please read:**
* **No Gradle build and no run on a device/emulator were possible** (the sandbox cannot reach Google's Maven repository or the Android SDK). The code has therefore never been compiled by `javac`/Room's annotation processor. Expect that the first sync/build may surface a few small compile or Room-validation errors; they should be quick fixes.
* No unit tests or UI tests were written. The transactional sale/purchase logic has not been exercised at runtime.
* Layouts have not been visually checked on different screen sizes (they use match_parent/weights/scroll views, so they should adapt).

## 8. Known limitations / not in Phase 1
* No sign-up or "first-run owner setup"; the owner account comes from the sample seed. Passwords are stored as salted PBKDF2 hashes; this is local-only security.
* Product tracking mode cannot be changed after creation (prevents inconsistent stock). Variants/products/categories are deactivated, not deleted, so history stays intact.
* Services (e.g. passport photo printing) still use stock counts (seeded with a large quantity).
* A purchase does not auto-update the variant's cost price; the owner edits cost prices in Variants. Cost snapshots on future sales use the then-current variant cost price.
* Inactive variants cannot be selected in Purchases (re-activate first).
* No backup/export, no multi-currency, no returns/refunds, no discount-by-percentage, UI strings are hard-coded in English (no localisation).
* Quantities are whole units; money uses `double` rounded to 2 decimals.
