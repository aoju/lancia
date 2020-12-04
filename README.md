<p align="center">
	<a target="_blank" href="https://www.mit-license.org">
		<img src="https://img.shields.io/badge/license-MIT-green.svg">
	</a>
	<a target="_blank" href="https://nodejs.org">
		<img src="https://img.shields.io/badge/node-%3E=8.0.0-brightgreen.svg">
	</a>
	<a target="_blank" href="https://www.npmjs.com">
		<img src="https://img.shields.io/badge/npm-%3E=7.0.0-green.svg">
	</a>
		<a target="_blank" href="https://eslint.org">
		<img src="https://img.shields.io/badge/eslint-%5E3.0.0-blue.svg">
	</a>
	<a target="_blank" href="https://travis-ci.org/aoju/lancia">
		<img src="https://travis-ci.org/aoju/lancia.svg?branch=master">
	</a>
</p>

<p align="center">
	-- QQ群：<a href="https://shang.qq.com/wpa/qunwpa?idkey=17fadd02891457034c6536c984f0d7db29b73ea14c9b86bba39ce18ed7a90e18">839128</a> --
</p>

---

## Lancia

> 网页转PDF渲染服务。提供收据、发票、报告或任何网页内容转PDF的微服务


**⚠️ 警告 ⚠️** *请不要将这个API服务公开与互联网,除非你是知道潜在的风险.
因为它允许用户在服务器上的Chrome会话中运行任何JavaScript代码,后果请自行负责*


**⭐️ 特性:**

