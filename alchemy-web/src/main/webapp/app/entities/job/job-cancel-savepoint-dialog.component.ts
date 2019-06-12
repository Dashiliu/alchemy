import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { NgbActiveModal, NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';

import { IJob } from 'app/shared/model/job.model';
import { JobService } from './job.service';
import {FormBuilder, Validators} from "@angular/forms";

@Component({
  selector: 'jhi-job-cancel-savepoint-dialog',
  templateUrl: './job-cancel-savepoint-dialog.component.html'
})
export class JobCancelSavepointDialogComponent {
  job: IJob;
  cancelForm = this.fb.group({
    savepointDirectory: [null, [Validators.required]]
  });

  constructor(protected jobService: JobService,
              public activeModal: NgbActiveModal,
              protected eventManager: JhiEventManager,
              private fb: FormBuilder) {}

  clear() {
    this.activeModal.dismiss('cancel with savepoint');
  }

  confirmcancel(id: number) {
    this.jobService.cancelWithSavepoint(id, this.cancelForm.get("savepointDirectory").value).subscribe(response => {
      this.eventManager.broadcast({
        name: 'jobListModification',
        content: 'cancel an job with savepoint'
      });
      this.activeModal.dismiss(true);
    });
  }
}

@Component({
  selector: 'jhi-job-cancel-savepoint-popup',
  template: ''
})
export class JobCancelSavepointPopupComponent implements OnInit, OnDestroy {
  protected ngbModalRef: NgbModalRef;

  constructor(protected activatedRoute: ActivatedRoute, protected router: Router, protected modalService: NgbModal) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ job }) => {
      setTimeout(() => {
        this.ngbModalRef = this.modalService.open(JobCancelSavepointDialogComponent as Component, { size: 'lg', backdrop: 'static' });
        this.ngbModalRef.componentInstance.job = job;
        this.ngbModalRef.result.then(
          result => {
            this.router.navigate(['/job', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          },
          reason => {
            this.router.navigate(['/job', { outlets: { popup: null } }]);
            this.ngbModalRef = null;
          }
        );
      }, 0);
    });
  }

  ngOnDestroy() {
    this.ngbModalRef = null;
  }
}
