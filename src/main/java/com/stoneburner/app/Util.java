package com.stoneburner.app;

import com.google.common.util.concurrent.AtomicDouble;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Double.valueOf;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.text.StringEscapeUtils.unescapeHtml4;
import static org.apache.commons.text.WordUtils.capitalizeFully;
import static org.joda.time.Weeks.weeksBetween;
import static org.jsoup.Jsoup.connect;

public class Util {
    private String inputURIPR = "https://thepowerrank.com/predictions/";
    private String inputURIDRNCAA = "http://www.dratings.com/predictor/ncaa-football-predictions/";
    private String inputURIDRNFL = "http://www.dratings.com/predictor/nfl-football-predictions/";
    private String inputURIOSNCAA = "https://www.oddsshark.com/ncaaf/computer-picks";
    private String inputURIOSNFL = "http://www.oddsshark.com/nfl/computer-picks";
    private String inputURIFoxNCAA = "https://www.foxsports.com/college-football/predictions?season=2018&seasonType=1&week=%d&group=-3";
    private String inputURIFoxNFL = "http://www.foxsports.com/nfl/predictions";
    private String inputSP = "https://www.footballstudyhall.com/pages/2018-%team%-advanced-statistical-profile";
    private String inputSPSheet = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTNXgxlcihtmzIbzHDsQH5CXI6aSXfsZzWB7E8IC0sf4CaMsgP_p4DRSwx6TtoektFRCL3wO5m64JLB/pubhtml";
    private String inputSagarinNCAA = "http://sagarin.com/sports/cfsend.htm";
    private String inputSagarinNFL = "http://sagarin.com/sports/nflsend.htm";
    private String inputMasseyNCAA = "http://www.masseyratings.com/predjson.php?s=cf&sub=11604&dt=$dt$";
    private String inputMasseyNFL = "http://www.masseyratings.com/predjson.php?s=nfl&dt=$dt$";
    private String inputSpreadNCAA = "http://www.vegasinsider.com/college-football/odds/offshore/2/";
    private String inputSpreadNFL = "http://www.vegasinsider.com/nfl/odds/offshore/2/";
    private String input538NCAA = "https://projects.fivethirtyeight.com/2018-college-football-predictions/sims.csv";
    private String input538NFL = "https://projects.fivethirtyeight.com/2018-nfl-predictions/games/";
    private String inputAtomic = "http://www.atomicfootball.com/archive/af_predictions_All.html";

    private List<String> teamwords = newArrayList();
    private HashMap<String,String> teamShortToLong = newHashMap();
    private HashMap<String,String> teamMascotToCity = newHashMap();
    private HashMap<String,Integer> teamToId = newHashMap();
    private List<Game> games = newArrayList();
    private HashMap<Integer,Game> idToGame = newHashMap();

    private boolean isNFL;
    
