package com.stoneburner.app;

public class BowlMain {

    public static void main( String[] args ) {
        Util util = new NCAAUtil();

        util.grabPowerRank();
        util.grabAtomic();
        util.grab538();
        util.grabSpread();
        util.grabSagarin();
        util.grabMassey();
        util.grabSandP();
        util.grabDRatings();
        util.grabFox();
        util.grabOddsShark();
        util.printResults();

        System.out.println("Done!");
    }
}
