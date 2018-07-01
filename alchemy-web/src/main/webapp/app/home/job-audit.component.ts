import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';
import { JobService } from './job.service';

@Component({
    selector: 'jhi-job-audit-dialog',
    templateUrl: 'job-audit.component.html'
})
export class JobAuditDialogComponent {
    id: String;
    audits: any[];
    selectAudit: number = 1;
    msg: string;

    constructor(private jobService: JobService, public activeModal: NgbActiveModal, private eventManager: JhiEventManager) {
        this.audits = [
            {
                label: '审核通过',
                value: 1
            },
            {
                label: '审核失败',
                value: 0
            }
        ];
    }

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmAudit() {
        if (this.selectAudit == 1) {
            this.jobService.pass(this.id).subscribe(response => {
                this.activeModal.dismiss(true);
            });
        } else {
            this.jobService.fail({ jobId: this.id, msg: this.msg }).subscribe(response => {
                this.activeModal.dismiss(true);
            });
        }
    }
}
