# 🏦 SwiftCodes Application
## Project Overview
SwiftCodes is a web application created as part of a recruitment assignment for an internship at Remitly for managing SWIFT (Society for Worldwide Interbank Financial Telecommunication) codes, which are standard identifiers for banks worldwide. The application provides comprehensive functionality for:
* Searching SWIFT code details for specific banks
* Retrieving SWIFT codes for entire countries
* Adding new SWIFT codes
* Deleting existing SWIFT codes
## Technologies Used
* Spring Boot (Java)
* PostgreSQL as the low-latency database
* Docker for containerization
* Maven for dependency management
### Key Components
1. **Data Models**:
   * `Country`: Represents a country with an ISO2 code
   * `SwiftCode`: Stores SWIFT code information, including relationships between headquarters and branch banks
2. **Controllers**:
   * `SwiftCodeController`: Exposes REST interface
3. **Services**:
   * `SwiftCodeService`: Implements business logic
   * `SwiftCodeParser`: Handles data import from TSV files to appropriate format
### Note⚠️
The SwiftCodeParser class violates the single responsibility principle in a way, because it is responsible for parsing the tsv file as well as saving this data into the database. However splitting this class into two separate classes is not a good idea, because then the whole input file would have to be converted into DTOs and stored in memory, which might lead to OOM error for large files.

## Functionality
### SWIFT Code Operations
1. **Retrieve SWIFT Code Details**
   * Returns complete bank information
   * For headquarters banks, displays a list of branches
2. **Retrieve Country SWIFT Codes**
   * Returns all codes for a given country
3. **Add SWIFT Code**
   * Validates code format
   * Automatically links to headquarters bank
   * Optional country creation
4. **Delete SWIFT Code**
   * Automatically cleans up branch relationships
## Requirements
* Java 21
* Docker
* Docker Compose
* Maven
## Installation and Running
### Using Docker Compose
```bash
# Clone the repository
git clone https://github.com/BartoszMordarski/SwiftCodeRestApi.git
cd swift-codes
# Build and run
docker-compose up --build
```
### Local Setup
```bash
# Build the project
mvn clean package
# Run the database
docker run -p 5432:5432 postgres:14-alpine
# Run the application
java -jar target/swiftcodes.jar
```
## API Documentation
Available endpoints:
* `GET /v1/swift-codes/{swiftCode}`: SWIFT code details
* `GET /v1/swift-codes/country/{countryIso2Code}`: SWIFT codes by country
* `POST /v1/swift-codes`: Add new code
* `DELETE /v1/swift-codes/{swiftCode}`: Delete code
## Security and Validation
* Automatic input data validation
* Data integrity control
* Protection against duplicates
* Automatic case normalization for data
## Tests
### Running tests
```bash
./mvnw test
```

### 1. Unit Tests
Unit tests focus on testing individual components and services in isolation. These tests use Mockito for mocking dependencies and creating controlled test environments.

* **SwiftCodeServiceTest**: Verifies the business logic of the SwiftCode service
* **SwiftCodeControllerUnitTest**: Tests the controller layer's request handling and response generation

### 2. Integration Tests
Integration tests validate the interaction between different components and ensure that the application works correctly in a more realistic environment.

**SwiftCodeControllerIntegrationTest**:
* Uses TestContainers to spin up a PostgreSQL database for testing
* Performs end-to-end testing of REST API endpoints
* Validates database interactions and complete request-response cycles


### 3. Validation Tests
Validation tests ensure that data models and DTOs meet specific validation requirements.

## Testing Frameworks and Libraries
* **JUnit 5**
* **Mockito**
* **AssertJ**
* **TestContainers**
* **Spring Boot Test**
* **Hibernate Validator**
