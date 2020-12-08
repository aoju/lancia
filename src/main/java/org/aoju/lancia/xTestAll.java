package org.aoju.lancia;

import org.aoju.lancia.kernel.browser.Fetcher;
import org.aoju.lancia.option.LaunchBuilder;
import org.aoju.lancia.option.LaunchOption;
import org.aoju.lancia.option.PDFOption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class xTestAll {

    public static void main(String[] args) {
        // nioClient();
        aioClient();
    }

    public static void aioClient() {
        try {
            // 自动下载，下载后不会再下载
            Fetcher.on(null);
            List<String> list = new ArrayList<>();
            // 生成pdf必须在无厘头模式下才能生效

            LaunchOption options = new LaunchBuilder()
                    .withArgs(list).withHeadless(true).withViewport(null).build();
            list.add("--no-sandbox");
            list.add("--disable-setuid-sandbox");
            Browser browser = Puppeteer.launch(options);
            Page page = browser.newPage();
            page.goTo("https://t1.hidoctor.wiki/mobile/login");

            // page.emulate(Device.IPHONE_X);
            page.waitFor("2000");
            PDFOption pdfOption = new PDFOption();
            pdfOption.setFormat("A4");
            pdfOption.setPrintBackground(true);
            pdfOption.setPath("A4.pdf");

            page.pdf(pdfOption);

            page.close();
            browser.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
