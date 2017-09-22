package main.java.com.stoneburner.app;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static java.net.URLEncoder.encode;
import static java.util.stream.Collectors.toList;

public class NCAAMain
{
    //Home  Away    PR  DR  Fox  OS S&P+    Massey  Sagarin 538 Atomic  Spread
    public static String[][] predictions = new String[100][12];
    public static int week;
    public static int numGames = 0;
    public static String inputURIPR = "https://thepowerrank.com/predictions/";
    public static String inputURIDR = "http://www.dratings.com/predictor/ncaa-football-predictions/";
    public static String inputURIOS = "http://www.oddsshark.com/ncaaf/computer-picks";
    public static String inputURIFox = "";
    public static String inputSP = "https://www.footballstudyhall.com/pages/2017-%team%-advanced-statistical-profile";
    public static String inputSagarin = "http://sagarin.com/sports/cfsend.htm";
    public static String inputMassey = "http://www.masseyratings.com/predjson.php?s=cf&sub=11604&dt=$dt$";
    public static String inputSpread = "http://www.vegasinsider.com/college-football/odds/offshore/2/";
    public static String input538 = "http://projects.fivethirtyeight.com/2016-college-football-predictions/sims.csv";
    public static String inputAtomic = "http://www.atomicfootball.com/archive/af_predictions_All.html";

    public static void main( String[] args )
    {
        //What week of the season is it?
        DateTime game1 = new DateTime(1504368000000l);
        DateTime today = new DateTime();
        week = Weeks.weeksBetween(game1, today).getWeeks()+2;
        inputURIFox = String.format("http://www.foxsports.com/college-football/predictions?season=2017&seasonType=1&week=%d&group=-3",week);

        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyyMMdd");
        inputMassey = inputMassey.replace("$dt$", dtfOut.print(today));

        grabPowerRank();
        grabAtomic();
        //grab538();
        grabSpread();
        grabSagarin();
        grabMassey();
        grabSandP();
        grabDRatings();
        grabFox();
        grabOddsShark();


        printResults();

        System.out.println("Done!");
    }

