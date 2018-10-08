package com.stoneburner.app;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.stoneburner.app.Util.*;

public class NFLMain
{
    public static void main( String[] args ) {
        List<Game> games = newArrayList();

        grabPowerRank(games);
        grabSpread(games);
        grabSagarin(games);
        grabMassey(games);
        grab538(games);
        grabDRatings(games);
        grabFox(games);
        grabOddsShark(games);

        printResults(games);

        System.out.println("Done!");
    }
}
