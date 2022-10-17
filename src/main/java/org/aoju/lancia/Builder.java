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
package org.aoju.lancia;


import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.thread.NamedThreadFactory;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.aoju.lancia.events.BrowserListenerWrapper;
import org.aoju.lancia.events.DefaultBrowserListener;
import org.aoju.lancia.events.EventEmitter;
import org.aoju.lancia.kernel.page.QueryHandler;
import org.aoju.lancia.kernel.page.QuerySelector;
import org.aoju.lancia.nimble.PageEvaluateType;
import org.aoju.lancia.nimble.runtime.CallFrame;
import org.aoju.lancia.nimble.runtime.ExceptionDetails;
import org.aoju.lancia.nimble.runtime.RemoteObject;
import org.aoju.lancia.worker.CDPSession;

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
     * 指定版本
     */
    public static final String VERSION = "818858";
    /**
     * 临时文件夹前缀
     */
    public static final String PROFILE_PREFIX = "puppeteer_dev_chrome_profile-";
    /**
     * 把浏览器版本存放到环境变量的字段
     */
    public static final String PUPPETEER_CHROMIUM_REVISION = "PUPPETEER_CHROMIUM_REVISION";
    /**
     * 把产品存放到环境变量的所有可用字段
     */
    public static final String[] PRODUCT_ENV = {
            "PUPPETEER_PRODUCT",
            "java_config_puppeteer_product",
            "java_package_config_puppeteer_product"
    };
    /**
     * 把浏览器执行路径存放到环境变量的所有可用字段
     */
    public static final String[] EXECUTABLE_ENV = {
            "PUPPETEER_EXECUTABLE_PATH",
            "java_config_puppeteer_executable_path",
            "java_package_config_puppeteer_executable_path"
    };
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
        private static final long serialVersionUID = 1L;

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

        private static final long serialVersionUID = -5224857570151968464L;

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
     * fastjson的一个实例
     */
    public static final ObjectMapper OBJECTMAPPER = new ObjectMapper()
            .setVisibility(PropertyAccessor.FIELD, Visibility.ANY)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    /**
     * 追踪信息的默认分类
     */
    public static final Set<String> DEFAULTCATEGORIES = new LinkedHashSet<>() {
        private static final long serialVersionUID = -5224857570151968464L;

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
     * 读取流中的数据的buffer size
     */
    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    public static final String NONE = "NONE";
    public static final String ONEWAY = "ONEWAY";
    public static final String TWOWAY = "TWOWAY";

    public static final String MATCHED = "MATCHED";
    public static final String NOT_MATCHED = "NOT_MATCHED";
    public static final String CONTINUOUS = "CONTINUOUS";
    public static final String TEXT = "TEXT";
    public static final String BINARY = "BINARY";
    public static final String PING = "PING";
    public static final String PONG = "PONG";
    public static final String CLOSING = "CLOSING";

    /**
     * 默认的超时时间：启动浏览器实例超时，websocket接受消息超时等
     */
    public static final int DEFAULT_TIMEOUT = 30000;
    /**
     * 内置线程池的数量
     */
    public static final String COMMONT_THREAD_POOL_NUM = "common_thread_number";
    /**
     * 读取数据超时
     */
    public static final int READ_TIME_OUT = 10000;
    /**
     * 连接超时设置
     */
    public static final int CONNECT_TIME_OUT = 10000;

    /**
     * 线程池数量
     */
    private static final int THREAD_COUNT = 5;
    /**
     * 每条线程下载的文件块大小 5M
     */
    private static final int CHUNK_SIZE = 5 << 20;
    /**
     * 重试次数
     */
    private static final int RETRY_TIMES = 5;
    private static final String FAIL_RESULT = "-1";

    private static final Map<String, QueryHandler> QUERY_HANDLER = new HashMap<>();

    /**
     * 单线程，一个浏览器只能有一个trcing 任务
     */
    private static ExecutorService COMMON_EXECUTOR = null;

    public static final Map<String, Map<String, String>> DOWNLOAD_URL = new HashMap<>() {
        private static final long serialVersionUID = -6918778699407093058L;

        {
            put("chrome", new HashMap<>() {
                private static final long serialVersionUID = 3441562966233820720L;

                {
                    put("host", "https://npm.taobao.org/mirrors");
                    put("linux", "%s/chromium-browser-snapshots/Linux_x64/%s/%s.zip");
                    put("mac", "%s/chromium-browser-snapshots/Mac/%s/%s.zip");
                    put("win32", "%s/chromium-browser-snapshots/Win/%s/%s.zip");
                    put("win64", "%s/chromium-browser-snapshots/Win_x64/%s/%s.zip");
                }
            });
            put("firefox", new HashMap<>() {
                private static final long serialVersionUID = 2053771138227029401L;

                {
                    put("host", "https://github.com/puppeteer/juggler/releases");
                    put("linux", "%s/download/%s/%s.zip");
                    put("mac", "%s/download/%s/%s.zip");
                    put("win32", "%s/download/%s/%s.zip");
                    put("win64", "%s/download/%s/%s.zip");
                }
            });
        }
    };

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

    /**
     * 断言路径是否是可执行的exe文件
     *
     * @param executablePath 要断言的文件
     * @return 可执行，返回true
     */
    public static boolean assertExecutable(String executablePath) {
        Path path = Paths.get(executablePath);
        return Files.isRegularFile(path) && Files.isReadable(path) && Files.isExecutable(path);
    }

    public static void registerCustomQueryHandler(String name, QueryHandler handler) {
        if (QUERY_HANDLER.containsKey(name))
            throw new RuntimeException("A custom query handler named " + name + " already exists");
        Pattern pattern = Pattern.compile("^[a-zA-Z]+$");
        Matcher isValidName = pattern.matcher(name);
        if (!isValidName.matches())
            throw new IllegalArgumentException("Custom query handler names may only contain [a-zA-Z]");

        QUERY_HANDLER.put(name, handler);
    }

    public static final void unregisterCustomQueryHandler(String name) {
        QUERY_HANDLER.remove(name);
    }

    public void clearQueryHandlers() {
        QUERY_HANDLER.clear();
    }

    public static Map<String, QueryHandler> customQueryHandlers() {
        return QUERY_HANDLER;
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
                    return "(element,selector) =>\n" +
                            "      element.querySelectorAll(selector)";
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

        ThreadPoolExecutor executor = getExecutor();
        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
        List<Future<String>> futureList = new ArrayList<>();
        int downloadCount = 0;
        if (contentLength <= CHUNK_SIZE) {
            Future<String> future = completionService.submit(new DownloadCallable(0, contentLength, filePath, url));
            futureList.add(future);
        } else {
            for (int i = 0; i < taskCount; i++) {
                if (i == taskCount - 1) {
                    Future<String> future = completionService.submit(new DownloadCallable((long) i * CHUNK_SIZE, contentLength, filePath, url));
                    futureList.add(future);
                } else {
                    Future<String> future = completionService.submit(new DownloadCallable((long) i * CHUNK_SIZE, (long) (i + 1) * CHUNK_SIZE, filePath, url));
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
     * 创建一个用于下载chrome的线程池
     *
     * @return 线程池
     */
    public static ThreadPoolExecutor getExecutor() {
        return new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 30000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
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

    public static String createProtocolError(JsonNode node) {
        JsonNode methodNode = node.get(org.aoju.lancia.Builder.RECV_MESSAGE_METHOD_PROPERTY);
        JsonNode errNode = node.get(org.aoju.lancia.Builder.RECV_MESSAGE_ERROR_PROPERTY);
        JsonNode errorMsg = errNode.get(org.aoju.lancia.Builder.RECV_MESSAGE_ERROR_MESSAGE_PROPERTY);
        String method = "";
        if (methodNode != null) {
            method = methodNode.asText();
        }
        String message = "Protocol error " + method + ": " + errorMsg;
        JsonNode dataNode = errNode.get(org.aoju.lancia.Builder.RECV_MESSAGE_ERROR_DATA_PROPERTY);
        if (dataNode != null) {
            message += " " + dataNode;
        }
        return message;
    }

    public static final void chmod(String path, String perms) throws IOException {

        if (StringKit.isEmpty(path))
            throw new IllegalArgumentException("Path must not be empty");

        char[] chars = perms.toCharArray();
        if (chars.length != 3) throw new IllegalArgumentException("perms length must be 3");

        Path path1 = Paths.get(path);
        Set<PosixFilePermission> permissions = new HashSet<>();
        //own
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
        //group
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
        //other
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
        return java.nio.file.Paths.get(root, args).toString();
    }

    /**
     * read stream from protocol : example for tracing  file
     *
     * @param client  CDPSession
     * @param handler 发送给websocket的参数
     * @param path    文件存放的路径
     * @param isSync  是否是在新的线程中执行
     * @return 可能是feture，可能是字节数组
     * @throws IOException 操作文件的异常
     */
    public static final Object readProtocolStream(CDPSession client, String handler, String path, boolean isSync) throws IOException {
        if (isSync) {
            return Builder.commonExecutor().submit(() -> {
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
            org.aoju.lancia.Builder.createNewFile(file);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("handle", handler);
        try {

            if (file != null) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                writer = new BufferedOutputStream(fileOutputStream);
            }
            byte[] buffer = new byte[org.aoju.lancia.Builder.DEFAULT_BUFFER_SIZE];
            byte[] bytes;
            List<byte[]> bufs = new ArrayList<>();
            int byteLength = 0;

            while (!eof) {
                JsonNode response = client.send("IO.read", params, true);
                JsonNode eofNode = response.get(org.aoju.lancia.Builder.RECV_MESSAGE_STREAM_EOF_PROPERTY);
                JsonNode base64EncodedNode = response.get(org.aoju.lancia.Builder.RECV_MESSAGE_BASE64ENCODED_PROPERTY);
                JsonNode dataNode = response.get(org.aoju.lancia.Builder.RECV_MESSAGE_STREAM_DATA_PROPERTY);
                String dataText;

                if (dataNode != null && StringKit.isNotEmpty(dataText = dataNode.asText())) {
                    try {
                        if (base64EncodedNode != null && base64EncodedNode.asBoolean()) {
                            bytes = Base64.getDecoder().decode(dataText);
                        } else {
                            bytes = dataNode.asText().getBytes();
                        }
                        bufs.add(bytes);
                        byteLength += bytes.length;
                        //转成二进制流 io
                        if (file != null) {
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                            reader = new BufferedInputStream(byteArrayInputStream);
                            int read;
                            while ((read = reader.read(buffer, 0, org.aoju.lancia.Builder.DEFAULT_BUFFER_SIZE)) != -1) {
                                writer.write(buffer, 0, read);
                                writer.flush();
                            }
                        }
                    } finally {
                        IoKit.close(reader);
                    }
                }
                eof = eofNode == null || eofNode.asBoolean();
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
        //返回字节数组
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

    public static final Set<DefaultBrowserListener> getConcurrentSet() {
        return new CopyOnWriteArraySet<>();
    }

    public static final <T> BrowserListenerWrapper<T> addEventListener(EventEmitter emitter, String eventName, DefaultBrowserListener<T> handler) {
        emitter.addListener(eventName, handler);
        return new BrowserListenerWrapper<>(emitter, eventName, handler);
    }

    public static final void removeEventListeners(List<BrowserListenerWrapper> eventListeners) {
        if (CollKit.isEmpty(eventListeners)) {
            return;
        }
        for (int i = 0; i < eventListeners.size(); i++) {
            BrowserListenerWrapper wrapper = eventListeners.get(i);
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
                return new BigInteger(remoteObject.getUnserializableValue().replace("n", ""));
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
            // Exceptions might happen in case of a page been navigated or closed.
            //重新导航到某个网页 或者页面已经关闭
            // Swallow these since they are harmless and we don't leak anything in this case.
            //在这种情况下不需要将这个错误在线程执行中抛出，打日志记录一下就可以了
        }
    }

    public static Object waitForEvent(EventEmitter eventEmitter, String eventName, Predicate predicate, int timeout, String abortPromise) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final Object[] result = {null};
        DefaultBrowserListener listener = new DefaultBrowserListener() {
            @Override
            public void onBrowserEvent(Object event) {
                if (!predicate.test(event))
                    return;
                result[0] = event;
                latch.countDown();
            }
        };
        listener.setMethod(eventName);
        BrowserListenerWrapper wrapper = addEventListener(eventEmitter, eventName, listener);
        try {
            boolean await = latch.await(timeout, TimeUnit.MILLISECONDS);
            if (!await) {
                throw new RuntimeException(abortPromise);
            }
            return result[0];
        } finally {
            List<BrowserListenerWrapper> removes = new ArrayList<>();
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
                argsList.add("undefined");
            } else {
                try {
                    argsList.add(org.aoju.lancia.Builder.OBJECTMAPPER.writeValueAsString(arg));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return MessageFormat.format("({0})({1})", fun, String.join(",", argsList));
    }

    public static final ExecutorService commonExecutor() {
        if (COMMON_EXECUTOR == null) {
            synchronized (Builder.class) {
                if (COMMON_EXECUTOR == null) {
                    String customNum = System.getProperty(org.aoju.lancia.Builder.COMMONT_THREAD_POOL_NUM);
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

    public static final CompletionService completionService() {
        return new ExecutorCompletionService(Builder.commonExecutor());
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
                conn.setRequestMethod("GET");
                String range = "bytes=" + startPosition + "-" + endPosition;
                conn.addRequestProperty("Range", range);
                conn.addRequestProperty("accept-encoding", "gzip, deflate, br");
                ByteBuffer buffer = ByteBuffer.allocate(Builder.DEFAULT_BUFFER_SIZE);
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

}
