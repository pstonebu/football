package com.stoneburner.app;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.stoneburner.app.Util.*;

public class NCAAMain {

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
        //disabling fox until they prove they don't suck
        //grabFox(games);
        grabOddsShark(games);
        printResults(games);

        System.out.println("Done!");
    }
}
