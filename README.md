# Honeywell Test Automation Challenge - Track & Trace (API + Web)

## 📋 Project Overview
This project is a comprehensive **Full-Stack Test Automation Solution** designed for the **Track & Trace** system. It simulates the end-to-end business flow of shipping cigarette pallets, ensuring compliance with GS1 standards across both **API (Backend)** and **Web Portal (Frontend)** layers.

The solution addresses critical challenges such as **asynchronous processing**, **dynamic data generation**, **stock exhaustion**, and **data continuity between API and Web tests**.

## 🛠 Tech Stack
*   **Language:** Java 17+
*   **API Testing:** Rest Assured (5.3.0)
*   **Web Testing:** Selenium WebDriver (4.16) & WebDriverManager
*   **Test Framework:** TestNG
*   **Data Management:** Apache POI (Excel), Lombok, Singleton Pattern
*   **Build Tool:** Maven
*   **Formats:** JSON, XML (SOAP)

## 🏗 Project Architecture
The framework follows the **Service Object Model** for API and **Page Object Model (POM)** for Web, ensuring modularity and maintainability.

```text
src/test/java
├── config       # Configuration Reader (Security & Environment variables)
├── models       # POJO classes for JSON Serialization (Builder Pattern)
├── pages        # Web Page Objects (POM) - Login, Home
├── services     # Business Logic Layer (API Calls, Endpoints)
├── tests        # Test Scripts (API & Web Integration)
└── utils        # Utilities (ExcelUtils, TestUtils, TestContext for Data Sharing)

src/test/resources
├── testData     # Excel files for Data-Driven Testing
├── *.xml        # SOAP XML Templates
├── testng.xml   # Test Suite Configuration
└── config.properties # Credentials and Base URLs
```

## 🚀 Key Technical Solutions

### 1. Handling Asynchronous Validation (Polling Mechanism)
The system processes shipment messages asynchronously, meaning an immediate `HTTP 200` response does not guarantee business success.
*   **Solution:** A **Polling Mechanism** was implemented in the `ValidationService`.
*   The test polls the validation endpoint every 10 seconds.
*   It waits up to a configurable threshold (e.g., 5 minutes) for the status `OK` and verifies the specific `Transaction ID` in the response body to ensure no false positives.

### 2. Smart Stock Retrieval (Dynamic Data)
The QA environment has a strict limit on available pallets (5 per warehouse). Hardcoding SSCCs causes tests to fail after a few runs once the stock is depleted.
*   **Solution:** A `PalletService` was created to query the **Pallet Manager API** before every test run.
*   It dynamically iterates through known product codes (e.g., `9990001`, `99990001`) to find an available, fresh SSCC.
*   The test adapts to use whichever product is currently in stock.

### 3. API-to-Web Data Transfer (Context Sharing)
*  The Web test needs to verify the exact shipment created by the API test.
*   **Solution:** A TestContext class (Singleton/Static) acts as a bridge. The API test writes the generated SSCC to this context, and the Web test reads it dynamically to perform the search in the UI.

### 4. Failover & Resilience (Dummy Data Injection)
If the test environment is completely exhausted of stock (common in shared QA environments), the test would normally crash at the start.
*   **Solution:** A `try-catch` block monitors the stock retrieval process.
*   If no stock is found, the system logs a warning and injects **Synthetic (Dummy) Data**.
*   This allows the test to verify the **Code Logic** (Order -> Delivery -> Shipment flow) even if the **Business Validation** fails due to environment data constraints.

## 🧪 How to Run

### Prerequisites
*   Java JDK 11 or higher
*   Maven

## 🔐 Configuration & Security

This project uses a `config.properties` file for credentials. For security reasons, the actual file is **not included** in the repository.

### How to run locally:
1. Go to `src/test/resources/`.
2. Rename `config.properties.template` to `config.properties`.
3. Open the file and fill in the credentials provided in the challenge documentation.
    - `app.password`
    - `shipment.password`

### Execution
Run the tests via Maven command line:

```bash
# To run E2E Flow (Fresh Data + Web UI)
mvn clean test -DsuiteXmlFile=testng-e2e.xml

# To run API Test (Excel Data Driven)
mvn clean test -DsuiteXmlFile=testng.xml
```

### Reporting

```bash
mvn allure:serve
