package com.stoneburner.app;

import org.joda.time.DateTime;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import static java.lang.String.format;
import static org.joda.time.format.DateTimeFormat.forPattern;

public class BowlUtil extends NCAAUtil {

    public BowlUtil() {
        sagarinPredictionQuery = "a[name=Predictions_with_Totals]";
        inputMassey = format(inputMasseyBlank, "cf", forPattern("yyyyMMdd").print(new DateTime(2018, 12, 31, 0, 0)));
    }

    @Override
    public Element getPowerRankCurrent(Document page) {
        return super.getPowerRankCurrent(page);
    }
}
