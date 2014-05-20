JUnit λ
=======
JUnit extensions build on Java 8 lambdas. A JDK level of 1.8 is required.

Assertions
----------

Simple assertions can be made like such:

```java
    assertRaises(() -> anObject.aMethod(anArgument), IllegalArgumentException.class);
    assertForAll(Arrays.asList(1, 2, 3), i -> i < 4);
    assertForAll(anyMapWithNumbersAsValues, n -> n >= 0);
    assertThat("Hello World", s -> s.matches("[\\w\\s]*"));
```

To get more informative descriptions of failed lambda methods you may use the `$$(String description, Predicate<T> predicate)` factory method:

```java
    assertForAll(Arrays.asList(1, 2, 3), $$("smaller than 4", i -> i < 4));
    assertThat("Hello World", $$("letters and spaces", s -> s.matches("[\\w\\s]*")));
```

Parallel Test Execution
-----------------------
JUnit λ comes with a JUnit Runner which will execute your tests in parallel, thereby improving test execution speed in many cases.

```java
    @RunWith(ParallelJUnitTestRunner.class)
    public class Tests {
        // ...
    }
```

This can be used with any JUnit tests, independent of whether they use lambda expressions or not.

Maven Dependency
----------------

Currently this version is not available on Maven Central. A previous version can received by the following dependency entry:

```xml
<dependency>
    <groupId>com.github.marschall</groupId>
    <artifactId>junit-lambda</artifactId>
    <version>0.1.0</version>
</dependency>
```

