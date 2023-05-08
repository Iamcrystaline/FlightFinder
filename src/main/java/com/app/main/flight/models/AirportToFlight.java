package com.app.main.flight.models;

import com.app.main.flight.aerodatabox.FlightsForAirport;

public record AirportToFlight(String IATACode, FlightsForAirport.Departures.Flight flight) {

}
