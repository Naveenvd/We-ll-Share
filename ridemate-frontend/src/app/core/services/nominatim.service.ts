import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { NominatimResult } from '../models/ride.model';

const NOMINATIM = 'https://nominatim.openstreetmap.org';

@Injectable({ providedIn: 'root' })
export class NominatimService {

  constructor(private http: HttpClient) {}

  /**
   * Forward geocoding: text → list of results
   * e.g. "Mumbai CST" → [{lat, lon, display_name}, ...]
   */
  search(query: string): Observable<NominatimResult[]> {
    if (!query || query.trim().length < 3) return of([]);

    const params = new HttpParams()
      .set('q', query)
      .set('format', 'json')
      .set('addressdetails', '1')
      .set('limit', '5')
      .set('countrycodes', 'in');   // restrict to India

    return this.http.get<NominatimResult[]>(`${NOMINATIM}/search`, {
      params,
      headers: { 'Accept-Language': 'en' }
    }).pipe(catchError(() => of([])));
  }

  /**
   * Reverse geocoding: lat/lng → address string
   */
  reverse(lat: number, lng: number): Observable<string> {
    const params = new HttpParams()
      .set('lat', lat.toString())
      .set('lon', lng.toString())
      .set('format', 'json');

    return this.http.get<{ display_name: string }>(`${NOMINATIM}/reverse`, {
      params,
      headers: { 'Accept-Language': 'en' }
    }).pipe(
      map(r => r.display_name),
      catchError(() => of(`${lat.toFixed(5)}, ${lng.toFixed(5)}`))
    );
  }
}
