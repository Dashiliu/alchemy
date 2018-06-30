import { Field } from './field.model';
export class Table {
    constructor(public fields?: Field[]) {
        this.fields = fields ? fields : [];
    }
}
