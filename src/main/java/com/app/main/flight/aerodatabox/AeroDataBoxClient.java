package com.app.main.flight.aerodatabox;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface AeroDataBoxClient {

    @RequestLine("GET /flights/airports/iata/{airportIATACode}/{searchBeginTime}/{searchEndTime}?" +
            "withLeg=true&direction=Departure&withCancelled=false&withPrivate=false&withLocation=false")
    @Headers({"X-RapidAPI-Key: {apiKey}", "X-RapidAPI-Host: {apiHost}"})
    FlightsForAirport.Departures getAirportDepartureInfo(@Param("apiKey") String apiKey,
                                                         @Param("apiHost") String apiHost,
                                                         @Param("airportIATACode") String cityIATACode,
                                                         @Param("searchBeginTime") String searchBeginTime,
                                                         @Param("searchEndTime") String searchEndTime);

    @RequestLine("GET /airports/iata/{IATACode}")
    @Headers({"X-RapidAPI-Key: {apiKey}", "X-RapidAPI-Host: {apiHost}"})
    AirportTimeZone getAirportTimeZone(@Param("apiKey") String apiKey,
                                       @Param("apiHost") String apiHost,
                                       @Param("IATACode") String IATACode);
}
