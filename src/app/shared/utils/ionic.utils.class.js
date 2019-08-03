import Path                  from 'path';
import Fs                    from 'fs';
import _                     from 'underscore';

export default class Ionic {

    /**
     * 安全转换字符串
     * @param string
     */
    static parse(string) {
        if (typeof string !== 'string') return {};
        if (typeof string === 'object') return string;
        try {
            return JSON.parse(string) || {};
        } catch (e) {
            return {};
        }
    }


    /**
     * 包装异常对象
     * @param e 异常对象
     * @param [_default] 默认值
     * @param [join] 多个异常信息之间的连接符
     */
    static errors(e, _default, join = '，') {
        if (!e) return null;
        if (typeof e.errors !== 'object') return _default || e.message;
        const errmsg = [];
        for (const key in e.errors) {
            const item = e.errors[key];
            if (!item || !item.message) return;
            errmsg.push(item.message);
        }
        if (errmsg.length == 0) errmsg.push(e.message);
        return errmsg.join(join);
    }

    /**
     * 按指定Key对数组内的对象排序
     * @param array 对象数组
     * @param key 指定key，值需为number类型
     */
    static sortBy(array, key) {
        if (!Array.isArray(array)) return array;
        if (typeof key !== 'string') return array;

        //是否逆序
        let reverse = false;
        if (key.startsWith('-')) {
            reverse = true;
            key = key.substring(1);
        }

        for (let i = 1; i < array.length; i++) {
            const value = array[i];
            let j = i - 1;
            let condtion = reverse ? (value[key] > array[j][key]) : (value[key] < array[j][key]);
            while (j >= 0 && condtion) {
                array[j + 1] = array[j];
                array[j] = value;
                j--;
            }
        }
        return array;
    }

    /**
     * 判断参数只为JavaScript对象
     * @param obj
     * @returns {boolean}
     */
    static isJustObject(obj) {
        return obj !== null && typeof obj === 'object' && Array.isArray(obj) === false ? true : false;
    }

    /**
     * 从对象中移除指定的key
     * @param obj
     * @param key，多个以英文空格分隔
     */
    static omit(obj, key) {
        if (typeof obj !== 'object' || Array.isArray(obj)) return obj;
        if (typeof key !== 'string' && !Array.isArray(key)) return obj;

        key = key.split(' ');

        const result = {};
        for (let k in obj) {
            if (key.indexOf(k) >= 0) continue;
            result[k] = obj[k];
        }
        return result;
    }

    /**
     * 从对象中获取指定的key
     * @param obj
     * @param key
     */
    static pick(obj, key) {
        if (!appClass.isJustObject(obj)) return obj;
        if (typeof key !== 'string' && !Array.isArray(key)) return obj;

        key = key.split(' ');

        const result = {};
        for (let k in obj) {
            if (key.indexOf(k) < 0) continue;
            result[k] = obj[k];
        }
        return result;
    }

    /**
     * 支持多层级不覆盖的Object.assign
     * @param dest
     * @param src
     */
    static assign(dest, ...src) {
        if (Array.isArray(src) && src.length >= 1) {
            if (src.length > 1) {
                for (const item of src) appClass.assign(dest, item);
            } else {
                src = src[0];
            }
        }
        if (!appClass.isJustObject(dest) || !appClass.isJustObject(src)) return dest;

        for (const key in src) {
            const value = src[key];
            if (!dest[key] || Array.isArray(value)) {
                dest[key] = value;
            } else if (appClass.isJustObject(value) && Object.keys(value).length > 0) {
                dest[key] = appClass.assign(dest[key], value);
            } else {
                dest[key] = value;
            }
        }
        return dest;
    }

    /**
     * 确保数组中的元素唯一
     * @param array
     */
    static uniq(array) {
        if (!Array.isArray(array)) return array;
        const result = [];
        for (const item of array) {
            if (result.indexOf(item) >= 0) continue;
            result.push(item);
        }
        return result;
    }

