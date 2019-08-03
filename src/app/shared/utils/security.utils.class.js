import CryptoJS              from 'crypto-js';
import criteria              from '../term/criteria.term.class'

export default class Security {

    static encrypt(val) {
        return CryptoJS.AES.encrypt(CryptoJS.enc.Utf8.parse(val), CryptoJS.enc.Utf8.parse(criteria.CRYPTOJS_KEY), {iv: CryptoJS.enc.Utf8.parse(criteria.CRYPTOJS_IV), mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7}).toString();
    }

    static decrypt(val) {
        let bytes = CryptoJS.AES.decrypt(val, CryptoJS.enc.Utf8.parse(criteria.CRYPTOJS_KEY), {iv: CryptoJS.enc.Utf8.parse(criteria.CRYPTOJS_IV),mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7});
        return bytes.toString(CryptoJS.enc.Utf8);
    }

    static md5(str) {
        return CryptoJS.MD5(str).toString(CryptoJS.enc.Hex).toUpperCase();
    }

    static raw(args) {
        var keys = Object.keys(args);
        keys = keys.sort()
        var newArgs = {};
        keys.forEach(function (key) {
            newArgs[key.toLowerCase()] = args[key];
        });

        var string = '';
        for (var k in newArgs) {
            string += '&' + k + '=' + newArgs[k];
        }
        string = string.substr(1);
        return string;
    };

    /**
     * @synopsis 签名算法验证
     *
     * @param
     * @param
     *
     * @returns
     */
    static sign(timestamp, method, sign, version) {
        var ret = {
            version: version,
            timestamp: timestamp,
            method: method
        };
        var string = raw(ret);
        if (md5(string) == sign) {
            return true;
        } else {
            return false;
        }
    }

}