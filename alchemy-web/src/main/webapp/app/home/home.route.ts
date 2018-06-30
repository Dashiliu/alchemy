import { Routes, Resolve, RouterStateSnapshot, ActivatedRouteSnapshot } from '@angular/router';
import { HomeComponent } from './';
import { JhiResolvePagingParams } from 'ng-jhipster';
import { JobCreateComponent } from './job-create.component';
import { ConfComponent } from './conf/conf.component';
import { ConfDeleteComponent } from './conf/conf-delete-dialog.component';
import { ConfUpdateComponent } from './conf/conf-update.component';
import { Injectable } from '@angular/core';
import { JobService } from './job.service';
import { Job } from './model/job.model';
import { ConfService } from './conf/conf.service';
import { Conf } from './model/conf.model';

@Injectable({ providedIn: 'root' })
export class JobResolve implements Resolve<any> {
    constructor(private service: JobService) {}

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const id = route.params['jobId'] ? route.params['jobId'] : null;
        if (id) {
            return this.service.find(id);
        }
        return new Job();
    }
}

@Injectable({ providedIn: 'root' })
export class ConfResolve implements Resolve<any> {
    constructor(private service: ConfService) {}

    resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        const jobId = route.params['jobId'] ? route.params['jobId'] : null;
        const type = route.params['type'] ? route.params['type'] : null;
        if (!jobId || !type) {
            return new Conf();
        }
        return this.service.query({ jobId: jobId, type: type });
    }
}

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
        path: 'confs/:jobId/:type/info',
        component: ConfComponent,
        resolve: {
            conf: ConfResolve
        },
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
    }
    // {
    //     path: 'confs/:jobId/:type/new',
    //     component: ConfUpdateComponent,
    //     resolve: {
    //         conf: ConfResolve
    //     }
    // },
    // {
    //     path: 'confs/:jobId/:id/edit',
    //     component: ConfUpdateComponent,
    //     resolve: {
    //         conf: ConfResolve
    //     }
    // }
];
