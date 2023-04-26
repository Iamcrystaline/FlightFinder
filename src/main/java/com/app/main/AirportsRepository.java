package com.app.main;

import com.app.main.models.AirportsForCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirportsRepository extends JpaRepository<AirportsForCity, String> {
}
