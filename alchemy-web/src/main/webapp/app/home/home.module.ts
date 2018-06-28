import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AlchemySharedModule } from 'app/shared';
import { HOME_ROUTE, HomeComponent } from './';
import { JobCreateComponent } from './job-create.component';
import { JobDeleteDialogComponent } from './job-delete-dialog.component';
import { StepsModule } from 'primeng/steps';
import { ConfComponent } from './conf/conf.component';
import { ConfDeleteComponent } from './conf/conf-delete-dialog.component';
import { ConfUpdateComponent } from './conf/conf-update.component';

@NgModule({
    imports: [AlchemySharedModule, RouterModule.forChild(HOME_ROUTE)],
    declarations: [HomeComponent, JobCreateComponent, JobDeleteDialogComponent, ConfComponent, ConfDeleteComponent, ConfUpdateComponent],
    entryComponents: [JobDeleteDialogComponent],
    exports: [StepsModule],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class AlchemyHomeModule {}
