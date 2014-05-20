package com.github.marschall.junitlambda.annotations;

import javax.lang.model.type.NullType;
import java.lang.annotation.*;

/**
 * This is a slightly modified version of {@link junitparams.Parameters} which
 * allows for repetition and the use of lambda expressions, so as to enable
 * users to define several sets of parameters using repeating annotations.
 *
 * @author Alasdair Collinson
 * @see com.github.marschall.junitlambda.annotations.ParameterRecords
 * @see junitparams.Parameters
 * @since 0.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(ParameterRecords.class)
public @interface ParameterRecord {
    /**
     * Parameter values defined as a String array. Unlike with
     * {@link junitparams.Parameters#value()},  each element in the array is a
     * separate parameter.
     * TODO
     * The values must match the method parameters in order and type.
     * Example: <code>
     *     @ParameterRecord("1", "joe", "26.4", "true")
     *     @ParameterRecord("2", "angie", "37.2", "false")
     * </code>
     *
     * @return TODO
     * @see junitparams.Parameters#value()
     */
    String[] value() default {};

    /**
     * Parameter values defined externally. The specified class must have at
     * least one public static method starting with <code>provide</code>
     * returning <code>Object[]</code>. All such methods are used, so you can
     * group your examples. The resulting array should contain parameter sets in
     * its elements. Each parameter set must be another Object[] array, which
     * contains parameter values in its elements.
     * Example: <code>@Parameters(source = PeopleProvider.class)</code>
     *
     * @return TODO
     * @see junitparams.Parameters#source()
     */
    Class<?> source() default NullType.class;

    /**
     * Parameter values returned by a method within the test class. This way you
     * don't need additional classes and the test code may be a bit cleaner. The
     * format of the data returned by the method is the same as for the source
     * annotation class.
     * Example: <code>@ParameterRecord(method = "examplaryPeople")</code>
     * <p/>
     * You can use multiple methods to provide parameters by using the annotation
     * repeatedly.
     * Example: <code>
     *     @ParameterRecord(method = "womenParams")
     *     @ParameterRecord(method = "menParams")
     * </code>
     *
     * @return TODO
     */
    String method() default "";

    /**
     * Parameter values returned by a {@link java.util.function.Supplier} within
     * the test class. This way you don't need additional classes and the test code
     * may be a bit cleaner. The format of the data returned by this supplier is the
     * same as for the source annotation class.
     * Example: <code>@ParameterRecord(lambda = "loadsOfIntegers")</code>
     * <p/>
     * You can use multiple methods to provide parameters by using the annotation
     * repeatedly.
     * Example: <code>
     *     @ParameterRecord(lambda = "evenIntegers")
     *     @ParameterRecord(lambda = "oddIntegers")
     * </code>
     *
     * @return TODO
     */
    String lambdas() default "";
}
