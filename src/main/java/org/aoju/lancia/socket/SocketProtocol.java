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
package org.aoju.lancia.socket;

import org.aoju.bus.core.lang.Normal;

import java.util.regex.Pattern;

/**
 * Class which represents the protocol used as Sec-Sockets-SocketProtocol
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class SocketProtocol {

    /**
     * Attribute for the provided protocol
     */
    private final String provided;

    /**
     * Constructor for a Sec-Websocket-SocketProtocol
     *
     * @param provided the protocol string
     */
    public SocketProtocol(String provided) {
        if (provided == null) {
            throw new IllegalArgumentException();
        }
        this.provided = provided;
    }

    public boolean accept(String inputProtocolHeader) {
        if (Normal.EMPTY.equals(provided)) {
            return true;
        }
        String protocolHeader = Pattern.compile(" ").matcher(inputProtocolHeader).replaceAll("");
        String[] headers = Pattern.compile(",").split(protocolHeader);
        for (String header : headers) {
            if (provided.equals(header)) {
                return true;
            }
        }
        return false;
    }

    public String getProvided() {
        return this.provided;
    }

    public SocketProtocol copyInstance() {
        return new SocketProtocol(getProvided());
    }

}
