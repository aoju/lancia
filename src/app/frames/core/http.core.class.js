import Router                from 'koa-router';

export default (function (HttpMethod) {
    HttpMethod[HttpMethod["ALL"] = -1] = "ALL";
    HttpMethod[HttpMethod["GET"] = 0] = "GET";
    HttpMethod[HttpMethod["POST"] = 1] = "POST";
    HttpMethod[HttpMethod["PUT"] = 2] = "PUT";
    HttpMethod[HttpMethod["DELETE"] = 3] = "DELETE";
    HttpMethod[HttpMethod["OPTIONS"] = 4] = "OPTIONS";
    HttpMethod[HttpMethod["HEAD"] = 5] = "HEAD";
    HttpMethod[HttpMethod["PATCH"] = 6] = "PATCH";
    HttpMethod[HttpMethod["TRACE"] = 7] = "TRACE";
    HttpMethod[HttpMethod["CONNECT"] = 8] = "CONNECT";
})

(exports.HttpMethod || (exports.HttpMethod = {}));

const HttpMethod = exports.HttpMethod;

export function Route(path, method, ...middleware) {

    return (target, key, descriptor) => {

        if (!target.prototype.router) {
            target.prototype.router = new Router();
        }
        if (typeof target === 'function' && key === undefined && descriptor === undefined) {
            target.prototype.router.prefix(path);
            return;
            if (middleware.length > 0) {
                target.prototype.router.use(...middleware);
            }
        }
        switch (method) {
            case HttpMethod.HEAD:
                target.prototype.router.head(path, ...middleware, descriptor.value);
                break;
            case HttpMethod.OPTIONS:
                target.prototype.router.options(path, ...middleware, descriptor.value);
                break;
            case HttpMethod.GET:
                target.prototype.router.get(path, ...middleware, descriptor.value);
                break;
            case HttpMethod.PUT:
                target.prototype.router.put(path, ...middleware, descriptor.value);
                break;
            case HttpMethod.PATCH:
                target.prototype.router.patch(path, ...middleware, descriptor.value);
                break;
            case HttpMethod.POST:
                target.prototype.router.post(path, ...middleware, descriptor.value);
                break;
            case HttpMethod.DELETE:
                target.prototype.router.delete(path, ...middleware, descriptor.value);
                break;
            case HttpMethod.CONNECT:
                target.prototype.router.connect(path, ...middleware, descriptor.value);
                break;
            case HttpMethod.ALL:
                target.prototype.router.all(path, ...middleware, descriptor.value);
                break;
            default:
                throw new Error('@Route decorator "method" is not valid');
        }
    };
}