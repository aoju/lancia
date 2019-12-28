import Net                   from 'net';
import Path                  from 'path';

import Koa                   from 'koa';
import Cors                  from 'koa-cors';
import KoaLogger             from 'koa-logger';
import KoaConvert            from 'koa-convert';
import KoaFavicon            from 'koa-favicon';
import KoaBetterBody         from 'koa-better-body';

import DefaultConfig         from './feature/config/clazz/default.config.class';

import Logger                from './frames/core/logger.core.class';
import Service               from './frames/core/service.core.class';
import Controller            from './frames/core/controller.core.class';
import Router                from './frames/core/router.core.class';

import AbstractService       from './frames/base/service.base.class';
import AbstractController    from './frames/base/controller.base.class';
import AbstractRouter        from './frames/base/router.base.class';
import Unify                 from "./shared/utils/unify.utils.class";
import Result                from "./shared/term/result.term.class";


export default class Bootstrap {

    constructor(rootPath) {
        this.startTime = new Date();
        this.koa = new Koa();
        this.opts = {};
        this.opts.RootPath = rootPath;
    }

    exec() {
        this._xConfig();
        this._xLogger();
        this._xHandle();
        this._xBetterBody();
        this._xService();
        this._xController();
        this._xRouter();
        this._xResource();
        this._xServer();
        logger.trace('Server Start Total Cost : ' + (new Date() - this.startTime) + 'ms');
        logger.trace('Current Service Version : ' + config.Version);
    }

    _xConfig() {
        global.config = new DefaultConfig(this.opts.RootPath);
        global.AbstractService = AbstractService;
        global.AbstractController = AbstractController;
        global.AbstractRouter = AbstractRouter;
        console.log('Finish Load Config');
    }

    _xLogger() {
        global.logger = new Logger();
        this.koa.use(KoaLogger());
        logger.use(this.koa);
        logger.trace('Finish Load Logger');
    }

    _xService() {
        global.service = new Service();
        logger.trace('Finish Load Service');
    }

    _xController() {
        global.controller = new Controller();
        logger.trace('Finish Load Controller');
    }

    _xRouter() {
        let router = new Router();
        this.koa.use(router.getRoutes()).use(router.getAllowedMethods());
        this.koa.use(KoaConvert(Cors({
            origin: '*',
            maxAge: 5,
            credentials: true,
            methods: ['GET', 'POST', 'OPTIONS'],
            headers: ['Content-Type', 'X-Requested-With', 'Accept', 'Cache-Control'],
        })));
        logger.trace('Finish Load Router');
    }

    _xBetterBody() {
        // this.koa.use(new KoaBodyParser());
        this.koa.use(KoaConvert(KoaBetterBody({
            multipart: true,
            formLimit: '500kb'
        })));
        logger.trace('Finish Load Body Parser');
    }

    _xServer() {
        let probe = function (port, callback) {
            let server = Net.createServer().listen(port);
            let calledOnce = false;
            let timeoutRef = setTimeout(() => {
                calledOnce = true;
                callback(false, port);
            }, 2000);
            timeoutRef.unref();
            server.on('listening', () => {
                clearTimeout(timeoutRef);
                if (server) {
                    server.close();
                }
                if (!calledOnce) {
                    calledOnce = true;
                    callback(true, port);
                }
            })
            server.on('error', err => {
                clearTimeout(timeoutRef)
                let result = true
                if (err.code === 'EADDRINUSE') {
                    result = false;
                }
                if (!calledOnce) {
                    calledOnce = true;
                    callback(result, port);
                }
            })
        }
        let that = this;
        let cb = function (bl, port) {
            if (bl === true) {
                that.koa.listen(port);
                logger.trace('Server Listening On Port : ' + port);
            } else {
                probe(port + 1, cb);
            }
        };
        this.port = process.argv[2] || config.Port;
        probe(this.port, cb);
    }

    _xResource() {
        this.koa.use(KoaFavicon(Path.dirname(__dirname) + '/assets/favicon.ico'));
        logger.trace('Finish Load Resource');
    }

    _xHandle() {
        this.koa.use((ctx, next) => {
            return next().catch((error) => {
                logger.trace("ERROR:" + error);
                return ctx.body = Unify.isWrapper(ctx, Result.ERROR.EM_SYSTEM, 'json');
            })
        })
        logger.trace('Finish Load Handle');
    }

}