JUnit λ [![Build Status](https://travis-ci.org/marschall/junit-lambda.png?branch=master)](https://travis-ci.org/marschall/junit-lambda) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/cz.jirutka.rsql/rsql-parser/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.marschall/junit-lambda)
=======
JUnit extensions build on Java 8 lambdas. In theory it works with Java 7 but you'll get most of the value with Java 8 lambdas.

```java
    assertRaises(() -> anObject.aMethod(anArgument), IllegalArgumentException.class);
```

```xml
<dependency>
    <groupId>com.github.marschall</groupId>
    <artifactId>junit-lambda</artifactId>
    <version>0.1.0</version>
</dependency>
```

FAQ
---
### Why the Java 7 requirement?
Because it uses [MethodHandle](http://docs.oracle.com/javase/7/docs/api/java/lang/invoke/MethodHandle.html) for dynamic catch.
