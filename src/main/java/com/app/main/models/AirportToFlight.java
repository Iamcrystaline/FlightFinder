package com.app.main.models;

import com.app.main.aerodatabox.FlightsForAirport;

public record AirportToFlight(String IATACode, FlightsForAirport.Departures.Flight flight) {

}
