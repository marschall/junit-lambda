package com.github.marschall.junitlambda;

import com.github.marschall.junitlambda.annotations.ParallelTesting;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static com.github.marschall.junitlambda.LambdaAssert.*;

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

    @Test(expected = AssertionError.class)
    public void doesNotRaiseRuntime() {
        assertRaises(() -> {
        }, RuntimeException.class);
    }

    @Test(expected = AssertionError.class)
    public void doesRaiseWrongRuntime() {
        assertRaises(() -> {
            throw new NullPointerException();
        }, IllegalArgumentException.class);
    }

    @Test(expected = AssertionError.class)
    public void doesRaiseCheckedInsteadOfRuntime() {
        assertRaises(() -> {
            throw new IOException();
        }, RuntimeException.class);
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

    @Test(expected = AssertionError.class)
    public void assertionErrorNotRaised() {
        assertRaises(() -> {
        }, AssertionError.class);
    }

    @Test
    public void testAssertForAllCollectionPositive() {
        assertForAll(names, n -> n.length() <= 5);
    }

    @Test(expected = AssertionError.class)
    public void testAssertForAllCollectionNegative() {
        assertForAll("Failed", names, n -> n.toLowerCase().contains("a"));
    }

    @Test
    public void testAssertForAllMapPositive() {
        assertForAll(ages, a -> a < 100);
    }

    @Test(expected = AssertionError.class)
    public void testAssertForAllMapNegative() {
        assertForAll("Failed", ages, a -> a % 2 == 1);
    }

    @Test
    public void testAssertForAllIterablePositive() {
        assertForAll(messages, m -> m.getSeverity() != Messages.Severity.FATAL);
    }

    @Test(expected = AssertionError.class)
    public void testAssertForAllIterableNegative() {
        assertForAll("Failed", messages, m -> m.getReason().equals(""));
    }

    @Test
    public void testAssertThatPositive() {
        assertThat(names, n -> n.contains("Bob"));
        assertThat("Hello World", $$("letters and spaces", s -> s.matches("[\\w\\s]*")));
    }

    @Test(expected = AssertionError.class)
    public void testAssertThatNegative() {
        assertThat(names, n -> n.contains("Greg"));
    }
}

/**
 * TODO AC: JavaDoc
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


