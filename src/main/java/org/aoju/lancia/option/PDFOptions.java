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
package org.aoju.lancia.option;

import org.aoju.bus.core.lang.Normal;
import org.aoju.lancia.nimble.dom.Margin;

/**
 * 生成pdf时候需要的选项
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class PDFOptions {

    private double scale = 1.00;

    private boolean displayHeaderFooter = false;

    private String headerTemplate = Normal.EMPTY;

    private String footerTemplate = Normal.EMPTY;

    private boolean printBackground = false;

    private boolean landscape = false;

    private String pageRanges = "";

    private String format;

    private String width;

    private String height;

    private boolean preferCSSPageSize;

    private Margin margin = new Margin();

    private String path;

    public PDFOptions() {
        super();
    }

    public PDFOptions(String path) {
        this.path = path;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public String getHeaderTemplate() {
        return headerTemplate;
    }

    public void setHeaderTemplate(String headerTemplate) {
        this.headerTemplate = headerTemplate;
    }

    public String getFooterTemplate() {
        return footerTemplate;
    }

    public void setFooterTemplate(String footerTemplate) {
        this.footerTemplate = footerTemplate;
    }

    public String getPageRanges() {
        return pageRanges;
    }

    public void setPageRanges(String pageRanges) {
        this.pageRanges = pageRanges;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public Margin getMargin() {
        return margin;
    }

    public void setMargin(Margin margin) {
        this.margin = margin;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean getPreferCSSPageSize() {
        return preferCSSPageSize;
    }

    public void setPreferCSSPageSize(boolean preferCSSPageSize) {
        this.preferCSSPageSize = preferCSSPageSize;
    }

    public boolean getLandscape() {
        return landscape;
    }

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }

    public boolean getPrintBackground() {
        return printBackground;
    }

    public void setPrintBackground(boolean printBackground) {
        this.printBackground = printBackground;
    }

    public boolean getDisplayHeaderFooter() {
        return displayHeaderFooter;
    }

    public void setDisplayHeaderFooter(boolean displayHeaderFooter) {
        this.displayHeaderFooter = displayHeaderFooter;
    }

}
