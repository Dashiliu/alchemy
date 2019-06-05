/* tslint:disable max-line-length */
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { Observable, of } from 'rxjs';

import { AlchemyTestModule } from '../../../test.module';
import { BusinessUpdateComponent } from 'app/entities/business/business-update.component';
import { BusinessService } from 'app/entities/business/business.service';
import { Business } from 'app/shared/model/business.model';

describe('Component Tests', () => {
  describe('Business Management Update Component', () => {
    let comp: BusinessUpdateComponent;
    let fixture: ComponentFixture<BusinessUpdateComponent>;
    let service: BusinessService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [AlchemyTestModule],
        declarations: [BusinessUpdateComponent],
        providers: [FormBuilder]
      })
        .overrideTemplate(BusinessUpdateComponent, '')
        .compileComponents();

      fixture = TestBed.createComponent(BusinessUpdateComponent);
      comp = fixture.componentInstance;
      service = fixture.debugElement.injector.get(BusinessService);
    });

    describe('save', () => {
      it('Should call update service on save for existing entity', fakeAsync(() => {
        // GIVEN
        const entity = new Business(123);
        spyOn(service, 'update').and.returnValue(of(new HttpResponse({ body: entity })));
        comp.updateForm(entity);
        // WHEN
        comp.save();
        tick(); // simulate async

        // THEN
        expect(service.update).toHaveBeenCalledWith(entity);
        expect(comp.isSaving).toEqual(false);
      }));

      it('Should call create service on save for new entity', fakeAsync(() => {
        // GIVEN
        const entity = new Business();
        spyOn(service, 'create').and.returnValue(of(new HttpResponse({ body: entity })));
        comp.updateForm(entity);
        // WHEN
        comp.save();
        tick(); // simulate async

        // THEN
        expect(service.create).toHaveBeenCalledWith(entity);
        expect(comp.isSaving).toEqual(false);
      }));
    });
  });
});
