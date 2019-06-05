import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { JhiAlertService } from 'ng-jhipster';
import { IJobSql, JobSql } from 'app/shared/model/job-sql.model';
import { JobSqlService } from './job-sql.service';
import { IJob } from 'app/shared/model/job.model';
import { JobService } from 'app/entities/job';

@Component({
  selector: 'jhi-job-sql-update',
  templateUrl: './job-sql-update.component.html'
})
export class JobSqlUpdateComponent implements OnInit {
  jobSql: IJobSql;
  isSaving: boolean;

  jobs: IJob[];

  editForm = this.fb.group({
    id: [],
    sql: [null, [Validators.required]],
    createdBy: [],
    createdDate: [],
    lastModifiedBy: [],
    lastModifiedDate: [],
    jobId: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected jobSqlService: JobSqlService,
    protected jobService: JobService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ jobSql }) => {
      this.updateForm(jobSql);
      this.jobSql = jobSql;
    });
    this.jobService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IJob[]>) => mayBeOk.ok),
        map((response: HttpResponse<IJob[]>) => response.body)
      )
      .subscribe((res: IJob[]) => (this.jobs = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(jobSql: IJobSql) {
    this.editForm.patchValue({
      id: jobSql.id,
      sql: jobSql.sql,
      createdBy: jobSql.createdBy,
      createdDate: jobSql.createdDate != null ? jobSql.createdDate.format(DATE_TIME_FORMAT) : null,
      lastModifiedBy: jobSql.lastModifiedBy,
      lastModifiedDate: jobSql.lastModifiedDate != null ? jobSql.lastModifiedDate.format(DATE_TIME_FORMAT) : null,
      jobId: jobSql.jobId
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const jobSql = this.createFromForm();
    if (jobSql.id !== undefined) {
      this.subscribeToSaveResponse(this.jobSqlService.update(jobSql));
    } else {
      this.subscribeToSaveResponse(this.jobSqlService.create(jobSql));
    }
  }

  private createFromForm(): IJobSql {
    const entity = {
      ...new JobSql(),
      id: this.editForm.get(['id']).value,
      sql: this.editForm.get(['sql']).value,
      createdBy: this.editForm.get(['createdBy']).value,
      createdDate:
        this.editForm.get(['createdDate']).value != null ? moment(this.editForm.get(['createdDate']).value, DATE_TIME_FORMAT) : undefined,
      lastModifiedBy: this.editForm.get(['lastModifiedBy']).value,
      lastModifiedDate:
        this.editForm.get(['lastModifiedDate']).value != null
          ? moment(this.editForm.get(['lastModifiedDate']).value, DATE_TIME_FORMAT)
          : undefined,
      jobId: this.editForm.get(['jobId']).value
    };
    return entity;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IJobSql>>) {
    result.subscribe((res: HttpResponse<IJobSql>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
  }

  protected onSaveSuccess() {
    this.isSaving = false;
    this.previousState();
  }

  protected onSaveError() {
    this.isSaving = false;
  }
  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }

  trackJobById(index: number, item: IJob) {
    return item.id;
  }
}
