package com.app.main;

import com.app.main.models.Flight;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService service;

    @GetMapping("/getNearestFlight")
    public Flight getNearestFlight(@RequestParam("arrivalCity") String arrivalCity,
                                   @RequestParam("departureCity") String departureCity) {
        return service.findNearestFlight(arrivalCity, departureCity);
    }
}
