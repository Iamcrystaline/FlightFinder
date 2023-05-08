package com.app.main.flight.models;

public record Flight(String departureAirport,
                     String arrivalAirport,
                     String departureDateTimeLocal,
                     String arrivalDateTimeLocal,
                     String timeInFlight,
                     String airline) {

}
