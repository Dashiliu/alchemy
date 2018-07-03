import { Component } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { JhiAlertService } from 'ng-jhipster';
import { Jar } from '../../home/model/jar.model';

@Component({
    selector: 'jhi-upload',
    templateUrl: 'upload.component.html'
})
export class UploadComponent {
    jarInfo: Jar;

    constructor(private route: ActivatedRoute) {
        this.route.data.subscribe(({ jar }) => {
            this.jarInfo = jar.body ? jar.body : jar;
        });
    }

    ngOnInit() {}

    upload(event) {
        if (event.xhr.status == 200) {
            this.jarInfo = new Jar();
            this.jarInfo = JSON.parse(event.xhr.response);
        }
    }

    previousState() {
        this.router.navigate(['']);
    }
}
