/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.lancia.nimble.runtime;

import java.util.List;

/**
 * Object containing abbreviated remote object value.
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class ObjectPreview {

    /**
     * Object type."object"|"function"|"undefined"|"string"|"number"|"boolean"|"symbol"|"bigint"
     */
    private String type;
    /**
     * Object subtype hint. Specified for `object` type values only.
     * "array"|"null"|"node"|"regexp"|"date"|"map"|"set"|"weakmap"|"weakset"|"iterator"|"generator"|"error"
     */
    private String subtype;
    /**
     * String representation of the object.
     */
    private String description;
    /**
     * True iff some of the properties or entries of the original object did not fit.
     */
    private boolean overflow;
    /**
     * List of the properties.
     */
    private List<PropertyPreview> properties;
    /**
     * List of the entries. Specified for `map` and `set` subtype values only.
     */
    private List<EntryPreview> entries;

}
