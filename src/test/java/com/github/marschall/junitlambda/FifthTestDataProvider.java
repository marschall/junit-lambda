package com.github.marschall.junitlambda;

import static junitparams.JUnitParamsRunner.$;

/**
 * TODO AC: JavaDoc
 *
 * @author Alasdair Collinson
 */
public class FifthTestDataProvider {

    public static Object[] provideItalian() {
        return $(
                $("unos", "2", "3.0")
        );
    }
}
