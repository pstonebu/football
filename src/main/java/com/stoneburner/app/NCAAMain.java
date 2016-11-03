package main.java.com.stoneburner.app;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.joda.time.Weeks;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.unbescape.html.HtmlEscape;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.*;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static String inputSP = "http://www.footballstudyhall.com/pages/2016-%team%-advanced-statistical-profile";
    public static String inputSagarin = "http://sagarin.com/sports/cfsend.htm";
    public static String inputMassey = "http://www.masseyratings.com/predjson.php?s=cf&sub=11604&dt=$dt$";
    public static String inputSpread = "http://www.vegasinsider.com/college-football/odds/offshore/2/";
    public static String input538 = "http://projects.fivethirtyeight.com/2016-college-football-predictions/sims.csv";
    public static String inputAtomic = "http://www.al.com/atomic-football/atfo_cf_predictions_FBS.html";

    public static void main( String[] args )
    {
        //What week of the season is it?
        DateTime game1 = new DateTime(1472918400000l);
        DateTime today = new DateTime();
        week = Weeks.weeksBetween(game1, today).getWeeks()+2;
        inputURIFox = String.format("http://www.foxsports.com/college-football/predictions?season=2016&seasonType=1&week=%d&group=-3",week);

        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyyMMdd");
        inputMassey = inputMassey.replace("$dt$", dtfOut.print(today));

        grabPowerRank();
        grabAtomic();
        grab538();
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

        //Instantiate client and method
        HttpClient client = new DefaultHttpClient();
        HttpGet method = new HttpGet(inputURIPR);

        //Execute client with our method
        try
        {


            HttpResponse response = client.execute(method);

            String source = EntityUtils.toString(response.getEntity());
            source = source.split("<h2>College Football, Week [0-9]*</h2>")[1];
            source = source.split("<h2>National Football League, Week [0-9]</h2")[0];
            String[] games = Arrays.copyOfRange(source.split("<strong>"), 1, source.split("<strong>").length);

            //for (String game : games) {
            for (int i = 0; i < games.length; i++) {
                String game = games[i];
                game = HtmlEscape.unescapeHtml(game).replace("</strong>","").replace("<br />", " ")
                        .replaceAll("\\([0-9]*\\) ","").replace("</p>", "").replace("\n", "").replace("<p>","");
                String home;
                String away;
                String spread;

                String[] sentences = game.split("\\.[ ]+");

                //Grab team names
                away = sentences[1].split("(\\bat\\b|\\bversus\\b)")[0].trim();
                home = sentences[1].split("(\\bat\\b|\\bversus\\b)")[1].trim();

                //Grab spread, and favorite
                boolean negative = sentences[2].startsWith(home);
                Pattern p = Pattern.compile("(.*)(\\d+)(.*)");
                Matcher m = p.matcher(sentences[2]);
                String[] spreadParts = sentences[2].split("\\.");
                String spreadTail = spreadParts[1].split(" ")[0];
                String spreadHead = spreadParts[0].split(" ")[spreadParts[0].split(" ").length - 1];

                spread = (negative ? "-" : "") + spreadHead + "." + spreadTail;

                predictions[i][0] = home;
                predictions[i][1] = away;
                predictions[i][2] = spread;
                numGames++;

            }
        }

        catch (Exception e) {
            System.out.println("Exception occurred: " + e.getStackTrace() + e.toString());
            System.exit(0);
        }
    }

    public static void grabDRatings() {
        System.out.println( "Fetching '" + inputURIDR + "'");

        //Instantiate client and method
        HttpClient client = new DefaultHttpClient();
        HttpGet method = new HttpGet(inputURIDR);

        //Execute client with our method
        try
        {

            HttpResponse response = client.execute(method);

            String source = EntityUtils.toString(response.getEntity());
            source = source.split("<h5>NCAA Football FBS Games Predictions</h5>")[1];
            source = source.split("<tbody>")[1];
            source = source.replaceAll("\\n","").trim().split("</tbody>")[0];
            String[] rows = Arrays.copyOfRange(source.split("<tr>"), 2, source.split("<tr>").length);

            for (int i = 0; i < numGames*2; i = i+2) {
                String row1 = rows[i];
                String row2 = rows[i+1];

                //Grab team names
                String home;
                String away;

                away = row1.split("(\\bat\\b|\\bvs\\b)")[0].trim().split("<td rowspan=\"2\">")[2];
                home = row1.split("(\\bat\\b|\\bvs\\b)")[1].trim().split("</td>")[0];

                away = cleanTeamName(away);
                home = cleanTeamName(home);

                //Grab spread, and favorite
                String homePoints = row2.split("<center>")[2].split("</center>")[0];
                String awayPoints = row1.split("<center>")[2].split("</center>")[0];

                double margin = Double.valueOf(awayPoints) - Double.valueOf(homePoints);

                //Find a spot in our array for these values
                int actualRow = -1;
                for (int j = 0; j < numGames; j++) {
                    double homeResult =  similarity(predictions[j][0], home);
                    double awayResult =  similarity(predictions[j][1], away);
                    if (homeResult == 1 || awayResult == 1) {
                        actualRow = j;
                        predictions[actualRow][3] = String.valueOf(margin);
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
                            predictions[actualRow][4] = String.valueOf(margin);
                        }
                    }
                }
                if (actualRow < 0) {
                    actualRow = askForRow(3, home, away);
                    if (actualRow >= 0) {
                        predictions[actualRow][3] = String.valueOf(Double.valueOf(awayPoints) - Double.valueOf(homePoints));
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

        //Instantiate client and method
        HttpClient client = new DefaultHttpClient();
        HttpGet method = new HttpGet(inputURIFox);

        //Execute client with our method
        try
        {

            HttpResponse response = client.execute(method);

            String source = EntityUtils.toString(response.getEntity());
            source = source.split("<table class=\"wisfb_standard wisfb_hoverHighlight\">")[1]
                    .replaceAll("\\n","").replaceAll("\\t","").replaceAll("\\r","")
                    .split("</table>")[0];

            String[] rows = Arrays.copyOfRange(source.split("<tr>"), 2, source.split("<tr>").length-1);

            for (int i = 0; i < rows.length; i ++) {
                String row = rows[i];
                String location = rows[i].split("<div class=\"wisfb_fullTeamStacked\">")[1].split("<td class=\"wisfb_oppCol\">")[1].split("<span>")[1].split("</span>")[0].trim();
                String favorite = rows[i].split("alt=\"")[1].split("\"")[0];
                String underdog = rows[i].split("<span class=\"wisfb_oppFull\">")[1].split("</span>")[0];
                String avgScore = rows[i].split("<a class=\"wisfb_avgScoreLink\".*target=\"_blank\">")[1].split("</a>")[0];

                String spread = rows[i].split("</span></td><td>")[1].split("</td>")[0];
                //FCS game
                if (spread.equals("--")) {
                    continue;
                }

                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                Object result = engine.eval(avgScore);
                double prediction;
                if (result instanceof Integer) {
                    prediction = (double)((Integer)result);
                } else {
                    prediction = (Double)result;
                }


                boolean homeIsFav = location.equals("vs.");
                String home = cleanTeamName(homeIsFav ? favorite : underdog);
                String away = cleanTeamName(homeIsFav ? underdog : favorite);

                //Find a spot in our array for these values
                int actualRow = -1;
                for (int j = 0; j < numGames; j++) {
                    double homeResult =  similarity(predictions[j][0], home);
                    double awayResult =  similarity(predictions[j][1], away);
                    if (homeResult == 1 || awayResult == 1) {
                        actualRow = j;
                        predictions[actualRow][4] = (homeIsFav ? "-" : "") + prediction;
                        //maybe the two are reversed?
                    } else {
                        homeResult =  similarity(predictions[j][0], away);
                        awayResult =  similarity(predictions[j][1], home);

                        if (homeResult == 1 || awayResult == 1) {
                            String third = home;
                            home = away;
                            away = third;
                            prediction = prediction * -1.0;
                            actualRow = j;
                            predictions[actualRow][4] = String.valueOf(prediction);
                        }
                    }
                }
                if (actualRow < 0) {
                    actualRow = askForRow(4, homeIsFav ? favorite : underdog, homeIsFav ? underdog : favorite);
                    if (actualRow >= 0) {
                        predictions[actualRow][4] = (homeIsFav ? "-" : "") + prediction;
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

        //Instantiate client and method
        HttpClient client = new DefaultHttpClient();
        HttpGet method = new HttpGet(inputURIOS);

        //Execute client with our method
        try
        {
            HttpResponse response = client.execute(method);

            String source = EntityUtils.toString(response.getEntity());
            String[] allRows = source.split("<div class=\"region region-content\"><div id=\"block-system-main\" class=\"block block-system\"><div class=\"content\">")[1]
                    .split("<table class=\"base-table\">");
            String[] rows = Arrays.copyOfRange(allRows, 1, allRows.length-1);

            for (int i = 0; i < rows.length; i++) {
                String current = rows[i];
                String away = current.split("<caption>")[1].split("<a href=")[0].trim();
                String home = current.split("</a>")[1].split("</caption")[0].trim();
                String prediction = current.split("Predicted Score</td><td>")[1].split("</td>")[0];

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
            String homeUrl = inputSP.replaceAll("%team%", URLEncoder.encode(homeTeam.toLowerCase()).replace("+","-"));
            String awayUrl = inputSP.replaceAll("%team%", URLEncoder.encode(awayTeam.toLowerCase()).replace("+","-"));
            System.out.println("Fetching '" + (homeTeamFailure ? awayUrl : homeUrl) + "'");

            //Instantiate client and method
            HttpClient client = new DefaultHttpClient();
            HttpGet method = new HttpGet(homeTeamFailure ? awayUrl : homeUrl);

            //Execute client with our method
            try {
                HttpResponse response = client.execute(method);
                if (response.getStatusLine().getStatusCode() >= 300) {
                    if (!homeTeamFailure) {
                        --i;
                    }
                    homeTeamFailure = !homeTeamFailure;
                    continue;
                }
                String source = EntityUtils.toString(response.getEntity());

                String[] tables = source.split("<table border=\"1\"");
                String predictionTable = tables[2].replaceAll("\\r","").replaceAll("\\n","");
                if (!predictionTable.contains("Cumulative")) {
                    System.out.println("The third table didn't look like the predictions table, continuing on.");
                    continue;
                }

                for (int j = 2; j < predictionTable.split("<tr>").length; j++) {
                    DateTime currentDate = new DateTime();
                    DateTime thisPastMonday = new DateTime().withWeekyear(currentDate.getWeekyear()).withYear(2016).withDayOfWeek(1).withHourOfDay(0);
                    DateTime inAWeek = thisPastMonday.plusWeeks(1);

                    String nextPredictionRow = predictionTable.split("<tr>")[j];
                    String nextGameDate = nextPredictionRow.split("<td>")[1].split("</td>")[0];

                    DateTimeFormatter format = DateTimeFormat.forPattern("d-MMM");
                    DateTime gameDate = format.withLocale(Locale.ENGLISH).parseDateTime(nextGameDate).withYear(thisPastMonday.getYear()).withHourOfDay(22);

                    if (gameDate.getMillis() < thisPastMonday.getMillis() || gameDate.getMillis() > inAWeek.getMillis()) {
                        System.out.println("Something weird with the next game date. Continuing.");
                        continue;
                    } else {
                        String spread = nextPredictionRow.split("<td align=\"center\">")[4].replaceAll("</td>","");
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

        //Instantiate client and method
        HttpClient client = new DefaultHttpClient();
        HttpGet method = new HttpGet(inputSagarin);

        //Execute client with our method
        try
        {
            HttpResponse response = client.execute(method);

            String html = EntityUtils.toString(response.getEntity());

            String[] rows = Arrays.copyOfRange(html.split("<a name=\"New_Feature\"><b>New_Feature</b></a></h2></font>")[1].split("\r\n"),
                    5, html.split("<a name=\"New_Feature\"><b>New_Feature</b></a></h2></font>")[1].split("\r\n").length-2);

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
            System.out.println("Exception occurred: " + e.getStackTrace() + e.toString());
            System.exit(0);
        }
    }

    public static void grabSpread() {
        System.out.println( "Fetching '" + inputSpread + "'");

        //Instantiate client and method
        HttpClient client = new DefaultHttpClient();
        HttpGet method = new HttpGet(inputSpread);

        //Execute client with our method
        try
        {
            HttpResponse response = client.execute(method);

            String source = EntityUtils.toString(response.getEntity());
            String[] rows = source.split("<table")[14].replaceAll("\n","").replaceAll("\t","").split("<tr>");

            for (int i = 1; i < rows.length; i = i+2) {
                String firstRow = rows[i];
                String teamOne = null;

                try {
                    teamOne = firstRow.split("<a href=\"")[1].split(">")[1].split("</a")[0];
                } catch (IndexOutOfBoundsException e) {
                    i = i - 3;
                    continue;
                }
                String teamTwo = firstRow.split("<a href=\"")[2].split(">")[1].split("</a")[0];

                String [] spreadParts;
                try {
                    spreadParts = Arrays.copyOfRange(HtmlEscape.unescapeHtml(firstRow.split("<a class=\"cellTextNorm\" href=\"")[firstRow.split("<a class=\"cellTextNorm\" href=\"").length - 1]
                            .split("_blank\">")[1].split("</a>")[0]).split("<br>"), 1, 3);
                } catch (IndexOutOfBoundsException ex) {
                    continue;
                }

                boolean teamOneIsFavorite;
                double spread = 0.0;
                if (spreadParts[0].startsWith("-")) {
                    teamOneIsFavorite = true;
                    String spreadString = spreadParts[0].split("-|\\+|EV")[1].replace("½", ".5").replace(" EV", "");
                    spread = Double.valueOf(spreadString.substring(0, spreadString.length()-1));
                } else {
                    teamOneIsFavorite = false;
                    String spreadString = spreadParts[1].split("-|\\+|EV")[1].replace("½", ".5").replace(" EV", "");
                    spread = Double.valueOf(spreadString.substring(0, spreadString.length()-1)) * -1.0;
                }

                //Find a spot in our array for these values
                int actualRow = -1;
                for (int j = 0; j < numGames; j++) {
                    double homeResult =  similarity(predictions[j][0], teamTwo);
                    double awayResult =  similarity(predictions[j][1], teamOne);
                    if (homeResult == 1 || awayResult == 1) {
                        actualRow = j;
                        predictions[actualRow][11] = String.valueOf(spread);
                        //maybe the two are reversed?
                    } else {
                        homeResult =  similarity(predictions[j][0], teamOne);
                        awayResult =  similarity(predictions[j][1], teamTwo);

                        if (homeResult == 1 || awayResult == 1) {
                            String third = teamOne;
                            teamOne = teamTwo;
                            teamTwo = third;
                            spread = spread * -1.0;
                            actualRow = j;
                            predictions[actualRow][11] = String.valueOf(spread);
                        }
                    }
                }
                if (actualRow < 0) {
                    actualRow = askForRow(11, teamOne, teamTwo);
                    if (actualRow >= 0) {
                        predictions[actualRow][11] = String.valueOf(spread);
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
        HttpClient client = new DecompressingHttpClient();
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
                Double winPct = teams.get(teamName) / 100.0;
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

        //Instantiate client and method
        HttpClient client = new DecompressingHttpClient();
        HttpGet method = new HttpGet(inputAtomic);

        //Execute client with our method
        try {

            HttpResponse response = client.execute(method);

            String source = EntityUtils.toString(response.getEntity());
            String[] rows = source.replaceAll("\r","").replaceAll("\n","").split("<table class=\"atfoTable\" cellspacing=\"1\">")[1].split("</table>")[0].split("<tr class=\"");
            rows = Arrays.copyOfRange(rows, 3, rows.length);

            DateTime currentDate = new DateTime();
            DateTime thisPastMonday = new DateTime().withWeekyear(currentDate.getWeekyear()).withYear(2016).withDayOfWeek(1).withHourOfDay(0);
            DateTime inAWeek = thisPastMonday.plusWeeks(1);

            for (String row : rows) {
                String[] elements = row.split("<td class=\"atfoCent\">");
                String date = elements[1].split("</td>")[0];
                String away = cleanTeamName(elements[2].split("</td>")[0]);
                String home = cleanTeamName(elements[4].split("</td>")[0]);
                String awayScore = elements[3].split("</td>")[0];
                String homeScore = elements[5].split("</td>")[0];
                String margin = String.valueOf(Integer.valueOf(awayScore) - Integer.valueOf(homeScore));

                DateTimeFormatter format = DateTimeFormat.forPattern("MM/dd");
                DateTime gameDate = format.withLocale(Locale.ENGLISH).parseDateTime(date).withYear(thisPastMonday.getYear()).withHourOfDay(22);

                if (gameDate.getMillis() < thisPastMonday.getMillis() || gameDate.getMillis() > inAWeek.getMillis()) {
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
        return teamName.replaceAll(" St$"," State").replaceFirst("E ", "Eastern ").replaceFirst("C ", "Central ").replace("&amp;","&")
                .replace("FL ", "Florida ").replaceAll("Intl$", "International").replace("FIU","Florida International")
                .replaceAll(" St.$"," State").replace("<b>","").replace("</b>","").trim();
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
