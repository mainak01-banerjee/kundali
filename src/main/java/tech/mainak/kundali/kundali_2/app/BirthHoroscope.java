package tech.mainak.kundali.kundali_2.app;



import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import tech.mainak.kundali.kundali_2.Datastructures.CircularLinkedList;
import tech.mainak.kundali.kundali_2.swissephemeris.swisseph.SweDate;
import tech.mainak.kundali.kundali_2.swissephemeris.swisseph.SwissEph;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
public class BirthHoroscope {


    private  String path;

    int year;
    int month;
    int day;
    int hour;
    int minute;
    double latitude;
    double longitude;
    CircularLinkedList horo_chart;
    Map<String, Object> horo;

    public BirthHoroscope(int year, int month, int day, int hour, int minute, double latitude, double longitude,String path) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.latitude = latitude;
        this.longitude = longitude;
        this.path=path;
        this.horo_chart = new CircularLinkedList();
        this.horo = new LinkedHashMap();
    }

    public Map<String, Object> getHoroScope() throws IOException {
        int year = this.year;
        int month = this.month;
        int day = this.day;
        int hour = this.hour;
        int minute = this.minute;
        double latitude = this.latitude;
        double longitude = this.longitude;
        Map<String, Double> utcDate = convertISTtoUTC(year, month, day, (double)hour, (double)minute, 0.0);
        BigDecimal decimalTime = calculateDecimalTime((Double)utcDate.get("Hour"), (Double)utcDate.get("Minute"), (Double)utcDate.get("Second"));
       // String ephePath = "D\\:\\ephe\\";// path for development use ;
        String ephePath = path;
        // "/home/bat/kundali.mbstudioz.in/ephe:/home/bat/kundali.mbstudioz.in/ephe/sat"
        SwissEph swissEph = new SwissEph(ephePath);

        int utcyear=(int)(double)utcDate.get("Year");

        SweDate sweDate = new SweDate((int)(double)utcDate.get("Year"),(int)(double)utcDate.get("Month"), (int)(double)utcDate.get("Day"), decimalTime.doubleValue());
        this.horo.put("JulianDay", sweDate.getJulDay());
        swissEph.swe_set_topo(longitude, latitude, 9.0);
        swissEph.swe_set_sid_mode(1);
        this.horo.put("Ayanamsa", swissEph.swe_get_ayanamsa(sweDate.getJulDay()));
        double[] cusp = new double[13];
        double[] ascmc = new double[10];
        int flag = 65538;
        int result = swissEph.swe_houses(sweDate.getJulDay(), flag, latitude, longitude, 69, cusp, ascmc);
        new LinkedHashMap();
        if (result == 0) {
            getHouseSign(cusp, this.horo_chart);
        }

        Map<String, ArrayList<Object>> planets = new LinkedHashMap();
        calculateplanetposition(0, "Sun", swissEph, sweDate.getJulDay(), planets);
        calculateplanetposition(1, "Moon", swissEph, sweDate.getJulDay(), planets);
        calculateplanetposition(2, "Mercury", swissEph, sweDate.getJulDay(), planets);
        calculateplanetposition(3, "Venus", swissEph, sweDate.getJulDay(), planets);
        calculateplanetposition(4, "Mars", swissEph, sweDate.getJulDay(), planets);
        calculateplanetposition(5, "Jupiter", swissEph, sweDate.getJulDay(), planets);
        calculateplanetposition(6, "Saturn", swissEph, sweDate.getJulDay(), planets);
        calculateplanetposition(7, "Uranus", swissEph, sweDate.getJulDay(), planets);
        calculateplanetposition(8, "Neptune", swissEph, sweDate.getJulDay(), planets);
        calculateplanetposition(11, "Rahu_True", swissEph, sweDate.getJulDay(), planets);
        calculateplanetposition(10, "Rahu_Mean", swissEph, sweDate.getJulDay(), planets);
        calculateKetuPosition(planets);

        Map<String, Map<String, Object>> planet_nakshatra = this.getNakshatra(planets, cusp);

        calculate_final_chart(planet_nakshatra,horo_chart,cusp);

        horo.put("planets", planet_nakshatra);

        this.horo.put("Version", swissEph.swe_java_version());
        return this.horo;
    }

    private void calculateKetuPosition(Map<String, ArrayList<Object>> planets) {
        ArrayList<Object>Rahu_Mean_data=planets.get("Rahu_Mean");
        ArrayList<Object>Rahu_True_data=planets.get("Rahu_True");
        double Rahu_Mean_long= (double) Rahu_Mean_data.get(0);
        double Rahu_True_long= (double) Rahu_True_data.get(0);

        double Ketu_Mean_long=(Rahu_Mean_long+180)%360;
        double Ketu_True_long=(Rahu_True_long+180)%360;

        ArrayList<Object>Ketu_Mean_data= new ArrayList<>(3);
        ArrayList<Object>Ketu_True_data= new ArrayList<>(3);
        Ketu_Mean_data.add(Ketu_Mean_long);
        Ketu_Mean_data.add(1,0);
        Ketu_Mean_data.add(2,0);
        Ketu_True_data.add(Ketu_True_long);
        Ketu_True_data.add(1,0);
        Ketu_True_data.add(2,0);


        planets.put("Ketu_Mean", Ketu_Mean_data);
        planets.put("Ketu_True", Ketu_True_data);

    }

    private void calculate_final_chart(Map<String, Map<String, Object>> planets_nakshatra, CircularLinkedList horo_chart, double[] cusp) {
        Map<String, ArrayList<Object>> houseMap = horo_chart.getDataMap();
        String[] zodiac = new String[]{"Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"};
        String[] planetsNames = new String[]{"ASC", "Sun", "Moon", "Mercury", "Venus", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Rahu_True", "Rahu_Mean", "Ketu_Mean", "Ketu_True"};

        // Iterate over the houses
        for (int i = 1; i < 13; i++) {
            ArrayList<Object> houseData = houseMap.get("house_" + i);
            String housesign = houseData.get(3).toString();

            // Iterate over the planets and find out which house they belong to
            for (String planetName : planetsNames) {
                Map<String, Object> planetMap = planets_nakshatra.get(planetName);
                String Planethouse = planetMap.get("house").toString();
                // If the planet belongs to this house, add it to the houseData
                if (Planethouse.equals(housesign)) {
                    houseData.add(planetName); // Add the planet to this house's data
                }
            }
            houseMap.put("house_" + i, houseData);
        }

       // System.out.println("Updated house map with planets: " + houseMap);
        horo.put("horoscope",houseMap);
    }



    private Map<String, Map<String, Object>> getNakshatra(Map<String, ArrayList<Object>> planets, double[] cusp) {
        String[] nakshatras = new String[]{"Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra", "Punarvasu", "Pushya", "Ashlesha", "Magha", "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha", "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishtha", "Shatabhisha", "Purva Bhadrapada", "Uttara Bhadrapada", "Revati"};
        String[] zodiac = new String[]{"Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"};
        Map<String, Map<String, Object>> planets_nakshatra = new LinkedHashMap();
        Map<String, Object> ascData = new LinkedHashMap();
        double asclong = cusp[1];
        int i = (int)(asclong / 30.0) % 12;
        ascData.put("longitude", asclong);
        ascData.put("house", zodiac[i]);
        double degree_in_house = asclong - (double)(i * 30);
        ascData.put("house_longitude", degree_in_house);
        int nakIndex = (int)(asclong / 13.3333) % 27;
        ascData.put("nakshatra", nakshatras[nakIndex]);
        int asc_pada = (int)(asclong % 13.3333 / 3.3333) + 1;
        ascData.put("pada", asc_pada);
        planets_nakshatra.put("ASC", ascData);
        Iterator var14 = planets.entrySet().iterator();

        while(var14.hasNext()) {
            Map.Entry<String, ArrayList<Object>> entry = (Map.Entry)var14.next();
            Map<String, Object> data = new LinkedHashMap();
            String planet = (String)entry.getKey();
            ArrayList<Object> planetData = (ArrayList)entry.getValue();
            double longitude = Double.parseDouble(planetData.get(0).toString());
            data.put("longitude", longitude);
            int zodiacIndex = (int)(longitude / 30.0) % 12;
            double house_longitude = longitude - (double)(zodiacIndex * 30);
            data.put("house", zodiac[zodiacIndex]);
            data.put("house_longitude", house_longitude);
            int nakshatraIndex = (int)(longitude / 13.3333) % 27;
            data.put("nakshatra", nakshatras[nakshatraIndex]);
            int pada = (int)(longitude % 13.3333 / 3.3333) + 1;
            data.put("pada", pada);
            planets_nakshatra.put(planet, data);
        }

        return planets_nakshatra;
    }

    private static BigDecimal calculateDecimalTime(double hour, double minute, double second) {
        BigDecimal bdHour = BigDecimal.valueOf(hour);
        BigDecimal bdMinute = BigDecimal.valueOf(minute).divide(BigDecimal.valueOf(60L), 10, RoundingMode.HALF_UP);
        BigDecimal bdSecond = BigDecimal.valueOf(second).divide(BigDecimal.valueOf(3600L), 10, RoundingMode.HALF_UP);
        return bdHour.add(bdMinute).add(bdSecond);
    }

    public static Map<String, Double> convertISTtoUTC(int year, int month, int day, double hour, double minute, double second) {
        hour -= 5.0;
        minute -= 30.0;
        if (minute < 0.0) {
            minute += 60.0;
            --hour;
        }

        if (hour < 0.0) {
            hour += 24.0;
            --day;
        }

        if (day == 0) {
            --month;
            if (month == 0) {
                month = 12;
                --year;
            }

            day = getDaysInMonth(year, month);
        }

        Map<String, Double> date = new HashMap();
        date.put("Hour", hour);
        date.put("Minute", minute);
        date.put("Second", second);
        date.put("Day", (double)day);
        date.put("Month", (double)month);
        date.put("Year", (double)year);
        System.out.printf("Converted UTC time: %04d-%02d-%02d %02d:%02d:%02.0f\n", year, month, day, (int)hour, (int)minute, second);
        return date;
    }

    private static int getDaysInMonth(int year, int month) {
        int[] daysInMonth = new int[]{31, isLeapYear(year) ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        return daysInMonth[month - 1];
    }

    private static boolean isLeapYear(int year) {
        return year % 4 == 0 && year % 100 != 0 || year % 400 == 0;
    }

    public static void calculateplanetposition(int planet, String planetName, SwissEph swissEph, double julian_date, Map<String, ArrayList<Object>> planets) {
        int iflag = 65538;
        double[] xx = new double[6];
        swissEph.swe_calc_ut(julian_date, planet, iflag, xx, new StringBuffer());
        ArrayList<Object> xz = new ArrayList();

        for(int i = 0; i < 3; ++i) {
            xz.add(i, xx[i]);
        }

        planets.put(planetName, xz);
    }

    public static void getHouseSign(double[] cusp, CircularLinkedList horo_chart) {
        String[] zodiacSigns = new String[]{"Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces", "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo", "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"};

        for(int i = 1; i < cusp.length; ++i) {
            int index;
            double start;
            double end;
            ArrayList data;
            String housename;
            if (i != cusp.length - 1) {
                index = (int)(cusp[i] / 30.0);
                start = cusp[i];
                end = cusp[i + 1];
                data = new ArrayList();
                data.add(i);
                data.add(start);
                data.add(end);
                housename = zodiacSigns[index];
                horo_chart.add(housename, data);
            } else {
                index = (int)(cusp[i] / 30.0);
                start = cusp[i];
                end = cusp[1];
                data = new ArrayList();
                data.add(i);
                data.add(start);
                data.add(end);
                housename = zodiacSigns[index];
                horo_chart.add(housename, data);
            }
        }

    }
}
