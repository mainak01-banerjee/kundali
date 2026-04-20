package tech.mainak.kundali.kundali_2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;


import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.mainak.kundali.kundali_2.app.BirthHoroscope;
import tech.mainak.kundali.kundali_2.app.VimshottariDashaCalculator;
import tech.mainak.kundali.kundali_2.dto.Period;

import java.io.IOException;
import java.security.KeyStore;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@RequiredArgsConstructor
public class HoroscopeService {

    private final Logger logger = LoggerFactory.getLogger(HoroscopeService.class);
    @Value("${ephe.path}")
    private String path;


    public String Chart(int year, int month, int day, int hour, int minute, double latitude, double longitude) throws IOException {
        String resp="";

        BirthHoroscope horoscope =new BirthHoroscope(year,month,day,hour,minute,latitude,longitude,path);
        Map<String,Object> horo = horoscope.getHoroScope();
        resp=formatResponse(horo);

       // System.out.println("*********************formattedResponse*********************:\n"+resp);
        return resp;
    }

    public String vimshottariDashaChart(int year, int month, int day, int hour, int minute, double latitude, double longitude) throws IOException {
        LocalDate birthDay =LocalDate.of(year,month,day);
        String resp="";
        BirthHoroscope horoscope =  new BirthHoroscope(year,month,day,hour,minute,latitude,longitude,path);
        Map<String,Object> horo = horoscope.getHoroScope();
        System.out.println("Horo: "+horo.toString());

        Map<String,Object> planets= (Map<String, Object>) horo.get("planets");

        System.out.println("Planets: "+planets.toString());

        Map<String,Object> Moondata= (Map<String, Object>) planets.get("Moon");

        System.out.println("Moondata: "+ Moondata.toString());

        double moonlongitude = (double) Moondata.get("longitude");

        System.out.println("Moonlongitude: "+moonlongitude);

        LinkedHashMap<String,Period> mahadasha = VimshottariDashaCalculator.calculateDasha(moonlongitude,birthDay);


        System.out.println("mahadasha: "+mahadasha.toString());


        LinkedHashMap<String,LinkedHashMap<String,Period>> antardasha =VimshottariDashaCalculator.calculateAntardasha(mahadasha);

        System.out.println("antardasha: "+antardasha.toString());

        StringBuilder mDasha=new StringBuilder();
        mDasha.append("\"mahadasha\":").append("{");

        LinkedHashMap<String,String> MAHADASHA = new LinkedHashMap<>();
        for(Map.Entry<String,Period> entry: mahadasha.entrySet()){
            String Key=entry.getKey();
            mDasha.append("\"").append(Key).append("\":");
            Period Period=entry.getValue();
            String period=Period.getStart()+"---"+Period.getEnd();
            mDasha.append("\"").append(period).append("\",");
            MAHADASHA.put(Key,period);
        }
        mDasha.deleteCharAt(mDasha.length()-1);
        mDasha.append("}");
        System.out.println("mDasha: "+mDasha.toString());


        StringBuilder aDasha=new StringBuilder();
        aDasha.append("\"antardasha\":").append("{");

        for(Map.Entry<String,LinkedHashMap<String,Period>> entry: antardasha.entrySet()){
            String Key=entry.getKey();
            aDasha.append("\"").append(Key).append("\":");
            aDasha.append("{");
            LinkedHashMap<String,Period> antardashas=entry.getValue();
            for(Map.Entry<String,Period> entry2: antardashas.entrySet()){
                String aKey=entry2.getKey();
                aDasha.append("\"").append(aKey).append("\":");
                Period Period2=entry2.getValue();
                String period2=Period2.getStart()+"---"+Period2.getEnd();
                aDasha.append("\"").append(period2).append("\",");
            }
            aDasha.deleteCharAt(aDasha.length()-1);
            aDasha.append("}");
            aDasha.append(",");
        }
        aDasha.deleteCharAt(aDasha.length()-1);
        aDasha.append("}");
        System.out.println("aDasha: "+aDasha.toString());

        StringBuilder karaka = getKarakas(planets);


        resp=formatResponse(horo);

        StringBuilder response=new StringBuilder();


        response.append(resp);

        response.deleteCharAt(response.length()-1);
        response.append(",");
        response.append(mDasha);
        response.append(",");
        response.append(aDasha);
        response.append(",");
        response.append(karaka);
        response.append("}");


        System.out.println("resp: "+response.toString());

        return response.toString();
    }
    private static final String[] karakaPlanetNames={"Venus", "Sun", "Moon", "Mars", "Jupiter", "Saturn", "Mercury"};

