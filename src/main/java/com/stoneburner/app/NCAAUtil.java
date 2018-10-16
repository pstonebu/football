package com.stoneburner.app;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Math.pow;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Locale.ENGLISH;
import static org.joda.time.Weeks.weeksBetween;
import static org.joda.time.format.DateTimeFormat.forPattern;

public class NCAAUtil extends Util {
    private String inputSP = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTNXgxlcihtmzIbzHDsQH5CXI6aSXfsZzWB7E8IC0sf4CaMsgP_p4DRSwx6TtoektFRCL3wO5m64JLB/pubhtml";
    private String inputAtomic = "http://www.atomicfootball.com/archive/af_predictions_All.html";
    private String inputFPI = "http://www.espn.com/college-football/team/fpi/_/id/%d/";
    private Set<String> acronymTeams = newHashSet();

    public NCAAUtil() {
        DateTime game1 = new DateTime(1535760000000l);
        int week = weeksBetween(game1, today).getWeeks()+2;
        isVegasWeek = week == 11;
        inputURIFox = format("https://www.foxsports.com/college-football/predictions?season=2018&seasonType=1&week=%d&group=-3", week);

        inputMassey = format(inputMassey, "cf", forPattern("yyyyMMdd").print(today));

        inputURIDR = format(inputURIDR, "ncaa");
        inputURIOS = format(inputURIOS, "ncaaf");
        inputSagarin = format(inputSagarin, "cf");
        inputSpread = format(inputSpread, "college-football", isVegasWeek ? "las-vegas" : "offshore");
        input538 = "https://projects.fivethirtyeight.com/2018-college-football-predictions/sims.csv";

        acronymTeams.addAll(asList("BYU", "LSU", "SMU", "TCU", "UAB","UCLA", "UNLV", "USC", "UTEP", "UTSA", "WKU"));

        teamToId.put("Air Force", 2005);
        teamToId.put("Akron", 2006);
        teamToId.put("Alabama", 333);
        teamToId.put("Appalachian State", 2026);
        teamToId.put("Arizona State", 9);
        teamToId.put("Arizona", 12);
        teamToId.put("Arkansas State", 2032);
        teamToId.put("Arkansas", 8);
        teamToId.put("Army", 349);
        teamToId.put("Auburn", 2);
        teamToId.put("Ball State", 2050);
        teamToId.put("Baylor", 239);
        teamToId.put("Boise State", 68);
        teamToId.put("Boston College", 103);
        teamToId.put("Bowling Green", 189);
        teamToId.put("Brigham Young", 252);
        teamToId.put("Buffalo", 2084);
        teamToId.put("California", 25);
        teamToId.put("Central Florida", 2116);
        teamToId.put("Central Michigan", 2117);
        teamToId.put("Charlotte", 2429);
        teamToId.put("Cincinnati", 2132);
        teamToId.put("Clemson", 228);
        teamToId.put("Coastal Carolina", 324);
        teamToId.put("Colorado State", 36);
        teamToId.put("Colorado", 38);
        teamToId.put("Connecticut", 41);
        teamToId.put("Duke", 150);
        teamToId.put("East Carolina", 151);
        teamToId.put("Eastern Michigan", 2199);
        teamToId.put("Florida Atlantic", 2226);
        teamToId.put("Florida International", 2229);
        teamToId.put("Florida State", 52);
        teamToId.put("Florida", 57);
        teamToId.put("Fresno State", 278);
        teamToId.put("Georgia Southern", 290);
        teamToId.put("Georgia State", 2247);
        teamToId.put("Georgia Tech", 59);
        teamToId.put("Georgia", 61);
        teamToId.put("Hawaii", 62);
        teamToId.put("Houston", 248);
        teamToId.put("Illinois", 356);
        teamToId.put("Indiana", 84);
        teamToId.put("Iowa State", 66);
        teamToId.put("Iowa", 2294);
        teamToId.put("Kansas State", 2306);
        teamToId.put("Kansas", 2305);
        teamToId.put("Kent State", 2309);
        teamToId.put("Kentucky", 96);
        teamToId.put("Liberty", 2335);
        teamToId.put("Louisiana Monroe", 2433);
        teamToId.put("Louisiana Tech", 2348);
        teamToId.put("Louisiana", 309);
        teamToId.put("Louisville", 97);
        teamToId.put("LSU", 99);
        teamToId.put("Marshall", 276);
        teamToId.put("Maryland", 120);
        teamToId.put("Massachusetts", 113);
        teamToId.put("Memphis", 235);
        teamToId.put("Miami (FL)", 2390);
        teamToId.put("Miami (OH)", 193);
        teamToId.put("Michigan State", 127);
        teamToId.put("Michigan", 130);
        teamToId.put("Middle Tennessee", 2393);
        teamToId.put("Minnesota", 135);
        teamToId.put("Mississippi State", 344);
        teamToId.put("Mississippi", 145);
        teamToId.put("Missouri", 142);
        teamToId.put("Navy", 2426);
        teamToId.put("Nebraska", 158);
        teamToId.put("Nevada", 2440);
        teamToId.put("New Mexico State", 166);
        teamToId.put("New Mexico", 167);
        teamToId.put("North Carolina State", 152);
        teamToId.put("North Carolina", 153);
        teamToId.put("North Texas", 249);
        teamToId.put("Northern Illinois", 2459);
        teamToId.put("Northwestern", 77);
        teamToId.put("Notre Dame", 87);
        teamToId.put("Ohio State", 194);
        teamToId.put("Ohio", 195);
        teamToId.put("Oklahoma State", 197);
        teamToId.put("Oklahoma", 201);
        teamToId.put("Old Dominion", 295);
        teamToId.put("Oregon State", 204);
        teamToId.put("Oregon", 2483);
        teamToId.put("Penn State", 213);
        teamToId.put("Pittsburgh", 221);
        teamToId.put("Purdue", 2509);
        teamToId.put("Rice", 242);
        teamToId.put("Rutgers", 164);
        teamToId.put("San Diego State", 21);
        teamToId.put("San Jose State", 23);
        teamToId.put("South Alabama", 6);
        teamToId.put("South Carolina", 2579);
        teamToId.put("South Florida", 58);
        teamToId.put("Southern Methodist", 2567);
        teamToId.put("Southern Miss", 2572);
        teamToId.put("Stanford", 24);
        teamToId.put("Syracuse", 183);
        teamToId.put("TCU", 2628);
        teamToId.put("Temple", 218);
        teamToId.put("Tennessee", 2633);
        teamToId.put("Texas A&M", 245);
        teamToId.put("Texas State", 326);
        teamToId.put("Texas Tech", 2641);
        teamToId.put("Texas", 251);
        teamToId.put("Toledo", 2649);
        teamToId.put("Troy", 2653);
        teamToId.put("Tulane", 2655);
        teamToId.put("Tulsa", 202);
        teamToId.put("UAB", 5);
        teamToId.put("UCLA", 26);
        teamToId.put("UNLV", 2439);
        teamToId.put("USC", 30);
        teamToId.put("Utah State", 328);
        teamToId.put("Utah", 254);
        teamToId.put("UTEP", 2638);
        teamToId.put("UTSA", 2636);
        teamToId.put("Vanderbilt", 238);
        teamToId.put("Virginia Tech", 259);
        teamToId.put("Virginia", 258);
        teamToId.put("Wake Forest", 154);
        teamToId.put("Washington State", 265);
        teamToId.put("Washington", 264);
        teamToId.put("West Virginia", 277);
        teamToId.put("Western Kentucky", 98);
        teamToId.put("Western Michigan", 2711);
        teamToId.put("Wisconsin", 275);
        teamToId.put("Wyoming", 2751);
    }

