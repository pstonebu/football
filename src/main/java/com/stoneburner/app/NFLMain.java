package com.stoneburner.app;

public class NFLMain
{
    public static void main( String[] args ) {
        Util util = new Util(true);

        util.grabPowerRank();
        util.grabSpread();
        util.grabSagarin();
        util.grabMassey();
        util.grab538();
        util.grabDRatings();
        util.grabFox();
        util.grabOddsShark();

        util.printResults();

        System.out.println("Done!");
    }
}
