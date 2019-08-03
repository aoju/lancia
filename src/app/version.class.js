import Path                  from 'path';

var json = require(Path.join(Path.dirname(__dirname), '../package.json'));
module.exports = json.version;