    public StringBuilder getKarakas(Map<String,Object> planets){

        JSONObject json=new JSONObject(planets);
        double[] longitudes=new double[karakaPlanetNames.length];
        LinkedHashMap<Double,String> LongitudePlanetMap=new LinkedHashMap<>();
        int i=0;
        for (String planet: karakaPlanetNames){
            JSONObject object=json.getJSONObject(planet);
            double houseLongitude = object.getDouble("house_longitude");
            longitudes[i]=houseLongitude;
            LongitudePlanetMap.put(houseLongitude,planet);
            i++;
        }

        String[] karakamap={"Dara","Gnati","Putra","Matrua","Bhratru","Amatya","Atma"};

        Arrays.sort(longitudes);

        StringBuilder response=new StringBuilder();

        response.append("\"karakas\":").append("{");

        for(i=longitudes.length-1;i>=0;i--){
            response.append("\"").append(karakamap[i]).append("\":").append("\"").append(LongitudePlanetMap.get(longitudes[i])).append("\"");
            response.append(",");
        }
        response.deleteCharAt(response.length()-1);
        response.append("}");


        return response;
    }


    private static final int [] daysInMonth ={31,28,31,30,31,30,31,31,30,31,30,31};




        private static final String[] planetNames={"Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury"};

    private LinkedHashMap<String ,LinkedHashMap<String, Period>> getAntardashaPeriod(LocalDate birthDay, LinkedHashMap<String,LinkedHashMap<String,Double>> antardasha) {

            Map<String,String> planetShortForms=new HashMap<>();
            for(String planet: planetNames){
                planetShortForms.put(planet,planet.substring(0,2));
            }
            LinkedHashMap<String,LinkedHashMap<String,Period>> antardashaperiod = new LinkedHashMap<>();
            LocalDate antardashaStart=birthDay;
            for(Map.Entry<String,LinkedHashMap<String,Double>> entry : antardasha.entrySet()){
                String mahadashaPlanet = entry.getKey();

                LinkedHashMap<String,Double> antardashas = entry.getValue();
                LinkedHashMap<String,Period> antarPeriod = new LinkedHashMap<>();
                 for(Map.Entry<String,Double> antarEntry : antardashas.entrySet()){
                     Period period =new Period();
                     String antardashaPlanet = antarEntry.getKey();
                     Double decimalYears=antarEntry.getValue();

                     int years = (int) (double) decimalYears;

                     double decimalMonths = (decimalYears - years) * 12;

                     int months = (int) decimalMonths;

                     LocalDate antardashaEnd=antardashaStart.plusYears(years).plusMonths(months);

                     int  endDateMonth=antardashaEnd.getMonthValue();
                     int  endDateMonthIndex=endDateMonth-1;
                     int daysInEndDateMonth=0;
                     if(antardashaEnd.isLeapYear() && endDateMonthIndex==1){
                         daysInEndDateMonth=29;
                     }
                     else {
                         daysInEndDateMonth=daysInMonth[endDateMonthIndex];
                     }

                     double decimalDays = (decimalMonths - months) * daysInEndDateMonth;
                     int days = (int) decimalDays;
                     antardashaEnd=antardashaEnd.plusDays(days);
                     period.setEnd(antardashaEnd.toString());
                     antardashaStart=antardashaEnd;

                     StringBuilder key = new StringBuilder();
                     key.append(planetShortForms.get(mahadashaPlanet)).append("-").append(planetShortForms.get(antardashaPlanet));
                     antarPeriod.put(key.toString(),period);

                 }
                 antardashaperiod.put(planetShortForms.get(mahadashaPlanet),antarPeriod);

            }
            return antardashaperiod;
    }




    private String formatResponse(Map<String, Object> response) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        // Convert the Map to a JSON string
        String resp = mapper.writeValueAsString(response);

        // Convert the response string into a JSONObject for easier manipulation
        JSONObject json = new JSONObject(resp);

        // Extract the necessary fields from the response JSON
        double julday = json.getDouble("JulianDay");
        double ayanamsa = json.getDouble("Ayanamsa");
        JSONObject horoscope = json.getJSONObject("horoscope");
        String svg = createNorthIndianSVG(horoscope);
        String Esvg = createEastIndianSVG(horoscope);
        String Ssvg = createSouthIndianSVG(horoscope);

