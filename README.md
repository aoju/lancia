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
* 支持可选的“X-Access-Toke”身份验证*


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


## 使用示例

*提示: 根据网站内容的大小，可设置响应的超时时间,必要时可设置为：30秒.*

**渲染baidu.com为PDF**

http://xxx:7003/router/rest?method=org.aoju.render.get&v=1.0&format=pdf&url=http://baidu.com

**渲染baidu.com为PNG**

http://xxx:7003/router/rest?method=org.aoju.render.get&v=1.0&format=pdf&url=http://baidu.com&output=screenshot

**使用默认的@media print而不是@media screen.**

http://xxx:7003/router/rest?method=org.aoju.render.get&v=1.0&format=pdf&url=http://baidu.com&emulateScreenMedia=false

**使用scrollPage=true，显示所有延迟加载的元素。不完美，但总比没有好.**

http://xxx:7003/router/rest?method=org.aoju.render.get&v=1.0&format=pdf&url=http://baidu.com&scrollPage=true

**只渲染第一页**

http://xxx:7003/router/rest?method=org.aoju.render.get&v=1.0&format=pdf&url=http://baidu.com&pdf.pageRanges=1

**横向渲染A5大小的PDF.**

http://xxx:7003/router/rest?method=org.aoju.render.get&v=1.0&format=pdf&url=http://baidu.com&pdf.format=A5&pdf.landscape=true

**在PDF中添加2cm的页边距.**

http://xxx:7003/router/rest?method=org.aoju.render.get&v=1.0&format=pdf&url=http://baidu.com&pdf.margin.right=2cm&pdf.margin.bottom=2cm&pdf.margin.left=2cm

**渲染超时时间为1000毫秒.**

http://xxx:7003/router/rest?method=org.aoju.render.get&v=1.0&format=pdf&url=http://baidu.com&waitFor=1000



**下载带有指定附件名称的PDF**

http://xxx:7003/router/rest?method=org.aoju.render.get&v=1.0&format=pdf&url=http://baidu.com&attachmentName=google.pdf

**等待匹配`input`元素.**

http://xxx:7003/router/rest?method=org.aoju.render.get&v=1.0&format=pdf&url=http://baidu.com&waitFor=input

**渲染json至html body**

```bash
curl -o html.pdf -XPOST -d'{"html": "<body>test</body>"}' -H"content-type: application/json"http://xxx:7003/router/rest
```

**渲染文本至HTML body**

```bash
curl -o html.pdf -XPOST -d@page.html -H"content-type: text/html" http://xxx:7003/router/rest
```

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
pdf.landscape | boolean | `false` | 页面放心.
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



#### 1. 环境要求


1. 本地运行需要安装 Node 8+及以上版本支持 (async, await).
2. CentOS(6.x及以下版本未测试) 需要安装如下LIB:
 ```
 yum install pango.x86_64 libXcomposite.x86_64 libXcursor.x86_64 libXdamage.x86_64 libXext.x86_64 libXi.x86_64 libXtst.x86_64 cups-libs.x86_64 libXScrnSaver.x86_64 libXrandr.x86_64 GConf2.x86_64 alsa-lib.x86_64 atk.x86_64 gtk3.x86_64 ipa-gothic-fonts xorg-x11-fonts-100dpi xorg-x11-fonts-75dpi xorg-x11-utils xorg-x11-fonts-cyrillic xorg-x11-fonts-Type1 xorg-x11-fonts-misc -y
 ```
 
 中文支持
  ```
   vim /etc/locale.conf
    LANG="en_US.UTF-8"
    LANG="zh_CN.UTF-8"
   ```
 
**WARNING:** *至少需要保证2GB左右的内存，一些新闻网站可能会更高甚至需要4GB内存.*

#### 2. 本地开发

* `npm install` 安装node相关lib
* `npm start` 启动本地服务，启动前请修改相关参数信息，详见bin/config.json
* 服务启动访问 http://127.0.0.1:7003 

## 技术栈

* Node 8+ (async, await), 需要ES7支持
* [Koajs](https://github.com/koajs/koa) 很好的框架
* [Puppeteer](https://github.com/GoogleChrome/puppeteer) 谷歌官方 Chrome
  node库