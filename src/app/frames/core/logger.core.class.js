import Path                  from 'path';
import Fs                    from 'fs';
import Log4js                from 'log4js';

export default class Logger {

    constructor() {
        console.log('Initlizing Logger');
        this.init();
    }

    init() {
        const baseDir = Path.join(config.objConfig.logs);
        let objConfig = JSON.parse(Fs.readFileSync(Path.join(config.ConfigPath, 'log4js.config.json'), "utf8"));
        for (let name in objConfig) {
            let item = objConfig[name];
            for (let key in item) {
                if (baseDir != null) {
                    if (item[key].type === "file" || item[key].type === "dateFile") {
                        if (item[key].filename == null) {
                            item[key].filename = baseDir;
                        } else {
                            item[key].filename = baseDir + '/' + config.objConfig.port + '/' + item[key].filename;
                        }
                    }
                }
                let fileName = item[key].filename;
                if (fileName == null) {
                    continue;
                }
                let pattern = item[key].pattern;
                if (pattern != null) {
                    fileName += pattern;
                }
                let dir = Path.dirname(fileName);
                if (!Fs.existsSync(dir)) {
                    Fs.mkdirSync(dir);
                }
            }
        }
        Log4js.configure(objConfig);
        this.createLogger(Log4js);
        this.use = function* (app) {
            app.use(Log4js.connectLogger(this.console, {
                level: config.LogLevel,
                format: ':method :url'
            }));
        }
    }

    createLogger(log4js) {
        this.console = log4js.getLogger('console');
        this.logAll = log4js.getLogger('logAll');
        this.logTrace = log4js.getLogger('logTrace');
        this.logDebug = log4js.getLogger('logDebug');
        this.logInfo = log4js.getLogger('logInfo');
        this.logWarn = log4js.getLogger('logWarn');
        this.logError = log4js.getLogger('logError');
        this.logFatal = log4js.getLogger('logFatal');
        if (config.Env === 1) {
            this.logger = this.console;
        } else {
            this.logger = this.logAll;
        }

    }

    trace(msg) {
        if (msg == null) {
            msg = "";
        }
        this.logger.trace(msg);
    }

    debug(msg) {
        if (msg == null) {
            msg = "";
        }
        this.logger.debug(msg);
    }

    info(msg) {
        if (msg == null) {
            msg = "";
        }
        this.logger.info(msg);
    }

    warn(msg) {
        if (msg == null) {
            msg = "";
        }
        this.logger.warn(msg);
    }

    error(msg, exp) {
        if (msg == null) {
            msg = "";
        }
        if (exp != null) {
            msg += "\r\n" + exp;
        }
        this.logger.error(msg);
    }

    fatal(msg, exp) {
        if (msg == null) {
            msg = "";
        }
        if (exp != null) {
            msg += "\r\n" + exp;
        }
        this.logger.fatal(msg);
    }

}