    public static void grabPowerRank() {
        System.out.println( "Fetching '" + inputURIPR + "'");

        //manually add a game for some weird reason
        predictions[numGames][0] = "Connecticut";
        predictions[numGames++][1] = "East Carolina";

        //Execute client with our method
        try
        {
            Document page = Jsoup.connect(inputURIPR).get();
            Element ncaaHeader = page.select("h2").stream()
                    .filter(e -> e.toString().contains("College Football,"))
                    .findFirst()
                    .orElse(null);
            Element current = ncaaHeader.nextElementSibling().nextElementSibling();

            while (current.nextElementSibling() != null) {
                current = current.nextElementSibling();
                if (!current.tagName().equals("p")) {
                    break;
                }

                String teamsString = current.select("strong").get(0).childNode(0).toString()
                        .replaceAll("[0-9]*","").replaceAll("\\.","").trim();
                String away = teamsString.split("(\\bat\\b|\\bversus\\b)")[0].trim();
                String home = teamsString.split("(\\bat\\b|\\bversus\\b)")[1].trim();

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
            System.out.println("Exception occurred: " + e.getStackTrace() + e.toString());
            System.exit(0);
        }
    }

    public static void grabDRatings() {
        System.out.println( "Fetching '" + inputURIDR + "'");

        try
        {
            Document page = Jsoup.connect(inputURIDR).get();
            Elements rows = page.select("table[class=small-text]").get(1).select("tr");

            for (int i = 2; i < rows.size(); i = i + 2) {
                Element rowOne = rows.get(i);
                Element rowTwo = rows.get(i+1);

                String away = rowOne.select("td").get(2).childNodes().get(0).toString();
                String home = rowTwo.select("td").get(0).childNodes().get(0).toString();

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
            System.out.println("Exception occurred: " + e.getStackTrace() + e.toString());
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
                if (teams.size() == 0) {
                    continue;
                }

                String away = teams.get(0).childNode(0).toString();
                String home = teams.get(1).childNode(0).toString();

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
            System.out.println("Exception occurred: " + e.getStackTrace() + e.toString());
            System.exit(0);
        }
    }

    public static void grabOddsShark() {
        System.out.println( "Fetching '" + inputURIOS + "'");

        try
        {
            Document page = Jsoup.connect(inputURIOS).get();
            Elements games = page.select("table");

            for (int i = 0; i < numGames-1; i++) {
                Element game = games.get(i);
                List<Node> teams = game.select("caption")
                        .get(0).select("caption").get(0).childNodes();
                String away = teams.get(0).toString().trim();
                String home = teams.get(2).toString().trim();
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
            System.out.println("Exception occurred: " + e.getStackTrace() + e.toString());
            System.exit(0);
        }
    }

    public static void grabSandP() {
        boolean homeTeamFailure = false;
        boolean awayTeamFailure = false;
        for (int i = 0; i < numGames; i++) {
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
                    page = Jsoup.connect(homeTeamFailure ? awayUrl : homeUrl).get();
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
                    DateTime thisPastMonday = new DateTime().withWeekyear(currentDate.getWeekyear()).withYear(2017).withDayOfWeek(1).withHourOfDay(0);
                    DateTime inAWeek = thisPastMonday.plusWeeks(1);

                    String nextGameString = currentPrediction.select("td").get(0).childNode(0).toString();
                    DateTimeFormatter format = DateTimeFormat.forPattern("d-MMM");
                    DateTime gameDate = format.withLocale(Locale.ENGLISH).parseDateTime(nextGameString).withYear(thisPastMonday.getYear()).withHourOfDay(22);

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
                System.out.println("Exception occurred: " + e.getStackTrace() + e.toString());
            }
        }
    }

    public static void grabMassey() {
        System.out.println( "Fetching '" + inputMassey + "'");

        //Instantiate client and method
        HttpClient client = HttpClientBuilder.create().build();
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
                double spread;
                Object prediction = ((JSONArray)current.get(12)).get(0);
                if (prediction instanceof Integer) {
                    spread = (double)((Integer)prediction) * -1.0;
                } else if (prediction instanceof Double) {
                    spread = (Double)prediction * -1.0;
                } else {
                    continue;
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
            System.out.println("Exception occurred: " + e.getStackTrace() + e.toString());
            System.exit(0);
        }
    }

    public static void grabSagarin() {
        System.out.println( "Fetching '" + inputSagarin + "'");

        try
        {
            Document page = Jsoup.connect(inputSagarin).get();
            Node predictionSection = page.select("a[name=New_Feature]").get(0).parent().parent().parent().childNode(2);
            String[] rows = Arrays.copyOfRange(predictionSection.toString().split("\r\n"), 7,
                    predictionSection.toString().split("\r\n").length);

            for (int i = 0; i < numGames; i++) {
                String home = predictions[i][0];
                String away = predictions[i][1];

                //iterate through list of games to find a match
                for (int j = 0; j < rows.length; j++) {

                    if (!rows[j].toLowerCase().contains(home.toLowerCase()) && !rows[j].toLowerCase().contains(away.toLowerCase())) {
                        continue;
                    }

                    //if home and away aren't in this row, then skip
                    String[] elements = rows[j].split("\\s+");
                    String favorite = elements[0];
                    int favoritePieces = 1;
                    try {
                        Double.parseDouble(elements[1]);
                    } catch (NumberFormatException ex) {
                        favorite = favorite + " " + elements[1];
                        favoritePieces++;

                        try {
                            Double.parseDouble(elements[2]);
                        } catch (NumberFormatException e) {
                            favorite = favorite + " " + elements[2];
                            favoritePieces++;
                        }
                    }
                    Double averageSpread = (Double.valueOf(elements[favoritePieces]) + Double.valueOf(elements[favoritePieces+1]) +
                            Double.valueOf(elements[favoritePieces+2]) + Double.valueOf(elements[favoritePieces+3])) / 4.0;

                    String underdog = elements[favoritePieces+4];
                    if (StringUtils.isAllLowerCase(underdog)) {
                        if (StringUtils.isAllLowerCase(elements[favoritePieces+5])) {
                            underdog = underdog + " " + elements[favoritePieces+5];
                            if (StringUtils.isAllLowerCase(elements[favoritePieces+6])) {
                                underdog = underdog + " " + elements[favoritePieces+6];
                            }
                        }
                    } else if (StringUtils.isAllUpperCase(underdog)) {
                        if (StringUtils.isAllUpperCase(elements[favoritePieces+5])) {
                            underdog = underdog + " " + elements[favoritePieces+5];
                            if (StringUtils.isAllUpperCase(elements[favoritePieces+6])) {
                                underdog = underdog + " " + elements[favoritePieces+6];
                            }
                        }
                    }

                    boolean homeIsFavorite = favorite.equals(favorite.toUpperCase());

                    if (homeIsFavorite) {
                        if (!favorite.toUpperCase().equals(home.toUpperCase()) && !underdog.toUpperCase().equals(away.toUpperCase())) {
                            continue;
                        }
                        averageSpread = averageSpread * -1.0;
                    } else {
                        if (!favorite.toUpperCase().equals(away.toUpperCase()) && !underdog.toUpperCase().equals(home.toUpperCase())) {
                            continue;
                        }
                    }
                    predictions[i][8] = String.valueOf(averageSpread);


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

                String teamOne = current.select("a[class=tabletext]").get(0).childNode(0).toString();
                String teamTwo = current.select("a[class=tabletext]").get(1).childNode(0).toString();

                List<String> spreadParts = current.select("a[href$=#BU]").get(0).childNodes().stream()
                        .filter(n -> (n instanceof TextNode))
                        .map(n -> HtmlEscape.unescapeHtml(n.toString()))
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
                            predictions[actualRow][11] = String.valueOf(spread);
                            break;
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
                                predictions[actualRow][11] = String.valueOf(spread);
                                break;
                            }
                        }
                    }
                    if (actualRow < 0) {
                        actualRow = askForRow(9, teamOne, teamTwo);
                        if (actualRow >= 0) {
                            predictions[actualRow][11] = String.valueOf(spread);
                        }
                    }
                }
            }
        }

