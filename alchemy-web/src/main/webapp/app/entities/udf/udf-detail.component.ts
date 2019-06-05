import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { IUdf } from 'app/shared/model/udf.model';

@Component({
  selector: 'jhi-udf-detail',
  templateUrl: './udf-detail.component.html'
})
export class UdfDetailComponent implements OnInit {
  udf: IUdf;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ udf }) => {
      this.udf = udf;
    });
  }

  previousState() {
    window.history.back();
  }
}
