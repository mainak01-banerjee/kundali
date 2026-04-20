package tech.mainak.kundali.kundali_2;

import com.fasterxml.jackson.databind.ObjectMapper;
import tech.mainak.kundali.kundali_2.app.BirthHoroscope;
import tech.mainak.kundali.kundali_2.app.VimshottariDashaCalculator;
import tech.mainak.kundali.kundali_2.dto.Period;
import tech.mainak.kundali.kundali_2.service.HoroscopeService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class Test {
    public static void main(String[] args) throws IOException {
        int year=1999;
        int month=3;
        int day = 19;
        int hour=11;
        int minute=35;
        double lat=22.5744;
        double lng=88.3629;

        HoroscopeService horoscopeService = new HoroscopeService();
        String horo=horoscopeService.vimshottariDashaChart(year, month, day, hour, minute, lat, lng);

       // Map<String,Double> antarDasha=VimshottariDashaCalculator.getAntardashasPeriods("Jupiter",0);
       // System.out.println(antarDasha.toString());
        LocalDate birthDay = LocalDate.of(year, month, day);
        LinkedHashMap<String, Period> mahadasha =VimshottariDashaCalculator.calculateDasha(354.412, birthDay);

        System.out.println(mahadasha);

        LinkedHashMap<String,LinkedHashMap<String,Period>> antardasha=VimshottariDashaCalculator.calculateAntardasha(mahadasha);
        System.out.println(antardasha);

       // System.out.println("***********************Finale Response****************************: \n"+horo);
    }
}
