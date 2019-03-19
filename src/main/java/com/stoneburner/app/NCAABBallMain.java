package com.stoneburner.app;

public class NCAABBallMain {

    public static void main( String[] args ) {
        NCAABBallUtil util = new NCAABBallUtil();

        util.grabPowerRank();
        util.grab538();
        util.grabKenPom();
        util.grabGamePredict();
        util.grabBartTorvik();
        util.grabMassey();
        util.grabDRatings();
        util.grabSagarin();
        util.grabSpread();
        util.printResults();

        System.out.println("Done!");
    }
}
