package com.github.marschall.junitlambda;

import com.github.marschall.junitlambda.annotations.ParallelTesting;
import com.github.marschall.junitlambda.runner.Java8JUnitTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static com.github.marschall.junitlambda.LambdaAssert.*;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link com.github.marschall.junitlambda.LambdaAssert} class.
 *
 * @author Philippe Marschall
 * @author Alasdair Collinson
 */
@RunWith(Java8JUnitTestRunner.class)
@ParallelTesting(parallel = false)
public class LambdaAssertTest {

    private static final Random random = new Random();
    private static Messages messages = new Messages();
    private static List<String> names = new ArrayList<>();
    private static Map<String, Integer> ages = new HashMap<>();

    static {
        names.add("Anna");
        names.add("Bob");
        names.add("Carla");
        names.add("Dan");
        names.add("Eric");
        names.add("Fred");
        names.add("Ginny");

        for (String name : names) {
            ages.put(name, random.nextInt(50) * 2);
        }

        messages.add(new Messages.Message("HW", "Greeting people is nice", "Hello World", Messages.Severity.LOG));
        messages.add(new Messages.Message("Oh oh", "This may be bad", "Your fly is open", Messages.Severity.WARNING));
        messages.add(new Messages.Message("This is bad", "I feel like it", "You are wrong. Just plain wrong.", Messages.Severity.ERROR));
    }

    // Test methods for void functions

    @Test
    public void voidDoesNotRaiseRuntime() {
        assertFailure(() ->
                assertVoidRaises(() -> {
                }, RuntimeException.class));
    }

    @Test
    public void voidDoesRaiseWrongRuntime() {
        assertFailure(() ->
                assertVoidRaises(() -> {
                    throw new NullPointerException();
                }, IllegalArgumentException.class));
    }

    @Test
    public void voidDoesRaiseCheckedInsteadOfRuntime() {
        assertFailure(() ->
                assertVoidRaises(() -> {
                    throw new IOException();
                }, RuntimeException.class));
    }

    @Test
    public void voidDoesRaiseRuntimeExact() {
        assertVoidRaises(() -> {
            throw new RuntimeException();
        }, RuntimeException.class);
    }

    @Test
    public void voidDoesRaiseRuntimeSubclass() {
        assertVoidRaises(() -> {
            throw new IndexOutOfBoundsException();
        }, RuntimeException.class);
    }

    @Test
    public void voidDoesRaiseCheckedExact() {
        assertVoidRaises(() -> {
            throw new IOException();
        }, IOException.class);
    }

    @Test
    public void voidDoesRaiseCheckedSubclass() {
        assertVoidRaises(() -> {
            throw new FileNotFoundException();
        }, IOException.class);
    }

    @Test
    public void assertionErrorRaisedByVoid() {
        assertVoidRaises(() -> {
            throw new AssertionError();
        }, AssertionError.class);
    }

    @Test
    public void assertionErrorNotRaisedByVoid() {
        assertFailure(() ->
                assertVoidRaises(() -> {
                }, AssertionError.class));
    }

    // Test methods for functions with return values

    @Test
    public void doesNotRaiseRuntime() {
        assertFailure(() ->
                assertRaises(() -> null, RuntimeException.class));
    }

    @Test
    public void doesRaiseWrongRuntime() {
        assertFailure(() ->
                assertRaises(() -> {
                    throw new NullPointerException();
                }, IllegalArgumentException.class));
    }

    @Test
    public void doesRaiseCheckedInsteadOfRuntime() {
        assertFailure(() ->
                assertRaises(() -> {
                    throw new IOException();
                }, RuntimeException.class));
    }

    @Test
    public void doesRaiseRuntimeExact() {
        assertRaises(() -> {
            throw new RuntimeException();
        }, RuntimeException.class);
    }

    @Test
    public void doesRaiseRuntimeSubclass() {
        assertRaises(() -> {
            throw new IndexOutOfBoundsException();
        }, RuntimeException.class);
    }

    @Test
    public void doesRaiseCheckedExact() {
        assertRaises(() -> {
            throw new IOException();
        }, IOException.class);
    }

    @Test
    public void doesRaiseCheckedSubclass() {
        assertRaises(() -> {
            throw new FileNotFoundException();
        }, IOException.class);
    }

    @Test
    public void assertionErrorRaised() {
        assertRaises(() -> {
            throw new AssertionError();
        }, AssertionError.class);
    }

    @Test
    public void assertionErrorNotRaised() {
        assertFailure(() ->
                assertRaises(() -> null, AssertionError.class));
    }

    // Test methods for iterables, collections and maps

    @Test
    public void testAssertForAllIterablePositive() {
        assertForEach(messages, m -> m.getSeverity() != Messages.Severity.FATAL);
    }

    @Test
    public void testAssertForAllIterableNegative() {
        assertFailure(() ->
                assertForEach("Failed", messages, m -> m.getReason().equals("")));
    }

    @Test
    public void testAssertForAllCollectionPositive() {
        assertForEach(names, n -> n.length() <= 5);
    }

    @Test
    public void testAssertForAllCollectionNegative() {
        assertFailure(() ->
                assertForEach("Failed", names, n -> n.toLowerCase().contains("a")));
    }

    @Test
    public void testAssertForAllMapPositive() {
        assertForEach(ages, a -> a < 100);
    }

    @Test
    public void testAssertForAllMapNegative() {
        assertFailure(() ->
                assertForEach("Failed", ages, a -> a % 2 == 1));
    }

    // Tests for single predicates

    @Test
    public void testAssertThatPositive() {
        assertThat(names, n -> n.contains("Bob"));
        assertThat("Hello World", $$("letters and spaces", s -> s.matches("[\\w\\s]*")));
    }

    @Test
    public void testAssertThatNegative() {
        assertFailure(() -> assertThat(names, n -> n.contains("Greg")));
    }

    // Tests for assertFailure

    @Test
    public void testAssertFailurePositive() {
        assertFailure(() -> assertTrue(false));
    }

    @Test(expected = AssertionError.class)
    public void testAssertFailureNegative() {
        assertFailure(() -> {
        });
    }
}

/**
 * A test class representing a non-standard {@link java.lang.Iterable}. In this case it wrapps a List of
 * {@link com.github.marschall.junitlambda.Messages.Message} objects, each of which has a set of properties.
 *
 * @author Alasdair Collinson
 */
class Messages implements Iterable<Messages.Message> {

    private List<Messages.Message> messageList = new ArrayList<>();

    @Override
    public Iterator<Messages.Message> iterator() {
        return messageList.iterator();
    }

    public void add(Messages.Message message) {
        messageList.add(message);
    }

    static enum Severity {
        LOG, WARNING, ERROR, FATAL
    }

    @SuppressWarnings("unused")
    /**
     * A test class representing a message object. Every message has a title, a reason, an additional text and a
     * severety. None of these are however null safe, so use with care.
     */
    static class Message {
        private String title, reason, text;
        private Severity severity;

        public Message(String title, String reason, String text, Severity severity) {
            this.title = title;
            this.reason = reason;
            this.text = text;
            this.severity = severity;
        }

        public String getTitle() {
            return title;
        }

        public Severity getSeverity() {
            return severity;
        }

        public String getReason() {
            return reason;
        }

        public String getText() {
            return text;
        }
    }
}


