package com.stoneburner.app;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.unbescape.html.HtmlEscape;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.WordUtils.capitalizeFully;
import static org.jsoup.Jsoup.connect;

public class NFLMain
{
    //Home  Away    PR  DR  Fox  OS 538    Massey  Sagarin Spread
    public static String[][] predictions = new String[16][10];
    public static int week;
    public static int numGames = 0;
    public static String inputURIPR = "https://thepowerrank.com/predictions/";
    public static String inputURIDR = "http://www.dratings.com/predictor/nfl-football-predictions/";
    public static String inputURIOS = "http://www.oddsshark.com/nfl/computer-picks";
    public static String inputURIFox = "http://www.foxsports.com/nfl/predictions";
    public static String input538 = "https://projects.fivethirtyeight.com/2018-nfl-predictions/games/";
    public static String inputSagarin = "http://sagarin.com/sports/nflsend.htm";
    public static String inputMassey = "http://www.masseyratings.com/predjson.php?s=nfl&dt=$dt$";
    public static String inputSpread = "http://www.vegasinsider.com/nfl/odds/offshore/2/";
    public static List<String> teamwords = new ArrayList<String>();
    public static HashMap<String,String> teamShortToLong = new HashMap<String,String>();
    public static HashMap<String,String> teamMascotToCity = new HashMap<String,String>();

    public static void main( String[] args )
    {
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

        DateTime today = new DateTime();
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyyMMdd");
        inputMassey = inputMassey.replace("$dt$", dtfOut.print(today));

        grabPowerRank();
        grabSpread();
        grabSagarin();
        grabMassey();
        grab538();
        grabDRatings();
        grabFox();
        grabOddsShark();


        printResults();

        System.out.println("Done!");
    }