    /**
     * 根据keys的安全获取指定层级的value
     * @param object
     * @param keys
     */
    static value(object, ...keys) {
        if (!appClass.isJustObject(object) || !Array.isArray(keys) || keys.length === 0) return null;
        for (let key of keys) {
            if (object === null) return object;
            object = object[key] || null;
        }
        return object;
    }

    /**
     * 递归文件夹，并对所有js文件执行回调
     * @param dir
     * @param callback
     */
    static recursiveDir(dir, callback) {
        setTimeout(() => {
            try {
                const files = Fs.readdirSync(dir);
                let that = this;
                for (const file of files) {
                    const fullpath = Path.resolve(dir, file);
                    const stat = Fs.statSync(fullpath);
                    if (stat.isFile() === false && stat.isDirectory() === false) continue;
                    if (stat.isDirectory()) {
                        that.recursiveDir(fullpath, callback);
                        continue;
                    }
                    const parse = Path.parse(fullpath);
                    if (!parse || parse.ext !== '.js') continue;
                    if (typeof callback === 'function') callback(require(fullpath));
                }
            } catch (e) {
                console.error(e);
            }
        });
    }

    /**
     * 验证一个对象是否为NULL
     * @method isNull
     * @param  {Object}  obj 要验证的对象
     * @return {Boolean}     结果
     * @static
     */
    static isNull(obj) {
        return obj === null || typeof obj === "undefined";
    }

    /**
     * 验证一个对象是否为NULL
     * @method isNull
     * @param  {Object}  obj 要验证的对象
     * @return {Boolean}     结果
     * @static
     */
    static isEmpty(obj) {
        return _.isEmpty(obj);
    }

    /**
     * 除去字符串两端的空格
     * @method trim
     * @param  {String} str 源字符串
     * @return {String}     结果字符串
     * @static
     */
    static trim(str) {
        if (this.isNull(str)) return str;
        if (str.trim) {
            return str.trim();
        } else {
            return str.replace(/(^[\\s]*)|([\\s]*$)/g, "");
        }
    }

    /**
     * 替换所有
     * @method replace
     * @param {String} str 源字符串
     * @param {String} str1 要替换的字符串
     * @param {String} str2 替换为的字符串
     * @static
     */
    static replace(str, str1, str2) {
        if (this.isNull(str)) return str;
        return str.replace(new RegExp(str1, 'g'), str2);
    }

    /**
     * 从字符串开头匹配
     * @method startWith
     * @param {String} str1 源字符串
     * @param {String} str2 要匹配的字符串
     * @return {Boolean} 匹配结果
     * @static
     */
    static startWith(str1, str2) {
        if (this.isNull(str1) || this.isNull(str2)) return false;
        return str1.indexOf(str2) === 0;
    }

    /**
     * 是否包含
     * @method contains
     * @param {String} str1 源字符串
     * @param {String} str2 检查包括字符串
     * @return {Boolean} 结果
     * @static
     */
    static contains(str1, str2) {
        if (this.isNull(str1) || this.isNull(str2)) return false;
        return str1.indexOf(str2) > -1;
    }

    /**
     * 从字符串结束匹配
     * @method endWidth
     * @param {String} str1 源字符串
     * @param {String} str2 匹配字符串
     * @return {Boolean} 匹配结果
     * @static
     */
    static endWith(str1, str2) {
        if (this.isNull(str1) || this.isNull(str2)) return false;
        return str1.indexOf(str2) === (str1.length - str2.length);
    }

    /**
     * 是否包含属性
     * @method has
     * @param  {Object}  obj  对象
     * @param  {String}  name 属性名
     * @return {Boolean}      结果
     * @static
     */

    static has(obj, name) {
        return this.hasProperty(obj, name);
    }

