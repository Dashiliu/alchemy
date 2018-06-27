export class Conf {
    id?: any;
    acJobId?: string;
    content?: string;
    type?: number;
    createTime?: Date;
    updateTime?: Date;

    constructor(
        public id?: any,
        public acJobId?: string,
        public content?: string,
        public type?: number,
        public createTime?: Date,
        public updateTime?: Date
    ) {
        this.id = id ? id : null;
        this.acJobId = acJobId ? acJobId : null;
        this.content = content ? content : null;
        this.type = type ? type : null;
        this.createTime = createTime ? createTime : null;
        this.updateTime = updateTime ? updateTime : null;
    }
}
