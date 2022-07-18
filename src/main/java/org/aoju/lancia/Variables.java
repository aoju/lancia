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
package org.aoju.lancia;

import java.util.*;

/**
 * 存放所用到的常量
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Variables {

    /**
     * 读取数据超时
     */
    public static final int READ_TIME_OUT = 10000;
    /**
     * 连接超时设置
     */
    public static final int CONNECT_TIME_OUT = 10000;
    /**
     * 指定版本
     */
    public static final String VERSION = "818858";
    /**
     * 临时文件夹前缀
     */
    public static final String PROFILE_PREFIX = "puppeteer_dev_chrome_profile-";
    /**
     * 把产品存放到环境变量的所有可用字段
     */
    public static final String[] PRODUCT_ENV = {"PUPPETEER_PRODUCT", "java_config_puppeteer_product", "java_package_config_puppeteer_product"};
    /**
     * 把浏览器执行路径存放到环境变量的所有可用字段
     */
    public static final String[] EXECUTABLE_ENV = {"PUPPETEER_EXECUTABLE_PATH", "java_config_puppeteer_executable_path", "java_package_config_puppeteer_executable_path"};
    /**
     * 把浏览器版本存放到环境变量的字段
     */
    public static final String PUPPETEER_CHROMIUM_REVISION_ENV = "PUPPETEER_CHROMIUM_REVISION";
    /**
     * 读取流中的数据的buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
    /**
     * 启动浏览器时，如果没有指定路径，那么会从以下路径搜索可执行的路径
     */
    public static final String[] PROBABLE_CHROME_EXECUTABLE_PATH =
            new String[]{
                    "/usr/bin/chromium",
                    "/usr/bin/chromium-browser",
                    "/usr/bin/google-chrome-stable",
                    "/usr/bin/google-chrome",
                    "/Applications/Chromium.app/Contents/MacOS/Chromium",
                    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                    "/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary",
                    "C:/Program Files (x86)/Google/Chrome/Application/chrome.exe",
                    "C:/Program Files/Google/Chrome/Application/chrome.exe"
            };
    /**
     * 谷歌浏览器默认启动参数
     */
    public static final List<String> DEFAULT_ARGS = Collections.unmodifiableList(new ArrayList<>() {
        {
            addAll(Arrays.asList(
                    "--disable-background-networking",
                    "--disable-background-timer-throttling",
                    "--disable-breakpad",
                    "--disable-browser-side-navigation",
                    "--disable-client-side-phishing-detection",
                    "--disable-default-apps",
                    "--disable-dev-shm-usage",
                    "--disable-extensions",
                    "--disable-features=site-per-process",
                    "--disable-hang-monitor",
                    "--disable-popup-blocking",
                    "--disable-prompt-on-repost",
                    "--disable-sync",
                    "--disable-translate",
                    "--metrics-recording-only",
                    "--no-first-run",
                    "--safebrowsing-disable-auto-update",
                    "--enable-automation",
                    "--password-store=basic",
                    "--use-mock-keychain"));
        }
    });

    public static final Set<String> SUPPORTED_METRICS = new HashSet<>() {
        {
            add("Timestamp");
            add("Documents");
            add("Frames");
            add("JSEventListeners");
            add("Nodes");
            add("LayoutCount");
            add("RecalcStyleCount");
            add("LayoutDuration");
            add("RecalcStyleDuration");
            add("ScriptDuration");
            add("TaskDuration");
            add("JSHeapUsedSize");
            add("JSHeapTotalSize");
        }
    };

    /**
     * 从浏览器的websocket接受到消息中有以下这些字段，在处理消息用到这些字段
     */
    public static final String RECV_MESSAGE_METHOD_PROPERTY = "method";
    public static final String RECV_MESSAGE_PARAMS_PROPERTY = "params";
    public static final String RECV_MESSAGE_ID_PROPERTY = "id";
    public static final String RECV_MESSAGE_RESULT_PROPERTY = "result";
    public static final String RECV_MESSAGE_SESSION_ID_PROPERTY = "sessionId";
    public static final String RECV_MESSAGE_TARGETINFO_PROPERTY = "targetInfo";
    public static final String RECV_MESSAGE_TYPE_PROPERTY = "type";
    public static final String RECV_MESSAGE_ERROR_PROPERTY = "error";
    public static final String RECV_MESSAGE_ERROR_MESSAGE_PROPERTY = "message";
    public static final String RECV_MESSAGE_ERROR_DATA_PROPERTY = "data";
    public static final String RECV_MESSAGE_TARFETINFO_TARGETID_PROPERTY = "targetId";
    public static final String RECV_MESSAGE_STREAM_PROPERTY = "stream";
    public static final String RECV_MESSAGE_STREAM_EOF_PROPERTY = "eof";
    public static final String RECV_MESSAGE_STREAM_DATA_PROPERTY = "data";
    public static final String RECV_MESSAGE_BASE64ENCODED_PROPERTY = "base64Encoded";

    /**
     * 默认的超时时间：启动浏览器实例超时，websocket接受消息超时等
     */
    public static final int DEFAULT_TIMEOUT = 30000;

    /**
     * 追踪信息的默认分类
     */
    public static final Set<String> DEFAULTCATEGORIES = new LinkedHashSet<>() {
        {
            add("-*");
            add("devtools.timeline");
            add("v8.execute");
            add("disabled-by-default-devtools.timeline");
            add("disabled-by-default-devtools.timeline.frame");
            add("toplevel");
            add("blink.console");
            add("blink.user_timing");
            add("latencyInfo");
            add("disabled-by-default-devtools.timeline.stack");
            add("disabled-by-default-v8.cpu_profiler");
            add("disabled-by-default-v8.cpu_profiler.hires");
        }
    };

    /**
     * 内置线程池的数量
     */
    public static final String COMMONT_THREAD_POOL_NUM = "common_thread_number";

    /**
     * 要监听的事件的名字枚举类
     */
    public enum Event {

        PAGE_CLOSE("close"),
        PAGE_CONSOLE("console"),
        PAGE_DIALOG("dialog"),
        PAGE_DOMContentLoaded("domcontentloaded"),
        PAGE_ERROR("error"),
        PAGE_PageError("pageerror"),
        PAGE_REQUEST("request"),
        PAGE_RESPONSE("response"),
        PAGE_REQUESTFAILED("requestfailed"),
        PAGE_REQUESTFINISHED("requestfinished"),
        PAGE_FRAMEATTACHED("frameattached"),
        PAGE_FRAMEDETACHED("framedetached"),
        PAGE_FRAMENAVIGATED("framenavigated"),
        PAGE_LOAD("load"),
        PAGE_METRICS("metrics"),
        PAGE_POPUP("popup"),
        PAGE_WORKERCREATED("workercreated"),
        PAGE_WORKERDESTROYED("workerdestroyed"),

        BROWSER_TARGETCREATED("targetcreated"),
        BROWSER_TARGETDESTROYED("targetdestroyed"),
        BROWSER_TARGETCHANGED("targetchanged"),
        BROWSER_DISCONNECTED("disconnected"),

        BROWSERCONTEXT_TARGETCREATED("targetcreated"),
        BROWSERCONTEXT_TARGETDESTROYED("targetdestroyed"),
        BROWSERCONTEXT_TARGETCHANGED("targetchanged"),

        NETWORK_MANAGER_REQUEST("Events.NetworkManager.Request"),
        NETWORK_MANAGER_RESPONSE("Events.NetworkManager.Response"),
        NETWORK_MANAGER_REQUEST_FAILED("Events.NetworkManager.RequestFailed"),
        NETWORK_MANAGER_REQUEST_FINISHED("Events.NetworkManager.RequestFinished"),

        FRAME_MANAGER_FRAME_ATTACHED("Events.FrameManager.FrameAttached"),
        FRAME_MANAGER_FRAME_NAVIGATED("Events.FrameManager.FrameNavigated"),
        FRAME_MANAGER_FRAME_DETACHED("Events.FrameManager.FrameDetached"),
        FRAME_MANAGER_LIFECYCLE_EVENT("Events.FrameManager.LifecycleEvent"),
        FRAME_MANAGER_FRAME_NAVIGATED_WITHIN_DOCUMENT("Events.FrameManager.FrameNavigatedWithinDocument"),
        FRAME_MANAGER_EXECUTION_CONTEXTCREATED("Events.FrameManager.ExecutionContextCreated"),
        FRAME_MANAGER_EXECUTION_CONTEXTDESTROYED("Events.FrameManager.ExecutionContextDestroyed"),

        CONNECTION_DISCONNECTED("Events.Connection.Disconnected"),
        CDPSESSION_DISCONNECTED("Events.CDPSession.Disconnected");

        private String name;

        Event(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public enum Result {

        CONTENT_SUCCESS("Content-success"),
        SUCCESS("success"),
        TIMEOUT("timeout"),
        TERMINATION("termination"),
        ERROR("error");

        private final String result;

        Result(String result) {
            this.result = result;
        }

        public String getResult() {
            return result;
        }

    }

    public enum Paper {

        letter(8.5, 11),
        legal(8.5, 14),
        tabloid(11, 17),
        ledger(17, 11),
        a0(33.1, 46.8),
        a1(23.4, 33.1),
        a2(16.54, 23.4),
        a3(11.7, 16.54),
        a4(8.27, 11.7),
        a5(5.83, 8.27),
        a6(4.13, 5.83);

        private double width;

        private double height;

        Paper(double width, double height) {
            this.width = width;
            this.height = height;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

    }

    public enum PageEvaluateType {

        STRING("string"),
        NUMBER("number"),
        FUNCTION("function");

        private String type;

        PageEvaluateType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    public enum DialogType {

        Alert("alert"),
        BeforeUnload("beforeunload"),
        Confirm("confirm"),
        Prompt("prompt");

        private String type;

        DialogType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

    /**
     * 视力缺陷类,设置不同级别的实力缺陷，截图有不同的效果
     */
    public enum VisionDeficiency {

        ACHROMATOPSIA("achromatopsia"),
        DEUTERANOPIA("deuteranopia"),
        PROTANOPIA("protanopia"),
        TRITANOPIA("tritanopia"),
        BLURREDVISION("blurredVision"),
        NONE("none");

        private final String value;

        VisionDeficiency(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

}
