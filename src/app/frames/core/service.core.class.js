import Path                  from 'path';
import Fs                    from 'fs';

export default class Service {

    constructor() {
        logger.trace("Initlizing Service");
        this.initService();
    }

    initService() {
        let service = this;
        this.recursiveFile(config.ClassPath, function (file) {
            let Service = require(file).default;
            let json = JSON.parse(Fs.readFileSync(Path.join(config.ConfigPath, 'service.config.json'), "utf8"));
            let files = Object.keys(json).sort();
            for (let i = 0, j = files.length; i < j; i++) {
                if (json[files[i]] == Path.basename(file).split('.js')[0]) {
                    service[files[i]] = new Service();
                    logger.debug("Load Service --> " + files[i]);
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
            if (file.indexOf('service.class') > -1) {
                done(filePath);
            }
        });
    }

}