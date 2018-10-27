package com.stoneburner.app;

import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Double.valueOf;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.text.StringEscapeUtils.unescapeHtml4;
import static org.apache.commons.text.WordUtils.capitalizeFully;

@NoArgsConstructor
public class Util {
    protected String inputURIPR = "https://thepowerrank.com/predictions/";
    protected String inputURIDR = "http://www.dratings.com/predictor/%s-football-predictions/";
    protected String inputURIOS = "https://www.oddsshark.com/%s/computer-picks";
    protected String inputURIFox = "";
    protected String inputSagarin = "http://sagarin.com/sports/%ssend.htm";
    protected String inputMassey = "http://www.masseyratings.com/predjson.php?s=%s&sub=11604&dt=%s";
    protected String inputSpread = "http://www.vegasinsider.com/%s/odds/%s/2/";
    protected String input538 = "";

    protected HashMap<String,String> teamMascotToCity = newHashMap();
    protected HashMap<String,Integer> teamToId = newHashMap();
    protected List<Game> games = newArrayList();
    protected HashMap<Integer,Game> idToGame = newHashMap();

    protected DateTime today = new DateTime();
    protected boolean isVegasWeek = false;

    public void grabPowerRank() {
        log( "Fetching '" + inputURIPR + "'");
        int numGames = 0;

        //Execute client with our method
        try
        {
            Document page = connect(inputURIPR);
            Element leagueHeader = page.select("h2").stream()
                    .filter(e -> e.toString().contains(isNfl() ? "NFL" : "College Football"))
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
                String away = cleanTeamName(teamsString.split("(\\bat\\b|\\bversus\\b)")[0].trim());
                String home = cleanTeamName(teamsString.split("(\\bat\\b|\\bversus\\b)")[1].trim());

                String summary = unescapeHtml4(current.childNodes().get(2).toString());
                boolean negative = summary.startsWith(home);
                String[] spreadParts = summary.split("\\.");
                String spreadTail = spreadParts[1].split(" ")[0];
                String spreadHead = spreadParts[0].split(" ")[spreadParts[0].split(" ").length - 1];

                String spread = (negative ? "-" : "") + spreadHead + "." + spreadTail;

                Game game = getNewGame();
                Integer homeId = teamToId.get(home);
                if (homeId == null) {
                    log("Could not find " + home + " in our map!");
                }
                Integer awayId = teamToId.get(away);
                if (awayId == null) {
                    log("Could not find " + away + " in our map!");
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

    protected void grab538() {
        return;
    }

    public void grabSpread() {
        log( "Fetching '" + inputSpread + "'");

        try
        {
            Document page = connect(inputSpread);
            Elements rows = page.select("table[class=frodds-data-tbl] tr");

            for (int i = 0; i < rows.size(); i++) {
                Element current = rows.get(i);
                String cssQuery = new StringBuilder("a[href$=#").append(isVegasWeek ? "E" : "BU").append("]").toString();
                Elements fiveDimes = current.select(cssQuery);
                if (fiveDimes.size() == 0) {
                    //if this is just an info row or there's no spread posted, move on
                    continue;
                }

                String teamOne = cleanTeamName(current.select("a[class=tabletext]").get(0).childNode(0).toString());
                String teamTwo = cleanTeamName(current.select("a[class=tabletext]").get(1).childNode(0).toString());

                List<String> spreadParts = current.select(cssQuery).get(0).childNodes().stream()
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
                    Integer teamOneId = teamToId.get(teamOne);
                    Integer teamTwoId = teamToId.get(teamTwo);
                    if (teamOneId != null && teamTwoId != null) {
                        Game game = idToGame.get(teamOneId);
                        if (isCorrectGame(game, teamOne, teamTwo)) {
                            if (game.getHome().equals(teamOne)) {
                                game.setSpread(String.valueOf((teamOneIsFavorite ? -1.0 : 1.0) * spread));
                            } else if (game.getAway().equals(teamOne)) {
                                game.setSpread(String.valueOf((teamOneIsFavorite ? 1.0 : -1.0) * spread));
                            }
                        }
                    } else {
                        logBadTeam(teamOne, teamTwo);
                    }
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabSagarin() {
        log( "Fetching '" + inputSagarin + "'");

        try
        {
            Document page = connect(inputSagarin);
            Node predictionSection = page.select("a[name=Predictions]").get(0).parent().parent().parent().childNode(2);
            String[] rows = copyOfRange(predictionSection.toString().split("\r\n"), 8,
                    predictionSection.toString().split("\r\n").length);

            //iterate through list of games
            for (int j = 0; j < rows.length; j++) {
                String currentRow = unescapeHtml4(rows[j]);
                if (currentRow.startsWith("=====") || isBlank(currentRow) || currentRow.contains("eigen")) {
                    break;
                }

                String favorite = currentRow.substring(4, 27).trim();
                String cleanedFavorite = removeMascot(cleanTeamName(capitalizeFully(favorite)));
                String underdog = currentRow.substring(59).trim();
                String cleanedUnderdog = removeMascot(cleanTeamName(capitalizeFully(underdog)));

                String[] spreads = currentRow.substring(27, 59).trim().split("\\s+");
                double averageSpread = asList(spreads).stream().mapToDouble(Double::valueOf).average().getAsDouble();

                boolean isNeutral = favorite.equals(favorite.toLowerCase()) && underdog.equals(underdog.toLowerCase());

                Integer favoriteId = teamToId.get(cleanedFavorite);
                Integer underdogId = teamToId.get(cleanedUnderdog);

                if (favoriteId != null && underdogId != null) {
                    Game game = idToGame.get(favoriteId);
                    if (isCorrectGame(game, cleanedFavorite, cleanedUnderdog)) {
                        game.setSagarin(String.valueOf((game.getHome().equals(cleanedFavorite) ? -1.0 : 1.0) * averageSpread));
                    }
                } else {
                    logBadTeam(cleanedFavorite, cleanedUnderdog);
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabMassey() {
        log( "Fetching '" + inputMassey + "'");

        //Execute client with our method
        try
        {
            String source = connect(inputMassey).body().text();

            JSONObject json = new JSONObject(source);
            JSONArray gamesArray = json.getJSONArray("DI");

            for (Object currentGame : gamesArray) {
                JSONArray current = (JSONArray)currentGame;
                String away = cleanTeamName(current.getJSONArray(2).getString(0));
                String home = cleanTeamName(current.getJSONArray(3).getString(0).replace("@ ", ""));
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

                Integer homeId = teamToId.get(home);
                Integer awayId = teamToId.get(away);

                if (homeId != null && awayId != null) {
                    Game game = idToGame.get(homeId);
                    if (isCorrectGame(game, away, home)) {
                        game.setMassey(String.valueOf((game.getHome().equals(home) ? 1.0 : -1.0) * spread));
                    }
                } else {
                    logBadTeam(home, away);
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabDRatings() {
        log( "Fetching '" + inputURIDR + "'");

        try
        {
            Document page = connect(inputURIDR);
            Elements rows = page.select("table[class=small-text]").get(isNfl() ? 0 : 1).select("tr");

            for (int i = 2; i < rows.size(); i = i + 2) {
                Element rowOne = rows.get(i);
                Element rowTwo = rows.get(i+1);

                String away = cleanTeamName(rowOne.select("td").get(2).childNodes().get(0).toString());
                String home = cleanTeamName(rowTwo.select("td").get(0).childNodes().get(0).toString());

                //Grab spread, and favorite
                String homePoints = rowTwo.select("td").get(5).childNodes().get(0).childNodes().get(0).toString().trim();
                String awayPoints = rowOne.select("td").get(7).childNodes().get(0).childNodes().get(0).toString().trim();

                double margin = valueOf(awayPoints) - valueOf(homePoints);

                Integer homeId = teamToId.get(home);
                Integer awayId = teamToId.get(away);
                if (homeId != null && awayId != null) {
                    Game game = idToGame.get(homeId);
                    if (isCorrectGame(game, away, home)) {
                        game.setDRatings(String.valueOf((game.getHome().equals(home) ? 1.0 : -1.0) * margin));
                    }
                } else {
                    logBadTeam(home, away);
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabFox() {
        log( "Fetching '" + inputURIFox + "'");

        try
        {
            Document page = connect(inputURIFox);
            Elements gamesElements = page.select("div[class=wisbb_predictionChip]");

            for (Element gameElement : gamesElements) {
                Elements teams = gameElement.select("span[class=wisbb_teamName]");
                if (teams.size() == 0) {
                    continue;
                }

                String away = cleanTeamName(teams.get(0).childNode(0).toString());
                String home = cleanTeamName(teams.get(1).childNode(0).toString());

                away = teamMascotToCity.get(away);
                home = teamMascotToCity.get(home);

                String score = gameElement.select("span[class=wisbb_predData]").get(0).childNodes().get(0).toString();
                Double prediction = getDoublePredictionFromString(score);

                Integer homeId = teamToId.get(home);
                Integer awayId = teamToId.get(away);

                if (homeId != null && awayId != null) {
                    Game game = idToGame.get(homeId);
                    if (isCorrectGame(game, away, home)) {
                        game.setFox(String.valueOf((game.getHome().equals(home) ? 1.0 : -1.0) * prediction));
                    }
                } else {
                    logBadTeam(home, away);
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    public void grabOddsShark() {
        log( "Fetching '" + inputURIOS + "'");

        try
        {
            Document page = connect(inputURIOS);
            Elements gamesElements = page.select("table");

            for (int i = 0; i < gamesElements.size(); i++) {
                Element gameElement = gamesElements.get(i);
                Elements teams = gameElement.getElementsByClass("name-long");
                if (teams.size() != 2) {
                    continue;
                }
                if (gameElement.toString().contains("Results")) {
                    break;
                }
                String away = cleanTeamName(teams.get(0).text().trim());
                String home = cleanTeamName(teams.get(1).text().trim());
                String prediction = gameElement.select("td").get(1).childNode(0).toString();

                Double margin = getDoublePredictionFromString(prediction);

                Integer homeId = teamToId.get(home);
                Integer awayId = teamToId.get(away);
                if (homeId != null && awayId != null) {
                    Game game = idToGame.get(homeId);
                    if (isCorrectGame(game, away, home)) {
                        game.setOddsShark(String.valueOf((game.getHome().equals(home) ? 1.0 : -1.0) * margin));
                    }
                } else {
                    logBadTeam(home, away);
                }
            }
        }

        catch (Exception e) {
            logAndExit(e);
        }
    }

    protected String removeMascot(String city) {
        return null;
    }

    protected String cleanTeamName(String teamName) {
        return null;
    }

    protected String getCityFromMascot(String mascot) {
        return null;
    }

    protected Boolean isNfl() {
        return this instanceof NFLUtil;
    }

    protected <T extends Game> T getNewGame() {
        return null;
    }

    public void printResults() {
        try {
            File file = new File(format("/Users/patrick.stoneburner/Desktop/%s_picks.csv", isNfl() ? "nfl" : "ncaa"));
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

    protected void logAndExit(Exception e) {
        e.printStackTrace(System.out);
        System.exit(0);
    }

    protected void logBadTeam(String one, String two) {
        log("Unable to locate either " + one + " or " + two);
    }

    protected Document connect(String url) {
        try {
            return Jsoup.connect(url).timeout(0).maxBodySize(0).ignoreContentType(true).get();
        } catch (IOException e) {
            log("Error getting connection from: " + url);
            return null;
        }
    }
    
    protected void log(String message) {
        System.out.println(message);
    }

    protected boolean isCorrectGame(Game game, String expectedAway, String expectedHome) {
        if (game == null) {
            return false;
        }
        String actualAway = game.getAway();
        String actualHome = game.getHome();
        return (actualHome.equals(expectedHome) && actualAway.equals(expectedAway)) ||
                (actualAway.equals(expectedHome) && actualHome.equals(expectedAway));
    }

    protected Double getDoublePredictionFromString(String score) {
        try {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            Object result = engine.eval(score);
            double prediction;
            if (result instanceof Integer) {
                prediction = (double) ((Integer) result);
            } else {
                prediction = (double) result;
            }
            return prediction;
        } catch (ScriptException e) {
            return null;
        }
    }

    protected Double getPredictionFromWinPct(double winPct) {
        Double spread = null;
        if (winPct < 50.0) {
            winPct = 100.0 - winPct;
            spread = pow((winPct / 49.25), (1.0/.194)) * -1.0;
        } else {
            spread = pow((winPct / 49.25), (1.0/.194));
        }
        return spread;
    }
}
