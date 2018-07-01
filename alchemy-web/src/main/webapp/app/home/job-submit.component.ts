import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';
import { JobService } from './job.service';

@Component({
    selector: 'jhi-job-submit-dialog',
    templateUrl: 'job-submit.component.html'
})
export class JobSubmitDialogComponent {
    id: String;

    constructor(private jobService: JobService, public activeModal: NgbActiveModal, private eventManager: JhiEventManager) {}

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmSubmit() {
        this.jobService.updateStatus(this.id, 1).subscribe(response => {
            this.activeModal.dismiss(true);
        });
    }
}
