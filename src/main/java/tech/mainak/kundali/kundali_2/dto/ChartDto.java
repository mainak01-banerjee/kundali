package tech.mainak.kundali.kundali_2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChartDto {
    private String username;
    private String password;
    private LocalDate date;
    private LocalTime time;
    private double latitude;
    private double longitude;

}
