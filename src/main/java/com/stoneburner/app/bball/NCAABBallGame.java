package com.stoneburner.app.bball;

import com.stoneburner.app.Game;
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
    private String kenPomOU;
    private String torvikOU;
    private String masseyOU;
    private String dRatingsOU;
    private String sagarinOU;

    public String getHeader() {
        return "Favorite, Underdog, 538, PR, KP, GP, Torvik, Massey, DRatings, Sagarin, KPOU, TorvikOU, MasseyOU, DROU, SagarinOU, " + (playIn ? "" : "Session, " + "Spread, Over/Under");
    }

    public String toString() {
        return String.join(",", playIn ?
                asList(home, away, fiveThirtyEight, powerRank, kenPom, gamePredict, torvik, massey, dRatings, sagarin, kenPomOU, torvikOU, masseyOU, dRatingsOU, sagarinOU, spread, overunder) :
                asList(home, away, fiveThirtyEight, powerRank, kenPom, gamePredict, torvik, massey, dRatings, sagarin, kenPomOU, torvikOU, masseyOU, dRatingsOU, sagarinOU, session, spread, overunder));
    }
}
