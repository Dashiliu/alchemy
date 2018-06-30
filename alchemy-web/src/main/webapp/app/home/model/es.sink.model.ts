import { Descriptor } from './descriptor.model';
export class EsSink implements Descriptor {
    constructor(
        public name?: string,
        public address?: string,
        public clusterName?: string,
        public index?: string,
        public bufferSize?: number
    ) {}
}
