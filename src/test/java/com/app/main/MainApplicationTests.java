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
    private static Clock clock;

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
        clock = Clock.fixed(
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

    @Test
    @DisplayName("Given 2 cities with working airports, arrival city has more airports than departure - When getNearestFlight - Then should return nearest flight")
    void testBothWorkingAirports1() {
        // Given
        String arrivalCity = "Екатеринбург";
        String departureCity = "Москва";
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
        // When
        // Then
        io.restassured.RestAssured.get("/api/v1/getNearestFlight?arrivalCity=" + arrivalCity + "&departureCity=" + departureCity)
                .then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath("expected_answers/fully_correct_response.json"));
    }

}
