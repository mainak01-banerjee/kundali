package tech.mainak.kundali.kundali_2.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import tech.mainak.kundali.kundali_2.dto.ChartDto;
import tech.mainak.kundali.kundali_2.service.HoroscopeService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class Controller {

    public final HoroscopeService horoscopeService;
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @PostMapping("/api/getchart")
    public ResponseEntity<String> getChart(@RequestBody ChartDto request, HttpServletRequest httpServletRequest) {
        logger.info(httpServletRequest.getLocalAddr());
        logger.info(httpServletRequest.getRequestURI());
        String username = request.getUsername();
        String password = request.getPassword();
        String url = "https://mainak.mbstudioz.in/auth/authenticate";

        // Create JSON request body for authentication
        String body = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();

        try {
            // Authenticate the user
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {

                LocalDate date=request.getDate();
                LocalTime time=request.getTime();
                double latitude=request.getLatitude();
                double longitude=request.getLongitude();

                logger.info("Date: "+date.toString());
                logger.info("Time: "+time.toString());
                logger.info("Latitude: "+latitude);
                logger.info("Longitude: "+longitude);

                int year=date.getYear();
                int month=date.getMonthValue();
                int day=date.getDayOfMonth();

                int hour=time.getHour();
                int minute=time.getMinute();

                String birthChart=horoscopeService.Chart(year,month,day,hour,minute,latitude,longitude);





                return ResponseEntity.ok().body(birthChart);
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Authentication failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
    @PostMapping("/api/get-dasha-chart")
    public ResponseEntity<String> getDashaChart(@RequestBody ChartDto request, HttpServletRequest httpServletRequest) {
        logger.info(httpServletRequest.getLocalAddr());
        logger.info(httpServletRequest.getRequestURI());
        String username = request.getUsername();
        String password = request.getPassword();
        String url = "https://mainak.mbstudioz.in/auth/authenticate";
        String body = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Authenticate the user
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {

                LocalDate date=request.getDate();
                LocalTime time=request.getTime();
                double latitude=request.getLatitude();
                double longitude=request.getLongitude();

                logger.info("Date: "+date.toString());
                logger.info("Time: "+time.toString());
                logger.info("Latitude: "+latitude);
                logger.info("Longitude: "+longitude);

                int year=date.getYear();
                int month=date.getMonthValue();
                int day=date.getDayOfMonth();

                int hour=time.getHour();
                int minute=time.getMinute();

                String birthChart=horoscopeService.vimshottariDashaChart(year,month,day,hour,minute,latitude,longitude);


                return ResponseEntity.ok().body(birthChart);
            } else {
                return ResponseEntity.status(response.getStatusCode()).body("Authentication failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }


}
