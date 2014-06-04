package com.github.marschall.junitlambda;

import com.github.marschall.junitlambda.annotations.ParallelTesting;
import com.github.marschall.junitlambda.annotations.ParameterRecord;
import com.github.marschall.junitlambda.runner.Java8JUnitTestRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.github.marschall.junitlambda.LambdaAssert.$$;
import static com.github.marschall.junitlambda.LambdaAssert.assertThat;
import static junitparams.JUnitParamsRunner.$;

/**
 * TODO AC: JavaDoc
 *
 * @author Alasdair Collinson
 */
@RunWith(Java8JUnitTestRunner.class)
@ParallelTesting(parallel = false)
public class ParameterizedTestingTest {
/*
    @FirstTest
    public void firstTest() {
        assertTrue(true);
    }

    @Test
    @Parameters({"1, Hello, true", "2, Hi, false"})
    public void firstTestWithParameters(int number, String greeting, boolean truth) {
        assertThat(number, $$("number < 3", n -> n < 3));
        assertThat(greeting, $$("greeting starts with 'H'", s -> s.charAt(0) == 'H'));
        assertTrue(truth || greeting.length() < 3);
    }

    @Test
    @Parameters(method = "zonedDateTimes")
    public void secondTestWithParameters(ChronoZonedDateTime time) {
        ChronoZonedDateTime startedAt = ZonedDateTime.now();
        assertThat(time, t -> t.isAfter(startedAt));
    }

    private Object[] zonedDateTimes() {
        return $(
                LocalDateTime.now().plusMinutes(30).atZone(ZoneId.of("+0")),
                LocalDate.now().plusDays(1).atStartOfDay(ZoneId.of("+1")),
                ZonedDateTime.now().plusHours(3).withZoneSameInstant(ZoneId.of("-1"))
        );
    }*/

    @Test
    @ParameterRecord({"Hello", "Hell‚"})
    @Parameters(source = ThirdTestDataProvider.class)
    public void thirdTestWithParameters(String first, String second) {
        Set<Character> firstSet = new HashSet<>();
        for (Character c : first.toCharArray()) {
            firstSet.add(c);
        }
        Set<Character> secondSet = new HashSet<>();
        for (Character c : second.toCharArray()) {
            secondSet.add(c);
        }
        assertThat(Pair.of(firstSet, secondSet), $$("at least three identical characters", p -> {
            int common = 0;
            for (Character c : p.getLeft()) {
                if (p.getRight().contains(c)) {
                    common++;
                }
            }
            return common >= 3;
        }));
    }

    @Test
    @Parameters
    public void fourthTestWithParameters(int number, boolean even) {
        assertThat(number,
                $$("is the boolean stating whether the number is even or not correct", n -> (n % 2 == 0) == even));
    }

    @SuppressWarnings("unused")
    private Object[] parametersForFourthTestWithParameters() {
        return IntStream.range(1, 10).mapToObj(i -> $(i, i % 2 == 0)).toArray();
    }

    /*@Test
    @ParameterRecord(method = "getParamsForFifthTestHere")
    @ParameterRecord({"eins", "2", "3.0"}) // German
    @ParameterRecord({"one", "2", "3.0"}) // English
    @ParameterRecord(source = FifthTestDataProvider.class)
    public void fithTestWithParameters(String one, int two, Double three) {
        assertThat(one, input -> Arrays.asList("eins", "one", "uno", "un", "unos").contains(input));
        assertThat(() -> two == 2);
        assertThat(() -> three == 3.0);
    }

    public Object[] getParamsForFifthTestHere() {
        return $(
                $("uno", "2", "3.0"), // Spanish
                $("un", "2", "3.0") // French
        );
    }

    @LastTest
    public void lastTest() {
        assertTrue(true);
    }
*/
    /*private static List<String> woerter = Arrays.asList("Hello", "World");
    private static int counter = 0;

    public static Supplier<Object[]> supplier = () -> {
        if(counter < woerter.size()) {
            return $(
                    woerter.get(counter),
                    woerter.get(counter++).length()
            );
        } else {
            return null;
        }
    };

    @ParameterRecord(lambda = "supplier")
    @Test
    public void sixthTestWithParameters(String word, int length) {
        System.out.println("Word: " + word);
        Assert.assertNotNull(word);
        Assert.assertTrue(word.length() == length);
    }*/

    private static List<String> woerter =
            Arrays.asList("Hallo", "Welt");
    private static int counter = 0;

    public static Supplier<Object[]> supplier = () -> {
        if (counter < woerter.size()) {
            return new Object[]{
                    woerter.get(counter),
                    woerter.get(counter++).length()
            };
        } else {
            return null;
        }
    };

    @ParameterRecord(lambda = "supplier")
    @Test
    public void testMitSupplier(String wort, int laenge) {
        Assert.assertNotNull(wort);
        Assert.assertTrue(wort.length() == laenge);
    }

}