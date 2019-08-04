import {HttpMethod}          from '../../frames/core/http.core.class';
import Render                from "../../shared/utils/render.utils.class";
import Ionic                 from '../../shared/utils/ionic.utils.class';
import Unify                 from '../../shared/utils/unify.utils.class';
import _R                    from '../../shared/term/result.term.class';
import _C                    from '../../shared/term/criteria.term.class';
import Restful               from '../../widget/restful/org.aoju.restful.class';


export default class AbstractRestMatch {

    static async all(ctx, next, method, params) {
        logger.trace(HttpMethod[method] + " => " + JSON.stringify(params));
        if (Ionic.isEmpty(params.method)) {
            return ctx.body = Unify.isWrapper(ctx, _R.ERROR.EM_100103, params.format);
        }
        if (Ionic.isEmpty(params.v)) {
            return ctx.body = Unify.isWrapper(ctx, _R.ERROR.EM_100102, params.format);
        }
        if (Ionic.isEmpty(params.format)) {
            return ctx.body = Unify.isWrapper(ctx, _R.ERROR.EM_100105, params.format);
        }
        return this.process(ctx, next, method, params);
    }

    static async process(ctx, next, method, params) {
        try {
            if (config.token) {
                let accessToken = ctx.header['X-Access-Token'] || ctx.header['x-access-token'];
                if (accessToken) {
                    if (!_.includes(config.token, accessToken)) {
                        return ctx.body = Unify.isWrapper(ctx, _R.ERROR.EM_100100, params.format);
                    }
                } else {
                    return ctx.body = Unify.isWrapper(ctx, _R.ERROR.EM_100106, params.format);
                }
            }
            return this.internal(ctx, next, method, params)
        } catch (e) {
            logger.error(e);
            return ctx.body = Unify.isWrapper(ctx, _R.ERROR.EM_SYSTEM, params.format);
        }
    }

    static async internal(ctx, next, method, params) {
        let isMethod = [params.method.split(".")[0], params.method.split(".")[1], params.method.split(".")[2], params.method.split(".")[3]];
        if (!Ionic.has(Restful, isMethod.join('.'))) {
            return ctx.body = Unify.isWrapper(ctx, _R.ERROR.EM_100103, params.format);
        }
        if (!Ionic.has(Restful[isMethod.join('.')]['default'], params.v)
            || !Ionic.has(Restful[isMethod.join('.')]['default'][params.v], params.method)) {
            return ctx.body = Unify.isWrapper(ctx, _R.ERROR.EM_100103, params.format);
        }
        this.api = Restful[isMethod.join('.')]['default'][params.v][params.method]['fun'].split('.');

        logger.trace(HttpMethod[method] + ' => ' + JSON.stringify({
            "mode": "internal",
            "method": params.method,
            params
        }));

        if (params.format === _C.FOTMAT_TYPE_PDF) {
            params = Render.getOptsFromQuery(ctx.query);
            console.log(params);
            if (params.attachmentName) {
                ctx.attachment(params.attachmentName);
            }
            ctx.set('content-type', Render.getMimeType(params));
            this.result = await controller[this.api[0]][this.api[1]](ctx, next, params);
        }
        return next(ctx.body = this.result);
    }

}