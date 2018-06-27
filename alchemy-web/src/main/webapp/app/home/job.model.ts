export class Job {
    id?: any;
    acServiceId?: string;
    name?: string;
    cluster?: string;
    submitMode?: number;
    status?: number;
    createTime?: Date;
    updateTime?: Date;

    constructor(
        public id?: any,
        public acServiceId?: string,
        public name?: string,
        public cluster?: string,
        public submitMode?: number,
        public status?: number,
        public createTime?: Date,
        public updateTime?: Date
    ) {
        this.id = id ? id : null;
        this.acServiceId = acServiceId ? acServiceId : null;
        this.name = name ? name : null;
        this.cluster = cluster ? cluster : '';
        this.submitMode = submitMode ? submitMode : null;
        this.status = status ? status : null;
        this.createTime = createTime ? createTime : null;
        this.updateTime = updateTime ? updateTime : null;
    }
}
