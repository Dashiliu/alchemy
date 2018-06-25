import { NgModule } from '@angular/core';

import { AlchemySharedLibsModule, JhiAlertComponent, JhiAlertErrorComponent } from './';

@NgModule({
    imports: [AlchemySharedLibsModule],
    declarations: [JhiAlertComponent, JhiAlertErrorComponent],
    exports: [AlchemySharedLibsModule, JhiAlertComponent, JhiAlertErrorComponent]
})
export class AlchemySharedCommonModule {}
