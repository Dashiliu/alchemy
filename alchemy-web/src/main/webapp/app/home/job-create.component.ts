import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { User, UserService } from 'app/core';
import { Job } from './model/job.model';
import { JobService } from './job.service';

@Component({
    selector: 'jhi-job-create',
    templateUrl: './job-create.component.html'
})
export class JobCreateComponent implements OnInit {
    job: Job;

    modes: any[];

    constructor(private jobService: JobService, private route: ActivatedRoute, private router: Router) {
        this.job = new Job();
        this.modes = [
            {
                label: 'sql任务',
                value: 2
            },
            {
                label: 'jar包',
                value: 1
            }
        ];
    }

    ngOnInit() {}

    previousState() {
        this.router.navigate(['']);
    }

    save() {
        this.jobService.create(this.job).subscribe(response => this.onSaveSuccess(response), () => this.onSaveError());
    }

    private onSaveSuccess(result) {
        this.previousState();
    }

    private onSaveError() {}
}