    public Util(boolean isNFL) {
        //What week of the season is it?
        DateTime game1 = new DateTime(1535760000000l);
        DateTime today = new DateTime();
        int week = weeksBetween(game1, today).getWeeks()+2;
        inputURIFoxNCAA = format(inputURIFoxNCAA, week);

        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyyMMdd");
        inputMasseyNCAA = inputMasseyNCAA.replace("$dt$", dtfOut.print(today));
        inputMasseyNFL = inputMasseyNFL.replace("$dt$", dtfOut.print(today));

        this.isNFL = isNFL;

        teamwords.add("new");
        teamwords.add("england");
        teamwords.add("oakland");
        teamwords.add("kansas");
        teamwords.add("city");
        teamwords.add("cincinnati");
        teamwords.add("arizona");
        teamwords.add("atlanta");
        teamwords.add("tennessee");
        teamwords.add("york");
        teamwords.add("jets");
        teamwords.add("giants");
        teamwords.add("houston");
        teamwords.add("dallas");
        teamwords.add("seattle");
        teamwords.add("denver");
        teamwords.add("minnesota");
        teamwords.add("buffalo");
        teamwords.add("tampa");
        teamwords.add("bay");
        teamwords.add("indianapolis");
        teamwords.add("washington");
        teamwords.add("carolina");
        teamwords.add("green");
        teamwords.add("jacksonville");
        teamwords.add("cleveland");
        teamwords.add("detroit");
        teamwords.add("philadelphia");
        teamwords.add("orleans");
        teamwords.add("san");
        teamwords.add("diego");
        teamwords.add("chicago");
        teamwords.add("new");
        teamwords.add("baltimore");
        teamwords.add("los");
        teamwords.add("angeles");
        teamwords.add("miami");
        teamwords.add("pittsburgh");
        teamwords.add("francisco");

        teamShortToLong.put("TEN","Tennessee");
        teamShortToLong.put("JAX","Jacksonville");
        teamShortToLong.put("CIN","Cincinnati");
        teamShortToLong.put("WSH","Washington");
        teamShortToLong.put("CAR","Carolina");
        teamShortToLong.put("ARI","Arizona");
        teamShortToLong.put("HOU","Houston");
        teamShortToLong.put("DET","Detroit");
        teamShortToLong.put("IND","Indianapolis");
        teamShortToLong.put("KC","Kansas City");
        teamShortToLong.put("BUF","Buffalo");
        teamShortToLong.put("NE","New England");
        teamShortToLong.put("CLE","Cleveland");
        teamShortToLong.put("NYJ","New York Jets");
        teamShortToLong.put("TB","Tampa Bay");
        teamShortToLong.put("OAK","Oakland");
        teamShortToLong.put("NO","New Orleans");
        teamShortToLong.put("SEA","Seattle");
        teamShortToLong.put("DEN","Denver");
        teamShortToLong.put("SD","San Diego");
        teamShortToLong.put("ATL","Atlanta");
        teamShortToLong.put("GB","Green Bay");
        teamShortToLong.put("DAL","Dallas");
        teamShortToLong.put("PHI","Philadelphia");
        teamShortToLong.put("CHI","Chicago");
        teamShortToLong.put("MIN","Minnesota");
        teamShortToLong.put("SF","San Francisco");
        teamShortToLong.put("MIA","Miami");
        teamShortToLong.put("PIT","Pittsburgh");
        teamShortToLong.put("BAL","Baltimore");
        teamShortToLong.put("NYG","New York Giants");
        teamShortToLong.put("LA","Los Angeles");

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

        teamToId.put("Alabama", 333);
        teamToId.put("Ohio State", 194);
        teamToId.put("Georgia", 61);
        teamToId.put("Clemson", 228);
        teamToId.put("Penn State", 213);
        teamToId.put("Michigan", 130);
        teamToId.put("Oklahoma", 201);
        teamToId.put("Notre Dame", 87);
        teamToId.put("Washington", 264);
        teamToId.put("Florida", 57);
        teamToId.put("Wisconsin", 275);
        teamToId.put("Miami (FL)", 2390);
        teamToId.put("Mississippi State", 344);
        teamToId.put("West Virginia", 277);
        teamToId.put("Auburn", 2);
        teamToId.put("LSU", 99);
        teamToId.put("Texas", 251);
        teamToId.put("Texas A&M", 245);
        teamToId.put("Iowa", 2294);
        teamToId.put("Stanford", 24);
        teamToId.put("Oklahoma State", 197);
        teamToId.put("Texas Tech", 2641);
        teamToId.put("Utah", 254);
        teamToId.put("Michigan State", 127);
        teamToId.put("Missouri", 142);
        teamToId.put("Kentucky", 96);
        teamToId.put("Boise State", 68);
        teamToId.put("Washington State", 265);
        teamToId.put("North Carolina State", 152);
        teamToId.put("South Carolina", 2579);
        teamToId.put("Oregon", 2483);
        teamToId.put("Fresno State", 278);
        teamToId.put("Duke", 150);
        teamToId.put("Virginia Tech", 259);
        teamToId.put("Central Florida", 2116);
        teamToId.put("TCU", 2628);
        teamToId.put("USC", 30);
        teamToId.put("Georgia Tech", 59);
        teamToId.put("Boston College", 103);
        teamToId.put("Utah State", 328);
        teamToId.put("Arizona State", 9);
        teamToId.put("Northwestern", 77);
        teamToId.put("Appalachian State", 2026);
        teamToId.put("Iowa State", 66);
        teamToId.put("Syracuse", 183);
        teamToId.put("Purdue", 2509);
        teamToId.put("Mississippi", 145);
        teamToId.put("Maryland", 120);
        teamToId.put("Memphis", 235);
        teamToId.put("Colorado", 38);
        teamToId.put("Houston", 248);
        teamToId.put("Florida State", 52);
        teamToId.put("Cincinnati", 2132);
        teamToId.put("California", 25);
        teamToId.put("Arizona", 12);
        teamToId.put("Baylor", 239);
        teamToId.put("Indiana", 84);
        teamToId.put("Virginia", 258);
        teamToId.put("Minnesota", 135);
        teamToId.put("North Texas", 249);
        teamToId.put("South Florida", 58);
        teamToId.put("Vanderbilt", 238);
        teamToId.put("Tennessee", 2633);
        teamToId.put("San Diego State", 21);
        teamToId.put("Army", 349);
        teamToId.put("Kansas State", 2306);
        teamToId.put("Temple", 218);
        teamToId.put("Wake Forest", 154);
        teamToId.put("UCLA", 26);
        teamToId.put("Brigham Young", 252);
        teamToId.put("Toledo", 2649);
        teamToId.put("Pittsburgh", 221);
        teamToId.put("Nebraska", 158);
        teamToId.put("Western Michigan", 2711);
        teamToId.put("Kansas", 2305);
        teamToId.put("Georgia Southern", 290);
        teamToId.put("Troy", 2653);
        teamToId.put("Air Force", 2005);
        teamToId.put("Tulane", 2655);
        teamToId.put("Illinois", 356);
        teamToId.put("North Carolina", 153);
        teamToId.put("Arkansas", 8);
        teamToId.put("Northern Illinois", 2459);
        teamToId.put("Buffalo", 2084);
        teamToId.put("Eastern Michigan", 2199);
        teamToId.put("Marshall", 276);
        teamToId.put("UAB", 5);
        teamToId.put("Arkansas State", 2032);
        teamToId.put("Louisiana Tech", 2348);
        teamToId.put("Florida Atlantic", 2226);
        teamToId.put("Florida International", 2229);
        teamToId.put("Louisville", 97);
        teamToId.put("Middle Tennessee State", 2393);
        teamToId.put("Ohio", 195);
        teamToId.put("Tulsa", 202);
        teamToId.put("Miami (OH)", 193);
        teamToId.put("New Mexico", 167);
        teamToId.put("Nevada", 2440);
        teamToId.put("Wyoming", 2751);
        teamToId.put("Southern Miss", 2572);
        teamToId.put("Navy", 2426);
        teamToId.put("Southern Methodist", 2567);
        teamToId.put("Akron", 2006);
        teamToId.put("Oregon State", 204);
        teamToId.put("Ball State", 2050);
        teamToId.put("Coastal Carolina", 324);
        teamToId.put("Hawaii", 62);
        teamToId.put("Central Michigan", 2117);
        teamToId.put("UNLV", 2439);
        teamToId.put("Massachusetts", 113);
        teamToId.put("Western Kentucky", 98);
        teamToId.put("Colorado State", 36);
        teamToId.put("Rutgers", 164);
        teamToId.put("Liberty", 2335);
        teamToId.put("Louisiana", 309);
        teamToId.put("East Carolina", 151);
        teamToId.put("Old Dominion", 295);
        teamToId.put("Georgia State", 2247);
        teamToId.put("UTSA", 2636);
        teamToId.put("Louisiana Monroe", 2433);
        teamToId.put("San Jose State", 23);
        teamToId.put("Kent State", 2309);
        teamToId.put("Bowling Green", 189);
        teamToId.put("Charlotte", 2429);
        teamToId.put("New Mexico State", 166);
        teamToId.put("South Alabama", 6);
        teamToId.put("Connecticut", 41);
        teamToId.put("Rice", 242);
        teamToId.put("UTEP", 2638);
        teamToId.put("Texas State", 326);
    }

