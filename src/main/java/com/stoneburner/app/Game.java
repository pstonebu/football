package com.stoneburner.app;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static java.util.Arrays.asList;

@Getter @Setter @NoArgsConstructor
public class Game {
    protected String home = "";
    protected String away = "";
    protected String spread = "";
    protected String overunder = "";
    protected String fiveThirtyEight = "";
    protected String powerRank = "";
    protected String sagarin = "";
    protected String massey = "";
    protected String dRatings = "";
    protected String fox = "";
    protected String oddsShark = "";

    public String getHeader() {
        return "Home Team, Away Team, PR, Dratings, Fox, OS, 538, Massey, Sagarin, Spread";
    }

    public String toString() {
        return String.join(",", asList(home, away, powerRank, dRatings, fox, oddsShark, fiveThirtyEight, massey, sagarin, spread));
    }
}
