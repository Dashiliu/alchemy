import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ConfService } from './conf.service';
import { Conf } from './conf.model';

@Component({
    selector: 'jhi-conf-update',
    templateUrl: 'conf-update.component.html'
})
export class ConfUpdateComponent implements OnInit {
    jobId: string;
    conf: Conf;
    isSaving: boolean;

    constructor(private confService: ConfService, private route: ActivatedRoute, private router: Router) {}

    ngOnInit() {
        this.jobId = route.params['jobId'];
        const id = route.params['id'] ? route.params['id'] : null;
        if (id) {
            let conf = this.service.find(id);
            this.conf = conf.body ? conf.body : conf;
        } else {
            this.conf = new Conf();
        }
        this.isSaving = false;
    }

    previousState() {
        this.router.navigate(['/confs/:jobId/list', this.jobId]);
    }

    save() {
        this.isSaving = true;
        if (this.conf.id !== null) {
            this.confService.update(this.conf).subscribe(response => this.onSaveSuccess(response), () => this.onSaveError());
        } else {
            this.confService.create(this.conf).subscribe(response => this.onSaveSuccess(response), () => this.onSaveError());
        }
    }

    private onSaveSuccess(result) {
        this.isSaving = false;
        this.previousState();
    }

    private onSaveError() {
        this.isSaving = false;
    }
}
