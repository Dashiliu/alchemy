import { Moment } from 'moment';

export const enum UdfType {
  AVG = 'AVG',
  CODE = 'CODE'
}

export interface IUdf {
  id?: number;
  name?: string;
  type?: UdfType;
  config?: string;
  createdBy?: string;
  createdDate?: Moment;
  lastModifiedBy?: string;
  lastModifiedDate?: Moment;
  businessId?: number;
}

export class Udf implements IUdf {
  constructor(
    public id?: number,
    public name?: string,
    public type?: UdfType,
    public config?: string,
    public createdBy?: string,
    public createdDate?: Moment,
    public lastModifiedBy?: string,
    public lastModifiedDate?: Moment,
    public businessId?: number
  ) {}
}
