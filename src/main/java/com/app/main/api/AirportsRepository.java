package com.app.main.api;

import com.app.main.api.models.AirportsForCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirportsRepository extends JpaRepository<AirportsForCity, String> {
}
