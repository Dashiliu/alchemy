export class Jar {
    constructor(
        public fileName?: string,
        public jarPath?: string,
        public remoteUrl?: string,
        public parallelism?: number,
        public programArgs?: string,
        public entryClass?: string,
        public uploadTime?: Date
    ) {}
}
