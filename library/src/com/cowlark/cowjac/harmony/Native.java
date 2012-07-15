/* cowjac © 2012 David Given
 * This file is licensed under the Simplified BSD license. Please see
 * COPYING.cowjac for the full text.
 */

package com.cowlark.cowjac.harmony;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation type used to indicate that this method or field has a specific
 * native code name (typically because it's going to be referenced from
 * native code).
 */

@Target( { ElementType.FIELD, ElementType.METHOD,
        ElementType.PARAMETER, ElementType.CONSTRUCTOR })
@Retention(RetentionPolicy.CLASS)
public @interface Native
{
    /**
     * The native name.
     */
    public String value();
}