    /**
     * 是否包含属性
     * @method hasProperty
     * @param  {Object}  obj  对象
     * @param  {String}  name 属性名
     * @return {Boolean}      结果
     * @static
     */
    static hasProperty(obj, name) {
        if (this.isNull(obj) || this.isNull(name))
            return false;
        return (name in obj) || (
            _.hasOwnProperty(name));
    }

    /**
     * 验证一个对象是否为Function
     * @method isFunction
     * @param  {Object}  obj 要验证的对象
     * @return {Boolean}     结果
     * @static
     */
    static isFunction(obj) {
        if (this.isNull(obj)) return false;
        return typeof obj === "function";
    }


    /**
     * 验证一个对象是否为String
     * @method isString
     * @param  {Object}  obj 要验证的对象
     * @return {Boolean}     结果
     * @static
     */
    static isString(obj) {
        if (this.isNull(obj)) return false;
        return typeof obj === 'string' || obj instanceof String;
    }


    /**
     * 验证一个对象是否为Number
     * @method isNumber
     * @param  {Object}  obj 要验证的对象
     * @return {Boolean}     结果
     * @static
     */
    static isNumber(obj) {
        if (this.isNull(obj)) return false;
        return typeof obj === 'number' || obj instanceof Number;
    }


    /**
     * 验证一个对象是否为Boolean
     * @method isBoolean
     * @param  {Object}  obj 要验证的对象
     * @return {Boolean}     结果
     * @static
     */
    static isBoolean(obj) {
        if (this.isNull(obj)) return false;
        return typeof obj === 'boolean' || obj instanceof Boolean;
    }


    /**
     * 验证一个对象是否为HTML Element
     * @method isElement
     * @param  {Object}  obj 要验证的对象
     * @return {Boolean}     结果
     * @static
     */
    static isElement(obj) {
        if (this.isNull(obj)) return false;
        if (window.Element) {
            return obj instanceof Element;
        } else {
            return (obj.tagName && obj.nodeType && obj.nodeName && obj.attributes && obj.ownerDocument);
        }
    }


    /**
     * 验证一个对象是否为HTML Text Element
     * @method isText
     * @param  {Object}  obj 要验证的对象
     * @return {Boolean}     结果
     * @static
     */
    static isText(obj) {
        if (this.isNull(obj)) return false;
        return obj instanceof Text;
    }


    /**
     * 验证一个对象是否为Object
     * @method isObject
     * @param  {Object}  obj 要验证的对象
     * @return {Boolean}     结果
     * @static
     */
    static isObject(obj) {
        if (this.isNull(obj)) return false;
        return typeof obj === "object";
    }


    /**
     * 验证一个对象是否为Array或伪Array
     * @method isArray
     * @param  {Object}  obj 要验证的对象
     * @return {Boolean}     结果
     * @static
     */
    static isArray(obj) {
        if (this.isNull(obj)) return false;
        var v1 = Object.prototype.toString.call(obj) === '[object Array]';
        var v2 = obj instanceof Array;
        var v3 = !this.isString(obj) && this.isNumber(obj.length) && this.isFunction(obj.splice);
        var v4 = !this.isString(obj) && this.isNumber(obj.length) && obj[0];
        return v1 || v2 || v3 || v4;
    }


    /**
     * 验证是不是一个日期对象
     * @method isDate
     * @param {Object} val   要检查的对象
     * @return {Boolean}           结果
     * @static
     */
    static isDate(val) {
        if (this.isNull(val)) return false;
        return val instanceof Date;
    }


    /**
     * 验证是不是一个正则对象
     * @method isDate
     * @param {Object} val   要检查的对象
     * @return {Boolean}           结果
     * @static
     */
    static isRegexp(val) {
        return val instanceof RegExp;
    }


    /**
     * 转换为数组
     * @method toArray
     * @param {Array|Object} array 伪数组
     * @return {Array} 转换结果数组
     * @static
     */
    static toArray(array) {
        if (this.isNull(array)) return [];
        return Array.prototype.slice.call(array);
    }


