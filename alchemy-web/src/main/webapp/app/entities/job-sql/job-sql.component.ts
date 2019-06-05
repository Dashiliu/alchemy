import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import { JhiEventManager, JhiAlertService } from 'ng-jhipster';

import { IJobSql } from 'app/shared/model/job-sql.model';
import { AccountService } from 'app/core';
import { JobSqlService } from './job-sql.service';

@Component({
  selector: 'jhi-job-sql',
  templateUrl: './job-sql.component.html'
})
export class JobSqlComponent implements OnInit, OnDestroy {
  jobSqls: IJobSql[];
  currentAccount: any;
  eventSubscriber: Subscription;

  constructor(
    protected jobSqlService: JobSqlService,
    protected jhiAlertService: JhiAlertService,
    protected eventManager: JhiEventManager,
    protected accountService: AccountService
  ) {}

  loadAll() {
    this.jobSqlService
      .query()
      .pipe(
        filter((res: HttpResponse<IJobSql[]>) => res.ok),
        map((res: HttpResponse<IJobSql[]>) => res.body)
      )
      .subscribe(
        (res: IJobSql[]) => {
          this.jobSqls = res;
        },
        (res: HttpErrorResponse) => this.onError(res.message)
      );
  }

  ngOnInit() {
    this.loadAll();
    this.accountService.identity().then(account => {
      this.currentAccount = account;
    });
    this.registerChangeInJobSqls();
  }

  ngOnDestroy() {
    this.eventManager.destroy(this.eventSubscriber);
  }

  trackId(index: number, item: IJobSql) {
    return item.id;
  }

  registerChangeInJobSqls() {
    this.eventSubscriber = this.eventManager.subscribe('jobSqlListModification', response => this.loadAll());
  }

  protected onError(errorMessage: string) {
    this.jhiAlertService.error(errorMessage, null, null);
  }
}
