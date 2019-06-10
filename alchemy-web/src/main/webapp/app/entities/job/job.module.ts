import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterModule } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { JhiLanguageHelper } from 'app/core';
import { CodemirrorModule } from 'ng2-codemirror';
import { NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap';

import { AlchemySharedModule } from 'app/shared';
import {
  JobComponent,
  JobDetailComponent,
  JobUpdateComponent,
  JobDeletePopupComponent,
  JobDeleteDialogComponent,
  jobRoute,
  jobPopupRoute
} from './';

const ENTITY_STATES = [...jobRoute, ...jobPopupRoute];

@NgModule({
  imports: [AlchemySharedModule, CodemirrorModule,NgbDropdownModule , RouterModule.forChild(ENTITY_STATES)],
  declarations: [JobComponent, JobDetailComponent, JobUpdateComponent, JobDeleteDialogComponent, JobDeletePopupComponent],
  entryComponents: [JobComponent, JobUpdateComponent, JobDeleteDialogComponent, JobDeletePopupComponent],
  providers: [{ provide: JhiLanguageService, useClass: JhiLanguageService }],
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class AlchemyJobModule {
  constructor(private languageService: JhiLanguageService, private languageHelper: JhiLanguageHelper) {
    this.languageHelper.language.subscribe((languageKey: string) => {
      if (languageKey !== undefined) {
        this.languageService.changeLanguage(languageKey);
      }
    });
  }
}
