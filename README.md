# FlightFinder

This project is created to help people find the nearest flight between two cities. To do this, you have to go to http://localhost:8080, type departure and arrival cities in the text fields and press "Find flight" button.

### Notes
- Due to API constraints, application can work only with russian cities and names, typed on russian language (Москва instead of Moscow) and starting with capital letter
- Application can find the flight only in the next 12 hours
- To start the project you have to sign up in AeroDataBox (https://rapidapi.com/aedbx-aedbx/api/aerodatabox) and pass the API key in aerodatabox.api.key field in src/main/resources/application.properties
- Application get an information about airports from Aeroportix (https://aeroportix.ru/strana/rossiya)

### Prerequisites
- JDK 17
- Docker
- Maven

### Run & Test
General:
1. Download the project
2. Go to the project main directory

To run it locally:
1. `docker compose up`
2. `mvn clean install`
3. `java -jar ./target/demo-0.0.1-SNAPSHOT.jar`

To run tests with reports:
1. `docker compose up`
2. `mvn clean test`
