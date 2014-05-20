package com.github.marschall.junitlambda.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * TODO AC: JavaDoc
 *
 * @author Alasdair Collinson
 * @see com.github.marschall.junitlambda.annotations.ParameterRecord
 * @see junitparams.Parameters
 * @since 0.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ParameterRecords {

    /**
     * TODO AC: JavaDoc
     * @return TODO
     */
    ParameterRecord[] value();
}
