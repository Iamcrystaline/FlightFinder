package com.app.main;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class MainApplicationTests {

    private static WireMockServer aeroportixMockServer;
    private static WireMockServer aeroDataBoxMockServer;

    @BeforeAll
    static void beforeClass() {
        aeroportixMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8081));
        aeroportixMockServer.start();
        aeroportixMockServer.stubFor(WireMock
                .get(urlEqualTo("/strana/rossiya"))
                .willReturn(aResponse().withBodyFile("aeroportixResponse.html").withStatus(200))
        );
        aeroDataBoxMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8082));
        aeroDataBoxMockServer.start();
        Clock clock = Clock.fixed(
                Instant.parse("2023-04-04T17:00:00.000Z"),
                ZoneId.of("Europe/Moscow")
        );
        Mockito.mockStatic(Clock.class).when(Clock::systemUTC).thenReturn(clock);
    }

    @AfterAll
    static void afterClass() {
        aeroportixMockServer.stop();
        aeroDataBoxMockServer.stop();
    }

    @AfterEach
    void tearDown() {
        aeroDataBoxMockServer.resetAll();
    }

    @Test
    @DisplayName("Given 2 cities with working airports, departure and arrival in different days, airports in departure city > arrival city - When getNearestFlight - Then should return nearest flight")
    void testBothWorkingAirports1() {
        // Given
        String arrivalCity = "Екатеринбург";
        String departureCity = "Москва";
        createStubsForMoscow();

        // When
        // Then
        io.restassured.RestAssured.get("/api/v1/getNearestFlight?arrivalCity=" + arrivalCity + "&departureCity=" + departureCity)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("expected_answers/fully_correct_response.json"))
                .body("departureAirport", org.hamcrest.Matchers.equalTo("DME"))
                .body("departureDateTimeLocal", org.hamcrest.Matchers.equalTo("2023-04-04 20:35+03:00"))
                .body("timeInFlight", org.hamcrest.Matchers.equalTo("2h 20m"));
    }

    @Test
    @DisplayName("Given 2 cities with working airports, arrival and departure in the same day, airports in departure city > arrival city - When getNearestFlight - Then should return nearest flight")
    void testBothWorkingAirports2() {
        // Given
        String arrivalCity = "Новосибирск";
        String departureCity = "Москва";
        createStubsForMoscow();

        // When
        // Then
        io.restassured.RestAssured.get("/api/v1/getNearestFlight?arrivalCity=" + arrivalCity + "&departureCity=" + departureCity)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("expected_answers/fully_correct_response.json"))
                .body("departureAirport", org.hamcrest.Matchers.equalTo("VKO"))
                .body("departureDateTimeLocal", org.hamcrest.Matchers.equalTo("2023-04-04 20:20+03:00"))
                .body("timeInFlight", org.hamcrest.Matchers.equalTo("3h 5m"));
    }

    @Test
    @DisplayName("Given 2 cities with working airports, arrival and departure in the same day, airports in departure city < arrival city - When getNearestFlight - Then should return nearest flight")
    void testBothWorkingAirports3() {
        // Given
        String arrivalCity = "Москва";
        String departureCity = "Екатеринбург";
        createStubsForYekaterinburg();

        // When
        // Then
        io.restassured.RestAssured.get("/api/v1/getNearestFlight?arrivalCity=" + arrivalCity + "&departureCity=" + departureCity)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("expected_answers/fully_correct_response.json"))
                .body("arrivalAirport", org.hamcrest.Matchers.equalTo("SVO"))
                .body("departureDateTimeLocal", org.hamcrest.Matchers.equalTo("2023-04-04 22:10+05:00"))
                .body("timeInFlight", org.hamcrest.Matchers.equalTo("2h 40m"));
    }

    @Test
    @DisplayName("Given 2 cities with working airports, arrival and departure in the same day, airports in departure city = arrival city = 1 - When getNearestFlight - Then should return nearest flight")
    void testBothWorkingAirports4() {
        // Given
        String arrivalCity = "Адлер";
        String departureCity = "Екатеринбург";
        createStubsForYekaterinburg();

        // When
        // Then
        io.restassured.RestAssured.get("/api/v1/getNearestFlight?arrivalCity=" + arrivalCity + "&departureCity=" + departureCity)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("expected_answers/fully_correct_response.json"))
                .body("arrivalAirport", org.hamcrest.Matchers.equalTo("AER"))
                .body("departureDateTimeLocal", org.hamcrest.Matchers.equalTo("2023-04-05 05:40+05:00"))
                .body("timeInFlight", org.hamcrest.Matchers.equalTo("4h 0m"));
    }

    @Test
    @DisplayName("Given only 1 city with working airports - When getNearestFlight - Then should throw exception")
    void testOnlyOneWorkingAirports1() {
        // Given
        String arrivalCity = "Сургут";
        String departureCity = "Екатеринбург";
        createStubsForYekaterinburg();

        // When
        // Then
        io.restassured.RestAssured.get("/api/v1/getNearestFlight?arrivalCity=" + arrivalCity + "&departureCity=" + departureCity)
                .then()
                .assertThat()
                .body("message", org.hamcrest.Matchers.equalTo("There are no departures in the next 12 hours"))
                .body("status", org.hamcrest.Matchers.equalTo(500));
    }

    @Test
    @DisplayName("Given lower case city name - When getNearestFlight - Then should throw exception")
    void testWrongCity1() {
        // Given
        String arrivalCity = "сургут";
        String departureCity = "Екатеринбург";

        // When
        // Then
        io.restassured.RestAssured.get("/api/v1/getNearestFlight?arrivalCity=" + arrivalCity + "&departureCity=" + departureCity)
                .then()
                .assertThat()
                .body("message", org.hamcrest.Matchers.equalTo("сургут doesn't have any airports or this city doesn't exist"))
                .body("status", org.hamcrest.Matchers.equalTo(500));
    }

    @Test
    @DisplayName("Given foreign city name - When getNearestFlight - Then should throw exception")
    void testWrongCity2() {
        // Given
        String arrivalCity = "Астана";
        String departureCity = "Екатеринбург";

        // When
        // Then
        io.restassured.RestAssured.get("/api/v1/getNearestFlight?arrivalCity=" + arrivalCity + "&departureCity=" + departureCity)
                .then()
                .assertThat()
                .body("message", org.hamcrest.Matchers.equalTo("Астана doesn't have any airports or this city doesn't exist"))
                .body("status", org.hamcrest.Matchers.equalTo(500));
    }

    @Test
    @DisplayName("Given city name without airports - When getNearestFlight - Then should throw exception")
    void testWrongCity3() {
        // Given
        String arrivalCity = "Мегион";
        String departureCity = "Екатеринбург";

        // When
        // Then
        io.restassured.RestAssured.get("/api/v1/getNearestFlight?arrivalCity=" + arrivalCity + "&departureCity=" + departureCity)
                .then()
                .assertThat()
                .body("message", org.hamcrest.Matchers.equalTo("Мегион doesn't have any airports or this city doesn't exist"))
                .body("status", org.hamcrest.Matchers.equalTo(500));
    }

    @Test
    @DisplayName("Given unknown city name - When getNearestFlight - Then should throw exception")
    void testWrongCity4() {
        // Given
        String arrivalCity = "Ашфыгврвпа";
        String departureCity = "Екатеринбург";

        // When
        // Then
        io.restassured.RestAssured.get("/api/v1/getNearestFlight?arrivalCity=" + arrivalCity + "&departureCity=" + departureCity)
                .then()
                .assertThat()
                .body("message", org.hamcrest.Matchers.equalTo("Ашфыгврвпа doesn't have any airports or this city doesn't exist"))
                .body("status", org.hamcrest.Matchers.equalTo(500));
    }

    private void createStubsForYekaterinburg() {
        aeroDataBoxMockServer.stubFor(WireMock
                .get(urlEqualTo("/flights/airports/iata/SVX/2023-04-04T22:00/2023-04-05T10:00?" +
                        "withLeg=true&direction=Departure&withCancelled=false&withPrivate=false&withLocation=false"))
                .withHeader("X-RapidAPI-Key", equalTo("secretKey"))
                .withHeader("X-RapidAPI-Host", equalTo("secretHost"))
                .willReturn(aResponse().withBodyFile("SVXResponse.json").withStatus(200))
        );
        aeroDataBoxMockServer.stubFor(WireMock
                .get(urlEqualTo("/airports/iata/SVX"))
                .withHeader("X-RapidAPI-Key", equalTo("secretKey"))
                .withHeader("X-RapidAPI-Host", equalTo("secretHost"))
                .willReturn(aResponse().withBodyFile("SVXTimeZone.json").withStatus(200))
        );
    }

    private void createStubsForMoscow() {
        aeroDataBoxMockServer.stubFor(WireMock
                .get(urlEqualTo("/flights/airports/iata/DME/2023-04-04T20:00/2023-04-05T08:00?" +
                        "withLeg=true&direction=Departure&withCancelled=false&withPrivate=false&withLocation=false"))
                .withHeader("X-RapidAPI-Key", equalTo("secretKey"))
                .withHeader("X-RapidAPI-Host", equalTo("secretHost"))
                .willReturn(aResponse().withBodyFile("DMEResponse.json").withStatus(200))
        );
        aeroDataBoxMockServer.stubFor(WireMock
                .get(urlEqualTo("/flights/airports/iata/BKA/2023-04-04T20:00/2023-04-05T08:00?" +
                        "withLeg=true&direction=Departure&withCancelled=false&withPrivate=false&withLocation=false"))
                .withHeader("X-RapidAPI-Key", equalTo("secretKey"))
                .withHeader("X-RapidAPI-Host", equalTo("secretHost"))
                .willReturn(aResponse().withStatus(204))
        );
        aeroDataBoxMockServer.stubFor(WireMock
                .get(urlEqualTo("/flights/airports/iata/VKO/2023-04-04T20:00/2023-04-05T08:00?" +
                        "withLeg=true&direction=Departure&withCancelled=false&withPrivate=false&withLocation=false"))
                .withHeader("X-RapidAPI-Key", equalTo("secretKey"))
                .withHeader("X-RapidAPI-Host", equalTo("secretHost"))
                .willReturn(aResponse().withBodyFile("VKOResponse.json").withStatus(200))
        );
        aeroDataBoxMockServer.stubFor(WireMock
                .get(urlEqualTo("/flights/airports/iata/SVO/2023-04-04T20:00/2023-04-05T08:00?" +
                        "withLeg=true&direction=Departure&withCancelled=false&withPrivate=false&withLocation=false"))
                .withHeader("X-RapidAPI-Key", equalTo("secretKey"))
                .withHeader("X-RapidAPI-Host", equalTo("secretHost"))
                .willReturn(aResponse().withBodyFile("SVOResponse.json").withStatus(200))
        );
        aeroDataBoxMockServer.stubFor(WireMock
                .get(urlEqualTo("/airports/iata/DME"))
                .withHeader("X-RapidAPI-Key", equalTo("secretKey"))
                .withHeader("X-RapidAPI-Host", equalTo("secretHost"))
                .willReturn(aResponse().withBodyFile("DMETimeZone.json").withStatus(200))
        );
    }

}
