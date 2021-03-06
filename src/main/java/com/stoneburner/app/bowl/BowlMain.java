package com.stoneburner.app.bowl;

import com.stoneburner.app.ncaa.NCAAUtil;

public class BowlMain {

    public static void main( String[] args ) {
        NCAAUtil util = new BowlUtil();

        util.grabPowerRank();
        util.grabFEI();
        util.grabFPI();
        util.grabAtomic();
        //util.grab538();
        util.grabSpread();
        util.grabSagarin();
        util.grabMassey();
        util.grabSandP();
        util.grabDRatings();
        //disabling fox until they prove they don't suck
        //util.grabFox();
        util.grabOddsShark();
        util.printResults();

        System.out.println("Done!");
    }
}
