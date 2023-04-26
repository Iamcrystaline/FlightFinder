package com.app.main.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class AirportsForCity {

    @Id
    @Column(length = 30)
    private String city;
    private String IATACodes;
    private String ICAOCodes;
}
