import { Descriptor } from './descriptor.model';
export class Sql implements Descriptor {
    constructor(
        public jarPath?: string,
        public parallelism?: number,
        public checkpointingInterval?: number,
        public timeCharacteristic?: string,
        public restartAttempts?: number,
        public delayBetweenAttempts?: number,
        public sql?: string
    ) {}
}
