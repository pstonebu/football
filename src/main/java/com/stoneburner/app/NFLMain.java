package com.stoneburner.app;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

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
    public static String input538 = "http://projects.fivethirtyeight.com/2016-nfl-predictions/";
    public static String inputSagarin = "http://sagarin.com/sports/nflsend.htm";
    public static String inputMassey = "http://www.masseyratings.com/predjson.php?s=nfl&dt=$dt$";
    public static String inputSpread = "http://www.vegasinsider.com/nfl/odds/offshore/2/";
    public static List<String> teamwords = new ArrayList<String>();
    public static HashMap<String,String> teamShortToLong = new HashMap<String,String>();

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

        //Instantiate client and method
        HttpClient client = new DefaultHttpClient();
        HttpGet method = new HttpGet(inputURIPR);

        //Execute client with our method
        try
        {


            HttpResponse response = client.execute(method);

            String source = EntityUtils.toString(response.getEntity());
            source = source.split("<h2>National Football League, Week [0-9]</h2")[1];
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
            source = source.split("<h4>NFL Game Predictions: Week [0-9]+</h4>")[1];
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

                //Grab spread, and favorite
                String homePoints = row2.split("<center>")[2].split("</center>")[0];
                String awayPoints = row1.split("<center>")[2].split("</center>")[0];

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

        //Instantiate client and method
        HttpClient client = new DefaultHttpClient();
        HttpGet method = new HttpGet(inputURIFox);

        //Execute client with our method
        try
        {

            HttpResponse response = client.execute(method);

            String source = EntityUtils.toString(response.getEntity());
            source = source.split("<table id=\"wisfb_basicTable\"")[1]
                    .replaceAll("\\n","").replaceAll("\\t","").replaceAll("\\r","")
                    .split("</table>")[0];

            String[] rows = Arrays.copyOfRange(source.split("<tr"), 2, source.split("<tr").length-1);

            for (int i = 0; i < rows.length; i ++) {
                String row = rows[i];
                String location = rows[i].split("<div class=\"wisfb_fullTeamStacked\">")[1].split("<td class=\"wisfb_oppCol\">")[1]
                        .split("<span class=\"wisfb_oppInd\">")[1].split("</span>")[0];
                String favorite = rows[i].split("<div class=\"wisfb_fullTeamStacked\">")[1].split("<span>")[1].split("</span>")[0].trim();
                String underdog = rows[i].split("<span class=\"wisfb_oppFull\">")[1].split("</span>")[0];
                String avgScore = rows[i].split("<a class=\"wisfb_avgScoreLink\"")[1].split("target=\"_blank\">")[1].split("</a>")[0];

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
                String home = homeIsFav ? favorite : underdog;
                String away = homeIsFav ? underdog : favorite;

                //Find a spot in our array for these values
                for (int j = 0; j < numGames; j++) {
                    double homeResult =  similarity(predictions[j][0], home);
                    double awayResult =  similarity(predictions[j][1], away);
                    if (homeResult == 1 || awayResult == 1) {
                        predictions[j][4] = (homeIsFav ? "-" : "") + prediction;
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

    public static void grab538() {
        System.out.println( "Fetching '" + input538 + "'");

        HttpClient client = new DecompressingHttpClient();
        HttpGet method = new HttpGet(input538);

        //Execute client with our method
        try
        {
            HttpResponse response = client.execute(method);

            String source = EntityUtils.toString(response.getEntity());

            String[] awayTeams = Arrays.copyOfRange(source.split("<tr class=\"away\">")[1].split("</tr>")[0]
                    .split("<td data-team=\""), 1, numGames+1);
            String[] homeTeams = Arrays.copyOfRange(source.split("<tr class=\"home\">")[1].split("</tr>")[0]
                    .split("<td data-team=\""), 1, numGames+1);
            String[] awaySpreads = Arrays.copyOfRange(source.split("<tr class=\"prob-row\">")[1].split("</tr>")[0]
                    .split("<td class=\"pct away\">"), 1, numGames+1);
            String[] homeSpreads = Arrays.copyOfRange(source.split("<tr class=\"prob-row\">")[1].split("<tr>")[1]
                    .split("</tr>")[0].split("<td class=\"pct home\">"), 1, numGames+1);

            for (int i = 0; i < numGames; i++) {
                String awayShort = awayTeams[i].split("\"")[0];
                String homeShort = homeTeams[i].split("\"")[0];
                String awaySpread = awaySpreads[i].split("<div class=\"spread fav\">").length > 1 ?
                    awaySpreads[i].split("<div class=\"spread fav\">")[1].split("</div>")[0] : null;
                String homeSpread = awaySpread == null ? homeSpreads[i].split("<div class=\"spread fav\">")[1].split("</div>")[0] : null;

                if (homeSpread != null && homeSpread.equals("PK")) {
                    homeSpread = "0.0";
                } else if (awaySpread != null && awaySpread.equals("PK")) {
                    awaySpread = "+0.0";
                }

                String away = teamShortToLong.get(awayShort);
                String home = teamShortToLong.get(homeShort);

                for (int j = 0; j < numGames; j++) {
                    double homeResult =  similarity(predictions[j][0], home);
                    double awayResult =  similarity(predictions[j][1], away);
                    if (homeResult == 1 || awayResult == 1) {
                        predictions[j][6] = awaySpread != null ? awaySpread.substring(1) : homeSpread;
                    }
                }
            }

        }

        catch (Exception e) {
            System.out.println("Exception occurred: " + e.getStackTrace() + e.toString());
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
                String away = (String)((JSONArray)current.get(2)).get(0);
                String home = ((String)((JSONArray)current.get(3)).get(0)).replace("@ ", "");
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

            String[] rows = Arrays.copyOfRange(html.split("<a href=\"#New_Feature\"><b>New_Feature</b></a></h2></font>")[1].split("\r\n"),
                    5, numGames+5);

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
                        if (teamwords.contains(elements[1].toLowerCase())) {
                            favorite = favorite + " " + elements[1];
                        }
                        favoritePieces++;

                        try {
                            Double.parseDouble(elements[2]);
                        } catch (NumberFormatException e) {
                            if (teamwords.contains(elements[2].toLowerCase())) {
                                favorite = favorite + " " + elements[2];
                            }
                            favoritePieces++;
                        }
                    }
                    Double averageSpread = (Double.valueOf(elements[favoritePieces]) + Double.valueOf(elements[favoritePieces+1]) +
                            Double.valueOf(elements[favoritePieces+2]) + Double.valueOf(elements[favoritePieces+3])) / 4.0;

                    String underdog = elements[favoritePieces+4];
                    if (StringUtils.isAllLowerCase(underdog)) {
                        if (StringUtils.isAllLowerCase(elements[favoritePieces+5])) {
                            if (teamwords.contains(elements[favoritePieces+5].toLowerCase())) {
                                underdog = underdog + " " + elements[favoritePieces + 5];

                                if (StringUtils.isAllLowerCase(elements[favoritePieces+6])) {
                                    if (teamwords.contains(elements[favoritePieces+6].toLowerCase())) {
                                        underdog = underdog + " " + elements[favoritePieces + 6];
                                    }
                                }
                            }
                        }
                    } else if (StringUtils.isAllUpperCase(underdog)) {
                        if (StringUtils.isAllUpperCase(elements[favoritePieces+5])) {
                            if (teamwords.contains(elements[favoritePieces+5].toLowerCase())) {
                                underdog = underdog + " " + elements[favoritePieces + 5];

                                if (StringUtils.isAllUpperCase(elements[favoritePieces+6])) {
                                    if (teamwords.contains(elements[favoritePieces+6].toLowerCase())) {
                                        underdog = underdog + " " + elements[favoritePieces + 6];
                                    }
                                }
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

                String teamOne = firstRow.split("<a href=\"")[1].split(">")[1].split("</a")[0];
                String teamTwo = firstRow.split("<a href=\"")[2].split(">")[1].split("</a")[0];

                String [] spreadParts;
                try {
                    spreadParts = Arrays.copyOfRange(HtmlEscape.unescapeHtml(firstRow.split("<a class=\"cellTextNorm\" href=\"")[firstRow.split("<a class=\"cellTextNorm\" href=\"").length - 1]
                            .split("_blank\">")[1].split("</a>")[0]).split("<br>"), 1, 3);
                } catch (IndexOutOfBoundsException ex) {
                    continue;
                }

                boolean teamOneIsFavorite;
                Double spread = null;
                if (spreadParts[0].startsWith("-")) {
                    teamOneIsFavorite = true;
                    if (StringUtils.isNotEmpty(spreadParts[0]) && !spreadParts[0].equals(" ")) {
                        String spreadString = spreadParts[0].split("-|\\+|EV")[1].replace("½", ".5").replace(" EV", "");
                        spread = Double.valueOf(spreadString.substring(0, spreadString.length() - 1));
                    }
                } else {
                    teamOneIsFavorite = false;
                    if (StringUtils.isNotEmpty(spreadParts[1]) && !spreadParts[1].equals(" ")) {
                        String spreadString = spreadParts[1].split("-|\\+|EV")[1].replace("½", ".5").replace(" EV", "");
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