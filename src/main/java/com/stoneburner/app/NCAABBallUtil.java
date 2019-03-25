package com.stoneburner.app;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;

import static com.google.common.collect.Maps.newHashMap;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static org.apache.commons.lang3.StringUtils.*;
import static org.apache.commons.text.StringEscapeUtils.unescapeHtml4;
import static org.apache.commons.text.WordUtils.capitalizeFully;
import static org.joda.time.DateTime.now;
import static org.joda.time.DateTimeZone.forID;
import static org.joda.time.format.DateTimeFormat.forPattern;

public class NCAABBallUtil extends Util {

    private int round = 0;
    private String pRHeader = "";
    private DateTime firstGame = new DateTime(2019, 3, 19, 16, 40, 0);
    private String kenPom = "https://kenpom.com/";
    private String gp = "https://gamepredict.us/games?date=%s&league=ncb";
    private String torvik = "http://barttorvik.com/tourneytime.php";
    private String inputMassey1 = "";
    private String inputMassey2 = "";

    public NCAABBallUtil() {

        if (now().isBefore(firstGame)) {
            pRHeader = "First Four Games";
        } else if (now().isBefore(firstGame.plusDays(2).withHourOfDay(9))) {
            round = 1;
            pRHeader = "Round of 64 games.";
        } else if (now().isBefore(firstGame.plusDays(4).withHourOfDay(9))) {
            round = 2;
            pRHeader = "Games on Sunday, March 24, 2019.";
        } else if (now().isBefore(firstGame.plusDays(9).withHourOfDay(15))) {
            round = 3;
            pRHeader = "Sweet 16 games.";
        }

        inputMassey1 = format(inputMasseyBlank, "cb", forPattern("yyyyMMdd").print(firstGame.plusDays(round == 0 ?
                0 : round == 1 ? 2 : round == 2 ? 4 : round == 3 ? 9 : round == 4 ? 11 : round == 5 ? 16 : 18)));
        inputMassey2 = format(inputMasseyBlank, "cb", forPattern("yyyyMMdd").print(firstGame.plusDays(round == 0 ?
                1 : round == 1 ? 3 : round == 2 ? 5 : round == 3 ? 10 : round == 4 ? 12 : round == 5 ? 17 : 20)));

        inputURIDR = "https://www.dratings.com/predictor/ncaa-basketball-predictions/";
        inputURIOS = format(inputURIOS, "ncaaf");
        inputSagarin = format(inputSagarin, "cb");
        inputSpread = format(inputSpread, "college-basketball", "offshore");
        input538 = "https://projects.fivethirtyeight.com/march-madness-api/2019/fivethirtyeight_ncaa_forecasts.csv";

        teamToId.put("Auburn", 2);
        teamToId.put("Arizona State", 9);
        teamToId.put("Yale", 43);
        teamToId.put("Florida State", 52);
        teamToId.put("Florida", 57);
        teamToId.put("Iowa State", 66);
        teamToId.put("Bradley", 71);
        teamToId.put("Murray State", 93);
        teamToId.put("Northern Kentucky", 94);
        teamToId.put("Kentucky", 96);
        teamToId.put("Louisville", 97);
        teamToId.put("LSU", 99);
        teamToId.put("Northeastern", 111);
        teamToId.put("Maryland", 120);
        teamToId.put("Michigan State", 127);
        teamToId.put("Michigan", 130);
        teamToId.put("Minnesota", 135);
        teamToId.put("Saint Louis", 139);
        teamToId.put("Mississippi", 145);
        teamToId.put("Montana", 149);
        teamToId.put("Duke", 150);
        teamToId.put("North Carolina", 153);
        teamToId.put("Fairleigh Dickinson", 161);
        teamToId.put("New Mexico State", 166);
        teamToId.put("Syracuse", 183);
        teamToId.put("Ohio State", 194);
        teamToId.put("Oklahoma", 201);
        teamToId.put("Temple", 218);
        teamToId.put("Villanova", 222);
        teamToId.put("Baylor", 239);
        teamToId.put("Houston", 248);
        teamToId.put("Virginia", 258);
        teamToId.put("Virginia Tech", 259);
        teamToId.put("Vermont", 261);
        teamToId.put("Washington", 264);
        teamToId.put("Marquette", 269);
        teamToId.put("Wisconsin", 275);
        teamToId.put("Old Dominion", 295);
        teamToId.put("UC-Irvine", 300);
        teamToId.put("Iona", 314);
        teamToId.put("Utah State", 328);
        teamToId.put("Mississippi State", 344);
        teamToId.put("Abilene Christian", 2000);
        teamToId.put("Belmont", 2057);
        teamToId.put("Buffalo", 2084);
        teamToId.put("Central Florida", 2116);
        teamToId.put("Cincinnati", 2132);
        teamToId.put("Colgate", 2142);
        teamToId.put("Gardner-Webb", 2241);
        teamToId.put("Georgia State", 2247);
        teamToId.put("Gonzaga", 2250);
        teamToId.put("Iowa", 2294);
        teamToId.put("Kansas", 2305);
        teamToId.put("Kansas State", 2306);
        teamToId.put("Liberty", 2335);
        teamToId.put("North Carolina Central", 2428);
        teamToId.put("Nevada", 2440);
        teamToId.put("North Dakota State", 2449);
        teamToId.put("Oregon", 2483);
        teamToId.put("Prairie View", 2504);
        teamToId.put("Purdue", 2509);
        teamToId.put("Seton Hall", 2550);
        teamToId.put("St John's", 2599);
        teamToId.put("Saint Mary's", 2608);
        teamToId.put("Tennessee", 2633);
        teamToId.put("Texas Tech", 2641);
        teamToId.put("Virginia Commonwealth", 2670);
        teamToId.put("Wofford", 2747);
    }

