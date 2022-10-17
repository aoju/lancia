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
package org.aoju.lancia.worker.exception;

/**
 * exception which indicates that a invalid data was received
 */
public class SocketException extends Exception {

    /**
     * Serializable
     */
    private static final long serialVersionUID = 1L;

    /**
     * attribute which value will be returned
     */
    private final int value;

    public SocketException() {
        this.value = 0;
    }

    /**
     * constructor for a SocketException
     *
     * @param value the value which will be returned
     */
    public SocketException(int value) {
        this.value = value;
    }

    /**
     * constructor for a SocketException.
     *
     * @param value the value which will be returned.
     * @param s     the detail message.
     */
    public SocketException(int value, String s) {
        super(s);
        this.value = value;
    }

    /**
     * constructor for a LimitExceededException
     * <p>
     * calling SocketException with closecode TOOBIG
     *
     * @param s     the detail message.
     * @param value the allowed size which was not enough
     */
    public SocketException(String s, int value) {
        // this.value = limit;
        // this(Framedata.TOOBIG, s);
        this(value, s);
    }

    /**
     * constructor for a SocketException.
     *
     * @param value the value which will be returned.
     * @param t     the throwable causing this exception.
     */
    public SocketException(int value, Throwable t) {
        super(t);
        this.value = value;
    }

    /**
     * Getter value
     *
     * @return the value
     */
    public int getValue() {
        return value;
    }

}
