import { Moment } from 'moment';

export interface IJobSql {
  id?: number;
  sql?: string;
  createdBy?: string;
  createdDate?: Moment;
  lastModifiedBy?: string;
  lastModifiedDate?: Moment;
  jobId?: number;
}

export class JobSql implements IJobSql {
  constructor(
    public id?: number,
    public sql?: string,
    public createdBy?: string,
    public createdDate?: Moment,
    public lastModifiedBy?: string,
    public lastModifiedDate?: Moment,
    public jobId?: number
  ) {}
}
