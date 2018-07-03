import { Route, Resolve, ActivatedRouteSnapshot } from '@angular/router';
import { UploadComponent } from './upload.component';
import { Injectable } from '@angular/core';
import { UploadService } from './upload.service';

@Injectable({ providedIn: 'root' })
export class JarResolve implements Resolve<any> {
    constructor(private service: UploadService) {}

    resolve() {
        return this.service.find();
    }
}

export const uploadRoute: Route = {
    path: 'upload',
    component: UploadComponent,
    resolve: {
        jar: JarResolve
    },
    data: {
        pageTitle: '文件上传'
    }
};
