import { Component, OnInit } from '@angular/core';
import { HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { filter, map } from 'rxjs/operators';
import * as moment from 'moment';
import { DATE_TIME_FORMAT } from 'app/shared/constants/input.constants';
import { JhiAlertService } from 'ng-jhipster';
import { IUdf, Udf } from 'app/shared/model/udf.model';
import { UdfService } from './udf.service';
import { IBusiness } from 'app/shared/model/business.model';
import { BusinessService } from 'app/entities/business';

@Component({
  selector: 'jhi-udf-update',
  templateUrl: './udf-update.component.html'
})
export class UdfUpdateComponent implements OnInit {
  udf: IUdf;
  isSaving: boolean;

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
    protected jhiAlertService: JhiAlertService,
    protected udfService: UdfService,
    protected businessService: BusinessService,
    protected activatedRoute: ActivatedRoute,
    private fb: FormBuilder
  ) {}

  ngOnInit() {
    this.isSaving = false;
    this.activatedRoute.data.subscribe(({ udf }) => {
      this.updateForm(udf);
      this.udf = udf;
    });
    this.businessService
      .query()
      .pipe(
        filter((mayBeOk: HttpResponse<IBusiness[]>) => mayBeOk.ok),
        map((response: HttpResponse<IBusiness[]>) => response.body)
      )
      .subscribe((res: IBusiness[]) => (this.businesses = res), (res: HttpErrorResponse) => this.onError(res.message));
  }

  updateForm(udf: IUdf) {
    this.editForm.patchValue({
      id: udf.id,
      name: udf.name,
      type: udf.type,
      config: udf.config,
      createdBy: udf.createdBy,
      createdDate: udf.createdDate != null ? udf.createdDate.format(DATE_TIME_FORMAT) : null,
      lastModifiedBy: udf.lastModifiedBy,
      lastModifiedDate: udf.lastModifiedDate != null ? udf.lastModifiedDate.format(DATE_TIME_FORMAT) : null,
      businessId: udf.businessId
    });
  }

  previousState() {
    window.history.back();
  }

  save() {
    this.isSaving = true;
    const udf = this.createFromForm();
    if (udf.id !== undefined) {
      this.subscribeToSaveResponse(this.udfService.update(udf));
    } else {
      this.subscribeToSaveResponse(this.udfService.create(udf));
    }
  }

  private createFromForm(): IUdf {
    const entity = {
      ...new Udf(),
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

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IUdf>>) {
    result.subscribe((res: HttpResponse<IUdf>) => this.onSaveSuccess(), (res: HttpErrorResponse) => this.onSaveError());
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
