export class Job {
    constructor(
        public id?: any,
        public acServiceId?: string,
        public name?: string,
        public cluster?: string,
        public submitMode?: number,
        public status?: number,
        public createTime?: Date,
        public updateTime?: Date
    ) {}
}
