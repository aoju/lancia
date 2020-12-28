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

import org.aoju.lancia.nimble.ScreenOrientation;
import org.aoju.lancia.worker.CDPSession;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class EmulationManager {

    private final CDPSession client;

    private boolean emulatingMobile;

    private boolean hasTouch;

    public EmulationManager(CDPSession client) {
        this.client = client;
    }

    public boolean emulateViewport(Viewport viewport) {
        boolean mobile = viewport.getIsMobile();
        int width = viewport.getWidth();
        int height = viewport.getHeight();
        Number deviceScaleFactor = 1;
        if (viewport.getDeviceScaleFactor() != null && viewport.getDeviceScaleFactor().intValue() != 0) {
            deviceScaleFactor = viewport.getDeviceScaleFactor();
        }

        ScreenOrientation screenOrientation = new ScreenOrientation();
        if (viewport.getIsLandscape()) {
            screenOrientation.setAngle(90);
            screenOrientation.setType("'landscapePrimary");
        } else {
            screenOrientation.setAngle(0);
            screenOrientation.setType("portraitPrimary");
        }

        boolean hasTouch = viewport.getHasTouch();

        Map<String, Object> params = new HashMap<>();
        params.put("mobile", mobile);
        params.put("width", width);
        params.put("height", height);
        params.put("deviceScaleFactor", deviceScaleFactor);
        params.put("screenOrientation", screenOrientation);
        this.client.send("Emulation.setDeviceMetricsOverride", params, false);
        params.clear();
        params.put("enabled", hasTouch);
        this.client.send("Emulation.setTouchEmulationEnabled", params, true);
        boolean reloadNeeded = this.emulatingMobile != mobile || this.hasTouch != hasTouch;
        this.emulatingMobile = mobile;
        this.hasTouch = hasTouch;
        return reloadNeeded;
    }

}
