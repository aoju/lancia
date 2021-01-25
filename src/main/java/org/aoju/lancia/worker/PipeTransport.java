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
package org.aoju.lancia.worker;

import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 与chromuim通过pipe通信暂时没实现
 *
 * @author Kimi Liu
 * @version 1.2.1
 * @since JDK 1.8+
 */
public class PipeTransport implements Transport {

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final StringBuffer pendingMessage = new StringBuffer();
    private InputStream pipeReader;
    private OutputStream pipeWriter;
    private Thread readThread;
    private Thread writerThread;

    public PipeTransport() {

    }

    public PipeTransport(InputStream pipeReader, OutputStream pipeWriter) {
        this.pipeReader = pipeReader;
        this.pipeWriter = pipeWriter;
        readThread = new Thread(new ReaderThread());
        readThread.start();
        writerThread = new Thread(new WriterThread());
        writerThread.start();
    }

    @Override
    public void send(String message) {
        try {
            messageQueue.put(message);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        IoKit.close(pipeWriter);
        IoKit.close(pipeReader);
    }

    private class WriterThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    //take()方法会阻塞，知道拿到位置
                    String message = messageQueue.take();
                    pipeWriter.write(message.getBytes());
                    pipeWriter.write('\0');
                    pipeWriter.flush();
                } catch (InterruptedException | IOException e) {
                    Logger.error("pipe transport send message fail ", e);
                }
            }
        }
    }

    /**
     * 读取管道中的消息线程
     */
    private class ReaderThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    int read = pipeReader.read();
                    if ((char) read != '\0') {
                        pendingMessage.append((char) read);

                    } else {
                        String message = pendingMessage.toString();
                        pendingMessage.delete(0, pendingMessage.length());
                        call(message);
                    }
                } catch (IOException e) {
                    Logger.error("read message from chrome error ", e);
                }
            }
        }

    }

}
