package com.stoneburner.app;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
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

import static java.lang.Double.valueOf;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeHtml4;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.lang3.text.WordUtils.capitalizeFully;
import static org.joda.time.Weeks.weeksBetween;
import static org.jsoup.Jsoup.connect;

public class Util {
    private static String inputURIPR = "https://thepowerrank.com/predictions/";
    private static String inputURIDRNCAA = "http://www.dratings.com/predictor/ncaa-football-predictions/";
    private static String inputURIDRNFL = "http://www.dratings.com/predictor/nfl-football-predictions/";
    private static String inputURIOSNCAA = "https://www.oddsshark.com/ncaaf/computer-picks";
    private static String inputURIOSNFL = "http://www.oddsshark.com/nfl/computer-picks";
    private static String inputURIFoxNCAA = "https://www.foxsports.com/college-football/predictions?season=2018&seasonType=1&week=%d&group=-3";
    private static String inputURIFoxNFL = "http://www.foxsports.com/nfl/predictions";
    private static String inputSP = "https://www.footballstudyhall.com/pages/2018-%team%-advanced-statistical-profile";
    private static String inputSagarinNCAA = "http://sagarin.com/sports/cfsend.htm";
    private static String inputSagarinNFL = "http://sagarin.com/sports/nflsend.htm";
    private static String inputMasseyNCAA = "http://www.masseyratings.com/predjson.php?s=cf&sub=11604&dt=$dt$";
    private static String inputMasseyNFL = "http://www.masseyratings.com/predjson.php?s=nfl&dt=$dt$";
    private static String inputSpreadNCAA = "http://www.vegasinsider.com/college-football/odds/offshore/2/";
    private static String inputSpreadNFL = "http://www.vegasinsider.com/nfl/odds/offshore/2/";
    private static String input538NCAA = "https://projects.fivethirtyeight.com/2018-college-football-predictions/sims.csv";
    private static String input538NFL = "https://projects.fivethirtyeight.com/2018-nfl-predictions/games/";
    private static String inputAtomic = "http://www.atomicfootball.com/archive/af_predictions_All.html";

    public static List<String> teamwords = new ArrayList<String>();
    public static HashMap<String,String> teamShortToLong = new HashMap<String,String>();
    public static HashMap<String,String> teamMascotToCity = new HashMap<String,String>();

    //static initializer
    static {
        //What week of the season is it?
        DateTime game1 = new DateTime(1535760000000l);
        DateTime today = new DateTime();
        int week = weeksBetween(game1, today).getWeeks()+2;
        inputURIFoxNCAA = format(inputURIFoxNCAA, week);

        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyyMMdd");
        inputMasseyNCAA = inputMasseyNCAA.replace("$dt$", dtfOut.print(today));
        inputMasseyNFL = inputMasseyNFL.replace("$dt$", dtfOut.print(today));

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
    }


