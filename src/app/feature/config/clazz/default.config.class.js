import Path                  from 'path';
import Fs                    from 'fs';
import Version               from '../../../version.class';

export default class DefaultConfig {

    constructor(rootPath) {
        this.Version = Version;
        this.RootPath = rootPath + '/app';
        this.setPathConfig();
        this.setEnvConfig();
        this.setServerConfig();
        this.setBrowserConfig();
    }

    setPathConfig() {
        this.objConfig = JSON.parse(Fs.readFileSync(Path.join(this.RootPath, '../../bin/config.json'), "utf8"));
        this.ConfigPath = Path.join(this.RootPath, './feature/config/json');
        this.ClassPath = Path.join(this.RootPath, './');
    }

    setEnvConfig() {
        this.debug = this.objConfig.debug || process.env.NODE_ENV;
        if (this.debug === 'prod') {
            this.Env = 3;
            this.LogLevel = 'logInfo';
        } else if (this.debug === 'test') {
            this.Env = 2;
            this.LogLevel = 'logDebug';
        } else {
            this.debug = 'dev';
            this.LogLevel = 'logAll';
            this.Env = 1;
        }
    }

    setServerConfig() {
        this.Port = this.objConfig.port;
    }

    setBrowserConfig() {
        this.size = this.objConfig.size;
        this.ignore = this.objConfig.ignore;
    }

}