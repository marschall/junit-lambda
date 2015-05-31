JUnit λ [![Build Status](https://travis-ci.org/marschall/junit-lambda.png?branch=master)](https://travis-ci.org/marschall/junit-lambda) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.rsql/rsql-parser/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/junit-lambda)
=======
JUnit extensions built on Java 8 lambdas. Helps to test exceptions and can be used instead of the following pattern:

```java
try {
    Long.parseLong("foo");
    fail("'foo' should not be a valid long");
} catch (NumberFormatException e) {
    // should reach here
}
```

You can either use `#assertRaises`

```java
import static com.github.marschall.junitlambda.LambdaAssert.assertRaises;
import org.junit.Test;

public final class JunitLambdaTest {
    @Test
    public void testNumberFormatException() {
        assertRaises(() -> Long.parseLong("foo"), NumberFormatException.class);
    }
}
```

or the Hamcrest matcher `#throwsException`


```java
import static com.github.marschall.junitlambda.ThrowsException.throwsException;
import org.junit.Test;

public final class JunitLambdaTest {
    @Test
    public void testNumberFormatException() {
        assertThat(() -> Long.parseLong("foo"), throwsException(NumberFormatException.class));
    }
}
```

```xml
<dependency>
    <groupId>com.github.marschall</groupId>
    <artifactId>junit-lambda</artifactId>
    <version>0.3.0</version>
    <scope>test</scope>
</dependency>
```

The code is under MIT license.