    protected void grab538() {
        log("Fetching '" + input538 + "'");

        //Execute client with our method
        try {
            HashMap<String,Integer> teams = newHashMap();

            String[] rows = connect(input538).body().html().split("(?<=(\\d|winout)[ ])");

            for (int i = 1; i < rows.length; i++) {
                String[] rowParts = rows[i].trim().split(",");
                String teamName = cleanTeamName(rowParts[0]);
                int won = Integer.valueOf(rowParts[6]);

                teams.merge(teamName, won, (a, b) -> a + b);
            }
            for (Map.Entry<String,Integer> entry : teams.entrySet()) {
                int wins = entry.getValue();
                if (wins == 0) {
                    continue;
                }
                Double winPct = entry.getValue() * 100.0 / ((rows.length-1 * 1.0) / teams.size());
                Double spread = null;
                if (winPct < 50.0) {
                    winPct = 100.0 - winPct;
                    spread = pow((winPct / 49.25), (1.0/.194)) * -1.0;
                } else {
                    spread = pow((winPct / 49.25), (1.0/.194));
                }

                Integer id = teamToId.get(entry.getKey());
                if (id != null) {
                    Game game = idToGame.get(id);
                    if (game != null) {
                        game.setFiveThirtyEight(String.valueOf(spread * (game.getHome().equals(entry.getKey()) ? -1.0 : 1.0)));
                    } else {
                        log("No game found for " + entry.getKey() + " with a spread of " + spread);
                    }

                } else {
                    log("No team found for " + entry.getKey() + " with a spread of " + spread);
                }
            }

        } catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabSandP() {
        log("Fetching sAndP predictions");

        try {
            Elements rows = connect(inputSP).select("table").get(1).select("tr");

            for (int i = 2; i < rows.size(); i++) {
                Elements tds = rows.get(i).select("td");
                String teams = tds.get(2).text().replaceAll("No. \\d{1,2} ", "");
                String away = teams.split("(\\bat\\b|vs\\.)")[0].trim();
                String home = teams.split("(\\bat\\b|vs\\.)")[1].trim();
                String spPrediction = tds.get(6).text();
                boolean homeIsSPFavorite = spPrediction.startsWith(home);
                String spMargin = spPrediction.substring(spPrediction.indexOf(" by ") + 4, spPrediction.indexOf("(")).trim();
                String fPlusPrediction = tds.get(10).text();
                boolean homeIsFPFavorite = fPlusPrediction.startsWith(home);
                String fpMargin = fPlusPrediction.substring(fPlusPrediction.indexOf(" by ") + 4).trim();

                home = cleanTeamName(home);
                away = cleanTeamName(away);
                Integer homeId = teamToId.get(home);
                Integer awayId = teamToId.get(away);

                if (homeId != null && awayId != null) {
                    NCAAGame game = (NCAAGame)idToGame.get(homeId);
                    if (isCorrectGame(game, away, home)) {
                        game.setSAndP((homeIsSPFavorite ? "-" : "") + spMargin);
                        game.setFPlus((homeIsFPFavorite ? "-" : "") + fpMargin);
                    } else {
                        logBadTeam(home, away);
                    }
                } else {
                    logBadTeam(home, away);
                }
            }
        } catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabAtomic() {
        log("Fetching '" + inputAtomic + "'");

        try {
            Document page = connect(inputAtomic);
            Elements rows = page.select("a[name=IA]").get(0).nextElementSibling().nextElementSibling().select("tr");
            DateTime currentDate = new DateTime();
            DateTime thisPastMonday = new DateTime().withWeekyear(currentDate.getWeekyear()).withYear(2018).withDayOfWeek(1).withHourOfDay(0);
            DateTime inAWeek = thisPastMonday.plusWeeks(1);

            for (int i = 1; i < rows.size(); i++) {
                Elements currentRowParts = rows.get(i).select("td");
                String date = currentRowParts.get(0).childNode(0).toString();
                String away = cleanTeamName(currentRowParts.get(1).childNode(0).childNode(0).toString());
                String home = cleanTeamName(currentRowParts.get(3).childNode(0).childNode(0).toString());
                String awayScore = currentRowParts.get(2).childNode(0).toString();
                String homeScore = currentRowParts.get(4).childNode(0).toString();
                String margin = String.valueOf(Integer.valueOf(awayScore) - Integer.valueOf(homeScore));

                DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd");
                DateTime gameDate = format.withLocale(ENGLISH).parseDateTime(date).withYear(thisPastMonday.getYear()).withHourOfDay(22);

                if (gameDate.getMillis() < thisPastMonday.getMillis()) {
                    continue;
                } else if (gameDate.getMillis() > inAWeek.getMillis()) {
                    break;
                }

                Integer awayId = teamToId.get(away);
                Integer homeId = teamToId.get(home);
                if (awayId != null && homeId != null) {
                    NCAAGame game = (NCAAGame)idToGame.get(homeId);
                    if (isCorrectGame(game, away, home)) {
                        game.setAtomic(margin);
                    }
                } else {
                    logBadTeam(away, home);
                }
            }
        } catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabFPI() {
        log("Fetching FPI predictions");

        games.stream().map(g -> {return (NCAAGame)g;}).forEach(g -> {
            try {
                Elements tables = connect(format(inputFPI, teamToId.get(g.getAway()))).select("table");
                Elements gameRows = tables.get(tables.size()-1).select("tr");

                for (int i = 2; i < gameRows.size(); i++) {
                    Elements currentGameCells = gameRows.get(i).select("td");
                    String[] nextGameParts = currentGameCells.get(0).text().split("(,)? ");
                    String opponent = cleanTeamName(currentGameCells.get(1).select("li[class=team-name]").text()
                            .replaceAll("#(\\d){1,2} ", "").replace("*", ""));
                    String prediction = currentGameCells.get(2).text().replace("%","");
                    DateTime today = new DateTime();
                    DateTime thisPastMonday = new DateTime().withWeekyear(today.getWeekyear()).withYear(2018).withDayOfWeek(1).withHourOfDay(23);
                    DateTime inAWeek = thisPastMonday.plusWeeks(1);

                    int gameMonth = 0;
                    if (nextGameParts[1].equals("Aug")) {
                        gameMonth = 8;
                    } else if (nextGameParts[1].equals("Sept")) {
                        gameMonth = 9;
                    } else if (nextGameParts[1].equals("Oct")) {
                        gameMonth = 10;
                    } else if (nextGameParts[1].equals("Nov")) {
                        gameMonth = 11;
                    } else if (nextGameParts[1].equals("Dec")) {
                        gameMonth = 12;
                    }

                    DateTime gameDate = new DateTime(thisPastMonday.getYear(), gameMonth, Integer.valueOf(nextGameParts[2]), 22, 0);

                    if (gameDate.getMillis() < thisPastMonday.getMillis() || gameDate.getMillis() > inAWeek.getMillis() ||
                            prediction.contains("W ") || prediction.contains("L ")) {
                        continue;
                    } else if (opponent.equals(g.getHome())) {
                        Double winPct = Double.valueOf(prediction);
                        Double spread = null;
                        if (winPct < 50.0) {
                            winPct = 100.0 - winPct;
                            spread = pow((winPct / 49.25), (1.0/.194)) * -1.0;
                        } else {
                            spread = pow((winPct / 49.25), (1.0/.194));
                        }
                        g.setFpi(String.valueOf(spread));
                        break;
                    } else {
                        logBadTeam(g.getAway(), opponent);
                    }
                }
            } catch (Exception e) {
                logAndExit(e);
            }
        });
    }

    protected String cleanTeamName(String teamName) {
        //Common cleanups
        if (acronymTeams.contains(teamName.toUpperCase())) {
            teamName = teamName.toUpperCase();
        }
        return teamName.replaceAll(" St(.)?$"," State").replaceAll("^E ", "Eastern ").replaceAll("^C ", "Central ")
                .replaceAll("&amp;","&").replaceAll("<b>","").replace("</b>","").replaceAll("i(`|')i", "ii")
                .replaceAll("^W ", "Western ").replace("A&m", "A&M").replace(" AM"," A&M").replaceAll("\\(ucf\\)$", "")
                //team specific cleanup
                .replaceAll("Army West Point", "Army")
                .replaceAll("Bowling Green State", "Bowling Green")
                .replaceAll("BYU", "Brigham Young")
                .replaceAll("SUNY-Buffalo", "Buffalo")
                .replaceAll("UCF", "Central Florida")
                .replaceAll("UNC( |-)Charlotte", "Charlotte")
                .replaceAll("^Coastal Car$", "Coastal Carolina")
                .replaceAll("FL Atlantic", "Florida Atlantic")
                .replaceAll("(FIU|Florida Int(')?l|Fla. International)", "Florida International")
                .replaceAll("Ga Southern", "Georgia Southern")
                .replaceAll("^Kent$", "Kent State")
                .replaceAll("^(ULM|Louisiana-([Mm])onroe|UL([- ])Monroe)$","Louisiana Monroe")
                .replaceAll("((UL|Louisiana)-([Ll])afayette|ULL)", "Louisiana")
                .replaceAll("Louisiana State", "LSU")
                .replaceAll("UMass", "Massachusetts")
                .replaceAll("(Miami( |-)(FL|florida)|^Miami$)", "Miami (FL)")
                .replaceAll("Miami( |-)(OH|ohio)", "Miami (OH)")
                .replaceAll("(Middle Tennessee State|MTSU)", "Middle Tennessee")
                .replaceAll("Ole Miss", "Mississippi")
                .replaceAll("N(\\.?)([Cc])(\\.?) State", "North Carolina State")
                .replaceAll("N Illinois", "Northern Illinois")
                .replaceAll("Ohio U.", "Ohio")
                .replaceAll("SMU", "Southern Methodist")
                .replaceAll("Southern Mississippi", "Southern Miss")
                .replaceAll("Texas Christian", "TCU")
                .replaceAll("Texas St-San Marcos", "Texas State")
                .replaceAll("Alabama-Birmingham", "UAB")
                .replaceAll("California-Los Angeles", "UCLA")
                .replaceAll("Nevada-Las Vegas", "UNLV")
                .replaceAll("Southern Cal(ifornia)?", "USC")
                .replaceAll("Texas El Paso", "UTEP")
                .replaceAll("(Texas-San Antonio|UT San Antonio)", "UTSA")
                .replaceAll("WKU", "Western Kentucky")
                .trim();
    }

    protected String removeMascot(String city) {
        return city;
    }

    protected String getCityFromMascot(String mascot) {
        return mascot;
    }

    protected NCAAGame getNewGame() {
        return new NCAAGame();
    }
}
