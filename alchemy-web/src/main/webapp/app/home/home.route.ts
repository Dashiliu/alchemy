import { Route, Routes } from '@angular/router';

import { HomeComponent } from './';
import { JhiResolvePagingParams } from 'ng-jhipster';
import { JobCreateComponent } from './job-create.component';
import { confRoute } from './conf/conf.route';
import { ConfComponent } from './conf/conf.component';
import { ConfDeleteComponent } from './conf/conf-delete-dialog.component';
import { ConfUpdateComponent } from './conf/conf-update.component';

export const HOME_ROUTE: Routes = [
    {
        path: '',
        component: HomeComponent,
        resolve: {
            pagingParams: JhiResolvePagingParams
        },
        data: {
            authorities: [],
            pageTitle: 'Welcome, Java Hipster!'
        }
    },
    {
        path: 'new',
        component: JobCreateComponent,
        data: {
            pageTitle: 'create job'
        }
    },
    {
        path: 'confs/:jobId/list',
        component: ConfComponent,
        data: {
            pageTitle: 'JobConf'
        }
    },
    {
        path: 'confs/:jobId/:id/delete',
        component: ConfDeleteComponent,
        data: {
            pageTitle: 'JobConf'
        }
    },
    {
        path: 'confs/:jobId/new',
        component: ConfUpdateComponent
    },
    {
        path: 'confs/:jobId/:id/edit',
        component: ConfUpdateComponent
    }
];
