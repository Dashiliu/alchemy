import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { AlchemySharedModule } from 'app/shared';
import { HOME_ROUTE, HomeComponent } from './';
import { JobCreateComponent } from './job-create.component';
import { JobDeleteDialogComponent } from './job-delete-dialog.component';
import { ConfComponent } from './conf/conf.component';
import { ConfDeleteComponent } from './conf/conf-delete-dialog.component';
import { CodemirrorModule } from 'ng2-codemirror';
import { JobAuditDialogComponent } from './job-audit.component';
import { JobSubmitDialogComponent } from './job-submit.component';
import { FileUploadModule } from 'primeng/fileupload';

@NgModule({
    imports: [AlchemySharedModule, FileUploadModule, CodemirrorModule, RouterModule.forChild(HOME_ROUTE)],
    declarations: [
        HomeComponent,
        JobCreateComponent,
        JobDeleteDialogComponent,
        JobAuditDialogComponent,
        JobSubmitDialogComponent,
        ConfComponent,
        ConfDeleteComponent
    ],
    entryComponents: [JobDeleteDialogComponent, JobAuditDialogComponent, JobSubmitDialogComponent],
    schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class AlchemyHomeModule {}
