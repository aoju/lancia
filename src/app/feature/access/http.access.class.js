import Ionic                 from '../../shared/utils/ionic.utils.class';
import Unify                 from '../../shared/utils/unify.utils.class';
import _R                    from '../../shared/term/result.term.class';
import _C                    from '../../shared/term/criteria.term.class';
import _match                from '../match/rest.match.class';
import {HttpMethod}          from "../../frames/core/http.core.class";

export default class AbstractAccess {

    static async all(ctx, next) {
        logger.trace(ctx.method.toUpperCase() + " => " + ctx.path);
        let params =   ctx.query;
        if (HttpMethod.POST == HttpMethod[ctx.method.toUpperCase()]) {
            params = ctx.body || ctx.request.fields || ctx.request.files || {};
        }
        if (!(ctx.path.indexOf(_C.DEFAULT_RULES_REST) >= 0)) {
            return ctx.body = Unify.isWrapper(ctx, _R.ERROR.EM_SYSTEM, params.format);
        }
        let path = ctx.path.split(_C.DEFAULT_RULES_REST)[1];
        if(path.indexOf('/') >= 0){
            params.v = '1.0';
            params.format = 'json';
            params.method = path;
        }
        if (!Ionic.has(params, 'method')) {
            return ctx.body = Unify.isWrapper(ctx, _R.ERROR.EM_100108, params.format);
        }
        if (!Ionic.has(params, 'v')) {
            return ctx.body = Unify.isWrapper(ctx, _R.ERROR.EM_100107, params.format);
        }
        if (!Ionic.has(params, 'format')) {
            return ctx.body = Unify.isWrapper(ctx, _R.ERROR.EM_100111, params.format);
        }
        return next();
    }

    static async match(ctx, next, method, params) {
        let path = ctx.path.split(_C.DEFAULT_RULES_REST)[1];
        if (path.indexOf('/') >= 0) {
            params.v = '1.0';
            params.format = 'json';
            params.method = path;
        }
        return _match.all(ctx, next, method, params);
    }

}