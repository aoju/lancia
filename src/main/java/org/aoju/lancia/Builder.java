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
package org.aoju.lancia;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Normal;
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
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class Builder {

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

    public static String createProtocolError(JsonNode node) {
        JsonNode methodNode = node.get(Variables.RECV_MESSAGE_METHOD_PROPERTY);
        JsonNode errNode = node.get(Variables.RECV_MESSAGE_ERROR_PROPERTY);
        JsonNode errorMsg = errNode.get(Variables.RECV_MESSAGE_ERROR_MESSAGE_PROPERTY);
        String method = Normal.EMPTY;
        if (methodNode != null) {
            method = methodNode.asText();
        }
        String message = "Protocol error " + method + ": " + errorMsg;
        JsonNode dataNode = errNode.get(Variables.RECV_MESSAGE_ERROR_DATA_PROPERTY);
        if (dataNode != null) {
            message += " " + dataNode.toString();
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
            byte[] buffer = new byte[Variables.DEFAULT_BUFFER_SIZE];
            byte[] bytes;
            List<byte[]> bufs = new ArrayList<>();
            int byteLength = 0;

            while (!eof) {
                JsonNode response = client.send("IO.read", params, true);
                JsonNode eofNode = response.get(Variables.RECV_MESSAGE_STREAM_EOF_PROPERTY);
                JsonNode base64EncodedNode = response.get(Variables.RECV_MESSAGE_BASE64ENCODED_PROPERTY);
                JsonNode dataNode = response.get(Variables.RECV_MESSAGE_STREAM_DATA_PROPERTY);
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
                        // 转成二进制流 io
                        if (file != null) {
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
                            reader = new BufferedInputStream(byteArrayInputStream);
                            int read;
                            while ((read = reader.read(buffer, 0, Variables.DEFAULT_BUFFER_SIZE)) != -1) {
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
        listener.setMothod(eventName);
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

    public static final String evaluationString(String fun, Variables.PageEvaluateType type, Object... args) {
        if (Variables.PageEvaluateType.STRING.equals(type)) {
            Assert.isTrue(args.length == 0, "Cannot evaluate a string with arguments");
            return fun;
        }
        List<String> argsList = new ArrayList<>();
        for (Object arg : args) {
            if (arg == null) {
                argsList.add("Undefined");
            } else {
                try {
                    argsList.add(Variables.OBJECTMAPPER.writeValueAsString(arg));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
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
                    String customNum = System.getProperty(Variables.COMMONT_THREAD_POOL_NUM);
                    int threadNum = 0;
                    if (StringKit.isNotEmpty(customNum)) {
                        threadNum = Integer.parseInt(customNum);
                    } else {
                        threadNum = Math.max(1, Runtime.getRuntime().availableProcessors());
                    }
                    COMMON_EXECUTOR = new ThreadPoolExecutor(threadNum, threadNum, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), new CommonThreadFactory());
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
            conn.setConnectTimeout(Variables.READ_TIME_OUT);
            conn.setReadTimeout(Variables.CONNECT_TIME_OUT);
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

    static class CommonThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CommonThreadFactory() {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "common-pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            t.setDaemon(true);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
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
                conn.setConnectTimeout(Variables.CONNECT_TIME_OUT);
                conn.setReadTimeout(Variables.READ_TIME_OUT);
                conn.setRequestMethod(Http.GET);
                String range = "bytes=" + startPosition + "-" + endPosition;
                conn.addRequestProperty("Range", range);
                conn.addRequestProperty("accept-encoding", "gzip, deflate, br");
                ByteBuffer buffer = ByteBuffer.allocate(Variables.DEFAULT_BUFFER_SIZE);
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
