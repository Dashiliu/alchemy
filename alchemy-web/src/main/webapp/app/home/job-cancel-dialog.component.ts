import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';
import { JobService } from './job.service';

@Component({
    selector: 'jhi-job-cancel-dialog',
    templateUrl: 'job-cancel-dialog.component.html'
})
export class JobCancelDialogComponent {
    id: String;

    constructor(private jobService: JobService, public activeModal: NgbActiveModal, private eventManager: JhiEventManager) {}

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmCancel(login) {
        this.jobService.cancel(login).subscribe(response => {
            this.activeModal.dismiss(true);
        });
    }
}
