import _C                    from '../../../shared/term/criteria.term.class';
import _I                    from '../../../shared/utils/ionic.utils.class';

export default class AssetsService extends AbstractService {

    constructor() {
        super();
        this.mapper = mapper['D_Assets'];
    }

    async selectMethodByWhereOL() {
        let result = await this.mapper.selectMethodByWhereOL(_C.ASSETS_SCOPE_SERVICE, _C.DEFAULT_STATUS_ENABLE);
        if (!_I.isEmpty(result)) {
            let data = {};
            for (let i in result) {
                let json = result[i].attributes;
                if (json.method !== null) {
                    data[json.method.replace(/\./g, '_')] = {
                        'type': json.type == 1 ? 'POST' : 'GET',
                        'method': json.method,
                        'format': 'json',
                        'v': json.version
                    };
                }
            }
            result[0] = data;
        }
        return result;
    }

    async selectMethodByWhere(method, version) {
        let result = await this.mapper.selectMethodByWhere(_C.ASSETS_SCOPE_SERVICE, _C.DEFAULT_STATUS_ENABLE, method, version);
        if (!_I.isEmpty(result)) {
            return result[0].attributes;
        }
        return result;
    }

}