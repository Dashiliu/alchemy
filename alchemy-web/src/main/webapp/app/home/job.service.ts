import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { Job } from './model/job.model';

@Injectable({ providedIn: 'root' })
export class JobService {
    private resourceUrl = SERVER_API_URL + 'api/jobs';

    constructor(private http: HttpClient) {}

    create(job: Job): Observable<HttpResponse<Job>> {
        return this.http.post<Job>(this.resourceUrl, job, { observe: 'response' });
    }

    query(req?: any): Observable<HttpResponse<Job[]>> {
        const options = createRequestOption(req);
        return this.http.get<Job[]>(this.resourceUrl, { params: options, observe: 'response' });
    }

    find(id: string): Observable<HttpResponse<Job>> {
        return this.http.get<Job>(`${this.resourceUrl}/${id}`, { observe: 'response' });
    }

    delete(id: string): Observable<HttpResponse<any>> {
        return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
    }
}
