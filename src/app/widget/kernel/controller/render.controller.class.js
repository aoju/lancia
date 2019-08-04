export default class RenderController extends AbstractController {

    constructor() {
        super();
        this.service = service['S_Render'];
    }

    async selectMethodByWhereOL(ctx, next, params) {
        return await this.service.selectMethodByWhereOL(ctx, next, params);
    }

}