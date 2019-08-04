## Lancia


> 网页转PDF渲染服务。提供收据、发票、报告或任何网页内容转PDF的微服务


**⚠️ 警告 ⚠️**

*请不要将这个API服务公开与互联网,除非你是知道潜在的风险.因为它允许用户在服务器上的Chrome会话中运行任何JavaScript代码,后果请自行负责*


**⭐️特性:**

* 将任何URL或HTML内容转换为PDF文件或图像(PNG/JPEG)
* 使用[Puppeteer](https://github.com/GoogleChrome/puppeteer)
  Chrome渲染网页。PDF文件与桌面Chrome生成的文件一致.
* 合理的默认值，大部分参数都是可配置的
* 单页app (SPA)支持。在呈现之前，等待所有网络请求完成
* Easy deployment to Heroku. We love Lambda but...Deploy to Heroku button.
* 呈现延迟加载的元素 *(scrollPage 选项)*
* 支持可选的“X-Access-Toke”身份验证*


* **默认情况下页面的 `@media print` CSS 规则将被忽略**.将Chrome设置为模拟
  `@media screen`
  ，使默认的pdf文件看起来更像实际站点。要获得更接近桌面Chrome的结果，请添加`&emulateScreenMedia=false`查询参数。更多信息请访问[Puppeteer API docs](https://github.com/GoogleChrome/puppeteer/blob/master/docs/api.md#pagepdfoptions).
  API文档。 
  
* Chrome启动时带有`--no-sandbox--disable-setuid-sandbox`标志，开启debian支持.

* 如果服务器没有足够的内存，超大页面加载可能会导致Chrome崩溃.



**为什么做这个服务?**

当您需要自动生成PDF文件时，此微服务非常有用,不管出于什么原因。这些文件可以呈现为收据，周报，发票，或任何内容。

PDF可以以多种方式生成，但其中难点之一是转换HTML+CSS,大部分工具无法呈现期望的结果，这个服务就是弥补不足才做的。

## 调研结果
|调研对象 | 优点 | 缺点| 分页 | 图片| 表格|链接 |中文 |特殊字符 |样式 |
|----|----|----|----|----|----|----|----|----|----|
|jsPDF|整个过程在客户端执行(不需要服务器参与)，调用简单|生成的pdf为图片形式，且内容失真|支持|支持|支持|不支持|支持|支持|半支持href不支持)|
|iText|1、功能基本可以实现，比较灵活2、生成pdf质量较高|1、对html标签严；格，少一个结束标签就会报错；2、后端实现复杂，服务器需要安装字体；3、图片渲染比较复杂(暂时还没解决)|支持|支持|支持|支持|支持|支持|半支持href不支持)|
|wkhtmltopdf|1、调用方式简单(只需执行一行脚本)；2、生成pdf质量较高|1、服务器需要安装wkhtmltopdf环境；2、根据网址生成pdf，对于有权限控制的页面需要在拦截器进行处理|支持|支持|支持|支持|支持|支持|半支持href不支持)|


## 示例

*提示: 根据网站内容的大小，可设置响应的超时时间,必要时可设置为：30秒.*

**渲染 baidu.com**

http://xxx:7003/router/rest?url=http://baidu.com

**将baidu.com渲染为PNG图像**

http://xxx:7003/router/rest?url=http://baidu.com&output=screenshot

**使用默认的@media print而不是@media screen.**

http://xxx:7003/router/rest?url=http://baidu.com&emulateScreenMedia=false

**使用scrollPage=true，显示所有延迟加载的元素。不完美，但总比没有好.**

http://xxx:7003/router/rest?url=http://baidu.com&scrollPage=true

**只渲染第一页**

http://xxx:7003/router/rest?url=http://baidu.com&pdf.pageRanges=1

**横向渲染A5大小的PDF.**

http://xxx:7003/router/rest?url=http://baidu.com&pdf.format=A5&pdf.landscape=true

**在PDF中添加2cm的页边距.**

http://xxx:7003/router/rest?url=http://baidu.com&pdf.margin.right=2cm&pdf.margin.bottom=2cm&pdf.margin.left=2cm

**渲染超时时间为1000毫秒.**

http://xxx:7003/router/rest?url=http://baidu.com&waitFor=1000



**下载带有指定附件名称的PDF**

http://xxx:7003/router/rest?url=http://baidu.com&attachmentName=google.pdf

**等待匹配`input`元素.**

http://xxx:7003/router/rest?url=http://baidu.com&waitFor=input

**渲染json至html body**

```bash
curl -o html.pdf -XPOST -d'{"html": "<body>test</body>"}' -H"content-type: application/json"http://xxx:7003/router/rest
```

**渲染文本至HTML body**

```bash
curl -o html.pdf -XPOST -d@page.html -H"content-type: text/html" http://xxx:7003/router/rest
```

## API


## 技术栈

* Node 8+ (async, await), 需要ES7支持
* [Koajs](https://github.com/koajs/koa) 很好的框架
* [Puppeteer](https://github.com/GoogleChrome/puppeteer) 谷歌官方 Chrome
  node库