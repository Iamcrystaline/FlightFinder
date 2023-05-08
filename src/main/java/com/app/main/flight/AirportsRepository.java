package com.app.main.flight;

import com.app.main.flight.models.AirportsForCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirportsRepository extends JpaRepository<AirportsForCity, String> {
}
