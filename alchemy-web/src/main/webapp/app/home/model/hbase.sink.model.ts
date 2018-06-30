import { Descriptor } from './descriptor.model';
export class HbaseSink implements Descriptor {
    constructor(
        public name?: string,
        public readMode?: number,
        public zookeeper?: string,
        public node?: string,
        public tableName?: string,
        public family?: string,
        public bufferSize?: number,
        public value?: number
    ) {}
}
