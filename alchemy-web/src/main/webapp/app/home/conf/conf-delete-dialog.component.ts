import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { JhiEventManager } from 'ng-jhipster';
import { ConfService } from './conf.service';

@Component({
    selector: 'jhi-conf-delete-dialog',
    templateUrl: 'conf-delete-dialog.component.html'
})
export class ConfDeleteComponent {
    id: String;

    constructor(private confService: ConfService, public activeModal: NgbActiveModal, private eventManager: JhiEventManager) {}

    clear() {
        this.activeModal.dismiss('cancel');
    }

    confirmDelete(id) {
        this.confService.delete(id).subscribe(response => {
            this.activeModal.dismiss(true);
        });
    }
}
