# ☕ Brew Haven — Coffee Shop Sales & Receipt Management System

Brew Haven is a complete, modern, and dependency-free coffee shop Point-of-Sale (POS) and receipt management web application. Built with standard **Java**, semantic **HTML5**, modern **CSS3**, and beginner-friendly **vanilla JavaScript**, it stores all sales transactions permanently on the local computer using atomic file storage.

Designed specifically for individual university/college assignments, the project uses **zero external dependencies** (no Spring Boot, no Tomcat, no Servlets, no MySQL) and runs directly in **VS Code** using Java's built-in `com.sun.net.httpserver.HttpServer`.

---

## 🎨 Design & Theme

- **Café Name**: Brew Haven
- **Signature Heading**: *"Your Daily Dose of Happiness"*
- **Warm Café Color Palette**:
  - Cream: `#FFF8EE`
  - Coffee Brown: `#4B2E24`
  - Caramel: `#C58B55`
  - Light Beige: `#EADBC8`
  - Dark Text: `#29211D`
- **Features**: Responsive layout (Desktop, Tablet, Mobile), rounded cards and buttons, smooth hover lift transitions, printable thermal-style receipts, and real-time daily metrics.

---

## 📁 Project Structure

```
Website/
├── src/
│   ├── Main.java              # Application entry point; starts HttpServer
│   ├── Product.java           # Model representing menu items with BigDecimal prices
│   ├── SaleItem.java          # Model representing purchased line items & subtotals
│   ├── Sale.java              # Model representing completed sales with verified totals
│   ├── FileStorage.java       # Atomic file-based persistence engine (data/sales/)
│   ├── SalesService.java      # Business logic, BigDecimal math, promo validation
│   ├── JsonUtil.java          # Pure Java JSON serializer & parser (no external JARs)
│   ├── StaticFileHandler.java # Serves HTML, CSS, JS, and SVG images securely
│   └── ApiHandler.java        # Handles REST endpoints (/api/products, /api/sales, etc.)
├── public/
│   ├── index.html             # Homepage with brand story & featured drinks
│   ├── menu.html              # 10-item menu with category filters (PKR prices)
│   ├── cart.html              # Interactive shopping cart with +/- qty & subtotals
│   ├── checkout.html          # Order checkout, customer name, coupon discounts
│   ├── receipt.html           # Thermal-style printable receipt (window.print())
│   ├── history.html           # Permanent sales history & daily sales dashboard
│   ├── css/
│   │   └── style.css          # Warm café styling & print media stylesheet
│   ├── js/
│   │   ├── app.js             # Cart state, navigation badges, toast notifications
│   │   ├── menu.js            # Menu fetching from Java & category tabs
│   │   ├── cart.js            # Cart quantity increments/decrements & calculations
│   │   ├── checkout.js        # Form validation, promo application & POST submission
│   │   └── history.js         # Daily summary metrics & real-time search
│   └── images/
│       ├── logo.svg           # Brew Haven artisan logo
│       ├── hero_coffee.svg    # Homepage hero illustration
│       ├── espresso.svg       # Demitasse espresso illustration
│       ├── americano.svg      # Bold Americano illustration
│       ├── cappuccino.svg     # Frothy cappuccino illustration
│       ├── latte.svg          # Layered café latte illustration
│       ├── mocha.svg          # Chocolate mocha illustration
│       ├── caramel_macchiato.svg # Caramel drizzle macchiato illustration
│       ├── iced_coffee.svg    # Chilled iced brew with straw illustration
│       ├── hot_chocolate.svg  # Belgian cocoa mug illustration
│       ├── chocolate_muffin.svg  # Oven-fresh bakery muffin illustration
│       └── chocolate_brownie.svg # Gourmet chocolate brownie illustration
├── data/
│   └── sales/                 # Individual transaction JSON files (e.g. BH-20261119-1001.json)
└── README.md                  # Comprehensive setup & viva presentation guide
```

---

## 🚀 How to Run the Project in VS Code

### Step 1: Open the Project in VS Code
1. Launch **Visual Studio Code**.
2. Click **File** → **Open Folder...**
3. Select this folder: `c:\Users\User\OneDrive\Documents\Discrete Sructures\Website`.

### Step 2: Verify Your JDK Installation
Open the integrated terminal in VS Code (`Ctrl + ` `~` or **Terminal** → **New Terminal**) and run:
```powershell
javac -version
java -version
```
> **Note**: You have **Java 25 (Oracle JDK 25 LTS)** installed. Any standard JDK version 11, 17, 21, or 25 works out-of-the-box.

### Step 3: Compile the Java Source Files
In the VS Code terminal, run:
```powershell
javac -d bin src/*.java
```
This compiles all Java classes into the `bin/` directory in 1-2 seconds with zero external dependencies.

### Step 4: Start the Built-in Java Server
Run:
```powershell
java -cp bin src.Main
```

