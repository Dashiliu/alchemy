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

    clusters(req?: any): Observable<HttpResponse<Job[]>> {
        const options = createRequestOption(req);
        return this.http.get<any[]>(`${this.resourceUrl}/clusters`, { params: options, observe: 'response' });
    }

    query(req?: any): Observable<HttpResponse<Job[]>> {
        const options = createRequestOption(req);
        return this.http.get<Job[]>(this.resourceUrl, { params: options, observe: 'response' });
    }

    updateStatus(id: any, status: any): Observable<HttpResponse<Job>> {
        const options = createRequestOption({ jobId: id, status: status });
        return this.http.get<Job>(`${this.resourceUrl}/status`, { params: options, observe: 'response' });
    }

    find(id: string): Observable<HttpResponse<Job>> {
        return this.http.get<Job>(`${this.resourceUrl}/${id}`, { observe: 'response' });
    }

    delete(id: string): Observable<HttpResponse<any>> {
        return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
    }

    cancel(id: string): Observable<HttpResponse<any>> {
        return this.http.get(`${this.resourceUrl}/cancel/${id}`, { observe: 'response' });
    }

    restart(id: string): Observable<HttpResponse<any>> {
        return this.http.get(`${this.resourceUrl}/restart/${id}`, { observe: 'response' });
    }

    pass(jobId: any, cluster: any): Observable<HttpResponse<any>> {
        const options = createRequestOption({ jobId: jobId, cluster: cluster });
        const requestURL = SERVER_API_URL + 'management/audits/pass';

        return this.http.get<any>(requestURL, {
            params: options,
            observe: 'response'
        });
    }

    fail(req: any): Observable<HttpResponse<any>> {
        const requestURL = SERVER_API_URL + 'management/audits/fail';
        return this.http.post<any>(requestURL, req, { observe: 'response' });
    }
}
