import { Descriptor } from './descriptor.model';
export class Udf implements Descriptor {
    constructor(public readMode?: number, public name?: string, public value?: string) {}
}
