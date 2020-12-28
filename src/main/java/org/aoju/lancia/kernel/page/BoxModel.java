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
package org.aoju.lancia.kernel.page;

import org.aoju.lancia.nimble.ClickablePoint;

import java.util.List;

/**
 * @author Kimi Liu
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class BoxModel {

    private List<ClickablePoint> content;
    private List<ClickablePoint> padding;
    private List<ClickablePoint> border;
    private List<ClickablePoint> margin;
    private int width;
    private int height;

    public BoxModel(List<ClickablePoint> content, List<ClickablePoint> padding, List<ClickablePoint> border, List<ClickablePoint> margin, int width, int height) {
        this.content = content;
        this.padding = padding;
        this.border = border;
        this.margin = margin;
        this.width = width;
        this.height = height;
    }

    public BoxModel() {
    }

    public List<ClickablePoint> getContent() {
        return content;
    }

    public void setContent(List<ClickablePoint> content) {
        this.content = content;
    }

    public List<ClickablePoint> getPadding() {
        return padding;
    }

    public void setPadding(List<ClickablePoint> padding) {
        this.padding = padding;
    }

    public List<ClickablePoint> getBorder() {
        return border;
    }

    public void setBorder(List<ClickablePoint> border) {
        this.border = border;
    }

    public List<ClickablePoint> getMargin() {
        return margin;
    }

    public void setMargin(List<ClickablePoint> margin) {
        this.margin = margin;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}
