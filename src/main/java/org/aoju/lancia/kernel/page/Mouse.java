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
package org.aoju.lancia.kernel.page;

import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.lancia.option.ClickOptions;
import org.aoju.lancia.worker.CDPSession;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * 鼠标
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Mouse {

    private final CDPSession client;

    private final Keyboard keyboard;

    private double x;

    private double y;

    private String button;

    public Mouse(CDPSession client, Keyboard keyboard) {
        this.client = client;
        this.keyboard = keyboard;
        this.x = 0;
        this.y = 0;
        /* @type {'none'|'left'|'right'|'middle'} */
        this.button = "none";
    }

    public void move(double x, double y) {
        this.move(x, y, 1);
    }

    public void move(double x, double y, int steps) {
        if (steps == 0) {
            steps = 1;
        }
        double fromX = this.x, fromY = this.y;
        this.x = x;
        this.y = y;
        for (int i = 1; i <= steps; i++) {
            stepRun(steps, fromX, fromY, i);
        }
    }

    private void stepRun(double steps, double fromX, double fromY, int i) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "mouseMoved");
        params.put("button", this.button);
        BigDecimal divide = new BigDecimal(i).divide(new BigDecimal(steps), 17, RoundingMode.HALF_UP);
        params.put("x", divide.multiply(BigDecimal.valueOf(this.x - fromX)).add(new BigDecimal(fromX)).doubleValue());
        params.put("y", divide.multiply(BigDecimal.valueOf(this.y - fromY)).add(new BigDecimal(fromY)).doubleValue());
        params.put("modifiers", this.keyboard.getModifiers());
        this.client.send("Input.dispatchMouseEvent", params, true);
    }

    public void click(int x, int y, ClickOptions options) throws InterruptedException {
        if (options.getDelay() != 0) {
            this.move(x, y, 0);
            this.down(options);
            if (options.getDelay() > 0) {
                Thread.sleep(options.getDelay());
            }
        } else {
            this.move(x, y, 0);
            this.down(options);
        }
        this.up(options);
    }

    public void up() {
        this.up(new ClickOptions());
    }

    public void up(ClickOptions options) {
        String button = "left";
        int clickCount = 1;
        this.button = "none";
        if (StringKit.isNotEmpty(options.getButton())) {
            button = options.getButton();
        }
        if (options.getClickCount() != 0) {
            clickCount = options.getClickCount();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("type", "mouseReleased");
        params.put("button", button);
        params.put("x", this.x);
        params.put("y", this.y);
        params.put("modifiers", this.keyboard.getModifiers());
        params.put("clickCount", clickCount);
        this.client.send("Input.dispatchMouseEvent", params, true);
    }

    public void down() {
        this.down(new ClickOptions());
    }

    public void down(ClickOptions options) {
        String button = "left";
        int clickCount = 1;
        if (StringKit.isNotEmpty(options.getButton())) {
            button = options.getButton();
        }
        if (options.getClickCount() != 0) {
            clickCount = options.getClickCount();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("type", "mousePressed");
        params.put("button", button);
        params.put("x", this.x);
        params.put("y", this.y);
        params.put("modifiers", this.keyboard.getModifiers());
        params.put("clickCount", clickCount);
        this.client.send("Input.dispatchMouseEvent", params, true);
    }

    public int buttonNameToButton(String buttonName) {
        if ("left".equals(buttonName))
            return 0;
        if ("middle".equals(buttonName))
            return 1;
        if ("right".equals(buttonName))
            return 2;
        throw new IllegalArgumentException("Unknown ButtonName: " + buttonName);
    }

    /**
     * 触发一个鼠标滚轮事件
     *
     * @param deltaX 坐标x
     * @param deltaY 坐标y
     */
    public void wheel(double deltaX, double deltaY) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "mouseWheel");
        params.put("x", this.x);
        params.put("y", this.y);
        params.put("deltaX", deltaX);
        params.put("deltaY", deltaY);
        params.put("modifiers", this.keyboard.getModifiers());
        params.put("pointerType", "mouse");
        this.client.send("Input.dispatchMouseEvent", params, true);
    }

    /**
     * 触发一个鼠标滚轮事件
     */
    public void wheel() {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "mouseWheel");
        params.put("x", this.x);
        params.put("y", this.y);
        params.put("deltaX", 0.00);
        params.put("deltaY", 0.00);
        params.put("modifiers", this.keyboard.getModifiers());
        params.put("pointerType", "mouse");
        this.client.send("Input.dispatchMouseEvent", params, true);
    }

}
