import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SERVER_API_URL } from 'app/app.constants';
import { createRequestOption } from 'app/shared/util/request-util';
import { Conf } from '../model/conf.model';

@Injectable({ providedIn: 'root' })
export class ConfService {
    private resourceUrl = SERVER_API_URL + 'api/confs';

    constructor(private http: HttpClient) {}

    create(conf: Conf): Observable<HttpResponse<Conf>> {
        return this.http.post<Conf>(this.resourceUrl, conf, { observe: 'response' });
    }

    update(conf: Conf): Observable<HttpResponse<Conf>> {
        return this.http.put<Conf>(this.resourceUrl, conf, { observe: 'response' });
    }

    find(id: string): Observable<HttpResponse<Conf>> {
        return this.http.get<Conf>(`${this.resourceUrl}/${id}`, { observe: 'response' });
    }

    query(req?: any): Observable<HttpResponse<Conf>> {
        const options = createRequestOption(req);
        return this.http.get<Conf[]>(this.resourceUrl, { params: options, observe: 'response' });
    }

    delete(id: string): Observable<HttpResponse<any>> {
        return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
    }
}