* 将任何URL或HTML内容转换为PDF文件或图像(PNG/JPEG)
* *使用[Puppeteer](https://github.com/GoogleChrome/puppeteer)渲染无头Chrome。PDF文件与桌面Chrome生成的文件一致.
* 合理的默认值，大部分参数都是可配置的
* 单页app (SPA)支持。在呈现之前，等待所有网络请求完成
* Easy deployment to Heroku. We love Lambda but...Deploy to Heroku button.
* 呈现延迟加载的元素 *(scrollPage 选项)*
* 支持可选的“X-Access-Token”身份验证*


* 默认情况下页面的 `@media print` CSS 规则将被忽略.将Chrome设置为模拟 `@media
  screen`，使默认的pdf文件看起来更像实际站点。要获得更接近桌面Chrome的结果，请添加`&emulateScreenMedia=false`查询参数。更多信息请访问[Puppeteer API docs](https://github.com/GoogleChrome/puppeteer/blob/master/docs/api.md#pagepdfoptions).
  API文档。
  
* Chrome启动时带有`--no-sandbox--disable-setuid-sandbox`标志，开启debian支持.

* 如果服务器没有足够的内存，超大页面加载可能会导致Chrome崩溃.



**为什么做这个服务?**

当您需要自动生成PDF文件时，此微服务非常有用,不管出于什么原因。这些文件可以呈现为收据，周报，发票，或任何内容。

PDF可以以多种方式生成，但其中难点之一是转换HTML+CSS,大部分工具无法呈现期望的结果，这个服务就是弥补不足才做的。


Target | Good | Deficiency | Link| Style
----------|------|---------|------------| --------
jsPDF|整个过程在客户端执行(不需要服务器参与)，调用简单|生成的pdf为图片形式，且内容失真|N|Y
iText|1、功能基本可以实现，比较灵活2、生成pdf质量较高|1、对html标签严；格，少一个结束标签就会报错；2、后端实现复杂，服务器需要安装字体；3、图片渲染比较复杂(暂时还没解决)|Y|N
wkhtmltopdf|1、调用方式简单(只需执行一行脚本)；2、生成pdf质量较高|1、服务器需要安装wkhtmltopdf环境；2、根据网址生成pdf，对于有权限控制的页面需要在拦截器进行处理|Y|N

## API介绍

要理解API选项，需要了解[Puppeteer](https://github.com/GoogleChrome/puppeteer/blob/master/docs/api.md)
谷歌官方 Chrome node库。
这个API在内部使用。(用来渲染html代码)(https://github.com/aoju/lancia/blob/master/src/app/frames/core/render.core.class.js)

很简单，来看看。渲染流程::

1. **`page.setViewport(options)`** 其中选项与 `viewport.*`匹配.
2. *默认* **`page.emulateMedia('screen')`** 选项与 `emulateScreenMedia=true` 匹配使用.
3. 渲染 URL **或** html.

   如果定义了“url”，则调用 **`page.goto(url,options) `**，选项匹配' goto.* '。
   否则,会从请求体获取html的地方调用 **`page.setContent(html, options)`** ，选项匹配' goto.* '。

4. *默认* **`page.waitFor(num)`** 等待时间为： `waitFor=1000`.
5. *默认* **`scrollPage=true`** 在页面渲染前会设置相关属性，如只需要第一页即可设置为false.

    如果您想呈现一个延迟加载元素的页面，这个参数非常有用。

6. 渲染输出

  * 如果输出是 `pdf` 则使用 **`page.pdf(options)`** 完成输出,其中选项与`pdf.*`匹配.
  * 如果输出是 `screenshot` 则使用 **`page.screenshot(options)`** 完成输出，其中选项与 `screenshot.*`匹配.

### GET/POST /router/rest

所有选项都作为查询参数传递。

参数名称匹配[Puppeteer options](https://github.com/GoogleChrome/puppeteer/blob/master/docs/api.md)。

这些选项与它的“POST”对应项完全相同，但是选项不同

用点符号表示。如。”? pdf。scale=2 '而不是' {pdf: {scale: 2}} '。

唯一需要的参数是“url”。


Parameter | Type | Default | Description
----------|------|---------|------------
url | string | - | URL渲染PDF。(必需)
output | string | pdf | 指定输出格式。可选值: `pdf` 、 `screenshot`.
emulateScreenMedia | boolean | `true` | 模拟 `@media screen` 渲染 PDF.
ignoreHttpsErrors | boolean | `false` | 忽略https错误.
scrollPage | boolean | `false` | 启用滚动页面触发延迟加载元素，可选值： `true` 、 `false`.
waitFor | number | - | 延迟加载超时时间.
attachmentName | string | - | 设置 `content-disposition` 确保浏览器下载属性,按照给定的字符串作为下载名称.
viewport.width | number | `1600` | 预览宽度.
viewport.height | number | `1200` | 预览高度.
viewport.deviceScaleFactor | number | `1` | 设备比例信息.
viewport.isMobile | boolean | `false` | 是否考虑移动端支持.
viewport.hasTouch | boolean | `false` | 是否支持触摸.
viewport.isLandscape | boolean | `false` | 是否考虑横屏模式.
cookies[0][name] | string | - | Cookie 名称 (必需)
cookies[0][value] | string | - | Cookie 值 (必需)
cookies[0][url] | string | - | Cookie URL
cookies[0][domain] | string | - | Cookie 域名
cookies[0][path] | string | - | Cookie 路径
cookies[0][expires] | number | - | Cookie 过期时间
cookies[0][httpOnly] | boolean | - | Cookie httpOnly
cookies[0][secure] | boolean | - | Cookie 安全
cookies[0][sameSite] | string | - | `Strict` or `Lax`
goto.timeout | number | `30000` | 最大超时时间(毫秒)，默认为30秒，通过0禁用超时.
goto.waitUntil | string | `networkidle` | 选项:`load`, `networkidle`. 
goto.networkIdleInflight | number | `2` | 允许最大请求数量。只在`goto.waitUntil`: 'networkidle'参数下生效.
goto.networkIdleTimeout | number | `2000` | 等待超时时间,只有在 waitUntil: 'networkidle' 下生效.
pdf.scale | number | `1` | 网页渲染比例.
pdf.printBackground | boolean | `false`| 打印背景图形.
pdf.displayHeaderFooter | boolean | `false` | 显示页眉和页脚.
pdf.headerTemplate | string | - | HTML模板，用于作为PDF中每个页面的页眉.
pdf.footerTemplate | string | - | HTML模板，用于作为PDF中每个页面的页脚.
pdf.landscape | boolean | `false` | 页面方向.
pdf.pageRanges | string | - | 可选页面信息，例如“1- 5,7,11 -13”。默认为空字符串，意味着输出所有页面.
pdf.format | string | `A4` | 页面格式,如果设置，则优先于宽度或高度选项.
pdf.width | string | - | 纸张宽度.
pdf.height | string | - | 纸张高度.
pdf.margin.top | string | - | 顶部空白.
pdf.margin.right | string | - | 右边空白.
pdf.margin.bottom | string | - | 底部空白.
pdf.margin.left | string | - | 左侧空白.
screenshot.fullPage | boolean | `true` | 如果为真，则获取整个可滚动页面的屏幕截图.
screenshot.type | string | `png` | 截图类型,可选值: `png`, `jpeg`
screenshot.quality | number | - | JPEG图像的质量，在0-100之间,只适用于当 `screenshot.type` 为 `jpeg`.
screenshot.omitBackground | boolean | `false` | 隐藏默认的白色背景，并允许捕获具有透明度的屏幕截图.
screenshot.clip.x | number | - | 指定页面裁剪区域左上角的x坐标.
screenshot.clip.y | number | - | 指定页面裁剪区域左上角的y坐标.
screenshot.clip.width | number | - | 指定页剪切区域的宽度.
screenshot.clip.height | number | - | 指定页剪切区域的高度.

## 技术开发

#### Maven

要使用 maven,请将此依赖添加到pom.xml文件中：

```xml

<dependency>
    <groupId>org.aoju</groupId>
    <artifactId>lancia</artifactId>
    <version>1.2.0</version>
</dependency>
```

#### Logging

该库使用 [SLF4J](https://www.slf4j.org/) 进行日志记录，并且不附带任何默认日志记录实现。

调试程序将日志级别设置为 TRACE

### 快速开始

#### 1、启动浏览器

```java
        List<String> argList = new ArrayList<>();
        Fetcher.on(null);
        LaunchOption options = new LaunchBuilder().withArgs(argList).withHeadless(false).build();
        argList.add("--no-sandbox");
        argList.add("--disable-setuid-sandbox");
        Puppeteer.launch(options);
```

在这个例子中，我们明确指明了启动路径，程序就会根据指明的路径启动对应的浏览器，如果没有明确指明路径，那么程序会尝试启动默认安装路径下的 Chrome 浏览器

#### 2、导航至某个页面

```java
        Fetcher.on(null);

        List<String> argList = new ArrayList<>();
        LaunchOption options = new LaunchBuilder().withArgs(argList).withHeadless(false).build();
        argList.add("--no-sandbox");
        argList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Browser browser2 = Puppeteer.launch(options);

        Page page = browser.newPage();
        page.goTo("http://news.baidu.com/");
        browser.close();
        Page page1 = browser2.newPage();
        page1.goTo("http://news.baidu.com/");
```

这个例子中，浏览器导航到具体某个页面后关闭。在这里并没有指明启动路径。argList是放一些额外的命令行启动参数的，在下面资源章节中我会给出相关资料。

#### 3、生成页面的 PDF

```java
        Fetcher.on(null);

        List<String> argList = new ArrayList<>();
        LaunchOption options = new LaunchBuilder().withArgs(argList).withHeadless(false).build();
        argList.add("--no-sandbox");
        argList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Browser browser2 = Puppeteer.launch(options);

        Page page = browser.newPage();
        page.goTo("http://news.baidu.com/");
        PDFOption pdfOptions = new PDFOption();
        pdfOptions.setPath("test.pdf");
        page.pdf(pdfOptions);
        page.close();
        browser.close();
```

在这个例子中，导航到某个页面后，将整个页面截图，并写成PDF文件。注意，生成PDF必须在headless模式下才能生效

#### 4、TRACING 性能分析

```java
        Fetcher.on(null);

        List<String> argList = new ArrayList<>();
        LaunchOption options = new LaunchBuilder().withArgs(argList).withHeadless(false).build();
        argList.add("--no-sandbox");
        argList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);

        Page page = browser.newPage();
        // 开启追踪
        page.tracing().start("/Users/xxx/Desktop/trace.json");
        page.goTo("http://news.baidu.com/");
        page.tracing().stop();
```

在这个例子中，将在页面导航完成后，生成一个 json 格式的文件，里面包含页面性能的具体数据，可以用 Chrome 浏览器开发者工具打开该 json 文件，并分析性能。

#### 5、页面截图

```java
        Fetcher.on(null);

        List<String> arrayList = new ArrayList<>();
        LaunchOption options = new LaunchBuilder().withArgs(arrayList).withHeadless(true).build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);

        Page page = browser.newPage();
        page.goTo("http://news.baidu.com/");
        ScreenshotOption screenshotOptions = new ScreenshotOption();
        //设置截图范围
        Clip clip = new Clip(1.0, 1.56, 400, 400);
        screenshotOptions.setClip(clip);
        //设置存放的路径
        screenshotOptions.setPath("test.png");
        page.screenshot(screenshotOptions);
```

#### 1. 环境要求

1. 本地运行需要安装 Java 8+及以上版本支持
2. CentOS(6.x及以下版本未测试) 需要安装如下LIB:

 ```
 yum install pango.x86_64 libXcomposite.x86_64 libXcursor.x86_64 libXdamage.x86_64 libXext.x86_64 libXi.x86_64 libXtst.x86_64 cups-libs.x86_64 libXScrnSaver.x86_64 libXrandr.x86_64 GConf2.x86_64 alsa-lib.x86_64 atk.x86_64 gtk3.x86_64 ipa-gothic-fonts xorg-x11-fonts-100dpi xorg-x11-fonts-75dpi xorg-x11-utils xorg-x11-fonts-cyrillic xorg-x11-fonts-Type1 xorg-x11-fonts-misc -y
 ```

如果出现中文，需要安装相关字体

 ```
 yum groupinstall "fonts" -y
 ```

### 版本提示：

 本项目有java版本和nodejs版本，请切换分支获取不同版本内容

**WARNING:** *至少需要保证2GB左右的内存，一些新闻网站可能会更高甚至需要4GB内存.*
