import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { JhiEventManager, JhiParseLinks, JhiAlertService } from 'ng-jhipster';
import { ITEMS_PER_PAGE } from '../shared';
import { LoginModalService, Principal } from '../core';
import { JobService } from './job.service';
import { Job } from './model/job.model';
import { NgbModalRef, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { JobDeleteDialogComponent } from './job-delete-dialog.component';
import { JobAuditDialogComponent } from './job-audit.component';
import { JobSubmitDialogComponent } from './job-submit.component';

@Component({
    selector: 'jhi-home',
    templateUrl: './home.component.html',
    styleUrls: ['home.css']
})
export class HomeComponent implements OnInit {
    modalRef: NgbModalRef;
    account: any;
    jobs: Job[];
    error: any;
    success: any;
    routeData: any;
    links: any;
    totalItems: any;
    queryCount: any;
    itemsPerPage: any;
    page: any;
    predicate: any;
    previousPage: any;
    reverse: any;

    constructor(
        private principal: Principal,
        private loginModalService: LoginModalService,
        private eventManager: JhiEventManager,
        private alertService: JhiAlertService,
        private parseLinks: JhiParseLinks,
        private activatedRoute: ActivatedRoute,
        private router: Router,
        private modalService: NgbModal,
        private jobService: JobService
    ) {
        this.itemsPerPage = ITEMS_PER_PAGE;
        this.routeData = this.activatedRoute.data.subscribe(data => {
            this.page = data['pagingParams'].page;
            this.previousPage = data['pagingParams'].page;
            this.reverse = data['pagingParams'].ascending;
            this.predicate = data['pagingParams'].predicate;
        });
    }

    ngOnInit() {
        this.principal.identity().then(account => {
            this.account = account;
        });
        this.loadAll();
        this.registerAuthenticationSuccess();
    }

    registerAuthenticationSuccess() {
        this.eventManager.subscribe('authenticationSuccess', message => {
            this.principal.identity().then(account => {
                this.account = account;
            });
        });
    }

    isAuthenticated() {
        return this.principal.isAuthenticated();
    }

    login() {
        this.modalRef = this.loginModalService.open();
    }

    ngOnDestroy() {
        this.routeData.unsubscribe();
    }

    audit(jobId) {
        const modalRef = this.modalService.open(JobAuditDialogComponent, { size: 'lg', backdrop: 'static' });
        modalRef.componentInstance.id = jobId;
        modalRef.result.then(
            result => {
                // Left blank intentionally, nothing to do here
                this.loadAll();
            },
            reason => {
                // Left blank intentionally, nothing to do here
            }
        );
        this.loadAll();
    }

    submit(jobId) {
        const modalRef = this.modalService.open(JobSubmitDialogComponent, { size: 'lg', backdrop: 'static' });
        modalRef.componentInstance.id = jobId;
        modalRef.result.then(
            result => {
                // Left blank intentionally, nothing to do here
                this.loadAll();
            },
            reason => {
                // Left blank intentionally, nothing to do here
            }
        );
        this.loadAll();
    }

    loadAll() {
        this.jobService
            .query({
                page: this.page - 1,
                size: this.itemsPerPage,
                sort: this.sort()
            })
            .subscribe(
                (res: HttpResponse<Job[]>) => this.onSuccess(res.body, res.headers),
                (res: HttpResponse<any>) => this.onError(res.body)
            );
    }

    trackIdentity(index, item: Job) {
        return item.id;
    }

    sort() {
        const result = [this.predicate + ',' + (this.reverse ? 'asc' : 'desc')];
        if (this.predicate !== 'id') {
            result.push('id');
        }
        return result;
    }

    loadPage(page: number) {
        if (page !== this.previousPage) {
            this.previousPage = page;
            this.transition();
        }
    }

    transition() {
        this.router.navigate(['/'], {
            queryParams: {
                page: this.page,
                sort: this.predicate + ',' + (this.reverse ? 'asc' : 'desc')
            }
        });
        this.loadAll();
    }

    deleteJob(id: String) {
        const modalRef = this.modalService.open(JobDeleteDialogComponent, { size: 'lg', backdrop: 'static' });
        modalRef.componentInstance.id = id;
        modalRef.result.then(
            result => {
                // Left blank intentionally, nothing to do here
                this.loadAll();
            },
            reason => {
                // Left blank intentionally, nothing to do here
            }
        );
    }

    private onSuccess(data, headers) {
        this.links = this.parseLinks.parse(headers.get('link'));
        this.totalItems = headers.get('X-Total-Count');
        this.queryCount = this.totalItems;
        this.jobs = data;
    }

    private onError(error) {
        this.alertService.error(error.error, error.message, null);
    }
}
