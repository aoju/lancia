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

/**
 * Mirror object referencing original JavaScript object.
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class RemoteObject {

    /**
     * Object type. "object"|"function"|"undefined"|"string"|"number"|"boolean"|"symbol"|"bigint";
     */
    private String type;
    /**
     * Object subtype hint. Specified for `object` type values only.
     * "array"|"null"|"node"|"regexp"|"date"|"map"|"set"|"weakmap"|"weakset"|"iterator"|"generator"|"error"|"proxy"|"promise"|"typedarray"|"arraybuffer"|"dataview";
     */
    private String subtype;
    /**
     * Object class (constructor) name. Specified for `object` type values only.
     */
    private String className;
    /**
     * Remote object value in case of primitive values or JSON values (if it was requested).
     */
    private Object value;
    /**
     * Primitive value which can not be JSON-stringified does not have `value`, but gets this
     * property.
     */
    private String unserializableValue;
    /**
     * String representation of the object.
     */
    private String description;
    /**
     * Unique object identifier (for non-primitive values).
     */
    private String objectId;
    /**
     * Preview containing abbreviated property values. Specified for `object` type values only.
     */
    private ObjectPreview preview;

    private CustomPreview customPreview;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getUnserializableValue() {
        return unserializableValue;
    }

    public void setUnserializableValue(String unserializableValue) {
        this.unserializableValue = unserializableValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public ObjectPreview getPreview() {
        return preview;
    }

    public void setPreview(ObjectPreview preview) {
        this.preview = preview;
    }

    public CustomPreview getCustomPreview() {
        return customPreview;
    }

    public void setCustomPreview(CustomPreview customPreview) {
        this.customPreview = customPreview;
    }

}
