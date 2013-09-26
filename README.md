JUnit λ
=======
JUnit extensions build on Java 8 lambdas. In theory it works with Java 7 but you'll get most of the value with Java 8 lambdas.

```
    assertRaises(() -> anObject.aMethod(anArgument), IllegalArgumentException.class);
```

FAQ
---
### Why the Java 7 requirement?
Because it uses (MethodHandle)[http://docs.oracle.com/javase/7/docs/api/java/lang/invoke/MethodHandle.html] for dynamic catch.