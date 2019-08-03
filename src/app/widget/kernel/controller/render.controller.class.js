import Render from "../../../frames/core/render.core.class";

export default class RenderController extends AbstractController {

    constructor() {
        super();
    }

    async selectMethodByWhereOL(ctx,next, params) {
        return new Promise(function (resolve, reject) {
            Render.render(params)
                .then((data) => {
                    resolve(data);
                }).catch(function (err) {
                reject(err);
            });
        });
    }

}