    public void grabPowerRank() {
        System.out.println( "Fetching '" + inputURIPR + "'");
        int numGames = 0;

        //Execute client with our method
        try
        {
            Document page = connect(inputURIPR).get();
            Element leagueHeader = page.select("h2").stream()
                    .filter(e -> e.toString().contains(isNFL ? "NFL" : "College Football"))
                    .findFirst()
                    .orElse(null);
            Element current = leagueHeader.nextElementSibling();

            while (current.nextElementSibling() != null) {
                current = current.nextElementSibling();
                if (!current.tagName().equals("p")) {
                    break;
                } else if (!current.child(0).tagName().equals("strong")) {
                    continue;
                }

                String teamsString = current.select("strong").get(0).childNode(0).toString()
                        .replaceAll("[0-9]*","").replaceAll("\\.","").trim();
                String away = teamsString.split("(\\bat\\b|\\bversus\\b)")[0].trim();
                String home = teamsString.split("(\\bat\\b|\\bversus\\b)")[1].trim();
                away = isNFL ? cleanNFLTeamName(away) : cleanNCAATeamName(away);
                home = isNFL ? cleanNFLTeamName(home) : cleanNCAATeamName(home);

                String summary = unescapeHtml4(current.childNodes().get(2).toString());
                boolean negative = summary.startsWith(home);
                String[] spreadParts = summary.split("\\.");
                String spreadTail = spreadParts[1].split(" ")[0];
                String spreadHead = spreadParts[0].split(" ")[spreadParts[0].split(" ").length - 1];

                String spread = (negative ? "-" : "") + spreadHead + "." + spreadTail;

                Game game = isNFL ? new Game() : new NCAAGame();
                Integer homeId = teamToId.get(home);
                if (homeId == null) {
                    System.out.println("Could not find " + home + " in our map!");
                }
                Integer awayId = teamToId.get(away);
                if (awayId == null) {
                    System.out.println("Could not find " + away + " in our map!");
                }
                game.setHome(home);
                game.setAway(away);
                game.setPowerRank(spread);
                games.add(game);
                idToGame.put(homeId, game);
                idToGame.put(awayId, game);
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabAtomic() {
        System.out.println("Fetching '" + inputAtomic + "'");

        try {
            Document page = connect(inputAtomic).get();
            Elements rows = page.select("a[name=IA]").get(0).nextElementSibling().nextElementSibling().select("tr");
            DateTime currentDate = new DateTime();
            DateTime thisPastMonday = new DateTime().withWeekyear(currentDate.getWeekyear()).withYear(2018).withDayOfWeek(1).withHourOfDay(0);
            DateTime inAWeek = thisPastMonday.plusWeeks(1);

            for (int i = 1; i < rows.size(); i++) {
                Elements currentRowParts = rows.get(i).select("td");
                String date = currentRowParts.get(0).childNode(0).toString();
                String away = cleanNCAATeamName(currentRowParts.get(1).childNode(0).childNode(0).toString());
                String home = cleanNCAATeamName(currentRowParts.get(3).childNode(0).childNode(0).toString());
                String awayScore = currentRowParts.get(2).childNode(0).toString();
                String homeScore = currentRowParts.get(4).childNode(0).toString();
                final AtomicReference<String> margin = new AtomicReference(String.valueOf(Integer.valueOf(awayScore) - Integer.valueOf(homeScore)));

                DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd");
                DateTime gameDate = format.withLocale(ENGLISH).parseDateTime(date).withYear(thisPastMonday.getYear()).withHourOfDay(22);

                if (gameDate.getMillis() < thisPastMonday.getMillis()) {
                    continue;
                } else if (gameDate.getMillis() > inAWeek.getMillis()) {
                    break;
                }

                //Find a spot in our array for these values
                AtomicBoolean shouldIterate = new AtomicBoolean(true);
                games.stream()
                        .filter(g -> shouldIterate.get())
                        .map(g -> {return (NCAAGame)g;})
                        .filter(g -> !isNullOrEmpty(g.getAtomic()))
                        .forEach(g -> {
                            double homeResult =  similarity(g.getHome(), home);
                            double awayResult =  similarity(g.getAway(), away);
                            if (homeResult == 1 || awayResult == 1) {
                                g.setAtomic(margin.get());
                                shouldIterate.set(false);
                            //maybe the two are reversed?
                            } else {
                                homeResult =  similarity(g.getHome(), away);
                                awayResult =  similarity(g.getAway(), home);

                                if (homeResult == 1 || awayResult == 1) {
                                    g.setAtomic("-" + margin.get());
                                    shouldIterate.set(false);
                                }
                            }
                        });

                if (shouldIterate.get()) {
                    Function<Game, String> getter = g -> ((NCAAGame)g).getAtomic();
                    int actualRow = askForRow(getter, games, home, away);
                    if (actualRow >= 0) {
                        ((NCAAGame)games.get(actualRow)).setAtomic(margin.get());
                    }
                }

            }
        } catch (Exception e) {
            logAndExit(e);
        }
    }

    private void grab538NCAA() {
        System.out.println("Fetching '" + input538NCAA + "'");

        //Execute client with our method
        try {
            HashMap<String,Integer> teams = new HashMap<String,Integer>();

            String[] rows = connect(input538NCAA).maxBodySize(0).get().body().html().split("(?<=(\\d|winout)[ ])");

            for (int i = 1; i < rows.length; i++) {
                String[] rowParts = rows[i].trim().split(",");
                String teamName = cleanNCAATeamName(rowParts[0]);
                int won = Integer.valueOf(rowParts[6]);

                if (teams.get(teamName) == null) {
                    teams.put(teamName, won);
                } else {
                    teams.put(teamName, teams.get(teamName) + won);
                }
            }
            for (String teamName : teams.keySet()) {
                int wins = teams.get(teamName);
                if (wins == 0) {
                    continue;
                }
                Double winPct = teams.get(teamName) * 100.0 / ((rows.length-1 * 1.0) / teams.size());
                final AtomicDouble spread = new AtomicDouble();
                if (winPct < 50.0) {
                    winPct = 100.0 - winPct;
                    spread.set(Math.pow((winPct / 49.25), (1.0/.194)) * -1.0);
                } else {
                    spread.set(Math.pow((winPct / 49.25), (1.0/.194)));
                }

                final AtomicBoolean found = new AtomicBoolean(false);
                games.stream().forEach(g -> {
                    String home = g.getHome();
                    String away = g.getAway();

                    if (similarity(home, teamName) == 1.0) {
                        found.set(true);
                        g.setFiveThirtyEight(String.valueOf(spread.get() * -1.0));
                    } else if (similarity(away, teamName) == 1.0) {
                        found.set(true);
                        g.setFiveThirtyEight(String.valueOf(spread));
                    }
                });

                if (!found.get()) {
                    System.out.println("Did not find a spot for " + teamName + " with a spread of " + spread);
                }
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void grab538() {
        if (!isNFL) {
            grab538NCAA();
            return;
        }
        System.out.println( "Fetching '" + input538NFL + "'");

        //Execute client with our method
        try
        {
            Document page = connect(input538NFL).get();

            Element week = page.select("section[class=week]").get(0);
            Elements rows = week.select("tr[class=tr]");

            for (int i = 0; i < rows.size(); i=i+3) {
                String awayTeam = rows.get(i+1).select("td[class=td text team]").get(0).childNodes().get(0).toString().trim();
                String homeTeam = rows.get(i+2).select("td[class=td text team]").get(0).childNodes().get(0).toString().trim();

                String awaySpread = rows.get(i+1).select("td[class=td number spread]").get(0).childNodes().get(0).toString().trim();
                String homeSpread = rows.get(i+2).select("td[class=td number spread]").get(0).childNodes().get(0).toString().trim();
                if (homeSpread != null && homeSpread.equals("PK")) {
                    homeSpread = "0.0";
                } else if (awaySpread != null && awaySpread.equals("PK")) {
                    awaySpread = "+0.0";
                }

                AtomicBoolean shouldIterate = new AtomicBoolean(false);
                String finalAwaySpread = awaySpread;
                String finalHomeSpread = homeSpread;
                games.stream().filter(g -> shouldIterate.get()).forEach(g -> {
                    double homeResult =  similarity(g.getHome(), homeTeam);
                    double awayResult =  similarity(g.getAway(), awayTeam);
                    if (homeResult == 1 || awayResult == 1) {
                        g.setFiveThirtyEight(!isEmpty(finalAwaySpread) ? finalAwaySpread.substring(1) : finalHomeSpread);
                    }
                });
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabSpread() {
        String url = isNFL ? inputSpreadNFL : inputSpreadNCAA;
        System.out.println( "Fetching '" + url + "'");

        try
        {
            Document page = connect(url).get();
            Elements rows = page.select("table[class=frodds-data-tbl] tr");

            for (int i = 0; i < rows.size(); i++) {
                Element current = rows.get(i);
                Elements fiveDimes = current.select("a[href$=#BU]");
                if (fiveDimes.size() == 0) {
                    //if this is just an info row or there's no spread posted, move on
                    continue;
                }

                final AtomicReference<String> teamOne = new AtomicReference(current.select("a[class=tabletext]").get(0).childNode(0).toString());
                teamOne.set(isNFL ? cleanNFLTeamName(teamOne.get()) : cleanNCAATeamName(teamOne.get()));
                final AtomicReference<String> teamTwo = new AtomicReference(current.select("a[class=tabletext]").get(1).childNode(0).toString());
                teamTwo.set(isNFL ? cleanNFLTeamName(teamTwo.get()) : cleanNCAATeamName(teamTwo.get()));

                List<String> spreadParts = current.select("a[href$=#BU]").get(0).childNodes().stream()
                        .filter(n -> (n instanceof TextNode))
                        .map(n -> unescapeHtml4(n.toString().replace("PK","-0")))
                        .collect(toList())
                        .subList(1,3);

                boolean teamOneIsFavorite;
                AtomicDouble spread = new AtomicDouble();
                if (spreadParts.get(0).startsWith("-")) {
                    teamOneIsFavorite = true;
                    if (isNotEmpty(spreadParts.get(0)) && !spreadParts.get(0).equals(" ")) {
                        String spreadString = spreadParts.get(0).split("-|\\+|EV")[1].replace("½", ".5").replace(" EV", "");
                        spread.set(abs(valueOf(spreadString.substring(0, spreadString.length() - 1))));
                    }
                } else {
                    teamOneIsFavorite = false;
                    if (isNotEmpty(spreadParts.get(1)) && !spreadParts.get(1).equals(" ")) {
                        String spreadString = spreadParts.get(1).split("-|\\+|EV")[1].replace("½", ".5").replace(" EV", "");
                        spread.set(abs(valueOf(spreadString.substring(0, spreadString.length() - 1))));
                    }
                }

                if (spread != null) {
                    //Find a spot in our array for these values
                    AtomicBoolean shouldIterate = new AtomicBoolean(true);
                    games.stream().filter(g -> shouldIterate.get()).forEach(g -> {
                        double homeResult = similarity(g.getHome(), teamTwo.get());
                        double awayResult = similarity(g.getAway(), teamOne.get());
                        if (homeResult == 1 || awayResult == 1) {
                            g.setSpread(String.valueOf(spread.get() * (teamOneIsFavorite ? 1.0 : -1.0)));
                            shouldIterate.set(false);
                            //maybe the two are reversed?
                        } else {
                            homeResult = similarity(g.getHome(), teamOne.get());
                            awayResult = similarity(g.getAway(), teamTwo.get());

                            if (homeResult == 1 || awayResult == 1) {
                                g.setSpread(String.valueOf(spread.get() * (teamOneIsFavorite ? -1.0 : 1.0)));
                                shouldIterate.set(false);
                            }
                        }
                    });

                    Function<Game,String> getter = g -> g.getSpread();
                    int actualGame = askForRow(getter, games, teamOne.get(), teamTwo.get());
                    if (actualGame >= 0) {
                        games.get(actualGame).setSpread(String.valueOf(spread.get() * (teamOneIsFavorite ? 1.0 : -1.0)));
                    }
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabSagarin() {
        String url = isNFL ? inputSagarinNFL : inputSagarinNCAA;
        System.out.println( "Fetching '" + url + "'");

        try
        {
            Document page = connect(url).get();
            Node predictionSection = page.select("a[name=Predictions]").get(0).parent().parent().parent().childNode(2);
            String[] rows = copyOfRange(predictionSection.toString().split("\r\n"), 8,
                    predictionSection.toString().split("\r\n").length);

            for (int i = 0; i < games.size(); i++) {
                Game game = games.get(i);
                String home = game.getHome();
                String away = game.getAway();

                //iterate through list of games to find a match
                for (int j = 0; j < rows.length; j++) {
                    String currentRow = unescapeHtml4(rows[j]);
                    if (currentRow.startsWith("=====") || isBlank(currentRow) || currentRow.contains("eigen")) {
                        break;
                    } else if (!currentRow.toLowerCase().contains(home.toLowerCase()) && !currentRow.toLowerCase().contains(away.toLowerCase())) {
                        continue;
                    }

                    String favorite = currentRow.substring(4, 27).trim();
                    String underdog = currentRow.substring(59).trim();

                    String[] spreads = currentRow.substring(27, 59).trim().split("\\s+");
                    double averageSpread = asList(spreads).stream().mapToDouble(Double::valueOf).average().getAsDouble();

                    if (isNFL) {
                        home = addMascot(home);
                        away = addMascot(away);
                    }

                    //neutral site game
                    if (currentRow.substring(0,3).contains("n")) {
                        favorite = capitalizeFully(favorite);
                        underdog = capitalizeFully(underdog);

                        if (favorite.equals(home) || underdog.equals(away)) {
                            averageSpread = averageSpread * -1.0;
                        }
                    } else {
                        if (favorite.equals(favorite.toUpperCase())) {
                            if (!favorite.toUpperCase().equals(home.toUpperCase()) && !underdog.toUpperCase().equals(away.toUpperCase())) {
                                continue;
                            }
                            averageSpread = averageSpread * -1.0;
                        } else {
                            if (!favorite.toUpperCase().equals(away.toUpperCase()) && !underdog.toUpperCase().equals
                                    (home.toUpperCase())) {
                                continue;
                            }
                        }
                    }
                    game.setSagarin(String.valueOf(averageSpread));
                    break;
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabMassey() {
        String url = isNFL ? inputMasseyNFL : inputMasseyNCAA;
        System.out.println( "Fetching '" + url + "'");

        //Execute client with our method
        try
        {
            String source = connect(url).ignoreContentType(true).get().body().text();

            JSONObject json = new JSONObject(source);
            JSONArray gamesArray = json.getJSONArray("DI");

            for (Object currentGame : gamesArray) {
                JSONArray current = (JSONArray)currentGame;
                AtomicReference<String> away = new AtomicReference(current.getJSONArray(2).getString(0));
                away.set(isNFL ? cleanNFLTeamName(away.get()) : cleanNCAATeamName(away.get()));
                AtomicReference<String> home = new AtomicReference(current.getJSONArray(3).getString(0).replace("@ ", ""));
                home.set(isNFL ? cleanNFLTeamName(home.get()) : cleanNCAATeamName(home.get()));
                final AtomicDouble spread = new AtomicDouble();
                Object prediction = ((JSONArray)current.get(12)).get(0);
                if (prediction instanceof Integer) {
                    spread.set((double)((Integer)prediction) * -1.0);
                } else if (prediction instanceof Double) {
                    spread.set((Double)prediction * -1.0);
                } else if (prediction instanceof String && prediction.equals("---")) {
                    Object innerprediction = ((JSONArray)current.get(13)).get(0);
                    if (innerprediction instanceof Integer) {
                        spread.set((double)((Integer)innerprediction));
                    } else if (innerprediction instanceof Double) {
                        spread.set((Double)innerprediction);
                    } else {
                        continue;
                    }
                }

                //Find a spot in our array for these values
                AtomicBoolean shouldIterate = new AtomicBoolean(true);
                games.stream().filter(g -> shouldIterate.get()).forEach(g -> {
                    double homeResult =  similarity(g.getHome(), home.get());
                    double awayResult =  similarity(g.getAway(), away.get());
                    if (homeResult == 1 || awayResult == 1) {
                        g.setMassey(String.valueOf(spread.get()));
                        shouldIterate.set(false);
                        //maybe the two are reversed?
                    } else {
                        homeResult =  similarity(g.getHome(), away.get());
                        awayResult =  similarity(g.getAway(), home.get());

                        if (homeResult == 1 || awayResult == 1) {
                            spread.set(spread.get() * -1.0);
                            g.setMassey(String.valueOf(spread));
                            shouldIterate.set(false);
                        }
                    }
                });

                if (shouldIterate.get()) {
                    Function<Game,String> getter = g -> g.getMassey();
                    int actualGame = askForRow(getter, games, home.get(), away.get());
                    if (actualGame >= 0) {
                        games.get(actualGame).setMassey(String.valueOf(spread));
                    }
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabSandP() {
        System.out.println("Fetching sAndP predictions");

        try {
            Elements rows = connect(inputSPSheet).maxBodySize(0).get().select("table").get(1).select("tr");

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

                String cleanedHome = cleanNCAATeamName(home);
                String cleanedAway = cleanNCAATeamName(away);

                AtomicBoolean shouldIterate = new AtomicBoolean(true);
                games.stream().filter(g -> shouldIterate.get()).map(g -> {return ((NCAAGame)g);}).filter(g -> g.getSAndP() == null).forEach(g -> {
                    if (similarity(g.getHome(), cleanedHome) == 1.0 || similarity(g.getAway(), cleanedAway) == 1.0) {
                        g.setSAndP((homeIsSPFavorite ? "-" : "") + spMargin);
                        g.setFPlus((homeIsFPFavorite ? "-" : "") + fpMargin);
                        shouldIterate.set(false);
                    } else if (teams.contains(" vs. ") && (similarity(g.getHome(), cleanedAway) == 1.0 || similarity(g.getAway(), cleanedHome) == 1.0)) {
                        g.setSAndP((homeIsSPFavorite ? "" : "-") + spMargin);
                        g.setFPlus((homeIsFPFavorite ? "" : "-") + fpMargin);
                        shouldIterate.set(false);
                    }
                });
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void grabDRatings() {
        String url = isNFL ? inputURIDRNFL : inputURIDRNCAA;
        System.out.println( "Fetching '" + url + "'");

        try
        {
            Document page = connect(url).get();
            Elements rows = page.select("table[class=small-text]").get(isNFL ? 0 : 1).select("tr");

            for (int i = 2; i < rows.size(); i = i + 2) {
                Element rowOne = rows.get(i);
                Element rowTwo = rows.get(i+1);

                String away = rowOne.select("td").get(2).childNodes().get(0).toString();
                away = isNFL ? cleanNFLTeamName(away) : cleanNCAATeamName(away);
                String home = rowTwo.select("td").get(0).childNodes().get(0).toString();
                home = isNFL ? cleanNFLTeamName(home) : cleanNCAATeamName(home);

                //Grab spread, and favorite
                String homePoints = rowTwo.select("td").get(5).childNodes().get(0).childNodes().get(0).toString().trim();
                String awayPoints = rowOne.select("td").get(7).childNodes().get(0).childNodes().get(0).toString().trim();

                double margin = valueOf(awayPoints) - valueOf(homePoints);

                //Find a spot in our array for these values
                for (int j = 0; j < games.size(); j++) {
                    Game game = games.get(j);
                    double homeResult =  similarity(game.getHome(), home);
                    double awayResult =  similarity(game.getAway(), away);
                    if (homeResult == 1 || awayResult == 1) {
                        game.setDRatings(String.valueOf(margin));
                    } else {
                        homeResult = similarity(game.getHome(), away);
                        awayResult = similarity(game.getAway(), home);
                        if (homeResult == 1 || awayResult == 1) {
                            game.setDRatings(String.valueOf(margin * -1.0));
                        }
                    }
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabFox() {
        String url = isNFL ? inputURIFoxNFL : inputURIFoxNCAA;
        System.out.println( "Fetching '" + url + "'");

        try
        {
            Document page = connect(url).get();
            Elements gamesElements = page.select("div[class=wisbb_predictionChip]");

            for (Element game : gamesElements) {
                Elements teams = game.select("span[class=wisbb_teamName]");
                if (teams.size() == 0) {
                    continue;
                }

                String away = teams.get(0).childNode(0).toString();
                away = isNFL ? cleanNFLTeamName(away) : cleanNCAATeamName(away);
                String home = teams.get(1).childNode(0).toString();
                home = isNFL ? cleanNFLTeamName(home) : cleanNFLTeamName(home);

                if (isNFL) {
                    away = teamMascotToCity.get(away);
                    home = teamMascotToCity.get(home);
                }


                String score = game.select("span[class=wisbb_predData]").get(0).childNodes().get(0).toString();
                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                Object result = engine.eval(score);
                double prediction;
                if (result instanceof Integer) {
                    prediction = (double)((Integer)result);
                } else {
                    prediction = (Double)result;
                }

                for (int j = 0; j < games.size(); j++) {
                    Game current = games.get(j);
                    double homeResult =  similarity(current.getHome(), home);
                    double awayResult =  similarity(current.getAway(), away);
                    if (homeResult == 1 || awayResult == 1) {
                        current.setFox(String.valueOf(prediction));
                        break;
                    } else {
                        homeResult =  similarity(current.getHome(), away);
                        awayResult =  similarity(current.getAway(), home);

                        if (homeResult == 1 || awayResult == 1) {
                            prediction = prediction * -1.0;
                            current.setFox(String.valueOf(prediction));
                            break;
                        }
                    }
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabOddsShark() {
        String url = isNFL ? inputURIOSNFL : inputURIOSNCAA;
        System.out.println( "Fetching '" + url + "'");

        try
        {
            Document page = connect(url).get();
            Elements gamesElements = page.select("table");

            for (int i = 0; i < gamesElements.size(); i++) {
                Element game = gamesElements.get(i);
                Elements teams = game.getElementsByClass("name-long");
                if (teams.size() != 2) {
                    continue;
                }
                if (game.toString().contains("Results")) {
                    break;
                }
                String away = teams.get(0).text().trim();
                away = isNFL ? cleanNFLTeamName(away) : cleanNCAATeamName(away);
                String home = teams.get(1).text().trim();
                home = isNFL ? cleanNFLTeamName(home) : cleanNCAATeamName(home);
                String prediction = game.select("td").get(1).childNode(0).toString();

                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                Object result = engine.eval(prediction);
                final AtomicDouble margin = new AtomicDouble();
                if (result instanceof Integer) {
                    margin.set((double)((Integer)result));
                } else {
                    margin.set((Double)result);
                }

                //Find a spot in our array for these values
                AtomicBoolean shouldIterate = new AtomicBoolean(true);
                String finalHome = home;
                String finalAway = away;
                games.stream().filter(g -> shouldIterate.get()).filter(g -> !isNullOrEmpty(g.getOddsShark())).forEach(g -> {
                    double homeResult =  similarity(g.getHome(), finalHome);
                    double awayResult =  similarity(g.getAway(), finalAway);
                    if (homeResult == 1 || awayResult == 1) {
                        g.setOddsShark(String.valueOf(margin.get()));
                        shouldIterate.set(false);
                        //maybe the two are reversed?
                    } else {
                        homeResult =  similarity(g.getHome(), finalAway);
                        awayResult =  similarity(g.getAway(), finalHome);

                        if (homeResult == 1 || awayResult == 1) {
                            g.setOddsShark(String.valueOf(margin.get() * -1.0));
                            shouldIterate.set(false);
                        }
                    }
                });

                if (shouldIterate.get()) {
                    Function<Game,String> getter = g -> g.getOddsShark();
                    int actualGame = askForRow(getter, games, home, away);
                    if (actualGame >= 0) {
                        games.get(actualGame).setOddsShark(String.valueOf(margin));
                    }
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    private String addMascot(String city) {
        if (city.contains("Los Angeles")) {
            return city;
        }
        for (Map.Entry<String,String> mascotAndCity : teamMascotToCity.entrySet()) {
            if (mascotAndCity.getValue().equals(city)) {
                return city + " " + mascotAndCity.getKey();
            }
        }
        return city;
    }

    private String cleanNCAATeamName(String teamName) {
        return teamName.replaceAll(" St$"," State").replaceFirst("E ", "Eastern ").replaceFirst("^C ", "Central ").replaceAll("&amp;","&")
                .replace("FL ", "Florida ").replaceAll("Intl$", "International").replace("FIU","Florida International").replace("AM","A&M").replace("NC ", "North Carolina ")
                .replaceAll(" St.$"," State").replace("<b>","").replace("</b>","").replaceFirst("^W ", "Western ").replaceFirst("^Ga ", "Georgia ")
                .replace("N Illinois","Northern Illinois").replaceAll("^Kent$","Kent State").replaceAll("^ULM$","Louisiana Monroe").replaceAll("^ULL$","Louisiana Lafayette")
                .replace("Louisiana-Monroe", "Louisiana Monroe").replace("Louisiana-Lafayette", "Louisiana Lafayette").replace("Ohio U.", "Ohio")
                .replace("Miami OH", "Miami (OH)").replace("Int'l", "International").replace("UCF", "Central Florida")
                .replace("SMU", "Southern Methodist").replace("Middle Tennessee", "Middle Tennessee State").replace("Texas El Paso", "UTEP")
                .replace("Texas-San Antonio", "UTSA").replace("Alabama-Birmingham", "UAB").replace("Southern California", "USC")
                .replace("Nevada-Las Vegas", "UNLV").replace("Louisiana State", "LSU").replace("Miami-FL", "Miami (FL)")
                .replace("Texas St-San Marcos", "Texas State").replace("UL-Monroe", "Louisiana Monroe").replace("Ole Miss", "Mississippi").trim();
    }

    private String cleanNFLTeamName(String teamName) {
        return teamName.replaceFirst("NY", "New York")
                .replaceFirst("N.Y.", "New York")
                .replaceFirst("LA", "Los Angeles")
                .replaceFirst("L.A.", "Los Angeles");
    }

    private <T extends Game> int askForRow(Function<T, String> getter, List<T> games, String home, String away) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(System.in));

            System.out.println(format("Which game is %s vs. %s? ", home, away));
            AtomicInteger count = new AtomicInteger(0);
            games.stream().forEach(g -> {
                if (isNullOrEmpty(getter.apply(g))) {
                    System.out.println(format("%d) %s vs. %s", count.get(), g.getHome(), g.getAway()));
                }
                count.incrementAndGet();
            });
            String input = br.readLine();

            return Integer.valueOf(input);

        } catch (IOException e) {
            logAndExit(e);
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }

    private double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; } // both strings are zero length

        return (longerLength - new LevenshteinDistance().apply(longer, shorter)) /
                (double) longerLength;

    }

    public void printResults() {
        try {
            File file = new File(format("/Users/patrick.stoneburner/Desktop/%s_picks.csv", isNFL ? "nfl" : "ncaa"));
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw1 = new FileWriter(file);
            final BufferedWriter bw = new BufferedWriter(fw1);
            bw.write(games.get(0).getHeader());
            bw.newLine();

            games.stream().forEach(g -> {
                try {
                    bw.write(g.toString());
                    bw.newLine();
                } catch (IOException ex) {/*no-op*/}
            });
            bw.close();

        } catch (Exception e) {
            logAndExit(e);
        }
    }

    private void logAndExit(Exception e) {
        e.printStackTrace(System.out);
        System.exit(0);
    }
}
