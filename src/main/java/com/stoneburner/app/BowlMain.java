package com.stoneburner.app;

import static com.stoneburner.app.Util.*;

public class BowlMain {

    public static void main( String[] args ) {
        //Home  Away    PR  DR  Fox  OS S&P+    Massey  Sagarin 538 Atomic  Spread
        String[][] predictions = new String[100][12];
        
        predictions = grabPowerRank(false, predictions);
        grabAtomic(predictions);
        grab538NCAA(predictions);
        grabSpread(false, predictions);
        grabSagarin(false, predictions);
        grabMassey(false, predictions);
        grabSandP(predictions);
        grabDRatings(false, predictions);
        grabFox(false, predictions);
        grabOddsShark(false, predictions);

        printResults(false, predictions);

        System.out.println("Done!");
    }
}
