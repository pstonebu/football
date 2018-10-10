package com.stoneburner.app;

import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static java.lang.Double.valueOf;
import static java.lang.Math.abs;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.Locale.ENGLISH;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.text.StringEscapeUtils.unescapeHtml4;
import static org.apache.commons.text.WordUtils.capitalizeFully;
import static org.jsoup.Jsoup.connect;

@NoArgsConstructor
public class Util {
    protected String inputURIPR = "https://thepowerrank.com/predictions/";
    protected String inputURIDRNCAA = "http://www.dratings.com/predictor/ncaa-football-predictions/";
    protected String inputURIDRNFL = "http://www.dratings.com/predictor/nfl-football-predictions/";
    protected String inputURIOSNCAA = "https://www.oddsshark.com/ncaaf/computer-picks";
    protected String inputURIOSNFL = "http://www.oddsshark.com/nfl/computer-picks";
    protected String inputURIFoxNCAA = "https://www.foxsports.com/college-football/predictions?season=2018&seasonType=1&week=%d&group=-3";
    protected String inputURIFoxNFL = "http://www.foxsports.com/nfl/predictions";
    protected String inputSP = "https://www.footballstudyhall.com/pages/2018-%team%-advanced-statistical-profile";
    protected String inputSPSheet = "https://docs.google.com/spreadsheets/d/e/2PACX-1vTNXgxlcihtmzIbzHDsQH5CXI6aSXfsZzWB7E8IC0sf4CaMsgP_p4DRSwx6TtoektFRCL3wO5m64JLB/pubhtml";
    protected String inputSagarinNCAA = "http://sagarin.com/sports/cfsend.htm";
    protected String inputSagarinNFL = "http://sagarin.com/sports/nflsend.htm";
    protected String inputMasseyNCAA = "http://www.masseyratings.com/predjson.php?s=cf&sub=11604&dt=$dt$";
    protected String inputMasseyNFL = "http://www.masseyratings.com/predjson.php?s=nfl&dt=$dt$";
    protected String inputSpreadNCAA = "http://www.vegasinsider.com/college-football/odds/offshore/2/";
    protected String inputSpreadNFL = "http://www.vegasinsider.com/nfl/odds/offshore/2/";
    protected String input538NCAA = "https://projects.fivethirtyeight.com/2018-college-football-predictions/sims.csv";
    protected String input538NFL = "https://projects.fivethirtyeight.com/2018-nfl-predictions/games/";
    protected String inputAtomic = "http://www.atomicfootball.com/archive/af_predictions_All.html";

    protected HashMap<String,String> teamMascotToCity = newHashMap();
    protected HashMap<String,Integer> teamToId = newHashMap();
    private List<Game> games = newArrayList();
    protected HashMap<Integer,Game> idToGame = newHashMap();

    public void grabPowerRank() {
        System.out.println( "Fetching '" + inputURIPR + "'");
        int numGames = 0;

        //Execute client with our method
        try
        {
            Document page = connect(inputURIPR).get();
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
                    game.setAtomic(margin);
                } else {
                     logBadTeam(away, home);
                }
            }
        } catch (Exception e) {
            logAndExit(e);
        }
    }

    protected void grab538() {
        return;
    }

    public void grabSpread() {
        String url = isNfl() ? inputSpreadNFL : inputSpreadNCAA;
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

                String teamOne = cleanTeamName(current.select("a[class=tabletext]").get(0).childNode(0).toString());
                String teamTwo = cleanTeamName(current.select("a[class=tabletext]").get(1).childNode(0).toString());

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
                    Integer teamOneId = teamToId.get(teamOne);
                    Integer teamTwoId = teamToId.get(teamTwo);
                    if (teamOneId != null && teamTwoId != null) {
                        Game game = idToGame.get(teamOneId);
                        if (game.getHome().equals(teamOne)) {
                            game.setSpread(String.valueOf((teamOneIsFavorite ? -1.0 : 1.0) * spread));
                        } else if (game.getAway().equals(teamOne)){
                            game.setSpread(String.valueOf((teamOneIsFavorite ? 1.0 : -1.0) * spread));
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
        String url = isNfl() ? inputSagarinNFL : inputSagarinNCAA;
        System.out.println( "Fetching '" + url + "'");

        try
        {
            Document page = connect(url).get();
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
                    game.setSagarin(String.valueOf((game.getHome().equals(cleanedFavorite) ? -1.0 : 1.0) * averageSpread));
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
        String url = isNfl() ? inputMasseyNFL : inputMasseyNCAA;
        System.out.println( "Fetching '" + url + "'");

        //Execute client with our method
        try
        {
            String source = connect(url).ignoreContentType(true).get().body().text();

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
                    game.setMassey(String.valueOf((game.getHome().equals(home) ? 1.0 : -1.0) * spread));
                } else {
                    logBadTeam(home, away);
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

                Integer homeId = teamToId.get(cleanTeamName(home));
                Integer awayId = teamToId.get(cleanTeamName(away));

                if (homeId != null && awayId != null) {
                    NCAAGame game = (NCAAGame)idToGame.get(homeId);
                    if (game != null && game.getHome().equals(home) && game.getAway().equals(away)) {
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

    public void grabDRatings() {
        String url = isNfl() ? inputURIDRNFL : inputURIDRNCAA;
        System.out.println( "Fetching '" + url + "'");

        try
        {
            Document page = connect(url).get();
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
                    game.setDRatings(String.valueOf((game.getHome().equals(home) ? 1.0 : -1.0) * margin));
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
        String url = isNfl() ? inputURIFoxNFL : inputURIFoxNCAA;
        System.out.println( "Fetching '" + url + "'");

        try
        {
            Document page = connect(url).get();
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
                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                Object result = engine.eval(score);
                double prediction;
                if (result instanceof Integer) {
                    prediction = (double)((Integer)result);
                } else {
                    prediction = (Double)result;
                }

                Integer homeId = teamToId.get(home);
                Integer awayId = teamToId.get(away);

                if (homeId != null && awayId != null) {
                    Game game = idToGame.get(homeId);
                    game.setFox(String.valueOf((game.getHome().equals(home) ? 1.0 : -1.0) * prediction));
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
        String url = isNfl() ? inputURIOSNFL : inputURIOSNCAA;
        System.out.println( "Fetching '" + url + "'");

        try
        {
            Document page = connect(url).get();
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

                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                Object result = engine.eval(prediction);
                Double margin = null;
                if (result instanceof Integer) {
                    margin = (double)((Integer)result);
                } else {
                    margin = (Double)result;
                }

                Integer homeId = teamToId.get(home);
                Integer awayId = teamToId.get(away);
                if (homeId != null && awayId != null) {
                    Game game = idToGame.get(homeId);
                    game.setOddsShark(String.valueOf((game.getHome().equals(home) ? 1.0 : -1.0) * margin));
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
        return null;
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
        System.out.println("Unable to locate either " + one + " or " + two);
    }
}
