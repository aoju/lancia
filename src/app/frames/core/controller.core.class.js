import Path                  from 'path';
import Fs                    from 'fs';

export default class Controller {

    constructor() {
        logger.trace("Initlizing Controller");
        this.iteratorController();
    }

    iteratorController() {
        let controller = this;
        this.recursiveFile(config.ClassPath, function (file) {
            let Controller = require(file).default;
            let json = JSON.parse(Fs.readFileSync(Path.join(config.ConfigPath, 'controller.config.json'), "utf8"));
            let files = Object.keys(json).sort();
            for (let i = 0, j = files.length; i < j; i++) {
                if (json[files[i]] == Path.basename(file).split('.js')[0]) {
                    controller[files[i]] = new Controller();
                    logger.debug("Load Controller --> " + files[i]);
                    break;
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
            if (file.indexOf('controller.class') > -1) {
                done(filePath);
            }
        });
    }

}