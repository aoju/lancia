import xml2js                from 'xml2js';
import Ionic                 from './ionic.utils.class';
import _C                    from './../term/criteria.term.class';

export default class Unify {

    /**
     * 返回包裹器
     * @param data 数据
     * @param format  json xml
     * @param ctx
     */
    static isWrapper(ctx, data, format) {
        if (!Ionic.isEmpty(format) && _C.FOTMAT_TYPE_XML == format.toLowerCase()) {
            return this.toXml(ctx, data);
        } else if (!Ionic.isEmpty(format) && _C.FOTMAT_TYPE_BINARY == format.toLowerCase()) {
            return this.toBinary(ctx, data);
        }
        return this.toJson(ctx, data);
    }

    static toXml(ctx, data) {
        ctx.type = 'application/xml ; charset="utf-8"';
        let builder = new xml2js.Builder({
            rootName: 'response',
            xmldec: {
                'version': '1.0',
                'encoding': 'UTF-8'
            }
        });
        if (typeof(data) != 'object') {
            data = JSON.parse(data);
        }
        return builder.buildObject(data);

    }

    static toJson(ctx, data) {
        ctx.type = 'application/json ; charset="utf-8"';
        return data;
    }

    static toBinary(ctx, data) {
        if (Ionic.isNull(data.errcode)) {
            return ctx.req.pipe(data);
        } else {
            return this.toJson(ctx, data);
        }
    }

}