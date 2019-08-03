import Path                  from 'path';
import Bootstrap             from './bootstrap.class';

export default class appClass {

    constructor() {
        this.bootstrap = new Bootstrap(Path.dirname(__dirname));
        this.bootstrap.exec();
    }

}
new appClass();