    public void grabPowerRank() {
        log("Fetching '" + inputURIPR + "'");
        int numGames = 0;

        //Execute client with our method
        try {
            Element current = getPowerRankCurrent(connect(inputURIPR));

            while (current.nextElementSibling() != null) {
                current = current.nextElementSibling();
                if (!current.tagName().equals("p") && !current.className().equals("wp-block-image")) {
                    break;
                } else if (!current.child(0).tagName().equals("strong")) {
                    continue;
                }

                String teamsString = current.select("strong").get(0).childNode(0).toString().replaceAll("[0-9]*", "").replaceAll("\\.", "").trim();
                String teamOne = cleanTeamName(teamsString.split("(\\bat\\b|\\bversus\\b)")[0].trim());
                String teamTwo = cleanTeamName(teamsString.split("(\\bat\\b|\\bversus\\b)")[1].trim());

                String summary = unescapeHtml4(current.childNodes().get(2).toString()).replaceFirst("\\. ", " ");
                String favorite = null;
                String underdog = null;
                if (summary.startsWith(teamOne) || cleanTeamName(summary).startsWith(teamOne)) {
                    favorite = teamOne;
                    underdog = teamTwo;
                } else if (summary.startsWith(teamTwo) || cleanTeamName(summary).startsWith(teamTwo)) {
                    favorite = teamTwo;
                    underdog = teamOne;
                } else {
                    log("Uh oh, couldn't find either team in the summary.");
                    continue;
                }
                String firstPart = summary.split("%")[0];
                Double winPct = Double.valueOf(firstPart.substring(firstPart.length()-2)) / 100.0;
                if (winPct < .5) {
                    winPct = 1.0 - winPct;
                }

                NCAABBallGame game = getNewGame();
                Integer homeId = teamToId.get(favorite);
                if (homeId == null) {
                    log("Could not find " + favorite + " in our map!");
                }
                Integer awayId = teamToId.get(underdog);
                if (awayId == null) {
                    log("Could not find " + underdog + " in our map!");
                }
                game.setHome(favorite);
                game.setAway(underdog);
                game.setPowerRank(String.valueOf(winPct));
                game.setPlayIn(round == 0);
                games.add(game);
                idToGame.put(homeId, game);
                idToGame.put(awayId, game);
            }
        } catch (Exception e) {
            logAndExit(e);
        }
    }