    /**
     * 转为日期格式
     * @method toDate
     * @param {Number|String} val 日期字符串或整型数值
     * @return {Date} 日期对象
     * @static
     */
    static toDate(val) {
        var self = this;
        if (self.isNumber(val))
            return new Date(val);
        else if (self.isString(val))
            return new Date(self.replace(self.replace(val, '-', '/'), 'T', ' '));
        else if (self.isDate(val))
            return val;
        else
            return null;
    }


    /**
     * 遍历一个对像或数组
     * @method each
     * @param  {Object or Array}   obj  要遍历的数组或对象
     * @param  {Function} fn            处理函数
     * @return {void}                   无返回值
     * @static
     */
    static each(list, handler, scope) {
        if (this.isNull(list) || this.isNull(handler)) return;
        if (this.isArray(list)) {
            var listLength = list.length;
            for (var i = 0; i < listLength; i++) {
                var rs = handler.call(scope || list[i], i, list[i]);
                if (!this.isNull(rs)) return rs;
            }
        } else {
            for (var key in list) {
                var rs = handler.call(scope || list[key], key, list[key]);
                if (!this.isNull(rs)) return rs;
            }
        }
    }


    /**
     * 格式化日期
     * @method formatDate
     * @param {Date|String|Number} date 日期
     * @param {String} format 格式化字符串
     * @param {object} dict 反译字典
     * @return {String} 格式化结果
     * @static
     */
    static formatDate(date, format, dict) {
        if (this.isNull(format) || this.isNull(date)) return date;
        date = this.toDate(date);
        dict = dict || {};
        var placeholder = {
            "M+": date.getMonth() + 1, //month
            "d+": date.getDate(), //day
            "h+": date.getHours(), //hour
            "m+": date.getMinutes(), //minute
            "s+": date.getSeconds(), //second
            "w+": date.getDay(), //week
            "q+": Math.floor((date.getMonth() + 3) / 3), //quarter
            "S": date.getMilliseconds() //millisecond
        }
        if (/(y+)/.test(format)) {
            format = format.replace(RegExp.$1, (date.getFullYear() + "").substr(4 - RegExp.$1.length));
        }
        for (var key in placeholder) {
            if (new RegExp("(" + key + ")").test(format)) {
                var value = placeholder[key];
                value = dict[value] || value;
                format = format.replace(RegExp.$1, RegExp.$1.length == 1
                    ? value : ("00" + value).substr(("" + value).length));
            }
        }
        return format;
    }


    /**
     * 拷贝对象
     * @method copy
     * @param {Object} src 源对象
     * @param {Object} dst 目标对象
     * @static
     */
    static copy(src, dst, igonres) {
        dst = dst || (this.isArray(src) ? [] : {});
        this.each(src, function (key) {
            if (igonres && igonres.indexOf(key) > -1) return;
            delete dst[key];
            if (Object.getOwnPropertyDescriptor) {
                try {
                    Object.defineProperty(dst, key, Object.getOwnPropertyDescriptor(src, key));
                } catch (ex) {
                    dst[key] = src[key];
                }
            } else {
                dst[key] = src[key];
            }
        })
        return dst;
    }


    /**
     * 深度克隆对象
     * @method clone
     * @param {Object} src 源对象
     * @return {Object} 新对象
     * @static
     */
    static clone(src, igonres) {
        if (this.isNull(src) ||
            this.isString(src) ||
            this.isNumber(src) ||
            this.isBoolean(src) ||
            this.isDate(src)) {
            return src;
        }
        var objClone = src;
        try {
            objClone = new src.constructor();
        } catch (ex) {
        }
        this.each(src, function (key, value) {
            if (objClone[key] != value && !this.contains(igonres, key)) {
                if (this.isObject(value)) {
                    objClone[key] = this.clone(value, igonres);
                } else {
                    objClone[key] = value;
                }
            }
        }, this);
        ['toString', 'valueOf'].forEach(function (key) {
            if (this.contains(igonres, key)) return;
            this.defineFreezeProp(objClone, key, src[key]);
        }, this);
        return objClone;
    }