    public static String[][] grabPowerRank(boolean isNFL, String[][] predictions) {
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
                boolean negative = summary.toString().startsWith(home);
                String[] spreadParts = summary.toString().split("\\.");
                String spreadTail = spreadParts[1].split(" ")[0];
                String spreadHead = spreadParts[0].split(" ")[spreadParts[0].split(" ").length - 1];

                String spread = (negative ? "-" : "") + spreadHead + "." + spreadTail;

                predictions[numGames][0] = home;
                predictions[numGames][1] = away;
                predictions[numGames++][2] = spread;
            }
        }

        catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }

        return copyOfRange(predictions, 0, numGames);
    }

    public static void grabAtomic(String[][] predictions) {
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
                String margin = String.valueOf(Integer.valueOf(awayScore) - Integer.valueOf(homeScore));

                DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd");
                DateTime gameDate = format.withLocale(ENGLISH).parseDateTime(date).withYear(thisPastMonday.getYear()).withHourOfDay(22);

                if (gameDate.getMillis() < thisPastMonday.getMillis()) {
                    continue;
                } else if (gameDate.getMillis() > inAWeek.getMillis()) {
                    break;
                }

                //Find a spot in our array for these values
                int actualRow = -1;
                for (int j = 0; j < predictions.length; j++) {
                    double homeResult =  similarity(predictions[j][0], home);
                    double awayResult =  similarity(predictions[j][1], away);
                    if (homeResult == 1 || awayResult == 1) {
                        actualRow = j;
                        predictions[actualRow][10] = margin;
                        //maybe the two are reversed?
                    } else {
                        homeResult =  similarity(predictions[j][0], away);
                        awayResult =  similarity(predictions[j][1], home);

                        if (homeResult == 1 || awayResult == 1) {
                            String third = home;
                            home = away;
                            away = third;
                            margin = String.valueOf(Integer.valueOf(margin) * -1);
                            actualRow = j;
                            predictions[actualRow][10] = String.valueOf(margin);
                        }
                    }
                }
                if (actualRow < 0) {
                    //TODO, put this back?
                    //actualRow = askForRow(10, home, away);
                    if (actualRow >= 0) {
                        predictions[actualRow][10] = margin;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
    }

    public static void grab538NCAA(String[][] predictions) {
        System.out.println("Fetching '" + input538NCAA + "'");

        //Instantiate client and method
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet method = new HttpGet(input538NCAA);

        //Execute client with our method
        try {
            HashMap<String,Integer> teams = new HashMap<String,Integer>();
            HttpResponse response = client.execute(method);

            String source = EntityUtils.toString(response.getEntity());
            String[] rows = source.split("\n");

            for (int i = 1; i < rows.length; i++) {
                String[] rowParts = rows[i].split(",");
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
                Double spread = null;
                if (winPct < 50.0) {
                    winPct = 100.0 - winPct;
                    spread = Math.pow((winPct / 49.25), (1.0/.194)) * -1.0;
                } else {
                    spread = Math.pow((winPct / 49.25), (1.0/.194));
                }

                boolean found = false;
                for (int i = 0; i < predictions.length; i++) {
                    String home = predictions[i][0];
                    String away = predictions[i][1];

                    if (similarity(home, teamName) == 1.0) {
                        found = true;
                        predictions[i][9] = String.valueOf(spread * -1.0);
                    } else if (similarity(away, teamName) == 1.0) {
                        found = true;
                        predictions[i][9] = String.valueOf(spread);
                    }
                }

                if (!found) {
                    System.out.println("Did not find a spot for " + teamName + " with a spread of " + spread);
                }
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public static void grab538NFL(String[][] predictions) {
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

                for (int j = 0; j < predictions.length; j++) {
                    double homeResult =  similarity(predictions[j][0], homeTeam);
                    double awayResult =  similarity(predictions[j][1], awayTeam);
                    if (homeResult == 1 || awayResult == 1) {
                        predictions[j][6] = !isEmpty(awaySpread) ? awaySpread.substring(1) : homeSpread;
                    }
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
    }

    public static void grabSpread(boolean isNFL, String[][] predictions) {
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

                String teamOne = current.select("a[class=tabletext]").get(0).childNode(0).toString();
                teamOne = isNFL ? cleanNFLTeamName(teamOne) : cleanNCAATeamName(teamOne);
                String teamTwo = current.select("a[class=tabletext]").get(1).childNode(0).toString();
                teamTwo = isNFL ? cleanNFLTeamName(teamTwo) : cleanNCAATeamName(teamTwo);

                List<String> spreadParts = current.select("a[href$=#BU]").get(0).childNodes().stream()
                        .filter(n -> (n instanceof TextNode))
                        .map(n -> unescapeHtml4(n.toString().replace("PK","-0")))
                        .collect(toList())
                        .subList(1,3);

                boolean teamOneIsFavorite;
                Double spread = null;
                if (spreadParts.get(0).startsWith("-")) {
                    teamOneIsFavorite = true;
                    if (isNotEmpty(spreadParts.get(0)) && !spreadParts.get(0).equals(" ")) {
                        String spreadString = spreadParts.get(0).split("-|\\+|EV")[1].replace("½", ".5").replace(" EV", "");
                        spread = abs(valueOf(spreadString.substring(0, spreadString.length() - 1)));
                    }
                } else {
                    teamOneIsFavorite = false;
                    if (isNotEmpty(spreadParts.get(1)) && !spreadParts.get(1).equals(" ")) {
                        String spreadString = spreadParts.get(1).split("-|\\+|EV")[1].replace("½", ".5").replace(" EV", "");
                        spread = abs(valueOf(spreadString.substring(0, spreadString.length() - 1)));
                    }
                }

                if (spread != null) {
                    //Find a spot in our array for these values
                    int actualRow = -1;
                    int column = predictions[0].length - 1;
                    for (int j = 0; j < predictions.length; j++) {
                        double homeResult = similarity(predictions[j][0], teamTwo);
                        double awayResult = similarity(predictions[j][1], teamOne);
                        if (homeResult == 1 || awayResult == 1) {
                            actualRow = j;
                            predictions[actualRow][column] = String.valueOf(spread * (teamOneIsFavorite ? 1.0 : -1.0));
                            break;
                            //maybe the two are reversed?
                        } else {
                            homeResult = similarity(predictions[j][0], teamOne);
                            awayResult = similarity(predictions[j][1], teamTwo);

                            if (homeResult == 1 || awayResult == 1) {
                                actualRow = j;
                                predictions[actualRow][column] = String.valueOf(spread * (teamOneIsFavorite ? -1.0 : 1.0));
                                break;
                            }
                        }
                    }
                    if (actualRow < 0) {
                        actualRow = askForRow(column, predictions, teamOne, teamTwo);
                        if (actualRow >= 0) {
                            predictions[actualRow][column] = String.valueOf(spread);
                        }
                    }
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
    }

    public static void grabSagarin(boolean isNFL, String[][] predictions) {
        String url = isNFL ? inputSagarinNFL : inputSagarinNCAA;
        System.out.println( "Fetching '" + url + "'");

        try
        {
            Document page = connect(url).get();
            Node predictionSection = page.select("a[name=Predictions]").get(0).parent().parent().parent().childNode(2);
            String[] rows = copyOfRange(predictionSection.toString().split("\r\n"), 8,
                    predictionSection.toString().split("\r\n").length);

            for (int i = 0; i < predictions.length; i++) {
                String home = predictions[i][0];
                String away = predictions[i][1];

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
                    predictions[i][8] = String.valueOf(averageSpread);
                    break;
                }
            }
        }

        catch (Exception e) {
            System.out.println("Exception occurred: " + e);
            System.exit(0);
        }
    }

    public static void grabMassey(boolean isNFL, String[][] predictions) {
        String url = isNFL ? inputMasseyNFL : inputMasseyNCAA;
        System.out.println( "Fetching '" + url + "'");

        //Instantiate client and method
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet method = new HttpGet(url);

        //Execute client with our method
        try
        {
            HttpResponse response = client.execute(method);

            String source = EntityUtils.toString(response.getEntity());

            JSONObject json = new JSONObject(source);
            JSONArray gamesArray = json.getJSONArray("DI");

            for (Object currentGame : gamesArray) {
                JSONArray current = (JSONArray)currentGame;
                String away = current.getJSONArray(2).getString(0);
                away = isNFL ? cleanNFLTeamName(away) : cleanNCAATeamName(away);
                String home = current.getJSONArray(3).getString(0).replace("@ ", "");
                home = isNFL ? cleanNFLTeamName(home) : cleanNCAATeamName(home);
                Double spread = null;
                Object prediction = ((JSONArray)current.get(12)).get(0);
                if (prediction instanceof Integer) {
                    spread = (double)((Integer)prediction) * -1.0;
                } else if (prediction instanceof Double) {
                    spread = (Double)prediction * -1.0;
                } else if (prediction instanceof String && prediction.equals("---")) {
                    Object innerprediction = ((JSONArray)current.get(13)).get(0);
                    if (innerprediction instanceof Integer) {
                        spread = (double)((Integer)innerprediction);
                    } else if (innerprediction instanceof Double) {
                        spread = (Double)innerprediction;
                    } else {
                        continue;
                    }
                }

                //Find a spot in our array for these values
                int actualRow = -1;
                for (int j = 0; j < predictions.length; j++) {
                    double homeResult =  similarity(predictions[j][0], home);
                    double awayResult =  similarity(predictions[j][1], away);
                    if (homeResult == 1 || awayResult == 1) {
                        actualRow = j;
                        predictions[actualRow][7] = String.valueOf(spread);
                        //maybe the two are reversed?
                    } else {
                        homeResult =  similarity(predictions[j][0], away);
                        awayResult =  similarity(predictions[j][1], home);

                        if (homeResult == 1 || awayResult == 1) {
                            String third = home;
                            home = away;
                            away = third;
                            spread = spread * -1.0;
                            actualRow = j;
                            predictions[actualRow][7] = String.valueOf(spread);
                        }
                    }
                }
                if (actualRow < 0) {
                    actualRow = askForRow(7, predictions, home, away);
                    if (actualRow >= 0) {
                        predictions[actualRow][7] = String.valueOf(spread);
                    }
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
    }

    public static void grabSandP(String[][] predictions) {
        boolean homeTeamFailure = false;
        boolean awayTeamFailure = false;
        for (int i = 0; i < predictions.length; i++) {
            String homeTeam = predictions[i][0];
            String awayTeam = predictions[i][1];
            String homeUrl;
            String awayUrl;
            try {
                homeUrl = inputSP.replaceAll("%team%", encode(homeTeam.toLowerCase(), "UTF-8").replace("+", "-"));
                awayUrl = inputSP.replaceAll("%team%", encode(awayTeam.toLowerCase(), "UTF-8").replace("+", "-"));
            } catch (UnsupportedEncodingException e) {
                break;
            }

            System.out.println("Fetching '" + (homeTeamFailure ? awayUrl : homeUrl) + "'");

            try {
                Document page = null;
                try {
                    page = connect(homeTeamFailure ? awayUrl : homeUrl).get();
                } catch (HttpStatusException ex) {
                    if (ex.getStatusCode() >= 300) {
                        if (!homeTeamFailure) {
                            --i;
                        }
                        homeTeamFailure = !homeTeamFailure;
                        continue;
                    }
                }

                Element predictionTable = page.select("table").get(1);
                if (!predictionTable.toString().contains("Cumulative")) {
                    System.out.println("The second table didn't look like the predictions table, continuing on.");
                    continue;
                }

                Elements predictionsRows = predictionTable.select("tr");
                for (int j = 1; j < predictionsRows.size()+1; j++) {
                    Element currentPrediction = predictionsRows.get(j);
                    DateTime currentDate = new DateTime();
                    DateTime thisPastMonday = new DateTime().withWeekyear(currentDate.getWeekyear()).withYear(2018).withDayOfWeek(1).withHourOfDay(0);
                    DateTime inAWeek = thisPastMonday.plusWeeks(1);

                    String nextGameString = currentPrediction.select("td").get(0).childNode(0).toString().trim();
                    DateTimeFormatter format = DateTimeFormat.forPattern("d-MMM");
                    DateTime gameDate = format.withLocale(ENGLISH).parseDateTime(nextGameString).withYear(thisPastMonday.getYear()).withHourOfDay(22);

                    if (gameDate.getMillis() < thisPastMonday.getMillis() || gameDate.getMillis() > inAWeek.getMillis()) {
                        System.out.println("Something weird with the next game date. Continuing.");
                        continue;
                    } else {
                        String spread = currentPrediction.select("td").get(5).childNode(0).toString();
                        if (!homeTeamFailure) {
                            spread = "-" + spread;
                        }
                        predictions[i][6] = spread;
                        homeTeamFailure = false;
                        break;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }

    public static void grabDRatings(boolean isNFL, String[][] predictions) {
        String url = isNFL ? inputURIDRNFL : inputURIDRNCAA;
        System.out.println( "Fetching '" + url + "'");

        try
        {
            Document page = connect(url).get();
            Elements rows = page.select("table[class=small-text]").get(1).select("tr");

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
                for (int j = 0; j < predictions.length; j++) {
                    double homeResult =  similarity(predictions[j][0], home);
                    double awayResult =  similarity(predictions[j][1], away);
                    if (homeResult == 1 || awayResult == 1) {
                        predictions[j][3] = String.valueOf(margin);
                    } else {
                        homeResult = similarity(predictions[j][0], away);
                        awayResult = similarity(predictions[j][1], home);
                        if (homeResult == 1 || awayResult == 1) {
                            predictions[j][3] = String.valueOf(margin * -1.0);
                        }
                    }
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
    }

    public static void grabFox(boolean isNFL, String[][] predictions) {
        String url = isNFL ? inputURIFoxNFL : inputURIFoxNCAA;
        System.out.println( "Fetching '" + url + "'");

        try
        {
            Document page = connect(url).get();
            Elements games = page.select("div[class=wisbb_predictionChip]");

            for (Element game : games) {
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

                for (int j = 0; j < predictions.length; j++) {
                    double homeResult =  similarity(predictions[j][0], home);
                    double awayResult =  similarity(predictions[j][1], away);
                    if (homeResult == 1 || awayResult == 1) {
                        predictions[j][4] = String.valueOf(prediction);
                        break;
                    } else {
                        homeResult =  similarity(predictions[j][0], away);
                        awayResult =  similarity(predictions[j][1], home);

                        if (homeResult == 1 || awayResult == 1) {
                            String third = home;
                            home = away;
                            away = third;
                            prediction = prediction * -1.0;
                            predictions[j][4] = String.valueOf(prediction);
                            break;
                        }
                    }
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
    }

    public static void grabOddsShark(boolean isNFL, String[][] predictions) {
        String url = isNFL ? inputURIOSNFL : inputURIOSNCAA;
        System.out.println( "Fetching '" + url + "'");

        try
        {
            Document page = connect(url).get();
            Elements games = page.select("table");

            for (int i = 0; i < games.size(); i++) {
                Element game = games.get(i);
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
                double margin;
                if (result instanceof Integer) {
                    margin = (double)((Integer)result);
                } else {
                    margin = (Double)result;
                }

                //Find a spot in our array for these values
                int actualRow = -1;
                for (int j = 0; j < predictions.length; j++) {
                    double homeResult =  similarity(predictions[j][0], home);
                    double awayResult =  similarity(predictions[j][1], away);
                    if (homeResult == 1 || awayResult == 1) {
                        actualRow = j;
                        predictions[actualRow][5] = String.valueOf(margin);
                        break;
                        //maybe the two are reversed?
                    } else {
                        homeResult =  similarity(predictions[j][0], away);
                        awayResult =  similarity(predictions[j][1], home);

                        if (homeResult == 1 || awayResult == 1) {
                            String third = home;
                            home = away;
                            away = third;
                            margin = margin * -1.0;
                            actualRow = j;
                            predictions[actualRow][5] = String.valueOf(margin);
                            break;
                        }
                    }
                }
                if (actualRow < 0) {
                    actualRow = askForRow(5, predictions, home, away);
                    if (actualRow >= 0) {
                        predictions[actualRow][5] = String.valueOf(margin);
                    }
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
    }

    private static String addMascot(String city) {
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

    private static String cleanNCAATeamName(String teamName) {
        return teamName.replaceAll(" St$"," State").replaceFirst("E ", "Eastern ").replaceFirst("^C ", "Central ").replaceAll("&amp;","&")
                .replace("FL ", "Florida ").replaceAll("Intl$", "International").replace("FIU","Florida International").replace("AM","A&M").replace("NC ", "North Carolina ")
                .replaceAll(" St.$"," State").replace("<b>","").replace("</b>","").replaceFirst("^W ", "Western ").replaceFirst("^Ga ", "Georgia ")
                .replace("N Illinois","Northern Illinois").replaceAll("^Kent$","Kent State").replaceAll("^ULM$","Louisiana Monroe").replaceAll("^ULL$","Louisiana Lafayette")
                .replace("Louisiana-Monroe", "Louisiana Monroe").replace("Louisiana-Lafayette", "Louisiana Lafayette").replace("Ohio U.", "Ohio")
                .replace("Miami OH", "Miami (OH)").replace("Int'l", "International").replace("UCF", "Central Florida")
                .replace("SMU", "Southern Methodist").replace("Middle Tennessee", "Middle Tennessee State").replace("Texas El Paso", "UTEP")
                .replace("Texas-San Antonio", "UTSA").replace("Alabama-Birmingham", "UAB").replace("Southern California", "USC")
                .replace("Nevada-Las Vegas", "UNLV").replace("Louisiana State", "LSU").replace("Miami-FL", "Miami (FL)")
                .replace("Texas St-San Marcos", "Texas State").trim();
    }

    private static String cleanNFLTeamName(String teamName) {
        return teamName.replaceFirst("NY", "New York")
                .replaceFirst("N.Y.", "New York")
                .replaceFirst("LA", "Los Angeles")
                .replaceFirst("L.A.", "Los Angeles");
    }

    private static int askForRow(int column, String[][] predictions, String...args) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(System.in));

            System.out.println(format("Which game is %s vs. %s? ", args[0], args[1]));
            for (int i = 0; i < predictions.length; i++) {
                if (predictions[i][column] == null) {
                    System.out.println(format("%d) %s vs. %s", i, predictions[i][0], predictions[i][1]));
                }
            }
            String input = br.readLine();

            return Integer.valueOf(input);

        } catch (IOException e) {
            e.printStackTrace(System.out);
            System.exit(0);
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }

    private static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; } // both strings are zero length

        return (longerLength - getLevenshteinDistance(longer, shorter)) /
                (double) longerLength;

    }

    public static void printResults(boolean isNFL, String[][] predictions) {
        BufferedWriter bw = null;

        try {
            File file = new File(format("/Users/patrick.stoneburner/Desktop/%s_picks.csv", isNFL ? "nfl" : "ncaa"));
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw1 = new FileWriter(file);
            bw = new BufferedWriter(fw1);
            if (isNFL) {
                bw.write("Home Team, Away Team, PR, Dratings, Fox, OS, 538, Massey, Sagarin, Spread");
            } else {
                bw.write("Home Team, Away Team, PR, Dratings, Fox, OS, S&P+, Massey, Sagarin, 538, Atomic, Spread");
            }
            bw.newLine();

            for (int i = 0; i < predictions.length; i++) {
                if (isNFL) {
                    bw.write(format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", predictions[i][0], predictions[i][1],
                            predictions[i][2] != null ? predictions[i][2] : "",
                            predictions[i][3] != null ? predictions[i][3] : "",
                            predictions[i][4] != null ? predictions[i][4] : "",
                            predictions[i][5] != null ? predictions[i][5] : "",
                            predictions[i][6] != null ? predictions[i][6] : "",
                            predictions[i][7] != null ? predictions[i][7] : "",
                            predictions[i][8] != null ? predictions[i][8] : "",
                            predictions[i][9] != null ? predictions[i][9] : ""));
                } else {
                    bw.write(format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", predictions[i][0], predictions[i][1], predictions[i][2] != null ? predictions[i][2] : "", predictions[i][3] != null ? predictions[i][3] : "", predictions[i][4] != null ? predictions[i][4] : "", predictions[i][5] != null ? predictions[i][5] : "", predictions[i][6] != null ? predictions[i][6] : "", predictions[i][7] != null ? predictions[i][7] : "", predictions[i][8] != null ? predictions[i][8] : "", predictions[i][9] != null ? predictions[i][9] : "", predictions[i][10] != null ? predictions[i][10] : "", predictions[i][11] != null ? predictions[i][11] : ""));
                }
                bw.newLine();
            }

            bw.close();

        } catch (IOException e) {
            System.exit(0);
        }
    }
}
