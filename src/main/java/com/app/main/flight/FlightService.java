package com.app.main.flight;

import com.app.main.flight.aerodatabox.AeroDataBoxClient;
import com.app.main.flight.aerodatabox.AeroDataBoxCredentials;
import com.app.main.flight.aerodatabox.FlightsForAirport;
import com.app.main.flight.aerodatabox.AirportTimeZone;
import com.app.main.flight.models.AirportToFlight;
import com.app.main.flight.models.Flight;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final AirportsRepository repository;
    private final AeroDataBoxClient aeroDataBoxClient;
    private final AeroDataBoxCredentials aeroDataBoxCredentials;
    private final Clock clock;

    public Flight findNearestFlight(String arrivalCity, String departureCity) {
        List<String> arrivalAirportCodes = getAirportIATACodesForCity(arrivalCity);
        List<String> departureAirportCodes = getAirportIATACodesForCity(departureCity);
        List<FlightsForAirport> flights = getFlightsForAirportCodes(departureAirportCodes);
        return findNearestFlightForDepartureCity(arrivalAirportCodes, flights);
    }

    private Flight findNearestFlightForDepartureCity(List<String> arrivalAirportCodes, List<FlightsForAirport> flights) {
        AirportToFlight nearestFlight = flights.stream()
                .flatMap(flightsForAirport -> flightsForAirport.departures()
                        .getDepartures()
                        .stream()
                        .map(flight -> new AirportToFlight(flightsForAirport.IATACode(), flight)))
                .filter(airportToFlight -> arrivalAirportCodes.contains(airportToFlight.flight().getArrival().getAirport().getIata()))
                .filter(airportToFlight -> Objects.nonNull(airportToFlight.flight().getArrival().getScheduledTimeUtc()))
                .filter(airportToFlight -> Objects.nonNull(airportToFlight.flight().getArrival().getAirport().getIata()))
                .min(Comparator.comparing(airportToFlight -> LocalDateTime.parse(
                        airportToFlight.flight().getDeparture().getScheduledTimeUtc(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm'Z'")
                )))
                .orElseThrow(() -> new FlightNotFoundException("There are no departures in the next 12 hours"));
        LocalDateTime arrivalDateTime = LocalDateTime.parse(
                nearestFlight.flight().getArrival().getScheduledTimeUtc(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm'Z'")
        );
        LocalDateTime departureDateTime = LocalDateTime.parse(
                nearestFlight.flight().getDeparture().getScheduledTimeUtc(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm'Z'")
        );
        long flightTimeInMinutes = ChronoUnit.MINUTES.between(departureDateTime, arrivalDateTime);
        return new Flight(nearestFlight.IATACode(),
                nearestFlight.flight().getArrival().getAirport().getIata(),
                nearestFlight.flight().getDeparture().getScheduledTimeLocal(),
                nearestFlight.flight().getArrival().getScheduledTimeLocal(),
                flightTimeInMinutes / 60 + "h " +
                flightTimeInMinutes % 60 + "m",
                nearestFlight.flight().getAirline().getName());
    }

    private List<String> getAirportIATACodesForCity(String city) {
        return Arrays.asList(repository.findById(city)
                .orElseThrow(() -> new InvalidCityException(city + " doesn't have any airports or this city doesn't exist"))
                .getIATACodes()
                .split(", "));
    }

    private List<FlightsForAirport> getFlightsForAirportCodes(List<String> airportCodes) {
        LocalDateTime airportLocalDateTime = getAirportLocalDateTime(airportCodes);
        return airportCodes.parallelStream()
                .map(IATACode -> new FlightsForAirport(IATACode, aeroDataBoxClient.getAirportDepartureInfo(
                        aeroDataBoxCredentials.getKey(),
                        aeroDataBoxCredentials.getHost(),
                        IATACode,
                        airportLocalDateTime.toString(),
                        airportLocalDateTime.plusHours(12L).toString())))
                .filter(flightsForAirport -> Objects.nonNull(flightsForAirport.departures()))
                .toList();
    }

    private LocalDateTime getAirportLocalDateTime(List<String> airportCodes) {
        // #TODO: add cashing
        for (String IATACode : airportCodes) {
            AirportTimeZone airportTimeZone = aeroDataBoxClient.getAirportTimeZone(
                    aeroDataBoxCredentials.getKey(),
                    aeroDataBoxCredentials.getHost(),
                    IATACode
            );
            if (airportTimeZone != null) {
                return ZonedDateTime.now(clock).withZoneSameInstant(ZoneId.of(airportTimeZone.getTimeZone())).truncatedTo(ChronoUnit.MINUTES).toLocalDateTime();
            }
        }
        throw new InvalidCityException("Can't find information about airports in departure city");
    }
}
