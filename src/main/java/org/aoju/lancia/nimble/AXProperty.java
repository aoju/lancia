/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.lancia.nimble;

/**
 * 可访问性树中的属性
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class AXProperty {
    /**
     * 此属性的名称
     * "busy"|"disabled"|"editable"|"focusable"|"focused"|"hidden"|"hiddenRoot"|"invalid"|"keyshortcuts"|"settable"|"roledescription"|"live"|"atomic"|"relevant"|"root"|"autocomplete"|"hasPopup"|"level"|"multiselectable"|"orientation"|"multiline"|"readonly"|"required"|"valuemin"|"valuemax"|"valuetext"|"checked"|"expanded"|"modal"|"pressed"|"selected"|"activedescendant"|"controls"|"describedby"|"details"|"errormessage"|"flowto"|"labelledby"|"owns";
     */
    private String name;
    /**
     * 此属性的值
     */
    private AXValue value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AXValue getValue() {
        return value;
    }

    public void setValue(AXValue value) {
        this.value = value;
    }

}
