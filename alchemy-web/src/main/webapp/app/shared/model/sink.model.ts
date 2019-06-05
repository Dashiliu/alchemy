import { Moment } from 'moment';

export const enum SinkType {
  REDIS = 'REDIS',
  KAFKA = 'KAFKA',
  MYSQL = 'MYSQL',
  HBASE = 'HBASE',
  TSDB = 'TSDB',
  FILE = 'FILE',
  ELASTICSEARCH = 'ELASTICSEARCH',
  PRINT = 'PRINT'
}

export interface ISink {
  id?: number;
  name?: string;
  type?: SinkType;
  config?: string;
  createdBy?: string;
  createdDate?: Moment;
  lastModifiedBy?: string;
  lastModifiedDate?: Moment;
  businessId?: number;
}

export class Sink implements ISink {
  constructor(
    public id?: number,
    public name?: string,
    public type?: SinkType,
    public config?: string,
    public createdBy?: string,
    public createdDate?: Moment,
    public lastModifiedBy?: string,
    public lastModifiedDate?: Moment,
    public businessId?: number
  ) {}
}
