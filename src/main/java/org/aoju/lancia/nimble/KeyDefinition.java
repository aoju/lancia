/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 * @author Kimi Liu
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class KeyDefinition {

    private int keyCode;

    private int shiftKeyCode;

    private String key;

    private String shiftKey;

    private String code;

    private String text;

    private String shiftText;

    private int location;

    public KeyDefinition() {
        super();
    }

    public KeyDefinition(int keyCode, int shiftKeyCode, String key, String code, String shiftKey, int location) {
        this.keyCode = keyCode;
        this.shiftKeyCode = shiftKeyCode;
        this.key = key;
        this.shiftKey = shiftKey;
        this.code = code;
        this.location = location;
    }

    public KeyDefinition(int keyCode, String key, String code) {
        super();
        this.keyCode = keyCode;
        this.key = key;
        this.code = code;
    }

    public KeyDefinition(String key, String code) {
        super();
        this.key = key;
        this.code = code;
    }

    public KeyDefinition(int keyCode, String key, String code, int location) {
        this.keyCode = keyCode;
        this.key = key;
        this.code = code;
        this.location = location;
    }

    public KeyDefinition(String key, String code, int location) {
        this.key = key;
        this.code = code;
        this.location = location;
    }

    public KeyDefinition(int keyCode, String key) {
        this.keyCode = keyCode;
        this.key = key;
    }

    public KeyDefinition(int keyCode, String code, String shiftKey, String key) {
        this.keyCode = keyCode;
        this.key = key;
        this.code = code;
        this.shiftKey = shiftKey;
    }

    public KeyDefinition(int keyCode, String code, String key, String text, int location) {
        this.keyCode = keyCode;
        this.key = key;
        this.code = code;
        this.text = text;
        this.location = location;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getShiftKeyCode() {
        return shiftKeyCode;
    }

    public void setShiftKeyCode(int shiftKeyCode) {
        this.shiftKeyCode = shiftKeyCode;
    }

    public String getShiftKey() {
        return shiftKey;
    }

    public void setShiftKey(String shiftKey) {
        this.shiftKey = shiftKey;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getShiftText() {
        return shiftText;
    }

    public void setShiftText(String shiftText) {
        this.shiftText = shiftText;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

}
