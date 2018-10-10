package com.stoneburner.app;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;

import static java.lang.String.format;
import static org.joda.time.Weeks.weeksBetween;
import static org.jsoup.Jsoup.connect;

public class NCAAUtil extends Util {

    public NCAAUtil() {
        DateTime game1 = new DateTime(1535760000000l);
        DateTime today = new DateTime();
        int week = weeksBetween(game1, today).getWeeks()+2;
        inputURIFoxNCAA = format(inputURIFoxNCAA, week);

        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyyMMdd");
        inputMasseyNCAA = inputMasseyNCAA.replace("$dt$", dtfOut.print(today));

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
        teamToId.put("Middle Tennessee", 2393);
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

    protected void grab538() {
        System.out.println("Fetching '" + input538NCAA + "'");

        //Execute client with our method
        try {
            HashMap<String,Integer> teams = new HashMap<String,Integer>();

            String[] rows = connect(input538NCAA).maxBodySize(0).get().body().html().split("(?<=(\\d|winout)[ ])");

            for (int i = 1; i < rows.length; i++) {
                String[] rowParts = rows[i].trim().split(",");
                String teamName = cleanTeamName(rowParts[0]);
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

                Integer id = teamToId.get(teamName);
                if (id != null) {
                    Game game = idToGame.get(id);
                    if (game != null) {
                        game.setFiveThirtyEight(String.valueOf(spread * (game.getHome().equals(teamName) ? -1.0 : 1.0)));
                    } else {
                        System.out.println("No game found for " + teamName + " with a spread of " + spread);
                    }

                } else {
                    System.out.println("No team found for " + teamName + " with a spread of " + spread);
                }
            }

        } catch (Exception e) {
            logAndExit(e);
        }
    }

    protected String cleanTeamName(String teamName) {
        //Common cleanups
        return teamName.replaceAll(" St(.)?$"," State").replaceAll("^E ", "Eastern ").replaceAll("^C ", "Central ")
                .replaceAll("&amp;","&").replaceAll("<b>","").replace("</b>","").replaceAll("i(`|')i", "ii")
                .replaceAll("^W ", "Western ").replace("A&m", "A&M").replace(" AM"," A&M").replaceAll("\\(ucf\\)$", "")
                //team specific cleanup
                .replaceAll("Army West Point", "Army")
                .replaceAll("Bowling Green State", "Bowling Green")
                .replaceAll("(BYU|Byu)", "Brigham Young")
                .replaceAll("SUNY-Buffalo", "Buffalo")
                .replaceAll("UCF", "Central Florida")
                .replaceAll("UNC( |-)Charlotte", "Charlotte")
                .replaceAll("^Coastal Car$", "Coastal Carolina")
                .replaceAll("(FIU|Florida Int(')?l|Fla. International)", "Florida International")
                .replaceAll("Ga Southern", "Georgia Southern")
                .replaceAll("^Kent$", "Kent State")
                .replaceAll("^(ULM|Louisiana-([Mm])onroe|UL-Monroe)$","Louisiana Monroe")
                .replaceAll("(Louisiana-([Ll])afayette|ULL)", "Louisiana")
                .replaceAll("(Louisiana State|Lsu)", "LSU")
                .replaceAll("(Miami( |-)(FL|florida)|^Miami$)", "Miami (FL)")
                .replaceAll("Miami( |-)(OH|ohio)", "Miami (OH)")
                .replaceAll("(Middle Tennessee State|MTSU)", "Middle Tennessee")
                .replaceAll("Ole Miss", "Mississippi")
                .replaceAll("N.C. State", "North Carolina State")
                .replaceAll("N Illinois", "Northern Illinois")
                .replaceAll("Ohio U.", "Ohio")
                .replaceAll("SMU", "Southern Methodist")
                .replaceAll("(Texas Christian|Tcu)", "TCU")
                .replaceAll("Texas St-San Marcos", "Texas State")
                .replaceAll("(Alabama-Birmingham|Uab)", "UAB")
                .replaceAll("(Ucla|California-Los Angeles)", "UCLA")
                .replaceAll("(Nevada-Las Vegas|Unlv)", "UNLV")
                .replaceAll("Southern Cal(ifornia)?", "USC")
                .replaceAll("Texas El Paso", "UTEP")
                .replaceAll("(Texas-San Antonio|Utsa|UT San Antonio)", "UTSA")
                .replaceAll("WKU", "Western Kentucky")
                .trim();
    }

    protected String removeMascot(String city) {
        return city;
    }

    protected String getCityFromMascot(String mascot) {
        return mascot;
    }

    protected Boolean isNfl() {
        return false;
    }

    protected NCAAGame getNewGame() {
        return new NCAAGame();
    }
}
