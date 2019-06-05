import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IJobSql } from 'app/shared/model/job-sql.model';

@Component({
  selector: 'jhi-job-sql-detail',
  templateUrl: './job-sql-detail.component.html'
})
export class JobSqlDetailComponent implements OnInit {
  jobSql: IJobSql;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ jobSql }) => {
      this.jobSql = jobSql;
    });
  }

  previousState() {
    window.history.back();
  }
}