    protected void grab538() {
        log("Fetching '" + input538 + "'");

        //Execute client with our method
        try {
            String[] rows = connect(input538).body().html().split("(?<=(\\d|team_slot)[ ])");

            for (int i = 1; i < rows.length; i++) {
                String[] rowParts = rows[i].trim().split(",");
                String teamName = cleanTeamName(rowParts[13]);
                int teamId = Integer.valueOf(rowParts[12]);
                Game game = idToGame.get(teamId);
                if (game != null) {
                    NCAABBallGame ballGame = (NCAABBallGame)game;
                    if (isBlank(game.getFiveThirtyEight())) {
                        boolean teamIsFavorite = teamName.equals(game.getHome());
                        Double winPct = Double.valueOf(rowParts[round+3]);
                        game.setFiveThirtyEight(String.valueOf(teamIsFavorite ? winPct : (1.0-winPct)));
                    }
                }
            }
        } catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabKenPom() {
        log("Fetching '" + kenPom + "'");

        HashMap<String, Elements> teamToRow = newHashMap();
        //Execute client with our method
        try {
            Elements teamRows = connect(kenPom).select("tr[class=tourney], tr[class=tourney bold-bottom]");
            for (Element tourneyTeam : teamRows) {
                Elements tds = tourneyTeam.select("td");
                String teamName = cleanTeamName(tds.get(1).select("a").text());
                if (teamToId.containsKey(teamName)) {
                    teamToRow.put(teamName, tds);
                } else {
                    log("Didn't match: " + teamName);
                }
            }

            games.parallelStream()
                    .map(NCAABBallGame.class::cast)
                    .filter(g -> !isNotEmpty(g.getKenPom()))
                    .forEach(g -> {
                        Elements favorite = teamToRow.get(g.getHome());
                        Elements underdog = teamToRow.get(g.getAway());
                        if (favorite != null && underdog != null) {
                            Double adjEmFav = Double.valueOf(favorite.get(4).text().substring(1));
                            Double adjEmUnd = Double.valueOf(underdog.get(4).text().substring(1));
                            Double adjTempoFav = Double.valueOf(favorite.get(9).text());
                            Double adjTempoUnd = Double.valueOf(underdog.get(9).text());
                            Double pointDiff = (adjEmFav - adjEmUnd) * (adjTempoFav + adjTempoUnd) / 200.0;
                            double winPct = (1.0 - new NormalDistribution(pointDiff, 11.0).cumulativeProbability(0));
                            g.setKenPom(String.valueOf(winPct));
                        } 
            });
        } catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabGamePredict() {
        ArrayList<String> urls = null;
        if (round == 0) {
            urls = new ArrayList<String>(asList(format(gp, forPattern("yyyyMMdd").print(firstGame)), format(gp, forPattern("yyyyMMdd").print(firstGame.plusDays(1)))));
        } else if (round == 1) {
            urls = new ArrayList<String>(asList(format(gp, forPattern("yyyyMMdd").print(firstGame.plusDays(2))), format(gp, forPattern("yyyyMMdd").print(firstGame.plusDays(3)))));
        } else if (round == 2) {
            urls = new ArrayList<String>(asList(format(gp, forPattern("yyyyMMdd").print(firstGame.plusDays(4))), format(gp, forPattern("yyyyMMdd").print(firstGame.plusDays(5)))));
        } else if (round == 3) {
            urls = new ArrayList<String>(asList(format(gp, forPattern("yyyyMMdd").print(firstGame.plusDays(9))), format(gp, forPattern("yyyyMMdd").print(firstGame.plusDays(10)))));
        }

        for (String url : urls) {
            log("Fetching '" + url + "'");

            //Execute client with our method
            try {
                Elements tables = connect(url).select("tbody[id=teams]");
                for (int i = 1; i < tables.size(); i++) {
                    Element row = tables.get(i).select("tr").get(0);
                    String teamName = cleanTeamName(row.select("a").text());
                    String winPctString = row.select("td[class=gp]").text();
                    if (isNotEmpty(winPctString)) {
                        Double winPct = Double.valueOf(winPctString.substring(0, winPctString.indexOf("%"))) / 100.0;
                        NCAABBallGame game = (NCAABBallGame)idToGame.get(teamToId.get(teamName));
                        if (game != null && isEmpty(game.getGamePredict())) {
                            boolean teamIsFavorite = teamName.equals(game.getHome());
                            game.setGamePredict(String.valueOf(teamIsFavorite ? winPct : (1.0-winPct)));
                        }
                    }
                }
            } catch (Exception e) {
                logAndExit(e);
            }
        }
    }

    public void grabBartTorvik() {
        log("Fetching '" + torvik + "'");

        //Execute client with our method
        try {
            Elements rows = connect(torvik).select("tbody").get(0).select("tr");
            for (Element row : rows) {
                String teamName = cleanTeamName(row.select("td").get(2).text());
                NCAABBallGame game = (NCAABBallGame)idToGame.get(teamToId.get(teamName));
                if (game != null && isEmpty(game.getTorvik())) {
                    try {
                        Double winPct = Double.valueOf(row.select("td").get(4 + round).text()) / 100.0;
                        boolean teamIsFavorite = teamName.equals(game.getHome());
                        game.setTorvik(String.valueOf(teamIsFavorite ? winPct : (1.0-winPct)));
                    } catch (NumberFormatException e) {
                        log("Game was probably already played: " + game.getHome() + " vs. " + game.getAway());
                    }
                }
            }
        } catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabDRatings() {
        log( "Fetching '" + inputURIDR + "'");

        try
        {
            Elements rows = connect(inputURIDR).select("table[class=small-text]").get(0).select("tr");

            for (int i = 1; i < rows.size(); i = i + 2) {
                Elements tds = rows.get(i).select("td");
                String teamName = cleanTeamName(tds.get(3).text());
                NCAABBallGame game = (NCAABBallGame)idToGame.get(teamToId.get(teamName));
                if (game != null && isEmpty(game.getDRatings())) {
                    boolean teamIsFavorite = teamName.equals(game.getHome());
                    Double winPct = Double.valueOf(tds.get(4).text().replace("%","")) / 100.0;
                    game.setDRatings(String.valueOf(teamIsFavorite ? winPct : (1.0-winPct)));
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabMassey() {

        for (String url : new ArrayList<String>(asList(inputMassey1, inputMassey2))) {
            log( "Fetching '" + url + "'");

            //Execute client with our method
            try
            {
                String source = connect(url).body().text();

                JSONObject json = new JSONObject(source);
                JSONArray gamesArray = json.getJSONArray("DI");

                for (Object currentGame : gamesArray) {
                    JSONArray current = (JSONArray)currentGame;
                    String teamName = cleanTeamName(current.getJSONArray(2).getString(0));
                    NCAABBallGame game = (NCAABBallGame)idToGame.get(teamToId.get(teamName));
                    if (game != null && isEmpty(game.getMassey())) {
                        DateTime gameDate = null;
                        try {
                            gameDate = forPattern("MM.dd.yyyy hh:mma").parseDateTime(
                                    current.getJSONArray(0).getString(0).substring(4) +
                                            ".2019 " + current.getJSONArray(1).getString(0).substring(0, 7).trim())
                                    .withZoneRetainFields(forID("America/New_York"));
                        } catch (IllegalArgumentException | StringIndexOutOfBoundsException e) {
                            log("Something messed up with the input date: " + e);
                            continue;
                        }
                        boolean teamIsFavorite = teamName.equals(game.getHome());
                        Double winPct = current.getJSONArray(10).getDouble(0) / 100.0;
                        game.setMassey(String.valueOf(teamIsFavorite ? winPct : (1.0-winPct)));
                        DateTime firstSessionCutoff = new DateTime(gameDate).withHourOfDay(14).withMinuteOfHour(41);
                        DateTime secondSessionCutoff = new DateTime(gameDate).withHourOfDay(17).withMinuteOfHour(0);
                        DateTime thirdSessionCutoff = new DateTime(gameDate).withHourOfDay(20).withMinuteOfHour(0);
                        game.setSession(gameDate.dayOfWeek().getAsText() +
                                (gameDate.isBefore(firstSessionCutoff) ? " 1st" :
                                 gameDate.isBefore(secondSessionCutoff) ? " 2nd" :
                                 gameDate.isBefore(thirdSessionCutoff) ? " 3rd" : " 4th"));
                    }
                }
            }

            catch (Exception e) {
                logAndExit(e);
            }
        }
    }

    public void grabSagarin() {
        log( "Fetching '" + inputSagarin + "'");

        try
        {
            Document page = connect(inputSagarin);
            Node predictionSection = page.select(sagarinPredictionQuery).get(0).parent().parent().parent().childNode(2);
            String[] rows = copyOfRange(predictionSection.toString().split("\r\n"), 8,
                    predictionSection.toString().split("\r\n").length);

            //iterate through list of games
            for (int j = 0; j < rows.length; j++) {
                String currentRow = unescapeHtml4(rows[j]);
                if (currentRow.startsWith("=====") || isBlank(currentRow) || currentRow.contains("eigen")) {
                    break;
                }

                String cleanedFavorite = cleanTeamName(capitalizeFully(currentRow.substring(4, 27).trim()));
                NCAABBallGame game = (NCAABBallGame)idToGame.get(teamToId.get(cleanedFavorite));
                if (game != null && isEmpty(game.getSagarin())) {
                    boolean teamIsFavorite = cleanedFavorite.equals(game.getHome());
                    Double winPct = Double.valueOf(currentRow.substring(86, 89).trim()) / 100.0;
                    game.setSagarin(String.valueOf(teamIsFavorite ? winPct : (1.0-winPct)));
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    @Override
    public Element getPowerRankCurrent(Document page) {
        return page.select("h3").stream()
                .filter(e -> e.toString().contains(pRHeader))
                .findFirst()
                .orElse(null);
    }

    protected String cleanTeamName(String teamName) {
        return teamName.replaceAll(" St(.)?$"," State").replaceAll("^E ", "Eastern ").replaceAll("^C ", "Central ").replaceAll("^Miss. ", "Mississippi ")
                .replaceAll("&amp;","&").replaceAll("<b>","").replace("</b>","").replaceAll("i(`|')i", "ii").replaceAll("^Mich. ", "Michigan ")
                .replaceAll("^W ", "Western ").replace("A&m", "A&M").replace(" AM"," A&M").replaceAll("\\(ucf\\)$", "").replaceAll("^S\\. ", "South ")
                .replaceAll(" (CA)", "").replaceAll(" (NY)", "").replaceAll("’", "'").replaceAll("^N ", "North ")
                //team specific cleanup
                .replaceAll("Army West Point", "Army")
                .replaceAll("^BC$", "Boston College")
                .replaceAll("Bowling Green State", "Bowling Green")
                .replaceAll("BYU", "Brigham Young")
                .replaceAll("SUNY-Buffalo", "Buffalo")
                .replaceAll("^Cal$", "California")
                .replaceAll("UCF", "Central Florida")
                .replaceAll("UNC( |-)Charlotte", "Charlotte")
                .replaceAll("^Coastal Car$", "Coastal Carolina")
                .replaceAll("UConn", "Connecticut")
                .replaceAll("FL Atlantic", "Florida Atlantic")
                .replaceAll("(FIU|Florida Int(')?l|Fla. International)", "Florida International")
                .replaceAll("Ga Southern", "Georgia Southern")
                .replaceAll("^Kent$", "Kent State")
                .replaceAll("^(ULM|Louisiana-([Mm])onroe|UL([- ])Monroe)$","Louisiana Monroe")
                .replaceAll("((UL|Louisiana)([ -])([Ll])afayette|ULL)", "Louisiana")
                .replaceAll("La. Tech", "Louisiana Tech")
                .replaceAll("Louisiana State", "LSU")
                .replaceAll("UMass", "Massachusetts")
                .replaceAll("(Miami( |-)(FL|florida)|^Miami$)", "Miami (FL)")
                .replaceAll("Miami( |-)(OH|ohio)", "Miami (OH)")
                .replaceAll("(Middle Tennessee State|MTSU)", "Middle Tennessee")
                .replaceAll("N'western", "Northwestern")
                .replaceAll("Ole Miss", "Mississippi")
                .replaceAll("N(\\.?)([Cc])(\\.?) State", "North Carolina State")
                .replaceAll("N Illinois", "Northern Illinois")
                .replaceAll("Ohio U.", "Ohio")
                .replaceAll("Okla. State", "Oklahoma State")
                .replaceAll("SDSU", "San Diego State")
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
                .replaceAll("Wash. State", "Washington State")
                .replaceAll("W. Virginia", "West Virginia")
                .replaceAll("WKU", "Western Kentucky")
                .replace("Farleigh", "Fairleigh")
                .replace("F Dickinson", "Fairleigh Dickinson")
                .replace("Prairie View A&M", "Prairie View")
                .replace("St. John's (NY)", "St John's")
                .replace("St. John's", "St John's")
                .replace("St. Johns", "St John's")
                .replace("St. Louis", "Saint Louis")
                .replace("St Louis", "Saint Louis")
                .replace("St Mary's", "Saint Mary's")
                .replace("St. Marys (CA)", "Saint Mary's")
                .replace("UC Irvine", "UC-Irvine")
                .replace("VCU", "Virginia Commonwealth")
                .replace("VA Commonwealth", "Virginia Commonwealth")
                .replace("Gardner Webb", "Gardner-Webb")
                .replace("N Kentucky", "Northern Kentucky")
                .replace("North Kentucky", "Northern Kentucky")
                .replaceAll("^Abilene Chr$", "Abilene Christian")
                .replace("Lsu", "LSU")
                .replace("NC Central", "North Carolina Central")
                .trim();
    }

    protected NCAABBallGame getNewGame() {
        return new NCAABBallGame();
    }
}