    public static void grabPowerRank() {
        System.out.println( "Fetching '" + inputURIPR + "'");

        //Execute client with our method
        try
        {
            Document page = Jsoup.connect(inputURIPR).get();
            Element nflHeader = page.select("h2").stream()
                    .filter(e -> e.toString().contains("NFL"))
                    .findFirst()
                    .orElse(null);
            Element current = nflHeader.nextElementSibling().nextElementSibling();

            while (current.nextElementSibling() != null) {
                current = current.nextElementSibling();
                if (!current.tagName().equals("p")) {
                    break;
                }

                String teamsString = current.select("strong").get(0).childNodes().get(0).toString()
                        .replaceAll("[0-9]*","").replaceAll("\\.","").trim();
                String away = cleanTeamName(teamsString.split("(\\bat\\b|\\bversus\\b)")[0].trim());
                String home = cleanTeamName(teamsString.split("(\\bat\\b|\\bversus\\b)")[1].trim());

                Node summary = current.childNodes().get(2);
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
    }

    public static void grabDRatings() {
        System.out.println( "Fetching '" + inputURIDR + "'");

        try
        {
            Document page = Jsoup.connect(inputURIDR).get();
            Elements rows = page.select("table[class=small-text]").get(0).select("tr");

            for (int i = 2; i < rows.size(); i = i + 2) {
                Element rowOne = rows.get(i);
                Element rowTwo = rows.get(i+1);

                String away = cleanTeamName(rowOne.select("td").get(2).childNodes().get(0).toString());
                String home = cleanTeamName(rowTwo.select("td").get(0).childNodes().get(0).toString());

                //Grab spread, and favorite
                String homePoints = rowTwo.select("td").get(5).childNodes().get(0).childNodes().get(0).toString().trim();
                String awayPoints = rowOne.select("td").get(7).childNodes().get(0).childNodes().get(0).toString().trim();

                double margin = Double.valueOf(awayPoints) - Double.valueOf(homePoints);

                //Find a spot in our array for these values
                for (int j = 0; j < numGames; j++) {
                    double homeResult =  similarity(predictions[j][0], home);
                    double awayResult =  similarity(predictions[j][1], away);
                    if (homeResult == 1 || awayResult == 1) {
                        predictions[j][3] = String.valueOf(margin);
                    }
                }
            }
        }

        catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
    }

    public static void grabFox() {
        System.out.println( "Fetching '" + inputURIFox + "'");

        try
        {
            Document page = Jsoup.connect(inputURIFox).get();
            Elements games = page.select("div[class=wisbb_predictionChip]");

            for (Element game : games) {
                Elements teams = game.select("span[class=wisbb_teamName]");

                String awayMascot = teams.get(0).childNodes().get(0).toString();
                String homeMascot = teams.get(1).childNodes().get(0).toString();

                String away = teamMascotToCity.get(awayMascot);
                String home = teamMascotToCity.get(homeMascot);

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

                for (int j = 0; j < numGames; j++) {
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

    public static void grabOddsShark() {
        System.out.println( "Fetching '" + inputURIOS + "'");

        //Execute client with our method
        try
        {
            Document page = Jsoup.connect(inputURIOS).get();
            Elements games = page.select("table");

            for (int i = 0; i < numGames-1; i++) {
                Element game = games.get(i);
                if (game.toString().contains("Results")) {
                    break;
                }
                Elements teams = game.select("caption").get(0).getElementsByClass("name-long");
                String away = cleanTeamName(teams.get(0).text().trim());
                String home = cleanTeamName(teams.get(1).text().trim());
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
                for (int j = 0; j < numGames; j++) {
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
                    actualRow = askForRow(5, home, away);
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

    public static void grab538() {
        System.out.println( "Fetching '" + input538 + "'");

        //Execute client with our method
        try
        {
            Document page = Jsoup.connect(input538).get();

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

                for (int j = 0; j < numGames; j++) {
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

    public static void grabMassey() {
        System.out.println( "Fetching '" + inputMassey + "'");

        //Instantiate client and method
        HttpClient client = new DefaultHttpClient();
        HttpGet method = new HttpGet(inputMassey);

        //Execute client with our method
        try
        {
            HttpResponse response = client.execute(method);

            String source = EntityUtils.toString(response.getEntity());

            JSONObject json = new JSONObject(source);
            JSONArray gamesArray = (JSONArray) json.get("DI");

            for (Object currentGame : gamesArray) {
                JSONArray current = (JSONArray)currentGame;
                String away = cleanTeamName((String)((JSONArray)current.get(2)).get(0));
                String home = cleanTeamName(((String)((JSONArray)current.get(3)).get(0)).replace("@ ", ""));
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
                for (int j = 0; j < numGames; j++) {
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
                    actualRow = askForRow(7, home, away);
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

    public static void grabSagarin() {
        System.out.println( "Fetching '" + inputSagarin + "'");

        try
        {
            Document page = connect(inputSagarin).get();
            Node predictionSection = page.select("a[name=Predictions]").get(0).parent().parent().parent().childNode(2);
            String[] rows = copyOfRange(predictionSection.toString().split("\r\n"), 8,
                    predictionSection.toString().split("\r\n").length);

            for (int i = 0; i < numGames; i++) {
                String home = predictions[i][0];
                String away = predictions[i][1];

                //iterate through list of games to find a match
                for (int j = 0; j < rows.length; j++) {
                    String currentRow = rows[j];
                    if (currentRow.startsWith("=====") || isBlank(currentRow) || currentRow.contains("eigen")) {
                        break;
                    } else if (!currentRow.toLowerCase().contains(home.toLowerCase()) && !currentRow.toLowerCase().contains(away.toLowerCase())) {
                        continue;
                    }


                    String favorite = currentRow.substring(4, 27).trim();
                    String underdog = currentRow.substring(59).trim();

                    String[] spreads = currentRow.substring(27, 59).trim().split("\\s+");
                    double averageSpread = asList(spreads).stream().mapToDouble(Double::valueOf).average().getAsDouble();

                    home = addMascot(home);
                    away = addMascot(away);

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

    public static void grabSpread() {
        System.out.println( "Fetching '" + inputSpread + "'");
        
        try
        {
            Document page = Jsoup.connect(inputSpread).get();
            Elements rows = page.select("table[class=frodds-data-tbl] tr");

            for (int i = 0; i < rows.size(); i = i+2) {
                Element current = rows.get(i);
                Elements fiveDimes = current.select("a[href$=#BU]");
                if (fiveDimes.size() == 0) {
                    //no spread posted
                    continue;
                }

                String teamOne = cleanTeamName(current.select("a[class=tabletext]").get(0).childNode(0).toString());
                String teamTwo = cleanTeamName(current.select("a[class=tabletext]").get(1).childNode(0).toString());

                List<String> spreadParts = current.select("a[href$=#BU]").get(0).childNodes().stream()
                        .filter(n -> (n instanceof TextNode))
                        .map(n -> HtmlEscape.unescapeHtml(n.toString().replace("PK","-0")))
                        .collect(toList())
                        .subList(1,3);

                boolean teamOneIsFavorite;
                Double spread = null;
                if (spreadParts.get(0).startsWith("-")) {
                    teamOneIsFavorite = true;
                    if (StringUtils.isNotEmpty(spreadParts.get(0)) && !spreadParts.get(0).equals(" ")) {
                        String spreadString = spreadParts.get(0).split("-|\\+|EV")[1].replace("½", ".5").replace(" EV", "");
                        spread = Double.valueOf(spreadString.substring(0, spreadString.length() - 1));
                    }
                } else {
                    teamOneIsFavorite = false;
                    if (StringUtils.isNotEmpty(spreadParts.get(1)) && !spreadParts.get(1).equals(" ")) {
                        String spreadString = spreadParts.get(1).split("-|\\+|EV")[1].replace("½", ".5").replace(" EV", "");
                        spread = Double.valueOf(spreadString.substring(0, spreadString.length() - 1)) * -1.0;
                    }
                }

                if (spread != null) {
                    //Find a spot in our array for these values
                    int actualRow = -1;
                    for (int j = 0; j < numGames; j++) {
                        double homeResult = similarity(predictions[j][0], teamTwo);
                        double awayResult = similarity(predictions[j][1], teamOne);
                        if (homeResult == 1 || awayResult == 1) {
                            actualRow = j;
                            predictions[actualRow][9] = String.valueOf(spread);
                            //maybe the two are reversed?
                        } else {
                            homeResult = similarity(predictions[j][0], teamTwo);
                            awayResult = similarity(predictions[j][1], teamOne);

                            if (homeResult == 1 || awayResult == 1) {
                                String third = teamOne;
                                teamOne = teamTwo;
                                teamTwo = third;
                                spread = spread * -1.0;
                                actualRow = j;
                                predictions[actualRow][9] = String.valueOf(spread);
                            }
                        }
                    }
                    if (actualRow < 0) {
                        actualRow = askForRow(9, teamOne, teamTwo);
                        if (actualRow >= 0) {
                            predictions[actualRow][9] = String.valueOf(spread);
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

    public static int askForRow(int column, String...args) {
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(System.in));

            System.out.println(String.format("Which game is %s vs. %s? ", args[0], args[1]));
            for (int i = 0; i < numGames; i++) {
                if (predictions[i][column] == null) {
                    System.out.println(String.format("%d) %s vs. %s", i, predictions[i][0], predictions[i][1]));
                }
            }
            String input = br.readLine();

            return Integer.valueOf(input);

        } catch (IOException e) {
            e.printStackTrace(System.out);
            System.exit(0);
        }
        return -1;
    }

    private static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }

        return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) /
                               (double) longerLength;

    }

    private static String cleanTeamName(String teamName) {
        return teamName.replaceFirst("NY", "New York")
                .replaceFirst("N.Y.", "New York")
                .replaceFirst("LA", "Los Angeles")
                .replaceFirst("L.A.", "Los Angeles");
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

    public static void printResults() {
        BufferedWriter bw = null;

        try {
            File file = new File("/Users/patrick.stoneburner/Desktop/nfl_picks.csv");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw1 = new FileWriter(file);
            bw = new BufferedWriter(fw1);
            bw.write("Home Team, Away Team, PR, Dratings, Fox, OS, 538, Massey, Sagarin, Spread");
            bw.newLine();

            for (int i = 0; i < numGames; i++) {
                bw.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", predictions[i][0], predictions[i][1],
                        predictions[i][2] != null ? predictions[i][2] : "",
                        predictions[i][3] != null ? predictions[i][3] : "",
                        predictions[i][4] != null ? predictions[i][4] : "",
                        predictions[i][5] != null ? predictions[i][5] : "",
                        predictions[i][6] != null ? predictions[i][6] : "",
                        predictions[i][7] != null ? predictions[i][7] : "",
                        predictions[i][8] != null ? predictions[i][8] : "",
                        predictions[i][9] != null ? predictions[i][9] : ""));
                bw.newLine();
            }

            bw.close();

        } catch (IOException e) {
            System.exit(0);
        }
    }
}