        catch (Exception e) {
            System.out.println("Exception occurred: " + e.getStackTrace() + e.toString());
            System.exit(0);
        }
    }

    public static void grab538() {
        System.out.println("Fetching '" + input538 + "'");

        //Instantiate client and method
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet method = new HttpGet(input538);

        //Execute client with our method
        try {

            HashMap<String,Integer> teams = new HashMap<String,Integer>();
            HttpResponse response = client.execute(method);

            String source = EntityUtils.toString(response.getEntity());
            String[] rows = source.split("\n");

            for (int i = 1; i < rows.length; i++) {
                String[] rowParts = rows[i].split(",");
                String teamName = cleanTeamName(rowParts[0]);
                int won = Integer.valueOf(rowParts[5]);

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
                for (int i = 0; i < numGames; i++) {
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
            System.out.println("Exception occurred: " + e.getStackTrace() + e.toString());
            System.exit(0);
        }
    }

    public static void grabAtomic() {
        System.out.println("Fetching '" + inputAtomic + "'");

        try {

            Document page = Jsoup.connect(inputAtomic).get();
            Elements rows = page.select("a[name=IA]").get(0).nextElementSibling().nextElementSibling().select("tr");
            DateTime currentDate = new DateTime();
            DateTime thisPastMonday = new DateTime().withWeekyear(currentDate.getWeekyear()).withYear(2017).withDayOfWeek(1).withHourOfDay(0);
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
                DateTime gameDate = format.withLocale(Locale.ENGLISH).parseDateTime(date).withYear(thisPastMonday.getYear()).withHourOfDay(22);

                if (gameDate.getMillis() < thisPastMonday.getMillis()) {
                    continue;
                } else if (gameDate.getMillis() > inAWeek.getMillis()) {
                    break;
                }

                //Find a spot in our array for these values
                int actualRow = -1;
                for (int j = 0; j < numGames; j++) {
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
                    actualRow = askForRow(10, home, away);
                    if (actualRow >= 0) {
                        predictions[actualRow][10] = margin;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getStackTrace() + e.toString());
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
            System.out.println("Exception occurred: " + e.getStackTrace() + e.toString());
            System.exit(0);
        }
        return -1;
    }

    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }

        return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) /
                               (double) longerLength;

    }

    public static String cleanTeamName(String teamName) {
        return teamName.replaceAll(" St$"," State").replaceFirst("E ", "Eastern ").replaceFirst("^C ", "Central ").replace("&amp;","&")
                .replace("FL ", "Florida ").replaceAll("Intl$", "International").replace("FIU","Florida International").replace("AM","A&M").replace("NC ", "North Carolina ")
                .replaceAll(" St.$"," State").replace("<b>","").replace("</b>","").replaceFirst("^W ", "Western ").replaceFirst("^Ga ", "Georgia ")
                .replace("N Illinois","Northern Illinois").replace("Kent","Kent State").replaceAll("^ULM$","Louisiana Monroe").replaceAll("^ULL$","Louisiana Lafayette")
                .replace("Louisiana-Monroe", "Louisiana Monroe").replace("Louisiana-Lafayette", "Louisiana Lafayette").trim();
    }

    public static void printResults() {
        BufferedWriter bw = null;

        try {
            File file = new File("/Users/patrick.stoneburner/Desktop/ncaa_picks.csv");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw1 = new FileWriter(file);
            bw = new BufferedWriter(fw1);
            bw.write("Home Team, Away Team, PR, Dratings, Fox, OS, S&P+, Massey, Sagarin, 538, Atomic, Spread");
            bw.newLine();

            for (int i = 0; i < numGames; i++) {
                bw.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", predictions[i][0], predictions[i][1],
                        predictions[i][2] != null ? predictions[i][2] : "",
                        predictions[i][3] != null ? predictions[i][3] : "",
                        predictions[i][4] != null ? predictions[i][4] : "",
                        predictions[i][5] != null ? predictions[i][5] : "",
                        predictions[i][6] != null ? predictions[i][6] : "",
                        predictions[i][7] != null ? predictions[i][7] : "",
                        predictions[i][8] != null ? predictions[i][8] : "",
                        predictions[i][9] != null ? predictions[i][9] : "",
                        predictions[i][10] != null ? predictions[i][10] : "",
                        predictions[i][11] != null ? predictions[i][11] : ""));
                bw.newLine();
            }

            bw.close();

        } catch (IOException e) {
            System.exit(0);
        }
    }
}
