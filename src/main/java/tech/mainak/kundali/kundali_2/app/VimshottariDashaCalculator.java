package tech.mainak.kundali.kundali_2.app;

import tech.mainak.kundali.kundali_2.dto.Period;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VimshottariDashaCalculator {
    private static final String[] PLANET_NAMES = {"Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury"};
    private static final String[] PLANET_SHORT_NAMES = {"Ke", "Ve", "Su", "Mo", "Ma", "Ra", "Ju", "Sa", "Me"};
    private static final int[] DASHA_YEARS = {7, 20, 6, 10, 7, 18, 16, 19, 17};

    // Correct Nakshatra ruler mapping
    private static final int[] NAKSHATRA_RULERS = {
            0,  // Ashwini - Ketu
            1,  // Bharani - Venus
            2,  // Krittika - Sun
            3,  // Rohini - Moon
            4,  // Mrigashira - Mars
            5,  // Ardra - Rahu
            6,  // Punarvasu - Jupiter
            7,  // Pushya - Saturn
            8,  // Ashlesha - Mercury
            0,  // Magha - Ketu
            1,  // Purva Phalguni - Venus
            2,  // Uttara Phalguni - Sun
            3,  // Hasta - Moon
            4,  // Chitra - Mars
            5,  // Swati - Rahu
            6,  // Vishakha - Jupiter
            7,  // Anuradha - Saturn
            8,  // Jyeshtha - Mercury
            0,  // Mula - Ketu
            1,  // Purva Ashadha - Venus
            2,  // Uttara Ashadha - Sun
            3,  // Shravana - Moon
            4,  // Dhanishtha - Mars
            5,  // Shatabhisha - Rahu
            6,  // Purva Bhadrapada - Jupiter
            7,  // Uttara Bhadrapada - Saturn
            8   // Revati - Mercury
    };

    // Each Nakshatra is 13°20' (or 13.3333 degrees)
    private static final double NAKSHATRA_DEGREE_LENGTH = 13.3333;

    public static LinkedHashMap<String, Period> calculateDasha(double moonLongitude,LocalDate birthday) {
        LinkedHashMap<String, Double> dashaPeriods = new LinkedHashMap<>();

        // Calculate which Nakshatra the Moon is in
        int nakshatraIndex = (int) (moonLongitude / NAKSHATRA_DEGREE_LENGTH); //330.28874774254655
        nakshatraIndex = nakshatraIndex % 27;  // Ensure it wraps around after 27 Nakshatras //24
        int planetIndex = NAKSHATRA_RULERS[nakshatraIndex];  // Find the ruling planet for this Nakshatra //6

        // Find how much of the Nakshatra is completed
        double nakshatraStart = nakshatraIndex * NAKSHATRA_DEGREE_LENGTH;//319.920
        double completedFraction = (moonLongitude - nakshatraStart) / NAKSHATRA_DEGREE_LENGTH;//0.7776
        double remainingFraction = 1 - completedFraction;//0.2224

        // Compute the remaining Dasha time
        double remainingYears = remainingFraction * DASHA_YEARS[planetIndex];//3.558

        // Start populating the Dasha periods
        int currentIndex = planetIndex;//6
        double yearsLeft = remainingYears;//3.558
        for (int i = 0; i < 9; i++) {
            dashaPeriods.put(PLANET_NAMES[currentIndex], yearsLeft);
            currentIndex = (currentIndex + 1) % 9;  // Circular rotation through planets
            yearsLeft = DASHA_YEARS[currentIndex];  // Full Dasha time for the next planet
        }

        return getdashaPeriod(birthday, dashaPeriods);
    }
    public static LinkedHashMap<String, Period> getdashaPeriod(LocalDate birthDay, LinkedHashMap<String, Double> mahadasha) {
        Map<String,Integer> dashaYears = new LinkedHashMap<>();
        for(int i=0;i<9;i++){
            dashaYears.put(PLANET_NAMES[i], DASHA_YEARS[i]);
        }
        Map<String,String> planetShortForms = new LinkedHashMap<>();
        for(String planets:PLANET_NAMES){
            planetShortForms.put(planets,planets.substring(0,2));
        }

        LinkedHashMap<String,Period> dashaPeriod = new LinkedHashMap<>();
        LocalDate referenceDate=birthDay;

        for(Map.Entry<String,Double> entry: mahadasha.entrySet()){
            String mahadashaPlanet=entry.getKey();

            double mahadashaDurationLeft=entry.getValue();

            int years = (int) mahadashaDurationLeft;
            referenceDate= referenceDate.plusYears(years);

            double fractionMonths=(mahadashaDurationLeft-years);
            double totalFractionMonths=fractionMonths*12;

            int months=(int) totalFractionMonths;

            referenceDate= referenceDate.plusMonths(months);

            double fractionDays=(totalFractionMonths-months);
            int daysinMonth=referenceDate.lengthOfMonth();

            double totalFractionDays=fractionDays*daysinMonth;

            int days=(int) totalFractionDays;

            referenceDate= referenceDate.plusDays(days);

            Period period = new Period();

            period.setEnd(referenceDate.toString());
            int totalDashaLength=dashaYears.get(mahadashaPlanet);
            period.setStart(referenceDate.minusYears(totalDashaLength).toString());
            String key = planetShortForms.get(mahadashaPlanet);

            dashaPeriod.put(key,period);

        }
        return dashaPeriod;
    }

    public static LinkedHashMap<String, LinkedHashMap<String,Period>> calculateAntardasha(Map<String, Period> mahadasha) {
        Map<String,Integer>dashaYears = new LinkedHashMap<>();
        for(int i=0;i<9;i++){
            dashaYears.put(PLANET_SHORT_NAMES[i].substring(0,2), DASHA_YEARS[i]);
        }
        Map<String,String> PLanetShortForms = new LinkedHashMap<>();
        for(String planets:PLANET_NAMES){
            PLanetShortForms.put(planets,planets.substring(0,2));
        }
        LinkedHashMap<String, LinkedHashMap<String,Period>> antardasha = new LinkedHashMap<>();

        for(Map.Entry<String,Period> entry: mahadasha.entrySet()){
            String mahadashaPlanet=entry.getKey();
            Period mahadashaPeriod=entry.getValue();
            String start=mahadashaPeriod.getStart();
            LocalDate  startDate=LocalDate.parse(start);
            String end=mahadashaPeriod.getEnd();
            LocalDate endDate=LocalDate.parse(end);
            LinkedHashMap<String,Period> antardashaPeriod = getAntardashasPeriod(mahadashaPlanet,startDate,endDate);

            antardasha.put(mahadashaPlanet,antardashaPeriod);
        }
        return antardasha;
    }
    public static LinkedHashMap<String,Period> getAntardashasPeriod(String mahadashaPlanet,LocalDate startDate,LocalDate endDate) {

        int planetIndex=0;

        for(int i=0;i<PLANET_NAMES.length;i++){
            if(PLANET_SHORT_NAMES[i].equals(mahadashaPlanet)){
                planetIndex=i;
                break;
            }
        }

        Map<String,Integer> dashaYears = new LinkedHashMap<>();
        for(int i=0;i<9;i++){
            dashaYears.put(PLANET_SHORT_NAMES[i], DASHA_YEARS[i]);
        }
        Map<String,String> planetShortForms = new LinkedHashMap<>();
        for(String planets:PLANET_NAMES){
            planetShortForms.put(planets,planets.substring(0,2));
        }

        LinkedHashMap<String,Period> antardashaDuration = new LinkedHashMap<>();

        LocalDate antarStartDate=startDate;
        LocalDate antarEndDate;

        int start=planetIndex;
        do{
            double duration = (double) (dashaYears.get(PLANET_SHORT_NAMES[start]) * dashaYears.get(PLANET_SHORT_NAMES[planetIndex])) /120;
            String key=PLANET_SHORT_NAMES[planetIndex]+"-"+PLANET_SHORT_NAMES[start];
            Period period = new Period();
            period.setStart(antarStartDate.toString());

            if(start==planetIndex-1){
                period.setEnd(endDate.toString());
            }
            else{
                int years = (int) duration;

                antarEndDate = antarStartDate.plusYears(years);

                double fractionMonths = (duration-years);
                double decimalMonths=fractionMonths*12;

                int months = (int) decimalMonths;

                antarEndDate= antarEndDate.plusMonths(months);

                int daysinMonth=antarEndDate.lengthOfMonth();

                double fractionDays = (decimalMonths-months);

                double decimalDays = fractionDays*daysinMonth;

                int days = (int) Math.round(decimalDays);

                antarEndDate= antarEndDate.plusDays(days);

                period.setEnd(antarEndDate.toString());
                antarStartDate=antarEndDate;
            }


            antardashaDuration.put(key,period);

            start=start+1;
            start=start%PLANET_NAMES.length;

        }while(start!=planetIndex);

        return antardashaDuration;
    }


}