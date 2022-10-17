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
package org.aoju.lancia.kernel;

import org.aoju.bus.logger.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 标准系统上下文信息
 * 搜索顺序:
 * <ul>
 *  <li>系统属性
 *  <li>系统环境变量
 * </ul>
 *
 *  <ul>
 *   <li>{@code foo.bar} -原始名字</li>
 *   <li>{@code foo_bar} - 用下划线表示句点(如果有的话)</li>
 *   <li>{@code FOO.BAR} - 原始，大写/li>
 *   <li>{@code FOO_BAR} - 有下划线和大写字母</li>
 *  </ul>
 *
 * @author Kimi Liu
 * @version 1.2.8
 * @since JDK 1.8+
 */
public class Standard implements Variables {

    private static final Map<String, String> SYSTEM_PROPERTIES_SOURCEMAP = new HashMap<>();
    private static final Map<String, String> SYSTEM_ENV_SOURCEMAP = new HashMap<>();

    static {
        Properties systemProperties = System.getProperties();
        systemProperties.entrySet().forEach(systemPropertiesEntry -> {
            String key = systemPropertiesEntry.getKey().toString();
            String value = systemPropertiesEntry.getValue() == null ? null : systemPropertiesEntry.getValue().toString();
            SYSTEM_PROPERTIES_SOURCEMAP.put(key, value);
        });

        SYSTEM_ENV_SOURCEMAP.putAll(System.getenv());
    }

    @Override
    public String getEnv(String name) {
        // order 0: find from system property
        String value = this.getPropertyValue(name, SYSTEM_PROPERTIES_SOURCEMAP, "XSystemProperties");

        // order 1: find from env
        if (value == null) {
            value = this.getPropertyValue(name, SYSTEM_ENV_SOURCEMAP, "XSystemEnv");
        }

        // order 3: find from LaunchOptions

        return value;
    }

    private String getPropertyValue(String name, Map<String, String> source, String sourceName) {
        String actualName = resolvePropertyName(name, source);
        if (actualName == null) {
            // retry to uppercase
            actualName = resolvePropertyName(name.toUpperCase(), source);
            if (actualName == null) {
                if (Logger.isDebug()) {
                    Logger.debug("PropertySource ' " + sourceName + " ' does not contain property '" + name);
                }
                return null;
            }
        }

        if (Logger.isDebug() && !name.equals(actualName)) {
            Logger.debug("PropertySource ' " + sourceName + " ' does not contain property '" + name +
                    "', but found equivalent '" + actualName + "'");
        }

        return source.get(actualName);
    }

    private final String resolvePropertyName(String name, Map<String, String> source) {
        if (source.containsKey(name)) {
            return name;
        }

        // check name with just dots replaced
        String noDotName = name.replace(".", "_");
        if (!name.equals(noDotName) && source.containsKey(noDotName)) {
            return noDotName;
        }

        // check name with just hyphens replaced
        String noHyphenName = name.replace("-", "_");
        if (!name.equals(noHyphenName) && source.containsKey(noHyphenName)) {
            return noHyphenName;
        }

        // Check name with dots and hyphens replaced
        String noDotAndHyphenName = noDotName.replace("-", "_");
        if (!name.equals(noDotAndHyphenName) && source.containsKey(noDotAndHyphenName)) {
            return noDotAndHyphenName;
        }

        // give up
        return null;
    }

}
