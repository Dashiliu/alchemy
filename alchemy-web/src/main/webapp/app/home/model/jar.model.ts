export class Jar {
    constructor(
        public fileName?: string,
        public avg?: string,
        public parallelism?: number,
        public programArgs?: string,
        public entryClass?: string,
        public uploadTime?: Date
    ) {}
}
