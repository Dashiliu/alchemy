import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { ISink } from 'app/shared/model/sink.model';

@Component({
  selector: 'jhi-sink-detail',
  templateUrl: './sink-detail.component.html'
})
export class SinkDetailComponent implements OnInit {
  sink: ISink;

  constructor(protected activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.activatedRoute.data.subscribe(({ sink }) => {
      this.sink = sink;
    });
  }

  previousState() {
    window.history.back();
  }
}
