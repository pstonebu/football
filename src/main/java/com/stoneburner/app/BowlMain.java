package com.stoneburner.app;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.stoneburner.app.Util.*;

public class BowlMain {

    public static void main( String[] args ) {
        List<NCAAGame> games = newArrayList();
        
        grabPowerRank(games);
        grabAtomic(games);
        grab538(games);
        grabSpread(games);
        grabSagarin(games);
        grabMassey(games);
        grabSandP(games);
        grabDRatings(games);
        grabFox(games);
        grabOddsShark(games);

        printResults(games);

        System.out.println("Done!");
    }
}
