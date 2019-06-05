import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { JhiAlertService, JhiDataUtils } from 'ng-jhipster';
import { ISink, Sink } from 'app/shared/model/sink.model';
import { SinkService } from './sink.service';
import { IBusiness } from 'app/shared/model/business.model';
import { BusinessService } from 'app/entities/business';
import 'codemirror/mode/yaml/yaml';
@Component({
  selector: 'jhi-sink-update',
  templateUrl: './sink-update.component.html'
})
export class SinkUpdateComponent implements OnInit {
  sink: ISink;
  isSaving: boolean;
  yamlConfig: any = { lineNumbers: true, mode: 'text/x-yaml', theme: 'material' };
  businesses: IBusiness[];

  editForm = this.fb.group({
    id: [],
    name: [],
    type: [null, [Validators.required]],
    config: [],
    createdBy: [],
    createdDate: [],
    lastModifiedBy: [],
    lastModifiedDate: [],
    businessId: []
  });

  constructor(
    protected dataUtils: JhiDataUtils,
    protected jhiAlertService: JhiAlertService,
    protected sinkService: SinkService,
    protected businessService: BusinessService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ sink }) => {
      this.updateForm(sink);
      this.sink = sink;
    });
    this.businessService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IBusiness[]>) => mayBeOk.ok),
        map((response: HttpResponse<IBusiness[]>) => response.body)
      )
      .subscribe((res: IBusiness[]) => (this.businesses = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(sink: ISink) {
    this.editForm.patchValue({
      id: sink.id,
      name: sink.name,
      type: sink.type,
      config: sink.config,
      createdBy: sink.createdBy,
      createdDate: sink.createdDate != null ? sink.createdDate.format(DATE_TIME_FORMAT) : null,
      lastModifiedBy: sink.lastModifiedBy,
      lastModifiedDate: sink.lastModifiedDate != null ? sink.lastModifiedDate.format(DATE_TIME_FORMAT) : null,
      businessId: sink.businessId
    });
  }

  byteSize(field) {
    return this.dataUtils.byteSize(field);
  }

  openFile(contentType, field) {
    return this.dataUtils.openFile(contentType, field);
  }

  setFileData(event, field: string, isImage) {
    return new Promise((resolve, reject) => {
      if (event && event.target && event.target.files && event.target.files[0]) {
        const file = event.target.files[0];
        if (isImage && !/^image\//.test(file.type)) {
          reject(`File was expected to be an image but was found to be ${file.type}`);
        } else {
          const filedContentType: string = field + 'ContentType';
          this.dataUtils.toBase64(file, base64Data => {
            this.editForm.patchValue({
              [field]: base64Data,
              [filedContentType]: file.type
            });
          });
        }
      } else {
        reject(`Base64 data was not set as file could not be extracted from passed parameter: ${event}`);
      }
    }).then(
      () => console.log('blob added'), // sucess
      this.onError
    );
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const sink = this.createFromForm();
    if (sink.id !== undefined) {
      this.subscribeToSaveResponse(this.sinkService.update(sink));
    } else {
      this.subscribeToSaveResponse(this.sinkService.create(sink));
    }
  }

  private createFromForm(): ISink {
    const entity = {
      ...new Sink(),
      id: this.editForm.get(['id']).value,
      name: this.editForm.get(['name']).value,
      type: this.editForm.get(['type']).value,
      config: this.editForm.get(['config']).value,
      createdBy: this.editForm.get(['createdBy']).value,
      createdDate:
        this.editForm.get(['createdDate']).value != null ? moment(this.editForm.get(['createdDate']).value, DATE_TIME_FORMAT) : undefined,
      lastModifiedBy: this.editForm.get(['lastModifiedBy']).value,
      lastModifiedDate:
        this.editForm.get(['lastModifiedDate']).value != null
          ? moment(this.editForm.get(['lastModifiedDate']).value, DATE_TIME_FORMAT)
          : undefined,
      businessId: this.editForm.get(['businessId']).value
    };
    return entity;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISink>>) {
    result.subscribe((res: HttpResponse<ISink>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
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

  trackBusinessById(index: number, item: IBusiness) {
    return item.id;
  }
}
