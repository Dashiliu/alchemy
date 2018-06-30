export class Content {
    constructor(public code?: string[], public config?: string) {
        this.code = code ? code : [''];
        this.config = config ? config : null;
    }
}