    /**
     * 合并对象
     * @method mix
     * @return 合并后的对象
     * @param {Object} dst 目标对象
     * @param {Object} src 源对象
     * @param {Array} igonres 忽略的属性名,
     * @param {Number} mode 模式
     */
    static merge(dst, src, igonres, mode, igonreNull) {
        //根据模式来判断，默认是Obj to Obj的
        if (mode) {
            switch (mode) {
                case 1: // proto to proto
                    return ntils.mix(dst.prototype, src.prototype, igonres, 0);
                case 2: // object to object and proto to proto
                    ntils.mix(dst.prototype, src.prototype, igonres, 0);
                    break; // pass through
                case 3: // proto to static
                    return ntils.mix(dst, src.prototype, igonres, 0);
                case 4: // static to proto
                    return ntils.mix(dst.prototype, src, igonres, 0);
                default: // object to object is what happens below
            }
        }
        src = src || {};
        dst = dst || (this.isArray(src) ? [] : {});
        this.keys(src).forEach(function (key) {
            if (this.contains(igonres, key)) return;
            if (igonreNull && this.isNull(src[key])) return;
            if (this.isObject(src[key]) &&
                (src[key].constructor == Object ||
                src[key].constructor == Array ||
                src[key].constructor == null)) {
                dst[key] = ntils.mix(dst[key], src[key], igonres, 0, igonreNull);
            } else {
                dst[key] = src[key];
            }
        }, this);
        return dst;
    }


    /**
     * 定义不可遍历的属性
     **/
    static defineFreezeProp(obj, name, value) {
        try {
            Object.defineProperty(obj, name, {
                value: value,
                enumerable: false,
                configurable: true, //能不能重写定义
                writable: false //能不能用「赋值」运算更改
            });
        } catch (err) {
            obj[name] = value;
        }
    }


    /**
     * 获取所有 key
     */
    static keys(obj) {
        if (Object.keys) return Object.keys(obj);
        var keys = [];
        this.each(obj, function (key) {
            keys.push(key);
        });
        return keys;
    }


    /**
     * 创建一个对象
     */
    static create(proto, props) {
        if (Object.create) return Object.create(proto, props);
        var Cotr = function () {
        };
        Cotr.prototype = proto;
        var obj = new Cotr();
        if (props) this.copy(props, obj);
        return obj;
    }


    /**
     * 设置 proto
     * 在不支持 setPrototypeOf 也不支持 __proto__ 的浏览器
     * 中，会采用 copy 方式
     */
    static setPrototypeOf(obj, proto) {
        if (Object.setPrototypeOf) {
            return Object.setPrototypeOf(obj, proto || this.create(null));
        } else {
            if (!('__proto__' in Object)) this.copy(proto, obj);
            obj.__proto__ = proto;
        }
    }


    /**
     * 获取 proto
     */
    static getPrototypeOf(obj) {
        if (obj.__proto__) return obj.__proto__;
        if (Object.getPrototypeOf) return Object.getPrototypeOf(obj);
        if (obj.constructor) return obj.constructor.prototype;
    }


    /**
     * 是否深度相等
     */
    static deepEqual(a, b) {
        if (a === b) return true;
        if (!this.isObject(a) || !this.isObject(b)) return false;
        var aKeys = this.keys(a);
        var bKeys = this.keys(b);
        if (aKeys.length !== bKeys.length) return false;
        var allKeys = aKeys.concat(bKeys);
        var checkedMap = this.create(null);
        var result = true;
        this.each(allKeys, function (i, key) {
            if (checkedMap[key]) return;
            if (!this.deepEqual(a[key], b[key])) result = false;
            checkedMap[key] = true;
        }, this);
        return result;
    }


