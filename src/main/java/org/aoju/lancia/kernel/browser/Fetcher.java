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
package org.aoju.lancia.kernel.browser;

import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.core.toolkit.ZipKit;
import org.aoju.bus.health.Platform;
import org.aoju.bus.logger.Logger;
import org.aoju.lancia.Builder;
import org.aoju.lancia.Variables;
import org.aoju.lancia.option.FetcherOption;

import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用于下载chrome浏览器
 *
 * @author Kimi Liu
 * @version 1.2.2
 * @since JDK 1.8+
 */
public class Fetcher {

    public static final Map<String, Map<String, String>> DOWNLOAD_URL = new HashMap<String, Map<String, String>>() {
        {
            put("chrome", new HashMap<String, String>() {
                {
                    put("host", "https://npm.taobao.org/mirrors");
                    put("linux", "%s/chromium-browser-snapshots/Linux_x64/%s/%s.zip");
                    put("mac", "%s/chromium-browser-snapshots/Mac/%s/%s.zip");
                    put("win32", "%s/chromium-browser-snapshots/Win/%s/%s.zip");
                    put("win64", "%s/chromium-browser-snapshots/Win_x64/%s/%s.zip");
                }
            });
            put("firefox", new HashMap<String, String>() {
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

    /**
     * 下载的域名
     */
    private final String downloadHost;
    /**
     * 目前支持两种产品：chrome or firefix
     */
    private final String product;
    /**
     * 平台 win linux mac
     */
    private String platform;
    /**
     * 下载的文件夹
     */
    private String downloadsFolder;

    public Fetcher() {
        this.product = "chrome";
        this.downloadsFolder = Builder.join(System.getProperty("user.dir"), ".local-browser");
        this.downloadHost = DOWNLOAD_URL.get(this.product).get("host");
        if (platform == null) {
            if (Platform.isMac()) {
                this.platform = "mac";
            } else if (Platform.isLinux()) {
                this.platform = "linux";
            } else if (Platform.isWindows()) {
                this.platform = Platform.is64Bit() ? "win64" : "win32";
            }
            Assert.notNull(this.platform, "Unsupported platform: " + Platform.getNativeLibraryResourcePrefix());
        }
        Assert.notNull(DOWNLOAD_URL.get(this.product).get(this.platform), "Unsupported platform: " + this.platform);
    }

    /**
     * 创建 BrowserFetcher 对象
     *
     * @param projectRoot 根目录，储存浏览器得根目录
     * @param options     下载浏览器得一些配置
     */
    public Fetcher(String projectRoot, FetcherOption options) {
        this.product = (StringKit.isNotEmpty(options.getProduct()) ? options.getProduct() : "chrome").toLowerCase();
        Assert.isTrue("chrome".equals(product) || "firefox".equals(product), "Unkown product: " + options.getProduct());
        this.downloadsFolder = StringKit.isNotEmpty(options.getPath()) ? options.getPath() : Builder.join(projectRoot, ".local-browser");
        this.downloadHost = StringKit.isNotEmpty(options.getHost()) ? options.getHost() : DOWNLOAD_URL.get(this.product).get("host");
        this.platform = StringKit.isNotEmpty(options.getPlatform()) ? options.getPlatform() : null;
        if (platform == null) {
            if (Platform.isMac()) {
                this.platform = "mac";
            } else if (Platform.isLinux()) {
                this.platform = "linux";
            } else if (Platform.isWindows()) {
                this.platform = Platform.is64Bit() ? "win64" : "win32";
            }
            Assert.notNull(this.platform, "Unsupported platform: " + Platform.getNativeLibraryResourcePrefix());
        }
        Assert.notNull(DOWNLOAD_URL.get(this.product).get(this.platform), "Unsupported platform: " + this.platform);
    }

    /**
     * 下载浏览器，如果项目目录下不存在对应版本时
     * 如果不指定版本，则使用默认配置版本
     *
     * @param version 浏览器版本
     * @return the Revision
     * @throws InterruptedException 异常
     * @throws ExecutionException   异常
     * @throws IOException          异常
     */
    public static Revision on() throws InterruptedException, ExecutionException, IOException {
        return on(Variables.VERSION);
    }

    /**
     * 下载浏览器，如果项目目录下不存在对应版本时
     * 如果不指定版本，则使用默认配置版本
     *
     * @param version 浏览器版本
     * @return the Revision
     * @throws InterruptedException 异常
     * @throws ExecutionException   异常
     * @throws IOException          异常
     */
    public static Revision on(String version) throws InterruptedException, ExecutionException, IOException {
        Fetcher fetcher = new Fetcher();
        String downLoadVersion = StringKit.isEmpty(version) ? Variables.VERSION : version;
        Revision revision = fetcher.revisionInfo(downLoadVersion);
        if (!revision.isLocal()) {
            return fetcher.download(downLoadVersion);
        }
        return revision;
    }

    /**
     * 检测对应的浏览器版本是否可以下载
     *
     * @param revision 浏览器版本
     * @param proxy    cant be null
     * @return boolean
     * @throws IOException 异常
     */
    public boolean on(String revision, Proxy proxy) throws IOException {
        String url = downloadURL(this.product, this.platform, this.downloadHost, revision);
        return httpRequest(proxy, url, "HEAD");
    }

    /**
     * 发送一个http请求
     *
     * @param proxy  代理 可以为null
     * @param url    请求的url
     * @param method 请求方法 get post head
     * @return boolean
     */
    private boolean httpRequest(Proxy proxy, String url, String method) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL urlSend = new URL(url);
            if (proxy != null) {
                conn = (HttpURLConnection) urlSend.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) urlSend.openConnection();
            }
            conn.setRequestMethod(method);
            conn.connect();
            if (conn.getResponseCode() >= 300 && conn.getResponseCode() <= 400 && StringKit.isNotEmpty(conn.getHeaderField("Location"))) {
                httpRequest(proxy, conn.getHeaderField("Location"), method);
            } else {
                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return true;
                }
            }

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return false;
    }

    /**
     * 根据给定得浏览器版本下载浏览器，可以利用下载回调显示下载进度
     *
     * @param revision         浏览器版本
     * @param progressCallback 下载回调
     * @return RevisionInfo
     * @throws IOException          异常
     * @throws InterruptedException 异常
     * @throws ExecutionException   异常
     */
    public Revision download(String revision, BiConsumer<Integer, Integer> progressCallback) throws IOException, InterruptedException, ExecutionException {
        String url = downloadURL(this.product, this.platform, this.downloadHost, revision);
        int lastIndexOf = url.lastIndexOf("/");
        String archivePath = Builder.join(this.downloadsFolder, url.substring(lastIndexOf));
        String folderPath = this.getFolderPath(revision);
        if (existsAsync(folderPath))
            return this.revisionInfo(revision);
        if (!(existsAsync(this.downloadsFolder)))
            mkdirAsync(this.downloadsFolder);
        try {
            if (progressCallback == null) {
                progressCallback = defaultDownloadCallback();
            }

            downloadFile(url, archivePath, progressCallback);
            install(archivePath, folderPath);
        } finally {
            unlinkAsync(archivePath);
        }
        Revision revisionInfo = this.revisionInfo(revision);
        if (revisionInfo != null) {
            try {
                File executableFile = new File(revisionInfo.getExecutablePath());
                executableFile.setExecutable(true, false);
            } catch (Exception e) {
                Logger.error("Set executablePath:{} file executation permission fail.", revisionInfo.getExecutablePath());
            }
        }
        //睡眠5s，让解压程序释放chrome.exe
        Thread.sleep(5000L);
        return revisionInfo;
    }

    /**
     * 指定版本下载chromuim
     *
     * @param revision 版本
     * @return 下载后的chromuim包有关信息
     * @throws IOException          异常
     * @throws InterruptedException 异常
     * @throws ExecutionException   异常
     */
    public Revision download(String revision) throws IOException, InterruptedException, ExecutionException {
        return this.download(revision, null);
    }

    /**
     * 默认的下载回调
     *
     * @return 回调函数
     */
    private BiConsumer<Integer, Integer> defaultDownloadCallback() {
        return (integer1, integer2) -> {
            BigDecimal decimal1 = new BigDecimal(integer1);
            BigDecimal decimal2 = new BigDecimal(integer2);
            int percent = decimal1.divide(decimal2, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
            Logger.info("Download progress: total[{}M],downloaded[{}M],{}", decimal2, decimal1, percent + "%");
        };
    }

    /**
     * 下载最新的浏览器版本
     *
     * @param progressCallback 下载回调
     * @return 浏览器版本
     * @throws IOException          异常
     * @throws InterruptedException 异常
     * @throws ExecutionException   异常
     */
    public Revision download(BiConsumer<Integer, Integer> progressCallback) throws IOException, InterruptedException, ExecutionException {
        return this.download(fetchRevision(), progressCallback);
    }

    /**
     * 下载最新的浏览器版本（使用自带的下载回调）
     *
     * @return 浏览器版本
     * @throws IOException          异常
     * @throws InterruptedException 异常
     * @throws ExecutionException   异常
     */
    public Revision download() throws IOException, InterruptedException, ExecutionException {
        return this.download(fetchRevision(), null);
    }

    private String fetchRevision() throws IOException {
        String downloadUrl = DOWNLOAD_URL.get(product).get(platform);
        URL urlSend = new URL(String.format(downloadUrl.substring(0, downloadUrl.length() - 9), this.downloadHost));
        URLConnection conn = urlSend.openConnection();
        conn.setConnectTimeout(Variables.CONNECT_TIME_OUT);
        conn.setReadTimeout(Variables.READ_TIME_OUT);
        String pageContent = Builder.toString(conn.getInputStream());
        return parseRevision(pageContent);
    }

    /**
     * 解析得到最新的浏览器版本
     *
     * @param pageContent 页面内容
     * @return 浏览器版本
     */
    private String parseRevision(String pageContent) {
        String result = null;
        Pattern pattern = Pattern.compile("<a href=\"/mirrors/chromium-browser-snapshots/(.*)?/\">");
        Matcher matcher = pattern.matcher(pageContent);
        while (matcher.find()) {
            result = matcher.group(1);
        }
        String[] split = Objects.requireNonNull(result).split("/");
        if (split.length == 2) {
            result = split[1];
        } else {
            throw new RuntimeException("Cant't find latest revision from pageConten:" + pageContent);
        }
        return result;
    }

    /**
     * 本地存在的浏览器版本
     *
     * @return 版本集合
     * @throws IOException 异常
     */
    public List<String> localRevisions() throws IOException {
        if (!existsAsync(this.downloadsFolder))
            return new ArrayList<>();
        Path path = Paths.get(this.downloadsFolder);
        Stream<Path> fileNames = this.readdirAsync(path);
        return fileNames.map(fileName -> parseFolderPath(this.product, fileName)).filter(entry -> entry != null && this.platform.equals(entry.getPlatform())).map(Revision::getRevision).collect(Collectors.toList());
    }

    /**
     * 删除指定版本的浏览器文件
     *
     * @param revision 版本
     * @throws IOException 异常
     */
    public void remove(String revision) throws IOException {
        String folderPath = this.getFolderPath(revision);
        Assert.isTrue(existsAsync(folderPath), "Failed to remove: revision " + revision + " is not downloaded");
        Files.delete(Paths.get(folderPath));
    }

    /**
     * 根据给定的浏览器产品以及文件夹解析浏览器版本和平台
     *
     * @param product    win linux mac
     * @param folderPath 文件夹路径
     * @return Revision Revision
     */
    private Revision parseFolderPath(String product, Path folderPath) {
        Path fileName = folderPath.getFileName();
        String[] split = fileName.toString().split("-");
        if (split.length != 2)
            return null;
        if (DOWNLOAD_URL.get(product).get(split[0]) == null)
            return null;
        Revision entry = new Revision();
        entry.setPlatform(split[0]);
        entry.setProduct(product);
        entry.setRevision(split[1]);
        return entry;
    }

    /**
     * 获取文件夹下所有项目，深度：一级
     *
     * @param downloadsFolder 下载文件夹
     * @return Stream<Path> Stream<Path>
     * @throws IOException 异常
     */
    private Stream<Path> readdirAsync(Path downloadsFolder) throws IOException {
        Assert.isTrue(Files.isDirectory(downloadsFolder), "DownloadsFolder " + downloadsFolder.toString() + " is not Directory");
        return Files.list(downloadsFolder);
    }

    /**
     * 修改文件权限，与linux上chmod命令一样，并非异步，只是方法名为了与nodejs的puppeteer库一样
     *
     * @param executablePath 执行路径
     * @param perms          权限字符串，例如"775",与linux上文件权限一样
     * @throws IOException 异常
     */
    private void chmodAsync(String executablePath, String perms) throws IOException {
        Builder.chmod(executablePath, perms);
    }

    /**
     * 删除压缩文件
     *
     * @param archivePath zip路径
     * @throws IOException 异常
     */
    private void unlinkAsync(String archivePath) throws IOException {
        Files.deleteIfExists(Paths.get(archivePath));
    }

    /**
     * intall archive file: *.zip,*.dmg
     *
     * @param archivePath zip路径
     * @param folderPath  存放的路径
     * @throws IOException          异常
     * @throws InterruptedException 异常
     */
    private void install(String archivePath, String folderPath) throws IOException, InterruptedException {
        Logger.info("Installing " + archivePath + " to " + folderPath);
        if (archivePath.endsWith(".zip")) {
            ZipKit.unzip(archivePath, folderPath);
        } else if (archivePath.endsWith(".dmg")) {
            mkdirAsync(folderPath);
            installDMG(archivePath, folderPath);
        } else {
            throw new IllegalArgumentException("Unsupported archive format: " + archivePath);
        }
    }

    /**
     * mount and copy
     *
     * @param archivePath zip路径
     * @param folderPath  存放路径
     * @return string
     * @throws IOException          异常
     * @throws InterruptedException 异常
     */
    private String mountAndCopy(String archivePath, String folderPath) throws IOException, InterruptedException {
        String mountPath = null;
        BufferedReader reader = null;
        String line;
        StringWriter stringWriter = null;
        try {
            List<String> arguments = new ArrayList<>();
            arguments.add("/bin/sh");
            arguments.add("-c");
            arguments.add("hdiutil");
            arguments.add("attach");
            arguments.add("-nobrowse");
            arguments.add("-noautoopen");
            arguments.add(archivePath);
            ProcessBuilder processBuilder = new ProcessBuilder().command(arguments).redirectErrorStream(true);
            Process process = processBuilder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            Pattern pattern = Pattern.compile("/Volumes/(.*)", Pattern.MULTILINE);
            stringWriter = new StringWriter();
            while ((line = reader.readLine()) != null) {
                stringWriter.write(line);
            }
            process.waitFor();
            process.destroyForcibly();
            Matcher matcher = pattern.matcher(stringWriter.toString());
            while (matcher.find()) {
                mountPath = matcher.group();
            }
        } finally {
            IoKit.close(reader);
            IoKit.close(stringWriter);
        }
        if (StringKit.isEmpty(mountPath)) {
            throw new RuntimeException("Could not find volume path in [" + stringWriter.toString() + "]");
        }
        Optional<Path> optionl = this.readdirAsync(Paths.get(mountPath)).filter(item -> item.toString().endsWith(".app")).findFirst();
        if (optionl.isPresent()) {
            try {
                Path path = optionl.get();
                String copyPath = path.toString();
                Logger.info("Copying " + copyPath + " to " + folderPath);
                List<String> arguments = new ArrayList<>();
                arguments.add("/bin/sh");
                arguments.add("-c");
                arguments.add("cp");
                arguments.add("-R");
                arguments.add(copyPath);
                arguments.add(folderPath);
                ProcessBuilder processBuilder2 = new ProcessBuilder().command(arguments);
                Process process2 = processBuilder2.start();
                reader = new BufferedReader(new InputStreamReader(process2.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    Logger.trace(line);
                }
                reader.close();
                reader = new BufferedReader(new InputStreamReader(process2.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    Logger.error(line);
                }
                process2.waitFor();
                process2.destroyForcibly();
            } finally {
                IoKit.close(reader);
            }
        }
        return mountPath;
    }

    /**
     * Install *.app directory from dmg file
     *
     * @param archivePath zip路径
     * @param folderPath  存放路径
     * @throws IOException          异常
     * @throws InterruptedException 异常
     */
    private void installDMG(String archivePath, String folderPath) throws IOException, InterruptedException {
        String mountPath = null;
        try {
            mountPath = mountAndCopy(archivePath, folderPath);
        } finally {
            unmount(mountPath);
        }
    }

    /**
     * unmount finally
     *
     * @param mountPath mount Path
     * @throws IOException          异常
     * @throws InterruptedException 异常
     */
    private void unmount(String mountPath) throws IOException, InterruptedException {
        BufferedReader reader = null;
        if (StringKit.isEmpty(mountPath)) {
            return;
        }
        List<String> arguments = new ArrayList<>();
        arguments.add("/bin/sh");
        arguments.add("-c");
        arguments.add("hdiutil");
        arguments.add("detach");
        arguments.add(mountPath);
        arguments.add("-quiet");
        try {
            ProcessBuilder processBuilder3 = new ProcessBuilder().command(arguments);
            Process process3 = processBuilder3.start();
            Logger.info("Unmounting " + mountPath);
            String line;
            reader = new BufferedReader(new InputStreamReader(process3.getInputStream()));
            while ((line = reader.readLine()) != null) {
                Logger.trace(line);
            }
            reader.close();
            reader = new BufferedReader(new InputStreamReader(process3.getErrorStream()));
            while ((line = reader.readLine()) != null) {
                Logger.error(line);
            }
            process3.waitFor();
            process3.destroyForcibly();
        } finally {
            IoKit.close(reader);
        }
    }

    /**
     * 下载浏览器到具体的路径
     * ContentTypeapplication/x-zip-compressed
     *
     * @param url              url
     * @param archivePath      zip路径
     * @param progressCallback 回调函数
     */
    private void downloadFile(String url, String archivePath, BiConsumer<Integer, Integer> progressCallback) throws IOException, ExecutionException, InterruptedException {
        Logger.info("Downloading binary from " + url);
        Builder.download(url, archivePath, progressCallback);
        Logger.info("Download successfully from " + url);
    }

    /**
     * 创建文件夹
     *
     * @param folder 要创建的文件夹
     * @throws IOException 创建文件失败
     */
    private void mkdirAsync(String folder) throws IOException {
        File file = new File(folder);
        if (!file.exists()) {
            Files.createDirectory(file.toPath());
        }
    }

    /**
     * 根据浏览器版本获取对应浏览器路径
     *
     * @param revision 浏览器版本
     * @return string
     */
    public String getFolderPath(String revision) {
        return Paths.get(this.downloadsFolder, this.platform + "-" + revision).toString();
    }

    /**
     * 获取浏览器版本相关信息
     *
     * @param revision 版本
     * @return RevisionInfo
     */
    public Revision revisionInfo(String revision) {
        String folderPath = this.getFolderPath(revision);
        String executablePath;
        if ("chrome".equals(this.product)) {
            if ("mac".equals(this.platform)) {
                executablePath = Builder.join(folderPath, archiveName(this.product, this.platform, revision), "Chromium.app", "Contents", "MacOS", "Chromium");
            } else if ("linux".equals(this.platform)) {
                executablePath = Builder.join(folderPath, archiveName(this.product, this.platform, revision), "chrome");
            } else if ("win32".equals(this.platform) || "win64".equals(this.platform)) {
                executablePath = Builder.join(folderPath, archiveName(this.product, this.platform, revision), "chrome.exe");
            } else {
                throw new IllegalArgumentException("Unsupported platform: " + this.platform);
            }
        } else if ("firefox".equals(this.product)) {
            if ("mac".equals(this.platform))
                executablePath = Builder.join(folderPath, "Firefox Nightly.app", "Contents", "MacOS", "firefox");
            else if ("linux".equals(this.platform))
                executablePath = Builder.join(folderPath, "firefox", "firefox");
            else if ("win32".equals(this.platform) || "win64".equals(this.platform))
                executablePath = Builder.join(folderPath, "firefox", "firefox.exe");
            else
                throw new IllegalArgumentException("Unsupported platform: " + this.platform);
        } else {
            throw new IllegalArgumentException("Unsupported product: " + this.product);
        }
        String url = downloadURL(this.product, this.platform, this.downloadHost, revision);
        boolean local = this.existsAsync(folderPath);
        Logger.info("Version:{}，executablePath:{}，folderPath:{}，local:{}，url:{}，product:{}", revision, executablePath, folderPath, local, url, this.product);
        return new Revision(revision, executablePath, folderPath, local, url, this.product);
    }

    /**
     * 检测给定的路径是否存在
     *
     * @param filePath 文件路径
     * @return boolean
     */
    public boolean existsAsync(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 根据平台信息和版本信息确定要下载的浏览器压缩包
     *
     * @param product  产品
     * @param platform 平台
     * @param revision 版本
     * @return 压缩包名字
     */
    public String archiveName(String product, String platform, String revision) {
        if ("chrome".equals(product)) {
            if ("linux".equals(platform))
                return "chrome-linux";
            if ("mac".equals(platform))
                return "chrome-mac";
            if ("win32".equals(platform) || "win64".equals(platform)) {
                return Integer.parseInt(revision) > 591479 ? "chrome-win" : "chrome-win32";
            }
        } else if ("firefox".equals(product)) {
            if ("linux".equals(platform))
                return "firefox-linux";
            if ("mac".equals(platform))
                return "firefox-mac";
            if ("win32".equals(platform) || "win64".equals(platform))
                return "firefox-" + platform;
        }
        return null;
    }

    /**
     * 确定下载的路径
     *
     * @param product  产品：chrome or firefox
     * @param platform win linux mac
     * @param host     域名地址
     * @param revision 版本
     * @return 下载浏览器的url
     */
    public String downloadURL(String product, String platform, String host, String revision) {
        return String.format(DOWNLOAD_URL.get(product).get(platform), host, revision, archiveName(product, platform, revision));
    }

}
