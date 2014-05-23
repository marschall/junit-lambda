package com.github.marschall.junitlambda;

import com.github.marschall.junitlambda.annotations.LastTest;
import com.github.marschall.junitlambda.annotations.FirstTest;
import com.github.marschall.junitlambda.annotations.ParallelTesting;
import com.github.marschall.junitlambda.runner.Java8JUnitTestRunner;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

import static com.github.marschall.junitlambda.LambdaAssert.$$;
import static com.github.marschall.junitlambda.LambdaAssert.assertThat;
import static junitparams.JUnitParamsRunner.$;
import static org.junit.Assert.assertTrue;

/**
 * TODO AC: JavaDoc
 *
 * @author Alasdair Collinson
 */
//@RunWith(JUnitParamsRunner.class)
@RunWith(Java8JUnitTestRunner.class)
@ParallelTesting(parallel = false)
public class ParameterizedTestingTest {

    @Ignore
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
    }

    @Test
    @Parameters(source = TestDataProvider.class)
    public void thirdTestWithParameters(String first, String second) {
        Set<Character> firstSet = new HashSet<>();
        for(Character c : first.toCharArray()) {
            firstSet.add(c);
        }
        Set<Character> secondSet = new HashSet<>();
        for(Character c : second.toCharArray()) {
            secondSet.add(c);
        }
        assertThat(Pair.of(firstSet, secondSet), $$("at least three identical characters", p -> {
            int common = 0;
            for(Character c : p.getLeft()) {
                if(p.getRight().contains(c)) {
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

    @LastTest
    public void lastTest() {
        assertTrue(true);
    }

}