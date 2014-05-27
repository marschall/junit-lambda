package com.github.marschall.junitlambda;

import static junitparams.JUnitParamsRunner.$;

/**
 * TODO AC: JavaDoc
 *
 * @author Alasdair Collinson
 */
public final class ThirdTestDataProvider {

    public static Object[] provideUpperCaseStrings() {
        return $(
                $("ONE", "NONE"),
                $("TWO", "TWOFLOUR")
        );
    }

    public static Object[] provideLowerCaseStrings() {
        return $(
                $("three", "her"),
                $("four", "flour"),
                $("five", "alive")
        );
    }

}
