import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SERVER_API_URL } from 'app/app.constants';
import { Jar } from '../../home/model/jar.model';

@Injectable({ providedIn: 'root' })
export class UploadService {
    private resourceUrl = SERVER_API_URL + 'upload/global';

    constructor(private http: HttpClient) {}

    find(): Observable<HttpResponse<Jar>> {
        return this.http.get<Jar>(`${this.resourceUrl}`, { observe: 'response' });
    }
}
