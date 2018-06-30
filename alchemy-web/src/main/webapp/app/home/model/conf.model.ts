import { Content } from './content.model';
export class Conf {
    constructor(
        public id?: any,
        public acJobId?: string,
        public content?: Content,
        public type?: number,
        public createTime?: Date,
        public updateTime?: Date
    ) {
        this.id = id ? id : null;
        this.acJobId = acJobId ? acJobId : null;
        this.content = content ? content : new Content();
        this.type = type ? type : 1;
        this.createTime = createTime ? createTime : null;
        this.updateTime = updateTime ? updateTime : null;
    }
}
