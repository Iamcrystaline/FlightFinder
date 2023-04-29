package com.app.main.api.models;

import com.app.main.api.aerodatabox.FlightsForAirport;

public record AirportToFlight(String IATACode, FlightsForAirport.Departures.Flight flight) {

}
