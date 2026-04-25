# Portfolio Viewer: Detailed Project Deep Dive (Hindi)

## 1. Project ka High-Level Purpose

Yeh application ek live portfolio valuation simulator hai.

- Initial positions CSV se load hoti hain.
- Stocks aur options definitions H2 in-memory database se load hoti hain.
- Market prices random-walk logic se continuously update hote hain.
- Har price move par poora portfolio revalue hota hai.
- Updated portfolio JMS topic par publish hota hai.
- Console subscriber live NAV print karta rehta hai.

Simple words me: yeh ek near real-time pricing and revaluation engine demo hai.

## 2. Tech Stack Reality (current codebase)

- Language: Java 8 source compatibility
- Framework: Spring Boot 2.7.17
- Build tool: Gradle wrapper
- Messaging: Spring JMS + embedded ActiveMQ
- Database: H2 in-memory
- ORM/Data: Spring Data JPA
- Math model: Apache Commons Math (normal distribution)
- Tests: JUnit 5 + Mockito

## 3. Package-by-Package Meaning

### 3.1 Application bootstrap

- src/main/java/com/crypto/portfolio/PortfolioViewerApplication.java
  - Spring context start karta hai.
  - Derivatives cache initialize karta hai.
  - Position snapshot load karta hai.
  - 4 second wait ke baad market publishing start karta hai.

Use-case: deterministic startup order maintain karna.

### 3.2 cache

- src/main/java/com/crypto/portfolio/cache/SecurityDerivativesCache.java
  - Options/security definitions ko memory map me load karta hai.

Use-case: fast lookup while repricing options.

### 3.3 calculation

- src/main/java/com/crypto/portfolio/calculation/CalculateStockPrice.java
  - Stock price simulation/random movement logic.
- src/main/java/com/crypto/portfolio/calculation/CommonCalculations.java
  - Generic financial calculations (position value etc.).
- src/main/java/com/crypto/portfolio/calculation/OptionsPriceCalculator.java
  - Black-Scholes style call/put pricing model.

Use-case: valuation engine ka mathematical core.

### 3.4 config

- src/main/java/com/crypto/portfolio/config/AppConfig.java
  - App-level bean/config wiring.

Use-case: framework behavior ko assemble karna.

### 3.5 constants

- src/main/java/com/crypto/portfolio/constants/UtilityConstant.java
  - Risk-free rate, formatting constants, separators etc.

Use-case: shared magic numbers ko centralize karna.

### 3.6 domainValue

- src/main/java/com/crypto/portfolio/domainValue/PositionType.java
- src/main/java/com/crypto/portfolio/domainValue/SecurityType.java

Use-case: domain enums + string parsing safety.

### 3.7 exception

- src/main/java/com/crypto/portfolio/exception/PortfolioSnapshotMissingException.java

Use-case: CSV/input related startup failure signal.

### 3.8 model

- src/main/java/com/crypto/portfolio/model/Position.java
  - Input position (symbol, size, long/short, type)
- src/main/java/com/crypto/portfolio/model/PortfolioElement.java
  - Revalued item (price, value, quantity)
- src/main/java/com/crypto/portfolio/model/Portfolio.java
  - Aggregate snapshot (stocks, options, NAV)
- src/main/java/com/crypto/portfolio/model/Stock.java
  - Stock master + simulation params
- src/main/java/com/crypto/portfolio/model/SecurityDefinition.java
  - Option contract metadata

Use-case: domain state representation.

### 3.9 repo

- src/main/java/com/crypto/portfolio/repo/StockRepository.java
- src/main/java/com/crypto/portfolio/repo/OptionsRepository.java

Use-case: H2 database se read operations.

### 3.10 service

- src/main/java/com/crypto/portfolio/service/MarketService.java
- src/main/java/com/crypto/portfolio/service/MarketServiceImpl.java

Use-case: repository calls ko service abstraction dena.

### 3.11 publisher

- src/main/java/com/crypto/portfolio/publisher/MarketDataPublisher.java
  - Har stock ke liye timer task schedule karta hai.
  - Naya stock price generate karke JMS destination par publish karta hai.

Use-case: synthetic live market feed create karna.

### 3.12 subscriber

- src/main/java/com/crypto/portfolio/subscriber/PortfolioSubscriber.java
  - Har stock topic subscribe karta hai.
  - Incoming price ko PortfolioViewer update method me bhejta hai.
- src/main/java/com/crypto/portfolio/subscriber/ConsoleSubscriber.java
  - Final portfolio topic subscribe karke pretty print karta hai.

Use-case: event-driven repricing and output pipeline.

### 3.13 viewer

- src/main/java/com/crypto/portfolio/viewer/PortfolioViewer.java
  - Position CSV read karta hai.
  - Initial portfolio build karta hai.
  - Price changes par stock/options recalculate karta hai.
  - NAV recompute karke portfolio topic par publish karta hai.

Use-case: core orchestration and business calculation entry point.

## 4. Resource Files ka Role

- src/main/resources/application.yaml
  - H2 datasource, JPA, JMS settings.
- src/main/resources/data.sql
  - Startup par stock + option reference data seed.
- src/main/resources/position/position.csv
  - Initial portfolio positions.

## 5. Test Coverage Snapshot

- src/test/java/com/crypto/portfolio/calculation/
  - Pricing/calculation classes ke unit tests.

Current direction: business flow integration tests limited lagte hain; CI hardening ke liye aur tests add karne honge.

## 6. Current Practical Use Cases

1. Portfolio simulation demo for interviews/training.
2. Event-driven revaluation architecture samjhane ke liye reference project.
3. JMS + pricing engine + seed-data based prototype.
4. Future productionization base (Docker/K8s/CI/CD).

## 7. Important Risks Jo Abhi Dikhti Hain

1. positionSnapshot CSV path hardcoded source path par hai, jar runtime me issue ho sakta hai.
2. data.sql me option maturities past dates me ho sakti hain, jisse option pricing unrealistic/negative-time scenarios de sakta hai.
3. Java 8 source target hai, lekin GitHub workflow JDK 17 use kar raha hai; compatibility check needed.
4. MarketDataPublisher me recurring Timer scheduling hai; graceful shutdown strategy needed.

## 8. Next Technical Milestones

1. Runtime-safe resource loading (classpath based).
2. Docker image build and local validation.
3. Kubernetes manifests (Deployment, Service, ConfigMap, Secret).
4. Git-based CI pipeline with test, build, image publish, deploy gates.
5. Versioning and release discipline.
