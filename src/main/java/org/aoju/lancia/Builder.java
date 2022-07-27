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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.thread.NamedThreadFactory;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.aoju.lancia.kernel.page.QueryHandler;
import org.aoju.lancia.kernel.page.QuerySelector;
import org.aoju.lancia.nimble.runtime.CallFrame;
import org.aoju.lancia.nimble.runtime.ExceptionDetails;
import org.aoju.lancia.nimble.runtime.RemoteObject;
import org.aoju.lancia.worker.BrowserListener;
import org.aoju.lancia.worker.CDPSession;
import org.aoju.lancia.worker.EventEmitter;
import org.aoju.lancia.worker.ListenerWrapper;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公共方法
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Builder {


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
     * 每条线程下载的文件块大小 5M
     */
    private static final int CHUNK_SIZE = 5 << 20;
    /**
     * 重试次数
     */
    private static final int RETRY_TIMES = 5;
    /**
     * 失败结果
     */
    private static final String FAIL_RESULT = "-1";
    private static final Map<String, QueryHandler> CUSTOM_QUERY_HANDLER = new HashMap<>();
    /**
     * 单线程，一个浏览器只能有一个trcing 任务
     */
    private static ExecutorService COMMON_EXECUTOR = null;

    public static String createProtocolError(JSONObject node) {
        JSONObject methodNode = node.getJSONObject(RECV_MESSAGE_METHOD_PROPERTY);
        JSONObject errNode = node.getJSONObject(RECV_MESSAGE_ERROR_PROPERTY);
        String errorMsg = errNode.getString(RECV_MESSAGE_ERROR_MESSAGE_PROPERTY);
        String method = Normal.EMPTY;
        if (methodNode != null) {
            method = methodNode.toJSONString();
        }
        String message = "Protocol error " + method + ": " + errorMsg;
        String dataNode = errNode.getString(RECV_MESSAGE_ERROR_DATA_PROPERTY);
        if (dataNode != null) {
            message += " " + dataNode;
        }
        return message;
    }

    public static final void chmod(String path, String perms) throws IOException {
        if (StringKit.isEmpty(path)) {
            throw new IllegalArgumentException("Path must not be empty");
        }

        char[] chars = perms.toCharArray();
        if (chars.length != 3) {
            throw new IllegalArgumentException("perms length must be 3");
        }

        Path path1 = Paths.get(path);
        Set<PosixFilePermission> permissions = new HashSet<>();

        if ('1' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        } else if ('2' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_WRITE);
        } else if ('3' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        } else if ('4' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_READ);
        } else if ('5' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        } else if ('6' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
        } else if ('7' == chars[0]) {
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
        }

        if ('1' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
        } else if ('2' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_WRITE);
        } else if ('3' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_WRITE);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
        } else if ('4' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_READ);
        } else if ('5' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_READ);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
        } else if ('6' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_READ);
            permissions.add(PosixFilePermission.GROUP_WRITE);
        } else if ('7' == chars[1]) {
            permissions.add(PosixFilePermission.GROUP_READ);
            permissions.add(PosixFilePermission.GROUP_WRITE);
            permissions.add(PosixFilePermission.GROUP_EXECUTE);
        }

        if ('1' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        } else if ('2' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_WRITE);
        } else if ('3' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_WRITE);
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        } else if ('4' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_READ);
        } else if ('5' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_READ);
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        } else if ('6' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
        } else if ('7' == chars[2]) {
            permissions.add(PosixFilePermission.OTHERS_READ);
            permissions.add(PosixFilePermission.OTHERS_WRITE);
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);
        }

        Files.setPosixFilePermissions(path1, permissions);
    }

    public static final String join(String root, String... args) {
        return Paths.get(root, args).toString();
    }

    /**
     * 从协议读取流:用于跟踪文件的示例
     *
     * @param client  客户端
     * @param handler 发送给websocket的参数
     * @param path    文件存放的路径
     * @param isSync  是否是在新的线程中执行
     * @return 可能是特征，可能是字节数组
     * @throws IOException 操作文件的异常
     */
    public static final Object readProtocolStream(CDPSession client, String handler, String path, boolean isSync) throws IOException {
        if (isSync) {
            return commonExecutor().submit(() -> {
                try {
                    printPDF(client, handler, path);
                } catch (IOException e) {
                    Logger.error("Method readProtocolStream error", e);
                }
            });
        } else {
            return printPDF(client, handler, path);
        }
    }

    private static byte[] printPDF(CDPSession client, String handler, String path) throws IOException {
        boolean eof = false;
        File file = null;
        BufferedOutputStream writer = null;
        BufferedInputStream reader = null;

        if (StringKit.isNotEmpty(path)) {
            file = new File(path);
            createNewFile(file);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("handle", handler);
        try {

            if (file != null) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                writer = new BufferedOutputStream(fileOutputStream);
            }
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            byte[] bytes;
            List<byte[]> bufs = new ArrayList<>();
            int byteLength = 0;

            while (!eof) {
                JSONObject response = client.send("IO.read", params, true);
                String eofNode = response.getString(RECV_MESSAGE_STREAM_EOF_PROPERTY);
                Boolean base64EncodedNode = response.getBoolean(RECV_MESSAGE_BASE64ENCODED_PROPERTY);
                String dataText = response.getString(RECV_MESSAGE_STREAM_DATA_PROPERTY);

                if (StringKit.isNotEmpty(dataText)) {
                    try {
                        if (base64EncodedNode != null && base64EncodedNode) {
                            bytes = Base64.getDecoder().decode(dataText);
                        } else {
                            bytes = dataText.getBytes();
                        }
                        bufs.add(bytes);
                        byteLength += bytes.length;
                        // 转成二进制流 io
                        if (file != null) {
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                            reader = new BufferedInputStream(byteArrayInputStream);
                            int read;
                            while ((read = reader.read(buffer, 0, DEFAULT_BUFFER_SIZE)) != -1) {
                                writer.write(buffer, 0, read);
                                writer.flush();
                            }
                        }
                    } finally {
                        IoKit.close(reader);
                    }
                }
                eof = eofNode == null || Boolean.parseBoolean(eofNode);
            }
            client.send("IO.close", params, true);
            return getBytes(bufs, byteLength);
        } finally {
            IoKit.close(writer);
            IoKit.close(reader);
        }
    }

    /**
     * 多个字节数组转成一个字节数组
     *
     * @param bufs       数组集合
     * @param byteLength 数组总长度
     * @return 总数组
     */
    private static byte[] getBytes(List<byte[]> bufs, int byteLength) {
        // 返回字节数组
        byte[] resultBuf = new byte[byteLength];
        int destPos = 0;
        for (byte[] buf : bufs) {
            System.arraycopy(buf, 0, resultBuf, destPos, buf.length);
            destPos += buf.length;
        }
        return resultBuf;
    }

    public static String getExceptionMessage(ExceptionDetails exceptionDetails) {
        if (exceptionDetails.getException() != null)
            return StringKit.isNotEmpty(exceptionDetails.getException().getDescription()) ? exceptionDetails.getException().getDescription() : (String) exceptionDetails.getException().getValue();
        String message = exceptionDetails.getText();
        StringBuilder sb = new StringBuilder(message);
        if (exceptionDetails.getStackTrace() != null) {
            for (CallFrame callframe : exceptionDetails.getStackTrace().getCallFrames()) {
                String location = callframe.getUrl() + ":" + callframe.getColumnNumber() + ":" + callframe.getColumnNumber();
                String functionName = StringKit.isNotEmpty(callframe.getFunctionName()) ? callframe.getFunctionName() : "<anonymous>";
                sb.append("\n    at ").append(functionName).append("(").append(location).append(")");
            }
        }
        return sb.toString();
    }

    public static final Set<BrowserListener> getConcurrentSet() {
        return new CopyOnWriteArraySet<>();
    }

    public static final <T> ListenerWrapper<T> addEventListener(EventEmitter emitter, String eventName, BrowserListener<T> handler) {
        emitter.addListener(eventName, handler);
        return new ListenerWrapper<>(emitter, eventName, handler);
    }

    public static final void removeEventListeners(List<ListenerWrapper> eventListeners) {
        if (CollKit.isEmpty(eventListeners)) {
            return;
        }
        for (int i = 0; i < eventListeners.size(); i++) {
            ListenerWrapper wrapper = eventListeners.get(i);
            wrapper.getEmitter().removeListener(wrapper.getEventName(), wrapper.getHandler());
        }
    }

    public static final boolean isString(Object value) {
        if (value == null)
            return false;
        return value.getClass().equals(String.class);
    }

    public static final boolean isNumber(String s) {
        Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
        Matcher matcher = pattern.matcher(s);
        return matcher.matches();
    }

    public static Object valueFromRemoteObject(RemoteObject remoteObject) {
        Assert.isTrue(StringKit.isEmpty(remoteObject.getObjectId()), "Cannot extract value when objectId is given");
        if (StringKit.isNotEmpty(remoteObject.getUnserializableValue())) {
            if ("bigint".equals(remoteObject.getType()))
                return new BigInteger(remoteObject.getUnserializableValue().replace("n", Normal.EMPTY));
            switch (remoteObject.getUnserializableValue()) {
                case "-0":
                    return -0;
                case "NaN":
                    return "NaN";
                case "Infinity":
                    return "Infinity";
                case "-Infinity":
                    return "-Infinity";
                default:
                    throw new IllegalArgumentException("Unsupported unserializable value: " + remoteObject.getUnserializableValue());
            }
        }
        return remoteObject.getValue();
    }

    public static void releaseObject(CDPSession client, RemoteObject remoteObject, boolean isBlock) {
        if (StringKit.isEmpty(remoteObject.getObjectId()))
            return;
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", remoteObject.getObjectId());
        try {
            client.send("Runtime.releaseObject", params, isBlock);
        } catch (Exception e) {
            //重新导航到某个网页 或者页面已经关闭
            //在这种情况下不需要将这个错误在线程执行中抛出，打日志记录一下就可以了
        }
    }

    public static Object waitForEvent(EventEmitter eventEmitter, String eventName, Predicate predicate, int timeout, String abortPromise) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Object[] result = {null};
        BrowserListener listener = new BrowserListener() {
            @Override
            public void onBrowserEvent(Object event) {
                if (!predicate.test(event))
                    return;
                result[0] = event;
                latch.countDown();
            }
        };
        listener.setMethod(eventName);
        ListenerWrapper wrapper = addEventListener(eventEmitter, eventName, listener);
        try {
            boolean await = latch.await(timeout, TimeUnit.MILLISECONDS);
            if (!await) {
                throw new RuntimeException(abortPromise);
            }
            return result[0];
        } finally {
            List<ListenerWrapper> removes = new ArrayList<>();
            removes.add(wrapper);
            removeEventListeners(removes);
        }
    }

    public static final String evaluationString(String fun, PageEvaluateType type, Object... args) {
        if (PageEvaluateType.STRING.equals(type)) {
            Assert.isTrue(args.length == 0, "Cannot evaluate a string with arguments");
            return fun;
        }
        List<String> argsList = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) {
                argsList.add("Undefined");
            } else {
                argsList.add(JSON.toJSONString(arg));
            }
        }
        return MessageFormat.format("({0})({1})", fun, String.join(",", argsList));
    }

    /**
     * 通用执行者
     *
     * @return 执行服务
     */
    public static final ExecutorService commonExecutor() {
        if (COMMON_EXECUTOR == null) {
            synchronized (Builder.class) {
                if (COMMON_EXECUTOR == null) {
                    String customNum = System.getProperty(COMMONT_THREAD_POOL_NUM);
                    int threadNum = 0;
                    if (StringKit.isNotEmpty(customNum)) {
                        threadNum = Integer.parseInt(customNum);
                    } else {
                        threadNum = Math.max(1, Runtime.getRuntime().availableProcessors());
                    }
                    COMMON_EXECUTOR = new ThreadPoolExecutor(threadNum, threadNum, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), new NamedThreadFactory("common-pool-"));
                }
            }
        }
        return COMMON_EXECUTOR;
    }

    /**
     * 判断js字符串是否是一个函数
     *
     * @param pageFunction js字符串
     * @return true代表是js函数
     */
    public static final boolean isFunction(String pageFunction) {
        pageFunction = pageFunction.trim();
        return pageFunction.startsWith("function") || pageFunction.startsWith("async") || pageFunction.contains("=>");
    }

    public static final String toString(InputStream in) throws IOException {
        StringWriter wirter = null;
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(in);
            int bufferSize = 4096;
            int perReadcount;
            char[] buffer = new char[bufferSize];
            wirter = new StringWriter();
            while ((perReadcount = reader.read(buffer, 0, bufferSize)) != -1) {
                wirter.write(buffer, 0, perReadcount);
            }
            return wirter.toString();
        } finally {
            IoKit.close(wirter);
            IoKit.close(reader);
        }
    }

    public static QuerySelector getQueryHandlerAndSelector(String selector, String defaultQueryHandler) {
        Pattern pattern = Pattern.compile("^[a-zA-Z]+\\/");
        Matcher hasCustomQueryHandler = pattern.matcher(selector);
        if (!hasCustomQueryHandler.find())
            return new QuerySelector(selector, new QueryHandler() {
                @Override
                public String queryOne() {
                    return "(element,selector) =>\n" +
                            "      element.querySelector(selector)";
                }

                @Override
                public String queryAll() {
                    return "(element,selector) => element.querySelectorAll(selector)";
                }
            });
        int index = selector.indexOf("/");
        String name = selector.substring(0, index);
        String updatedSelector = selector.substring(index + 1);
        QueryHandler queryHandler = customQueryHandlers().get(name);
        if (queryHandler == null)
            throw new RuntimeException("Query set to use " + name + ", but no query handler of that name was found");
        return new QuerySelector(updatedSelector, queryHandler);
    }

    public static void registerCustomQueryHandler(String name, QueryHandler handler) {
        if (CUSTOM_QUERY_HANDLER.containsKey(name))
            throw new RuntimeException("A custom query handler named " + name + " already exists");
        Pattern pattern = Pattern.compile("^[a-zA-Z]+$");
        Matcher isValidName = pattern.matcher(name);
        if (!isValidName.matches())
            throw new IllegalArgumentException("Custom query handler names may only contain [a-zA-Z]");

        CUSTOM_QUERY_HANDLER.put(name, handler);
    }

    public static final void unregisterCustomQueryHandler(String name) {
        CUSTOM_QUERY_HANDLER.remove(name);
    }

    public static Map<String, QueryHandler> customQueryHandlers() {
        return CUSTOM_QUERY_HANDLER;
    }

    /**
     * 下载
     *
     * @param url              载的资源定位路径
     * @param filePath         文件路径
     * @param progressCallback 下载回调
     * @throws IOException          异常
     * @throws ExecutionException   异常
     * @throws InterruptedException 异常
     */
    public static void download(String url, String filePath, BiConsumer<Integer, Integer> progressCallback) throws IOException, ExecutionException, InterruptedException {
        long contentLength = getContentLength(url);

        long taskCount = contentLength % CHUNK_SIZE == 0 ? contentLength / CHUNK_SIZE : contentLength / CHUNK_SIZE + 1;
        createFile(filePath, contentLength);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5, 30000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
        List<Future<String>> futureList = new ArrayList<>();
        int downloadCount = 0;
        if (contentLength <= CHUNK_SIZE) {
            Future<String> future = completionService.submit(new DownloadCallable(0, contentLength, filePath, url));
            futureList.add(future);
        } else {
            for (int i = 0; i < taskCount; i++) {
                if (i == taskCount - 1) {
                    Future<String> future = completionService.submit(new DownloadCallable(i * CHUNK_SIZE, contentLength, filePath, url));
                    futureList.add(future);
                } else {
                    Future<String> future = completionService.submit(new DownloadCallable(i * CHUNK_SIZE, (i + 1) * CHUNK_SIZE, filePath, url));
                    futureList.add(future);
                }
            }
        }
        executor.shutdown();
        for (Future<String> future : futureList) {
            String result = future.get();
            if (FAIL_RESULT.equals(result)) {
                Logger.error("download fail,url:" + url);
                Files.delete(Paths.get(filePath));
                executor.shutdownNow();
            } else {
                try {
                    downloadCount += Integer.parseInt(result);
                    if (progressCallback != null) {
                        progressCallback.accept(downloadCount, (int) (contentLength >> 20));
                    }
                } catch (Exception e) {
                    Logger.error("ProgressCallback has some problem", e);
                }
            }
        }
    }

    /**
     * 获取下载得文件长度
     *
     * @param url 资源定位路径
     * @return 长度
     * @throws IOException 连接异常
     */
    public static final long getContentLength(String url) throws IOException {
        URL uuuRl = new URL(url);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) uuuRl.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(READ_TIME_OUT);
            conn.setReadTimeout(CONNECT_TIME_OUT);
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode <= 204) {
                return conn.getContentLengthLong();
            } else {
                throw new RuntimeException(url + " responseCode: " + responseCode);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 创建固定大小的文件
     *
     * @param path   文件路径
     * @param length 文件大小
     * @throws IOException 操作文件异常
     */
    public static void createFile(String path, long length) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            createNewFile(file);
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(path, "rw");
        randomAccessFile.setLength(length);
        randomAccessFile.close();
    }

    /**
     * 判断路径是否是可执行文件
     *
     * @param execPath 要判断文件路径
     * @return 可执行，返回true
     */
    public static boolean isExecutable(String execPath) {
        Path path = Paths.get(execPath);
        return Files.isRegularFile(path) && Files.isReadable(path) && Files.isExecutable(path);
    }

    /**
     * 移除文件
     *
     * @param path 要移除的路径
     */
    public static void remove(String path) {
        File file = new File(path);
        delete(file);
    }

    private static void delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    delete(f);
                }
            }
            file.deleteOnExit();
        } else {
            file.deleteOnExit();
        }
    }

    /**
     * 创建一个文件，如果该文件上的有些文件夹路径不存在，会自动创建文件夹。
     *
     * @param file 创建的文件
     * @throws IOException 异常
     */
    public static final void createNewFile(File file) throws IOException {
        if (!file.exists()) {
            mkdir(file.getParentFile());
            file.createNewFile();
        }
    }

    /**
     * 递归创建文件夹
     *
     * @param parent 要创建的文件夹
     */
    public static final void mkdir(File parent) {
        if (parent != null && !parent.exists()) {
            mkdir(parent.getParentFile());
            parent.mkdir();
        }
    }

    public void clearQueryHandlers() {
        CUSTOM_QUERY_HANDLER.clear();
    }

    static class DownloadCallable implements Callable<String> {

        private final long startPosition;

        private final long endPosition;

        private final String filePath;

        private final String url;

        public DownloadCallable(long startPosition, long endPosition, String filePath, String url) {
            this.startPosition = startPosition;
            this.endPosition = endPosition;
            this.filePath = filePath;
            this.url = url;
        }

        @Override
        public String call() {
            RandomAccessFile file = null;
            HttpURLConnection conn = null;
            try {
                file = new RandomAccessFile(this.filePath, "rw");
                file.seek(this.startPosition);
                URL uRL = new URL(this.url);
                conn = (HttpURLConnection) uRL.openConnection();
                conn.setConnectTimeout(CONNECT_TIME_OUT);
                conn.setReadTimeout(READ_TIME_OUT);
                conn.setRequestMethod(Http.GET);
                String range = "bytes=" + startPosition + "-" + endPosition;
                conn.addRequestProperty("Range", range);
                conn.addRequestProperty("accept-encoding", "gzip, deflate, br");
                ByteBuffer buffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE);
                FileChannel channel = file.getChannel();
                for (int j = 0; j < RETRY_TIMES; j++) {
                    try {
                        conn.connect();
                        InputStream inputStream = conn.getInputStream();
                        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
                        while (readableByteChannel.read(buffer) != -1) {
                            buffer.flip();
                            while (buffer.hasRemaining()) {
                                channel.write(buffer);
                            }
                            buffer.clear();
                        }
                        return String.valueOf((endPosition - startPosition) >> 20);
                    } catch (Exception e) {
                        if (j == RETRY_TIMES - 1) {
                            Logger.error("download url[{}] bytes[{}] fail.", url, range);
                        }
                    }
                }
                return FAIL_RESULT;
            } catch (Exception e) {
                Logger.error("download url[{}] bytes[{}] fail.", url, startPosition + "-" + endPosition);
                return FAIL_RESULT;
            } finally {
                IoKit.close(file);
                if (conn != null) {
                    conn.disconnect();
                }

            }
        }
    }

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