    /**
     * 从一个数值循环到别一个数
     * @param {number} fromNum 开始数值
     * @param {Number} toNum 结束数值
     * @param {Number} step 步长值
     * @param {function} handler 执行函数
     * @returns {void} 无返回
     */
    static fromTo(fromNum, toNum, step, handler) {
        if (!handler) handler = [step, step = handler][0];
        step = Math.abs(step || 1);
        if (fromNum < toNum) {
            for (var i = fromNum; i <= toNum; i += step) handler(i);
        } else {
            for (var i = fromNum; i >= toNum; i -= step) handler(i);
        }
    }


    /**
     * 生成一个Guid
     * @method newGuid
     * @return {String} GUID字符串
     * @static
     */
    static newGuid() {
        var S4 = function () {
            return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
        };
        return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4() + S4() + S4());
    }


    /**
     * 对象变换
     **/
    static map(list, fn) {
        var buffer = this.isArray(list) ? [] : {};
        this.each(list, function (name, value) {
            buffer[name] = fn(name, value);
        });
        return buffer;
    }


    /**
     * 通过路径设置属性值
     */
    static setByPath(obj, path, value) {
        if (this.isNull(obj) || this.isNull(path) || path === '') {
            return;
        }
        if (!this.isArray(path)) {
            path = path.replace(/\[/, '.').replace(/\]/, '.').split('.');
        }
        this.each(path, function (index, name) {
            if (this.isNull(name) || name.length < 1) return;
            if (index === path.length - 1) {
                obj[name] = value;
            } else {
                obj[name] = obj[name] || {};
                obj = obj[name];
            }
        }, this);
    }


    /**
     * 通过路径获取属性值
     */
    static getByPath(obj, path) {
        if (this.isNull(obj) || this.isNull(path) || path === '') {
            return obj;
        }
        if (!this.isArray(path)) {
            path = path.replace(/\[/, '.').replace(/\]/, '.').split('.');
        }
        this.each(path, function (index, name) {
            if (this.isNull(name) || name.length < 1) return;
            if (!this.isNull(obj)) obj = obj[name];
        }, this);
        return obj;
    }


    /**
     * 数组去重
     **/
    static unique(array) {
        if (this.isNull(array)) return array;
        var newArray = [];
        this.each(array, function (i, value) {
            if (newArray.indexOf(value) > -1) return;
            newArray.push(value);
        });
        return newArray;
    }


    /**
     * 解析 function 的参数列表
     **/
    static etFunctionArgumentNames(fn) {
        if (!fn) return [];
        var src = fn.toString();
        var parts = src.split(')')[0].split('=>')[0].split('(');
        return (parts[1] || parts[0]).split(',').map(function (name) {
            return name.trim();
        }).filter(function (name) {
            return name != 'function';
        });
    }


    /**
     * 缩短字符串
     */
    static short(str, maxLength) {
        if (!str) return str;
        maxLength = maxLength || 40;
        var strLength = str.length;
        var trimLength = maxLength / 2;
        return strLength > maxLength ? str.substr(0, trimLength) + '...' + str.substr(strLength - trimLength) : str;
    }


    /**
     * 首字母大写
     */
    static firstUpper(str) {
        if (this.isNull(str)) return;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    /**
     * 编码正则字符串
     */
    static escapeRegExp(str) {
        return str.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
    }


    /**
     * 解析字符串为 dom
     * @param {string} str 字符串
     * @returns {HTMLNode} 解析后的 DOM
     */
    static parseDom(str) {
        this._PDD_ = this._PDD_ || document.createElement('div');
        this._PDD_.innerHTML = ntils.trim(str);
        var firstNode = this._PDD_.childNodes[0];
        //先 clone 一份再通过 innerHTML 清空
        //否则 IE9 下，清空时会导出返回的 DOM 没有子结点
        if (firstNode) firstNode = firstNode.cloneNode(true);
        this._PDD_.innerHTML = '';
        return firstNode;
    }

}