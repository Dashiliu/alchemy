import { TimeAttribute } from './time.attribute.model';
import { Table } from './table.model';
import { Descriptor } from './descriptor.model';
export class KafkaSource implements Descriptor {
    constructor(
        public name?: string,
        public timeAttribute?: TimeAttribute,
        public input?: Table,
        public output?: Table,
        public topic?: string,
        public brokers?: string,
        public consumerGroup?: string,
        public consumeFromWhere?: string,
        public properties?: Map<string, string>
    ) {
        this.name = name ? name : null;
        this.timeAttribute = timeAttribute ? timeAttribute : new TimeAttribute();
        this.input = input ? input : new Table();
        this.output = output ? output : new Table();
        this.topic = topic ? topic : null;
        this.brokers = brokers ? brokers : null;
        this.consumerGroup = consumerGroup ? consumerGroup : null;
        this.consumeFromWhere = consumeFromWhere ? consumeFromWhere : null;
        this.properties = properties ? properties : new Map<string, string>();
    }
}
