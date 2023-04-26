package com.app.main.aerodatabox;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public record FlightsForAirport(String IATACode, Departures flights) {

    @Getter
    @NoArgsConstructor
    public static class Departures {
        private List<Flight> departures;

        @Getter
        @NoArgsConstructor
        public static class Flight {

            private Departure departure;
            private Arrival arrival;
            private Airline airline;

            @Getter
            @NoArgsConstructor
            public static class Departure {

                private String scheduledTimeUtc;
                private String scheduledTimeLocal;
            }

            @Getter
            @NoArgsConstructor
            public static class Arrival {

                private Airport airport;
                private String scheduledTimeUtc;
                private String scheduledTimeLocal;

                @Getter
                @NoArgsConstructor
                public static class Airport {

                    private String iata;
                    private String name;
                }
            }

            @Getter
            @NoArgsConstructor
            public static class Airline {

                private String name;
            }
        }
    }
}