You will see the welcome banner:
```
=============================================================
            ☕ BREW HAVEN COFFEE SHOP SERVER ☕             
      Sales and Receipt Management System (University Lab)   
=============================================================
 [✔] Status: Server is RUNNING
 [✔] Local URL: http://localhost:8080
 [✔] Static Assets: public/
 [✔] Permanent File Storage: data/sales/
 [✔] Engine: Java Built-in com.sun.net.httpserver.HttpServer
 [✔] Dependencies: 100% Standard Java (Zero External JARs)
-------------------------------------------------------------
 Open your browser and navigate to: http://localhost:8080
 Press Ctrl + C in the terminal to stop the server anytime.
=============================================================
```

### Step 5: Open in Your Browser
Open your browser (Google Chrome, Microsoft Edge, Firefox) and visit:
👉 **[http://localhost:8080](http://localhost:8080)**

### Step 6: Stopping and Restarting
- **To Stop**: Press `Ctrl + C` in the terminal running the server.
- **To Restart**: Run `java -cp bin src.Main` again.

### Step 7: Verifying Data Persistence Across Server Restarts
1. Open the website, add products to your cart, and complete a sale on the Checkout page.
2. Note the generated receipt ID (e.g. `BH-20261119-1003`).
3. View the sale in **Sales History** and **Daily Summary**.
4. Go to your terminal and **stop the server** (`Ctrl + C`).
5. Close your browser.
6. Open the terminal and restart the server: `java -cp bin src.Main`.
7. Refresh or open [http://localhost:8080/history.html](http://localhost:8080/history.html).
8. Notice that your completed sale and daily totals are still there! Check the `data/sales/` directory to see the corresponding permanent `.json` file.

---

## ☕ Menu Items & Pricing (PKR)

| ID | Item Name | Category | Unit Price |
|:--:|:---|:---|:---:|
| 1 | **Espresso** | Hot Coffees | Rs. 250 |
| 2 | **Americano** | Hot Coffees | Rs. 300 |
| 3 | **Cappuccino** | Hot Coffees | Rs. 400 |
| 4 | **Café Latte** | Hot Coffees | Rs. 420 |
| 5 | **Mocha** | Hot Coffees | Rs. 450 |
| 6 | **Caramel Macchiato** | Hot Coffees | Rs. 480 |
| 7 | **Iced Coffee** | Cold Brews & Iced | Rs. 350 |
| 8 | **Hot Chocolate** | Bakery & Treats | Rs. 380 |
| 9 | **Chocolate Muffin** | Bakery & Treats | Rs. 220 |
| 10 | **Chocolate Brownie** | Bakery & Treats | Rs. 250 |

---

## 🎟️ Promotional Discount Coupons (For Testing)

On the **Checkout** page, you can apply these discount codes:
- `WELCOME10`: **10% discount**
- `STUDENT15`: **15% discount**
- `BREW20`: **20% discount**

---

## 🌐 HTTP Endpoints Reference

| Method | Endpoint | Description |
|:---|:---|:---|
| `GET` | `/` or `/index.html` | Serves the homepage |
| `GET` | `/menu.html` | Serves the interactive menu |
| `GET` | `/cart.html` | Serves the shopping cart |
| `GET` | `/checkout.html` | Serves the order checkout page |
| `GET` | `/receipt.html?id={receiptId}` | Serves the printable sale receipt |
| `GET` | `/history.html` | Serves sales records & daily dashboard |
| `GET` | `/api/products` | Returns JSON array of all menu items |
| `POST` | `/api/sales` | Submits an order, verifies prices, and saves to file |
| `GET` | `/api/sales` | Returns all completed sales from disk |
| `GET` | `/api/sales?id={receiptId}` | Returns single transaction details for receipt |
| `GET` | `/api/summary` | Returns today's total revenue, orders, and items sold |

---

## 🛠️ Troubleshooting

- **Port 8080 already in use?**
  The server automatically detects if port 8080 is busy and tries 8081, 8082, etc. Look at the console output to see which port was bound (e.g., `http://localhost:8081`). You can also specify an exact port manually:
  ```powershell
  java -cp bin src.Main 9000
  ```
- **Changes in CSS/JS not reflecting in browser?**
  Hard-refresh your browser by pressing `Ctrl + F5` (or `Shift + F5`) to bypass browser caching.
- **Compilation error "file not found: src/*.java"?**
  Ensure your terminal is in the project root folder `c:\Users\User\OneDrive\Documents\Discrete Sructures\Website`. Run `pwd` or `dir` to verify.

---

## 🎓 University Viva & Presentation Guide

### 1. How does the system work?
Brew Haven uses a clean client-server architecture:
- **Client (Frontend)**: HTML5 creates semantic structure, CSS3 provides the warm coffee aesthetic and print stylesheet, and JavaScript handles dynamic UI actions (adding items to cart, adjusting quantities, calculating live previews).
- **Server (Backend)**: Java's built-in `HttpServer` listens for incoming HTTP requests. When a user checks out, JavaScript sends the cart items to `POST /api/sales`.
- **Authoritative Business Logic**: The Java backend **never trusts client totals**. It looks up genuine product prices in its catalog, computes subtotals and discounts using Java's `BigDecimal` to prevent floating-point rounding errors, generates a unique receipt ID, and saves the sale to disk before returning a success status.

### 2. Purpose of Each Java Class
- `Product.java`: Data model holding product ID, name, description, category, unit price (`BigDecimal`), and image path.
- `SaleItem.java`: Represents a single line item in a completed transaction (unit price, quantity, line subtotal).
- `Sale.java`: Represents a finalized sale with receipt ID, timestamp, guest name, dining type, item list, subtotal, discount, and grand total.
- `SalesService.java`: Core business layer. Validates order quantities, looks up product prices, computes discount percentages, generates unique receipt IDs (`BH-YYYYMMDD-XXXX`), and calculates daily sales metrics.
- `FileStorage.java`: Persistence manager. Reads and writes transaction files in `data/sales/`. Uses **atomic file renaming** (`.tmp` → `.json`) to avoid file corruption if power drops during a write.
- `JsonUtil.java`: A custom, lightweight JSON serializer and parser written entirely in standard Java so the student does not need third-party libraries like Gson or Jackson.
- `StaticFileHandler.java`: Safely serves static website assets (`.html`, `.css`, `.js`, `.svg`) and protects against directory traversal attacks.
- `ApiHandler.java`: Dispatches REST API calls (`/api/products`, `/api/sales`, `/api/summary`) with proper HTTP status codes and CORS headers.
- `Main.java`: Launches `com.sun.net.httpserver.HttpServer`, registers context routes, configures multithreading, and displays the console welcome banner.

### 3. How do the Frontend and Java Backend communicate?
Communication occurs over HTTP:
- The browser uses the standard JavaScript `fetch()` API.
- For retrieving the menu: `fetch('/api/products')` sends a `GET` request. The Java `ApiHandler` responds with `Content-Type: application/json` containing the product list.
- For checkout: `fetch('/api/sales', { method: 'POST', body: JSON.stringify(orderData) })` sends the cart items. Java validates the order, computes `BigDecimal` totals, writes to disk, and sends back `HTTP 201 Created` with the confirmed receipt.
- Once the browser receives the confirmed receipt ID, it empties `localStorage` and redirects to `receipt.html?id=...`.

### 4. Why use `BigDecimal` instead of `double` or `float` for prices?
Binary floating-point types (`float` and `double`) cannot accurately represent fractions like `0.10` due to IEEE-754 representation (e.g., `0.1 + 0.2 = 0.30000000000000004`). In financial and sales systems, this introduces penny discrepancies. Java's `BigDecimal` provides arbitrary-precision arithmetic with explicit rounding modes (`RoundingMode.HALF_UP`), ensuring mathematical accuracy for college-grade accounting.

### 5. Why is file-based storage better than browser localStorage for sales history?
`localStorage` only exists inside a single user's browser tab cache. If the browser cache is cleared, another computer accesses the site, or a manager views sales from a different terminal, `localStorage` data is invisible or lost. Storing sales records as individual JSON files on the local filesystem (`data/sales/`) ensures that records are **durable, permanent, server-side, and survive browser refreshes or server reboots**.

### 6. Viva Questions & Quick Answers

| Question | Quick Answer for Presentation |
|:---|:---|
| **Why did you use `HttpServer` instead of Tomcat or Spring Boot?** | "Java includes a production-tested HTTP server in `com.sun.net.httpserver`. Using it eliminates heavy dependencies, keeps the code lightweight and transparent, and allows the project to run on any computer with just a standard JDK." |
| **How do you prevent duplicate receipt IDs?** | "`SalesService` uses an `AtomicInteger` initialized with the highest existing sequential ID found in `data/sales/` combined with the current date `BH-YYYYMMDD-XXXX`. `FileStorage` also checks `Files.exists()` before saving." |
| **How does the system prevent corrupt files if the server shuts down mid-write?** | "`FileStorage` implements an atomic write pattern: it writes first to a temporary file (`.tmp`) and then invokes `Files.move(..., ATOMIC_MOVE)`, which is an atomic filesystem operation." |
| **Why not calculate the total in JavaScript and send it to Java?** | "Client-side calculations can be manipulated by tampering with JavaScript or DevTools. Secure systems always calculate authoritative prices and totals on the backend." |
| **How does receipt printing work?** | "The Receipt page uses JavaScript `window.print()`. A dedicated `@media print` CSS block hides the header, buttons, and navigation, printing only the clean thermal receipt slip." |

### 7. Files to Review Before Your Presentation
1. `src/SalesService.java` — Shows your business logic, order validation, and `BigDecimal` calculations.
2. `src/FileStorage.java` — Shows how you permanently store and retrieve sales from files without databases.
3. `src/Main.java` — Shows how you configure the Java built-in HTTP server.
4. `public/js/checkout.js` — Shows how the frontend connects to the backend API.
5. `public/css/style.css` — Shows your design theme, responsive grid, and `@media print` rules.
