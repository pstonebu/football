package com.stoneburner.app;

import lombok.Getter;
import lombok.Setter;

import static java.util.Arrays.asList;

@Getter @Setter
public class NCAABBallGame extends Game {
    private boolean playIn;
    private String kenPom;
    private String gamePredict;
    private String torvik;
    private String session;

    public String getHeader() {
        return "Favorite, Underdog, 538, PR, KP, GP, Torvik, Massey, DRatings, Sagarin, " + (playIn ? "" : "Session, " + "Spread");
    }

    public String toString() {
        return String.join(",", playIn ?
                asList(home, away, fiveThirtyEight, powerRank, kenPom, gamePredict, torvik, massey, dRatings, sagarin, spread) :
                asList(home, away, fiveThirtyEight, powerRank, kenPom, gamePredict, torvik, massey, dRatings, sagarin, session, spread));
    }
}
