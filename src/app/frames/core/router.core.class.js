import Path                  from 'path';
import Fs                    from 'fs';
import KoaRouter             from 'koa-router';

/**
 * 生成模型默认路由
 * @param Router
 */
export default class Router {

    constructor() {
        logger.trace("Initlizing Router");
        this.koaRouter = new KoaRouter();
        this.initRouter();
    }

    initRouter() {
        let koaRouter = this.koaRouter;
        this.recursiveFile(config.ClassPath, function (file) {
            let Router = require(file).default;
            let json = JSON.parse(Fs.readFileSync(Path.join(config.ConfigPath, 'router.config.json'), "utf8"));
            let files = Object.keys(json).sort();
            for (let i = 0, j = files.length; i < j; i++) {
                if (json[files[i]] == Path.basename(file).split('.js')[0]) {
                    koaRouter.use(new Router());
                    logger.debug("Load Router --> " + files[i]);
                }
            }
        });
    }

    recursiveFile(clazzPath, done) {
        let that = this;
        Fs.readdirSync(clazzPath).forEach(function (file) {
            let filePath = Path.join(clazzPath, file);
            if (Fs.lstatSync(filePath).isDirectory()) {
                that.recursiveFile(filePath, done);
            }
            if (file.indexOf('router.class') > -1) {
                done(filePath);
            }
        });
    }

    getRoutes() {
        return this.koaRouter.routes();
    }

    getAllowedMethods() {
        return this.koaRouter.allowedMethods();
    }

}