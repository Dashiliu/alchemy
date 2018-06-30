import { Descriptor } from './descriptor.model';
export class KafkaSink implements Descriptor {
    constructor(public name?: string, public topic?: string, public brokers?: string, public properties?: Map<string, string>) {
        this.name = name ? name : null;
        this.topic = topic ? topic : null;
        this.brokers = brokers ? brokers : null;
        this.properties = properties ? properties : new Map<string, string>();
    }
}
