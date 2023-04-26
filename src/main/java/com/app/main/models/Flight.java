package com.app.main.models;

public record Flight(String departureAirport,
                     String arrivalAirport,
                     String departureDateTimeLocal,
                     String arrivalDateTimeLocal,
                     String timeInFlight,
                     String airline) {

}
