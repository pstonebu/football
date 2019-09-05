package com.stoneburner.app.ncaa;

public class NCAAMain {

    public static void main( String[] args ) {
        NCAAUtil util = new NCAAUtil();

        util.grabPowerRank();
        util.grabFEI();
        util.grabFPI();
        util.grabAtomic();
        //util.grab538();
        util.grabSpread();
        util.grabSagarin();
        util.grabMassey();
        //util.grabSandP();
        util.grabDRatings();
        //disabling fox until they prove they don't suck
        //util.grabFox();
        util.grabOddsShark();
        util.printResults();

        System.out.println("Done!");
    }
}
