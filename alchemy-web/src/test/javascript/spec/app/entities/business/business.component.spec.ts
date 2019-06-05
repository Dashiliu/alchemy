/* tslint:disable max-line-length */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Observable, of } from 'rxjs';
import { HttpHeaders, HttpResponse } from '@angular/common/http';

import { AlchemyTestModule } from '../../../test.module';
import { BusinessComponent } from 'app/entities/business/business.component';
import { BusinessService } from 'app/entities/business/business.service';
import { Business } from 'app/shared/model/business.model';

describe('Component Tests', () => {
  describe('Business Management Component', () => {
    let comp: BusinessComponent;
    let fixture: ComponentFixture<BusinessComponent>;
    let service: BusinessService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [AlchemyTestModule],
        declarations: [BusinessComponent],
        providers: []
      })
        .overrideTemplate(BusinessComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(BusinessComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(BusinessService);
    });

    it('Should call load all on init', () => {
      // GIVEN
      const headers = new HttpHeaders().append('link', 'link;link');
      spyOn(service, 'query').and.returnValue(
        of(
          new HttpResponse({
            body: [new Business(123)],
            headers
          })
        )
      );

      // WHEN
      comp.ngOnInit();

      // THEN
      expect(service.query).toHaveBeenCalled();
      expect(comp.businesses[0]).toEqual(jasmine.objectContaining({ id: 123 }));
    });
  });
});
