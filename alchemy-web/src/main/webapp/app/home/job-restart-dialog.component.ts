import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';
import { JobService } from './job.service';

@Component({
    selector: 'jhi-job-restart-dialog',
    templateUrl: 'job-restart-dialog.component.html'
})
export class JobRestartDialogComponent {
    id: String;

    constructor(private jobService: JobService, public activeModal: NgbActiveModal, private eventManager: JhiEventManager) {}

    clear() {
        this.activeModal.dismiss('restart');
    }

    confirmRestart(login) {
        this.jobService.restart(login).subscribe(response => {
            this.activeModal.dismiss(true);
        });
    }
}
