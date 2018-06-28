import { Component } from '@angular/core';
import { MenuItem } from 'primeng/components/common/menuitem';

@Component({
    selector: 'jhi-conf',
    templateUrl: 'conf.component.html'
})
export class ConfComponent {
    items: MenuItem[];

    ngOnInit() {
        this.items = [{ label: '数据来源' }, { label: '自定义函数' }, { label: '数据写入' }, { label: '基本信息' }];
    }
}
