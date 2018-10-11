package com.stoneburner.app;

import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Map;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.joda.time.Weeks.weeksBetween;
import static org.joda.time.format.DateTimeFormat.forPattern;
import static org.jsoup.Jsoup.connect;

public class NFLUtil extends Util {

    public NFLUtil() {
        int week = weeksBetween(new DateTime(1536451200000l), today).getWeeks()+2;
        isVegasWeek = week == 10;

        inputMassey = format(inputMassey, "nfl", forPattern("yyyyMMdd").print(today));
        inputURIDR = format(inputURIDR, "nfl");
        inputURIOS = format(inputURIOS, "nfl");
        inputURIFox = "http://www.foxsports.com/nfl/predictions";
        inputSagarin = format(inputSagarin, "nfl");
        inputSpread = format(inputSpread, "nfl", isVegasWeek ? "las-vegas" : "offshore");
        input538 = "https://projects.fivethirtyeight.com/2018-nfl-predictions/games/";

        teamMascotToCity.put("Titans","Tennessee");
        teamMascotToCity.put("Jaguars","Jacksonville");
        teamMascotToCity.put("Bengals","Cincinnati");
        teamMascotToCity.put("Redskins","Washington");
        teamMascotToCity.put("Panthers","Carolina");
        teamMascotToCity.put("Cardinals","Arizona");
        teamMascotToCity.put("Texans","Houston");
        teamMascotToCity.put("Lions","Detroit");
        teamMascotToCity.put("Colts","Indianapolis");
        teamMascotToCity.put("Chiefs","Kansas City");
        teamMascotToCity.put("Bills","Buffalo");
        teamMascotToCity.put("Patriots","New England");
        teamMascotToCity.put("Browns","Cleveland");
        teamMascotToCity.put("Jets","New York Jets");
        teamMascotToCity.put("Buccaneers","Tampa Bay");
        teamMascotToCity.put("Raiders","Oakland");
        teamMascotToCity.put("Saints","New Orleans");
        teamMascotToCity.put("Seahawks","Seattle");
        teamMascotToCity.put("Broncos","Denver");
        teamMascotToCity.put("Chargers","Los Angeles Chargers");
        teamMascotToCity.put("Falcons","Atlanta");
        teamMascotToCity.put("Packers","Green Bay");
        teamMascotToCity.put("Cowboys","Dallas");
        teamMascotToCity.put("Eagles","Philadelphia");
        teamMascotToCity.put("Bears","Chicago");
        teamMascotToCity.put("Vikings","Minnesota");
        teamMascotToCity.put("49ers","San Francisco");
        teamMascotToCity.put("Dolphins","Miami");
        teamMascotToCity.put("Steelers","Pittsburgh");
        teamMascotToCity.put("Ravens","Baltimore");
        teamMascotToCity.put("Giants","New York Giants");
        teamMascotToCity.put("Rams","Los Angeles Rams");

        teamToId.put("Kansas City", 1);
        teamToId.put("New England", 2);
        teamToId.put("Pittsburgh", 3);
        teamToId.put("Cincinnati", 4);
        teamToId.put("Baltimore", 5);
        teamToId.put("Tennessee", 6);
        teamToId.put("San Francisco", 7);
        teamToId.put("Green Bay", 8);
        teamToId.put("Jacksonville", 9);
        teamToId.put("Dallas", 10);
        teamToId.put("Los Angeles Chargers", 11);
        teamToId.put("Cleveland", 12);
        teamToId.put("Carolina", 13);
        teamToId.put("Washington", 14);
        teamToId.put("Philadelphia", 15);
        teamToId.put("New York Giants", 16);
        teamToId.put("Tampa Bay", 17);
        teamToId.put("Atlanta", 18);
        teamToId.put("Chicago", 19);
        teamToId.put("Miami", 20);
        teamToId.put("Indianapolis", 21);
        teamToId.put("New York Jets", 22);
        teamToId.put("Los Angeles Rams", 23);
        teamToId.put("Denver", 24);
        teamToId.put("Seattle", 25);
        teamToId.put("Oakland", 26);
        teamToId.put("Arizona", 27);
        teamToId.put("Minnesota", 28);
        teamToId.put("Buffalo", 29);
        teamToId.put("Houston", 30);
        teamToId.put("Detroit", 31);
        teamToId.put("New Orleans", 32);
    }

    protected void grab538() {
        System.out.println( "Fetching '" + input538 + "'");

        //Execute client with our method
        try
        {
            Document page = connect(input538).get();

            Element week = page.select("section[class=week]").get(0);
            Elements rows = week.select("tr[class=tr]");

            for (int i = 0; i < rows.size(); i=i+3) {
                String awayTeam = cleanTeamName(rows.get(i+1).select("td[class=td text team]").get(0).childNodes().get(0).toString().trim());
                String homeTeam = cleanTeamName(rows.get(i+2).select("td[class=td text team]").get(0).childNodes().get(0).toString().trim());

                String awaySpread = rows.get(i+1).select("td[class=td number spread]").get(0).childNodes().get(0).toString().trim();
                String homeSpread = rows.get(i+2).select("td[class=td number spread]").get(0).childNodes().get(0).toString().trim();
                if (homeSpread != null && homeSpread.equals("PK")) {
                    homeSpread = "0.0";
                } else if (awaySpread != null && awaySpread.equals("PK")) {
                    awaySpread = "+0.0";
                }

                Integer homeId = teamToId.get(homeTeam);
                Integer awayId = teamToId.get(awayTeam);
                if (homeId != null && awayId != null) {
                    Game game = idToGame.get(homeId);
                    game.setFiveThirtyEight(!isEmpty(awaySpread) ? awaySpread.substring(1) : homeSpread);
                }

            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    protected String cleanTeamName(String teamName) {
        return teamName.replaceAll("(NY|N.Y.)", "New York")
                .replaceAll("(LA|L.A.)", "Los Angeles");
    }

    protected String removeMascot(String city) {
        if (city.contains("Los Angeles")) {
            return city;
        }
        for (Map.Entry<String,String> mascotAndCity : teamMascotToCity.entrySet()) {
            if (city.endsWith(mascotAndCity.getKey())) {
                return mascotAndCity.getValue();
            }
        }
        return city;
    }

    protected String getCityFromMascot(String mascot) {
        return teamMascotToCity.get(mascot);
    }

    protected Game getNewGame() {
        return new Game();
    }
}
