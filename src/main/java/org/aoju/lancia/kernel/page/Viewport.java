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

/**
 * 查看事件
 *
 * @author Kimi Liu
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class Viewport {

    /**
     * 页面宽度像素
     */
    private int width = 800;
    /**
     * 页面高度像素
     */
    private int height = 600;
    /**
     * 设置设备的缩放（可以认为是 dpr）默认是 1
     */
    private Number deviceScaleFactor = 1;
    /**
     * 是否在页面中设置了 meta viewport 标签。默认是 false
     */
    private boolean isMobile;
    /**
     * 指定viewport是否支持触摸事件。默认是 false
     */
    private boolean hasTouch;
    /**
     * 指定视口是否处于横向模式。默认是 false
     */
    private boolean isLandscape;

    public Viewport() {
    }

    public Viewport(int width, int height, Number deviceScaleFactor, boolean isMobile, boolean hasTouch, boolean isLandscape) {
        this.width = width;
        this.height = height;
        this.deviceScaleFactor = deviceScaleFactor;
        this.isMobile = isMobile;
        this.hasTouch = hasTouch;
        this.isLandscape = isLandscape;
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

    public Number getDeviceScaleFactor() {
        return deviceScaleFactor;
    }

    public void setDeviceScaleFactor(Number deviceScaleFactor) {
        this.deviceScaleFactor = deviceScaleFactor;
    }

    public boolean getIsMobile() {
        return isMobile;
    }

    public void setIsMobile(boolean isMobile) {
        this.isMobile = isMobile;
    }

    public boolean getHasTouch() {
        return hasTouch;
    }

    public void setHasTouch(boolean hasTouch) {
        this.hasTouch = hasTouch;
    }

    public boolean getIsLandscape() {
        return isLandscape;
    }

    public void setIsLandscape(boolean isLandscape) {
        this.isLandscape = isLandscape;
    }

}
