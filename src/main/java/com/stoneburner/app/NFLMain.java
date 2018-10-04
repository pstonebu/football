package com.stoneburner.app;

import static com.stoneburner.app.Util.*;

public class NFLMain
{
    public static void main( String[] args ) {
        String[][] predictions = new String[16][10];

        predictions = grabPowerRank(true, predictions);
        grabSpread(true, predictions);
        grabSagarin(true, predictions);
        grabMassey(true, predictions);
        grab538NFL(predictions);
        grabDRatings(true, predictions);
        grabFox(true, predictions);
        grabOddsShark(true, predictions);

        printResults(true, predictions);

        System.out.println("Done!");
    }
}