        JSONObject planets = json.getJSONObject("planets");


        JSONObject formattedResponse = new JSONObject();
         // Add SVG data
        formattedResponse.put("planets", new JSONObject(planets.toString())); // Add planets data
        formattedResponse.put("ayanamsa", ayanamsa); // Add Ayanamsa data
        formattedResponse.put("svg", svg);
        formattedResponse.put("Esvg", Esvg);
        formattedResponse.put("Ssvg", Ssvg);


        return formattedResponse.toString();
    }

    private String createNorthIndianSVG(JSONObject horoscope) {
        StringBuilder svgBuilder = new StringBuilder();
        svgBuilder.append("<svg viewBox=\"0 0 700 700\" xmlns=\"http://www.w3.org/2000/svg\">\n");
        svgBuilder.append("    <rect x=\"50\" y=\"50\" width=\"600\" height=\"600\" fill=\"none\" stroke=\"black\" stroke-width=\"2\"></rect>\n");
        svgBuilder.append("    <line x1=\"50\" y1=\"350\" x2=\"350\" y2=\"50\" stroke=\"black\" stroke-width=\"2\"></line>\n");
        svgBuilder.append("    <line x1=\"50\" y1=\"350\" x2=\"350\" y2=\"650\" stroke=\"black\" stroke-width=\"2\"></line>\n");
        svgBuilder.append("    <line x1=\"350\" y1=\"50\" x2=\"650\" y2=\"350\" stroke=\"black\" stroke-width=\"2\"></line>\n");
        svgBuilder.append("    <line x1=\"650\" y1=\"350\" x2=\"350\" y2=\"650\" stroke=\"black\" stroke-width=\"2\"></line>\n");
        svgBuilder.append("    <line x1=\"50\" y1=\"50\" x2=\"650\" y2=\"650\" stroke=\"black\" stroke-width=\"2\"></line>\n");
        svgBuilder.append("    <line x1=\"650\" y1=\"50\" x2=\"50\" y2=\"650\" stroke=\"black\" stroke-width=\"2\"></line>\n");
        int[][] housePositions = new int[][] {
                {345, 90},
                {200, 180},
                {55, 150},
                {200, 270},
                {55, 500},
                {180, 540},
                {345, 630},
                {490, 540},
                {515, 510},
                {620, 355},
                {515, 210},
                {490, 180}
        };


        // Define text positions for houses
        int[][] textPositions = {
                {255, 160}, {110, 100}, {60, 200}, {75, 350}, {80, 500}, {90, 635},
                {240, 480}, {400, 620}, {530, 500}, {380, 350}, {540, 210}, {455, 100}
        };



        // Mapping of planet names to short forms
        Map<String, String> planetShortForms = new HashMap<>();
        planetShortForms.put("Sun", "Su");
        planetShortForms.put("Moon", "Mo");
        planetShortForms.put("Mars", "Ma");
        planetShortForms.put("Mercury", "Me");
        planetShortForms.put("Jupiter", "Ju");
        planetShortForms.put("Venus", "Ve");
        planetShortForms.put("Saturn", "Sa");
        planetShortForms.put("Uranus", "Ur");
        planetShortForms.put("Neptune", "Ne");
        planetShortForms.put("Rahu_True", "Ra");
        planetShortForms.put("Ketu_True", "Ke");
        planetShortForms.put("Rahu_Mean", "Ra_M");
        planetShortForms.put("Ketu_Mean", "Ke_M");

        Map<String, Integer> zodiacHouseNumber = new HashMap<>();
        zodiacHouseNumber.put("Aries", 1);
        zodiacHouseNumber.put("Taurus", 2);
        zodiacHouseNumber.put("Gemini", 3);
        zodiacHouseNumber.put("Cancer", 4);
        zodiacHouseNumber.put("Leo", 5);
        zodiacHouseNumber.put("Virgo", 6);
        zodiacHouseNumber.put("Libra", 7);
        zodiacHouseNumber.put("Scorpio", 8);
        zodiacHouseNumber.put("Sagittarius", 9);
        zodiacHouseNumber.put("Capricorn", 10);
        zodiacHouseNumber.put("Aquarius", 11);
        zodiacHouseNumber.put("Pisces", 12);



        // Loop through each house and add text elements
        for (int i = 1; i <= 12; i++) {
            JSONArray houseData = horoscope.getJSONArray("house_" + i);
            String sign = houseData.getString(3);
            int houseNUmber = zodiacHouseNumber.get(sign);

            svgBuilder.append("<text x=\"").append(housePositions[(i - 1) ][0])
                    .append("\" y=\"").append(housePositions[(i - 1)][1])
                    .append("\" font-size=\"20\" fill=\"black\">").append(houseNUmber).append(" ")
                    .append("</text>\n");

            if (houseData.length() >= 4) {
                StringBuilder planets = new StringBuilder();
                for (int j = 4; j < houseData.length(); j++) {
                    String planet = houseData.getString(j);
                    planets.append(planetShortForms.getOrDefault(planet, planet)).append(" ");
                }

                svgBuilder.append("<text x=\"").append(textPositions[(i - 1) ][0])
                        .append("\" y=\"").append(textPositions[(i - 1)][1])
                        .append("\" font-size=\"18\" fill=\"blue\">").append(planets.toString().trim())
                        .append("</text>\n");
            }
        }

        svgBuilder.append("</svg>");

       // System.out.println("NorthIndianChart: "+svgBuilder.toString());
        return svgBuilder.toString();
    }

    private String createEastIndianSVG(JSONObject horoscope) {
        StringBuilder svgBuilder = new StringBuilder();
        svgBuilder.append("<svg viewBox=\"0 0 700 700\" xmlns=\"http://www.w3.org/2000/svg\">\n");

        // Draw the main square and grid lines
        svgBuilder.append("    <rect x=\"50\" y=\"50\" width=\"600\" height=\"600\" ")
                .append("fill=\"none\" stroke=\"black\" stroke-width=\"2\"/>\n");

        // Horizontal lines
        svgBuilder.append("    <line x1=\"50\" y1=\"250\" x2=\"650\" y2=\"250\" ")
                .append("stroke=\"black\" stroke-width=\"2\"/>\n");
        svgBuilder.append("    <line x1=\"50\" y1=\"450\" x2=\"650\" y2=\"450\" ")
                .append("stroke=\"black\" stroke-width=\"2\"/>\n");

        // Vertical lines
        svgBuilder.append("    <line x1=\"250\" y1=\"50\" x2=\"250\" y2=\"650\" ")
                .append("stroke=\"black\" stroke-width=\"2\"/>\n");
        svgBuilder.append("    <line x1=\"450\" y1=\"50\" x2=\"450\" y2=\"650\" ")
                .append("stroke=\"black\" stroke-width=\"2\"/>\n");

        // Diagonal lines
        svgBuilder.append("    <line x1=\"50\" y1=\"50\" x2=\"250\" y2=\"250\" ")
                .append("stroke=\"black\" stroke-width=\"2\"/>\n");
        svgBuilder.append("    <line x1=\"650\" y1=\"650\" x2=\"450\" y2=\"450\" ")
                .append("stroke=\"black\" stroke-width=\"2\"/>\n");
        svgBuilder.append("    <line x1=\"50\" y1=\"650\" x2=\"250\" y2=\"450\" ")
                .append("stroke=\"black\" stroke-width=\"2\"/>\n");
        svgBuilder.append("    <line x1=\"650\" y1=\"50\" x2=\"450\" y2=\"250\" ")
                .append("stroke=\"black\" stroke-width=\"2\"/>\n");

        // Title
        svgBuilder.append("    <text x=\"310\" y=\"350\" font-size=\"20\" ")
                .append("font-family=\"cursive\">Kundali</text>\n");



        // Define house positions for numbers and planets
        int[][] housePositions = {
                {350, 70},   // House 1
                {230, 220},  // House 2
                {55, 150},   // House 3
                {55, 295},   // House 4
                {55, 500},   // House 5
                {230, 500},  // House 6
                {350, 645},  // House 7
                {455, 495},  // House 8
                {480, 470},  // House 9
                {620, 350},  // House 10
                {490, 240},  // House 11
                {460, 200}   // House 12
        };

        for (int i = 1; i <housePositions.length ; i++) {
            svgBuilder.append("    <text x=\"").append(housePositions[i-1][0])
                    .append("\" y=\"").append(housePositions[i-1][1])
                    .append("\" font-size=\"20\" font-family=\"cursive\">")
                    .append(i).append("</text>\n");
        }

        int[][] planetPositions = {
                {300, 100},  // House 1 planets
                {100, 100},  // House 2 planets
                {60, 240},   // House 3 planets
                {75, 320},   // House 4 planets
                {80, 500},   // House 5 planets
                {120, 610},  // House 6 planets
                {270, 550},  // House 7 planets
                {460, 610},  // House 8 planets
                {520, 500},  // House 9 planets
                {480, 350},  // House 10 planets
                {520, 200},  // House 11 planets
                {455, 100}   // House 12 planets
        };

        // Mapping of planet names to short forms (reusing from North Indian chart)
        Map<String, String> planetShortForms = new HashMap<>();
        planetShortForms.put("Sun", "Su");
        planetShortForms.put("Moon", "Mo");
        planetShortForms.put("Mars", "Ma");
        planetShortForms.put("Mercury", "Me");
        planetShortForms.put("Jupiter", "Ju");
        planetShortForms.put("Venus", "Ve");
        planetShortForms.put("Saturn", "Sa");
        planetShortForms.put("Uranus", "Ur");
        planetShortForms.put("Neptune", "Ne");
        planetShortForms.put("Rahu_True", "Ra");
        planetShortForms.put("Ketu_True", "Ke");
        planetShortForms.put("Rahu_Mean", "Ra_M");
        planetShortForms.put("Ketu_Mean", "Ke_M");

        Map<String, Integer> zodiacHouseNumber = new HashMap<>();
        zodiacHouseNumber.put("Aries", 1);
        zodiacHouseNumber.put("Taurus", 2);
        zodiacHouseNumber.put("Gemini", 3);
        zodiacHouseNumber.put("Cancer", 4);
        zodiacHouseNumber.put("Leo", 5);
        zodiacHouseNumber.put("Virgo", 6);
        zodiacHouseNumber.put("Libra", 7);
        zodiacHouseNumber.put("Scorpio", 8);
        zodiacHouseNumber.put("Sagittarius", 9);
        zodiacHouseNumber.put("Capricorn", 10);
        zodiacHouseNumber.put("Aquarius", 11);
        zodiacHouseNumber.put("Pisces", 12);

        // Add house numbers and planets
        for (int i = 1; i <= 12; i++) {
            JSONArray houseData = horoscope.getJSONArray("house_" + i);
            String sign = houseData.getString(3);
            int houseNUmber = zodiacHouseNumber.get(sign);

            // Add planets if present
            if (houseData.length() > 4) {
                StringBuilder planets = new StringBuilder();
                for (int j = 4; j < houseData.length(); j++) {
                    String planet = houseData.getString(j);
                    planets.append(planetShortForms.getOrDefault(planet, planet)).append(" ");
                }

                svgBuilder.append("    <text x=\"").append(planetPositions[houseNUmber-1][0])
                        .append("\" y=\"").append(planetPositions[houseNUmber-1][1])
                        .append("\" font-size=\"18\" font-family=\"cursive\">")
                        .append(planets.toString().trim()).append("</text>\n");
            }
        }

        svgBuilder.append("</svg>");
       // System.out.println("EastIndianChart: "+svgBuilder.toString());
        return svgBuilder.toString();
    }

    private String createSouthIndianSVG(JSONObject horoscope) {
        StringBuilder svgBuilder = new StringBuilder();
        svgBuilder.append("<svg viewBox=\" 0 0 900 900 \" xmlns=\"http://www.w3.org/2000/svg\">\n");
        svgBuilder.append("<rect x=\"50\" y=\"50\" width=\"800\" height=\"800\" fill=\"none\" stroke=\"black\" stroke-width=\"2\"></rect>\n");
        svgBuilder.append("<line x1=\"250\" y1=\"50\" x2=\"250\" y2=\"850\" stroke=\"black\" stroke-width=\"2\"></line>\n");
        svgBuilder.append("<line x1=\"450\" y1=\"50\" x2=\"450\" y2=\"850\" stroke=\"black\" stroke-width=\"2\"></line>\n");
        svgBuilder.append("<line x1=\"50\" y1=\"250\" x2=\"850\" y2=\"250\" stroke=\"black\" stroke-width=\"2\"></line>\n");
        svgBuilder.append("<line x1=\"50\" y1=\"450\" x2=\"850\" y2=\"450\" stroke=\"black\" stroke-width=\"2\"></line>\n");
        svgBuilder.append("<line x1=\"50\" y1=\"650\" x2=\"850\" y2=\"650\" stroke=\"black\" stroke-width=\"2\"></line>\n");
        svgBuilder.append("<line x1=\"650\" y1=\"50\" x2=\"650\" y2=\"850\" stroke=\"black\" stroke-width=\"2\"></line>\n");
        svgBuilder.append("<text x=\"345\" y=\"90\" font-size=\"20\" font-family=\"cursive\">1</text>\n" +
                "    <text x=\"550\" y=\"90\" font-size=\"20\" font-family=\"cursive\">2</text>\n" +
                "    <text x=\"700\" y=\"90\" font-size=\"20\" font-family=\"cursive\">3</text>\n" +
                "    <text x=\"700\" y=\"280\" font-size=\"20\" font-family=\"cursive\">4</text>\n" +
                "    <text x=\"700\" y=\"480\" font-size=\"20\" font-family=\"cursive\">5</text>\n" +
                "    <text x=\"700\" y=\"680\" font-size=\"20\" font-family=\"cursive\">6</text>\n" +
                "    <text x=\"550\" y=\"680\" font-size=\"20\" font-family=\"cursive\">7</text>\n" +
                "    <text x=\"340\" y=\"680\" font-size=\"20\" font-family=\"cursive\">8</text>\n" +
                "    <text x=\"150\" y=\"680\" font-size=\"20\" font-family=\"cursive\">9</text>\n" +
                "    <text x=\"150\" y=\"480\" font-size=\"20\" font-family=\"cursive\">10</text>\n" +
                "    <text x=\"150\" y=\"280\" font-size=\"20\" font-family=\"cursive\">11</text>\n" +
                "    <text x=\"150\" y=\"90\" font-size=\"20\" font-family=\"cursive\">12</text>\n");


        int[][] planetPositions = new int[][]{
                {255,160},
                {460,180},
                {660,200},
                {660,350},
                {660,540},
                {660,710},
                {460,710},
                {260,710},
                {60,710},
                {60,540},
                {60,340},
                {60,160},
        };

        Map<String, Integer> zodiacHouseNumber = new HashMap<>();
        zodiacHouseNumber.put("Aries", 1);
        zodiacHouseNumber.put("Taurus", 2);
        zodiacHouseNumber.put("Gemini", 3);
        zodiacHouseNumber.put("Cancer", 4);
        zodiacHouseNumber.put("Leo", 5);
        zodiacHouseNumber.put("Virgo", 6);
        zodiacHouseNumber.put("Libra", 7);
        zodiacHouseNumber.put("Scorpio", 8);
        zodiacHouseNumber.put("Sagittarius", 9);
        zodiacHouseNumber.put("Capricorn", 10);
        zodiacHouseNumber.put("Aquarius", 11);
        zodiacHouseNumber.put("Pisces", 12);


        Map<String, String> planetShortForms = new HashMap<>();
        planetShortForms.put("Sun", "Su");
        planetShortForms.put("Moon", "Mo");
        planetShortForms.put("Mars", "Ma");
        planetShortForms.put("Mercury", "Me");
        planetShortForms.put("Jupiter", "Ju");
        planetShortForms.put("Venus", "Ve");
        planetShortForms.put("Saturn", "Sa");
        planetShortForms.put("Uranus", "Ur");
        planetShortForms.put("Neptune", "Ne");
        planetShortForms.put("Rahu_True", "Ra");
        planetShortForms.put("Ketu_True", "Ke");
        planetShortForms.put("Rahu_Mean", "Ra_M");
        planetShortForms.put("Ketu_Mean", "Ke_M");






        for (int i = 1; i <= 12; i++) {
            JSONArray houseData = horoscope.getJSONArray("house_" + i);
            String sign = houseData.getString(3);
            int houseNUmber = zodiacHouseNumber.get(sign);

            // Add planets if present
            if (houseData.length() > 4) {
                StringBuilder planets = new StringBuilder();
                for (int j = 4; j < houseData.length(); j++) {
                    String planet = houseData.getString(j);
                    planets.append(planetShortForms.getOrDefault(planet, planet)).append(" ");
                }

                svgBuilder.append("    <text x=\"").append(planetPositions[houseNUmber-1][0])
                        .append("\" y=\"").append(planetPositions[houseNUmber-1][1])
                        .append("\" font-size=\"18\" font-family=\"cursive\">")
                        .append(planets.toString().trim()).append("</text>\n");
            }
        }

        svgBuilder.append("</svg>");
        //System.out.println("SouthIndianChart: "+svgBuilder.toString());
        return svgBuilder.toString();

    }




}
