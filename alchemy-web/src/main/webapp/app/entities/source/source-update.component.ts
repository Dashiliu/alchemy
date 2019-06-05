import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { JhiAlertService } from 'ng-jhipster';
import { ISource, Source } from 'app/shared/model/source.model';
import { SourceService } from './source.service';
import { IBusiness } from 'app/shared/model/business.model';
import { BusinessService } from 'app/entities/business';

@Component({
  selector: 'jhi-source-update',
  templateUrl: './source-update.component.html'
})
export class SourceUpdateComponent implements OnInit {
  source: ISource;
  isSaving: boolean;

  businesses: IBusiness[];

  editForm = this.fb.group({
    id: [],
    name: [null, [Validators.required]],
    tableType: [null, [Validators.required]],
    sourceType: [null, [Validators.required]],
    config: [null, [Validators.required]],
    createdBy: [],
    createdDate: [],
    lastModifiedBy: [],
    lastModifiedDate: [],
    businessId: []
  });

  constructor(
    protected jhiAlertService: JhiAlertService,
    protected sourceService: SourceService,
    protected businessService: BusinessService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ source }) => {
      this.updateForm(source);
      this.source = source;
    });
    this.businessService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IBusiness[]>) => mayBeOk.ok),
        map((response: HttpResponse<IBusiness[]>) => response.body)
      )
      .subscribe((res: IBusiness[]) => (this.businesses = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(source: ISource) {
    this.editForm.patchValue({
      id: source.id,
      name: source.name,
      tableType: source.tableType,
      sourceType: source.sourceType,
      config: source.config,
      createdBy: source.createdBy,
      createdDate: source.createdDate != null ? source.createdDate.format(DATE_TIME_FORMAT) : null,
      lastModifiedBy: source.lastModifiedBy,
      lastModifiedDate: source.lastModifiedDate != null ? source.lastModifiedDate.format(DATE_TIME_FORMAT) : null,
      businessId: source.businessId
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const source = this.createFromForm();
    if (source.id !== undefined) {
      this.subscribeToSaveResponse(this.sourceService.update(source));
    } else {
      this.subscribeToSaveResponse(this.sourceService.create(source));
    }
  }

  private createFromForm(): ISource {
    const entity = {
      ...new Source(),
      id: this.editForm.get(['id']).value,
      name: this.editForm.get(['name']).value,
      tableType: this.editForm.get(['tableType']).value,
      sourceType: this.editForm.get(['sourceType']).value,
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

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISource>>) {
    result.subscribe((res: HttpResponse<ISource>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
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
