import Ionic                 from './ionic.utils.class';

export function mt_rand(min, max) {
    let argc = arguments.length;
    if (argc === 0) {
        min = 0;
        max = 2147483647;
    } else if (argc === 1) {
        throw new Error('Warning: mt_rand() expects exactly 2 parameters, 1 given');
    } else {
        min = parseInt(min, 10);
        max = parseInt(max, 10);
    }
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

/**
 *  随机生成几位随机数
 * @param length
 * @returns {string}
 */
export function create_random_number(length) {
    let chars = "0123456789";
    let str = "";

    for (let i = 0; i < length; i++) {
        let r = mt_rand(0, chars.length - 1);
        str = str + chars.substring(r, r + 1);
    }
    return str;

}


export function create_noncestr(length) {
    let chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    let str = "";

    for (let i = 0; i < length; i++) {
        let r = mt_rand(0, chars.length - 1);
        str = str + chars.substring(r, r + 1);
    }
    return str;
}


function random_amount(min, max) {
    let argc = arguments.length;
    if (argc === 0) {
        min = 1;
        max = 10;
    } else if (argc === 1) {
        throw new Error('Warning: mt_rand() expects exactly 2 parameters, 1 given');
    } else {
        min = parseInt(min, 10);
        max = parseInt(max, 10);
    }
    return Math.floor(Math.random() * (max - min + 1)) + min;
}


export function ksort(inputArr, sort_flags) {
    //  discuss at: http://phpjs.org/functions/ksort/
    // original by: GeekFG (http://geekfg.blogspot.com)
    // improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // improved by: Brett Zamir (http://brett-zamir.me)
    //        note: The examples are correct, this is a new way
    //        note: This function deviates from PHP in returning a copy of the array instead
    //        note: of acting by reference and returning true; this was necessary because
    //        note: IE does not allow deleting and re-adding of properties without caching
    //        note: of property position; you can set the ini of "phpjs.strictForIn" to true to
    //        note: get the PHP behavior, but use this only if you are in an environment
    //        note: such as Firefox extensions where for-in iteration order is fixed and true
    //        note: property deletion is supported. Note that we intend to implement the PHP
    //        note: behavior by default if IE ever does allow it; only gives shallow copy since
    //        note: is by reference in PHP anyways
    //        note: Since JS objects' keys are always strings, and (the
    //        note: default) SORT_REGULAR flag distinguishes by key type,
    //        note: if the content is a numeric string, we treat the
    //        note: "original type" as numeric.
    //  depends on: i18n_loc_get_default
    //  depends on: strnatcmp
    //   example 1: data = {d: 'lemon', a: 'orange', b: 'banana', c: 'apple'};
    //   example 1: data = ksort(data);
    //   example 1: $result = data
    //   returns 1: {a: 'orange', b: 'banana', c: 'apple', d: 'lemon'}
    //   example 2: ini_set('phpjs.strictForIn', true);
    //   example 2: data = {2: 'van', 3: 'Zonneveld', 1: 'Kevin'};
    //   example 2: ksort(data);
    //   example 2: $result = data
    //   returns 2: {1: 'Kevin', 2: 'van', 3: 'Zonneveld'}

    let tmp_arr = {},
        keys = [],
        sorter, i, k, that = this,
        strictForIn = false,
        populateArr = {};

    switch (sort_flags) {
        case 'SORT_STRING':
            // compare items as strings
            sorter = function (a, b) {
                return that.strnatcmp(a, b);
            };
            break;
        case 'SORT_LOCALE_STRING':
            // compare items as strings, original by the current locale (set with  i18n_loc_set_default() as of PHP6)
            let loc = this.i18n_loc_get_default();
            sorter = this.php_js.i18nLocales[loc].sorting;
            break;
        case 'SORT_NUMERIC':
            // compare items numerically
            sorter = function (a, b) {
                return ((a + 0) - (b + 0));
            };
            break;
        // case 'SORT_REGULAR': // compare items normally (don't change types)
        default:
            sorter = function (a, b) {
                let aFloat = parseFloat(a),
                    bFloat = parseFloat(b),
                    aNumeric = aFloat + '' === a,
                    bNumeric = bFloat + '' === b;
                if (aNumeric && bNumeric) {
                    return aFloat > bFloat ? 1 : aFloat < bFloat ? -1 : 0;
                } else if (aNumeric && !bNumeric) {
                    return 1;
                } else if (!aNumeric && bNumeric) {
                    return -1;
                }
                return a > b ? 1 : a < b ? -1 : 0;
            };
            break;
    }

    // Make a list of key names
    for (let k in inputArr) {
        if (inputArr.hasOwnProperty(k)) {
            keys.push(k);
        }
    }
    keys.sort(sorter);

    // BEGIN REDUNDANT
    this.php_js = this.php_js || {};
    this.php_js.ini = this.php_js.ini || {};
    // END REDUNDANT
    strictForIn = this.php_js.ini['phpjs.strictForIn'] && this.php_js.ini[
            'phpjs.strictForIn'].local_value && this.php_js
            .ini['phpjs.strictForIn'].local_value !== 'off';
    populateArr = strictForIn ? inputArr : populateArr;

    // Rebuild array with sorted key names
    for (i = 0; i < keys.length; i++) {
        k = keys[i];
        tmp_arr[k] = inputArr[k];
        if (strictForIn) {
            delete inputArr[k];
        }
    }
    for (i in tmp_arr) {
        if (tmp_arr.hasOwnProperty(i)) {
            populateArr[i] = tmp_arr[i];
        }
    }

    return strictForIn || populateArr;
}


export function format_query_string_for_sign(object) {
    let _buff = "";
    let _sorted_object = ksort(object);
    for (let _key in _sorted_object) {
        if (null !== _sorted_object[_key] && "null" != _sorted_object[_key] && 0 !==
            _sorted_object[_key].length && "id" != _key && "sign" != _key) {
            _buff = _buff + _key + "=" + _sorted_object[_key] + "&";
        }
    }
    let _result = "";
    if (_buff.length > 0) {
        _result = _buff.substring(0, _buff.length - 1);
    }
    return _result;
}


export function json_to_xml(object) {
    let xml = "<xml>";
    for (let _key in object) {
        if (!Ionic.isFunction(object[_key]) && null !== object[_key] && "null" !=
            object[_key] && 0 !== object[_key].length && "id" != _key) {
            if (Ionic.isNumber(object[_key])) {
                xml = xml + "<" + _key + ">" + object[_key] + "</" + _key + ">";
                //xml = xml + "<" + _key + "><![CDATA[" + object[_key] + "]]></" + _key + ">";
            } else {
                //xml = xml + "<" + _key + ">" + object[_key] + "</" + _key + ">";
                xml = xml + "<" + _key + "><![CDATA[" + object[_key] + "]]></" + _key +
                    ">";
            }
        }
    }
    xml = xml + "</xml>";
    return xml;
}

export function strnatcmp(f_string1, f_string2, f_version) {
    //  discuss at: http://phpjs.org/functions/strnatcmp/
    // original by: Martijn Wieringa
    // improved by: Michael White (http://getsprink.com)
    // improved by: Jack
    // bugfixed by: Onno Marsman
    //  depends on: strcmp
    //        note: Added f_version argument against code guidelines, because it's so neat
    //   example 1: strnatcmp('Price 12.9', 'Price 12.15');
    //   returns 1: 1
    //   example 2: strnatcmp('Price 12.09', 'Price 12.15');
    //   returns 2: -1
    //   example 3: strnatcmp('Price 12.90', 'Price 12.15');
    //   returns 3: 1
    //   example 4: strnatcmp('Version 12.9', 'Version 12.15', true);
    //   returns 4: -6
    //   example 5: strnatcmp('Version 12.15', 'Version 12.9', true);
    //   returns 5: 6

    let i = 0;

    if (f_version == undefined) {
        f_version = false;
    }

    let __strnatcmp_split = function (f_string) {
        let result = [];
        let buffer = '';
        let chr = '';
        let i = 0,
            f_stringl = 0;

        let text = true;

        f_stringl = f_string.length;
        for (i = 0; i < f_stringl; i++) {
            chr = f_string.substring(i, i + 1);
            if (chr.match(/\d/)) {
                if (text) {
                    if (buffer.length > 0) {
                        result[result.length] = buffer;
                        buffer = '';
                    }

                    text = false;
                }
                buffer += chr;
            } else if ((text == false) && (chr === '.') && (i < (f_string.length -
                1)) && (f_string.substring(i + 1, i +
                    2)
                    .match(/\d/))) {
                result[result.length] = buffer;
                buffer = '';
            } else {
                if (text == false) {
                    if (buffer.length > 0) {
                        result[result.length] = parseInt(buffer, 10);
                        buffer = '';
                    }
                    text = true;
                }
                buffer += chr;
            }
        }

        if (buffer.length > 0) {
            if (text) {
                result[result.length] = buffer;
            } else {
                result[result.length] = parseInt(buffer, 10);
            }
        }

        return result;
    };

    let array1 = __strnatcmp_split(f_string1 + '');
    let array2 = __strnatcmp_split(f_string2 + '');

    let len = array1.length;
    let text = true;

    let result = -1;
    let r = 0;

    if (len > array2.length) {
        len = array2.length;
        result = 1;
    }

    for (i = 0; i < len; i++) {
        if (isNaN(array1[i])) {
            if (isNaN(array2[i])) {
                text = true;

                if ((r = this.strcmp(array1[i], array2[i])) != 0) {
                    return r;
                }
            } else if (text) {
                return 1;
            } else {
                return -1;
            }
        } else if (isNaN(array2[i])) {
            if (text) {
                return -1;
            } else {
                return 1;
            }
        } else {
            if (text || f_version) {
                if ((r = (array1[i] - array2[i])) != 0) {
                    return r;
                }
            } else {
                if ((r = this.strcmp(array1[i].toString(), array2[i].toString())) != 0) {
                    return r;
                }
            }

            text = false;
        }
    }

    return result;
}

export function sort(inputArr, sort_flags) {

    let valArr = [],
        keyArr = [],
        k = '',
        i = 0,
        sorter = false,
        that = this,
        strictForIn = false,
        populateArr = [];

    switch (sort_flags) {
        case 'SORT_STRING':
            // compare items as strings
            sorter = function (a, b) {
                return strnatcmp(a, b);
            };
            break;
        case 'SORT_LOCALE_STRING':
            // compare items as strings, original by the current locale (set with  i18n_loc_set_default() as of PHP6)
            let loc = this.i18n_loc_get_default();
            sorter = this.php_js.i18nLocales[loc].sorting;
            break;
        case 'SORT_NUMERIC':
            // compare items numerically
            sorter = function (a, b) {
                return (a - b);
            };
            break;
        case 'SORT_REGULAR':
        // compare items normally (don't change types)
        default:
            sorter = function (a, b) {
                let aFloat = parseFloat(a),
                    bFloat = parseFloat(b),
                    aNumeric = aFloat + '' === a,
                    bNumeric = bFloat + '' === b;
                if (aNumeric && bNumeric) {
                    return aFloat > bFloat ? 1 : aFloat < bFloat ? -1 : 0;
                } else if (aNumeric && !bNumeric) {
                    return 1;
                } else if (!aNumeric && bNumeric) {
                    return -1;
                }
                return a > b ? 1 : a < b ? -1 : 0;
            };
            break;
    }

    // BEGIN REDUNDANT
    try {
        this.php_js = this.php_js || {};
    } catch (e) {
        this.php_js = {};
    }

    this.php_js.ini = this.php_js.ini || {};
    // END REDUNDANT
    strictForIn = this.php_js.ini['phpjs.strictForIn'] && this.php_js.ini[
            'phpjs.strictForIn'].local_value && this.php_js
            .ini['phpjs.strictForIn'].local_value !== 'off';
    populateArr = strictForIn ? inputArr : populateArr;

    for (k in inputArr) { // Get key and value arrays
        if (inputArr.hasOwnProperty(k)) {
            valArr.push(inputArr[k]);
            if (strictForIn) {
                delete inputArr[k];
            }
        }
    }

    valArr.sort(sorter);

    for (i = 0; i < valArr.length; i++) { // Repopulate the old array
        populateArr[i] = valArr[i];
    }
    return strictForIn || populateArr;
}


export function implode(glue, pieces) {
    //  discuss at: http://phpjs.org/functions/implode/
    // original by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    // improved by: Waldo Malqui Silva
    // improved by: Itsacon (http://www.itsacon.net/)
    // bugfixed by: Brett Zamir (http://brett-zamir.me)
    //   example 1: implode(' ', ['Kevin', 'van', 'Zonneveld']);
    //   returns 1: 'Kevin van Zonneveld'
    //   example 2: implode(' ', {first:'Kevin', last: 'van Zonneveld'});
    //   returns 2: 'Kevin van Zonneveld'

    let i = '',
        retVal = '',
        tGlue = '';
    if (arguments.length === 1) {
        pieces = glue;
        glue = '';
    }
    if (typeof pieces === 'object') {
        if (Object.prototype.toString.call(pieces) === '[object Array]') {
            return pieces.join(glue);
        }
        for (i in pieces) {
            retVal += tGlue + pieces[i];
            tGlue = glue;
        }
        return retVal;
    }
    return pieces;
}


export function sha1(str) {
    //  discuss at: http://phpjs.org/functions/sha1/
    // original by: Webtoolkit.info (http://www.webtoolkit.info/)
    // improved by: Michael White (http://getsprink.com)
    // improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
    //    input by: Brett Zamir (http://brett-zamir.me)
    //  depends on: utf8_encode
    //   example 1: sha1('Kevin van Zonneveld');
    //   returns 1: '54916d2e62f65b3afa6e192e6a601cdbe5cb5897'

    let rotate_left = function (n, s) {
        let t4 = (n << s) | (n >>> (32 - s));
        return t4;
    };

    /*let lsb_hex = function (val) { // Not in use; needed?
     let str="";
     let i;
     let vh;
     let vl;

     for ( i=0; i<=6; i+=2 ) {
     vh = (val>>>(i*4+4))&0x0f;
     vl = (val>>>(i*4))&0x0f;
     str += vh.toString(16) + vl.toString(16);
     }
     return str;
     };*/

    let cvt_hex = function (val) {
        let str = '';
        let i;
        let v;

        for (i = 7; i >= 0; i--) {
            v = (val >>> (i * 4)) & 0x0f;
            str += v.toString(16);
        }
        return str;
    };

    let blockstart;
    let i, j;
    let W = new Array(80);
    let H0 = 0x67452301;
    let H1 = 0xEFCDAB89;
    let H2 = 0x98BADCFE;
    let H3 = 0x10325476;
    let H4 = 0xC3D2E1F0;
    let A, B, C, D, E;
    let temp;

    str = this.utf8_encode(str);
    let str_len = str.length;

    let word_array = [];
    for (i = 0; i < str_len - 3; i += 4) {
        j = str.charCodeAt(i) << 24 | str.charCodeAt(i + 1) << 16 | str.charCodeAt(
                i + 2) << 8 | str.charCodeAt(i + 3);
        word_array.push(j);
    }

    switch (str_len % 4) {
        case 0:
            i = 0x080000000;
            break;
        case 1:
            i = str.charCodeAt(str_len - 1) << 24 | 0x0800000;
            break;
        case 2:
            i = str.charCodeAt(str_len - 2) << 24 | str.charCodeAt(str_len - 1) << 16 |
                0x08000;
            break;
        case 3:
            i = str.charCodeAt(str_len - 3) << 24 | str.charCodeAt(str_len - 2) << 16 |
                str.charCodeAt(str_len - 1) <<
                8 | 0x80;
            break;
    }

    word_array.push(i);

    while ((word_array.length % 16) != 14) {
        word_array.push(0);
    }

    word_array.push(str_len >>> 29);
    word_array.push((str_len << 3) & 0x0ffffffff);

    for (blockstart = 0; blockstart < word_array.length; blockstart += 16) {
        for (i = 0; i < 16; i++) {
            W[i] = word_array[blockstart + i];
        }
        for (i = 16; i <= 79; i++) {
            W[i] = rotate_left(W[i - 3] ^ W[i - 8] ^ W[i - 14] ^ W[i - 16], 1);
        }

        A = H0;
        B = H1;
        C = H2;
        D = H3;
        E = H4;

        for (i = 0; i <= 19; i++) {
            temp = (rotate_left(A, 5) + ((B & C) | (~B & D)) + E + W[i] + 0x5A827999) &
                0x0ffffffff;
            E = D;
            D = C;
            C = rotate_left(B, 30);
            B = A;
            A = temp;
        }

        for (i = 20; i <= 39; i++) {
            temp = (rotate_left(A, 5) + (B ^ C ^ D) + E + W[i] + 0x6ED9EBA1) &
                0x0ffffffff;
            E = D;
            D = C;
            C = rotate_left(B, 30);
            B = A;
            A = temp;
        }

        for (i = 40; i <= 59; i++) {
            temp = (rotate_left(A, 5) + ((B & C) | (B & D) | (C & D)) + E + W[i] +
                0x8F1BBCDC) & 0x0ffffffff;
            E = D;
            D = C;
            C = rotate_left(B, 30);
            B = A;
            A = temp;
        }

        for (i = 60; i <= 79; i++) {
            temp = (rotate_left(A, 5) + (B ^ C ^ D) + E + W[i] + 0xCA62C1D6) &
                0x0ffffffff;
            E = D;
            D = C;
            C = rotate_left(B, 30);
            B = A;
            A = temp;
        }

        H0 = (H0 + A) & 0x0ffffffff;
        H1 = (H1 + B) & 0x0ffffffff;
        H2 = (H2 + C) & 0x0ffffffff;
        H3 = (H3 + D) & 0x0ffffffff;
        H4 = (H4 + E) & 0x0ffffffff;
    }

    temp = cvt_hex(H0) + cvt_hex(H1) + cvt_hex(H2) + cvt_hex(H3) + cvt_hex(H4);
    return temp.toLowerCase();
}


export function treeify(nodes) {
    let indexed_nodes = {},
        tree_roots = [];
    for (let i = 0; i < nodes.length; i += 1) {
        nodes[i] = JSON.parse(nodes[i]);

        indexed_nodes[nodes[i].id] = nodes[i];
        indexed_nodes[nodes[i].id].children = [];
    }

    for (let i = 0; i < nodes.length; i += 1) {
        if (nodes[i].depth == '1' || nodes[i].depth == 1) {
            tree_roots.push(nodes[i]);
        } else {
            indexed_nodes[nodes[i].parent_id].children.push(nodes[i]);
        }
    }
    return tree_roots;
}