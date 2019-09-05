package com.stoneburner.app.ncaa;

import com.stoneburner.app.Game;
import lombok.Getter;
import lombok.Setter;

import static java.util.Arrays.asList;

@Getter @Setter
public class NCAAGame extends Game {
    private String atomic = "";
    private String sAndP = "";
    private String fPlus = "";
    private String fpi = "";
    private String fei = "";

    public String getHeader() {
        return "Home Team, Away Team, PR, Dratings, Fox, OS, 538, Massey, Sagarin, SP+, F/+, Atomic, FPI, FEI, Spread";
    }

    public String toString() {
        return String.join(",", asList(home, away, powerRank, dRatings, fox, oddsShark, fiveThirtyEight, massey,
                sagarin, sAndP, fPlus, atomic, fpi, fei, spread));
    }
}
