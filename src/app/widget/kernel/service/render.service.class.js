import                       Render from "../../../shared/utils/render.utils.class";

export default class RenderService extends AbstractService {

    constructor() {
        super();
    }

    async selectMethodByWhereOL(ctx, next, params) {
        return new Promise(function (resolve, reject) {
            Render.render(params)
                .then((data) => {
                    resolve(data);
                }).catch(function (err) {
                reject(err);
            });
        });
        ;
    }

}