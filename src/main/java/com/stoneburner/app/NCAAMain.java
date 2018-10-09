package com.stoneburner.app;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class NCAAMain {

    public static void main( String[] args ) {
        Util util = new Util(false);

        util.grabPowerRank();
        util.grabAtomic();
        util.grab538();
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
