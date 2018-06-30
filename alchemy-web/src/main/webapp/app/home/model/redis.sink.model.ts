import { Descriptor } from './descriptor.model';
export class RedisSink implements Descriptor {
    constructor(
        public name?: string,
        public sentinels?: string,
        public master?: string,
        public database?: number,
        public maxTotal?: number,
        public queueSize?: number,
        public thread?: number
    ) {}
}
