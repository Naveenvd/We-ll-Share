import { Gender } from './auth.model';

export type RideStatus   = 'SCHEDULED' | 'STARTED' | 'COMPLETED' | 'CANCELLED';
export type ParcelSize   = 'SMALL' | 'MEDIUM' | 'LARGE';

export interface LatLng {
  lat: number;
  lng: number;
  address?: string;
}

export interface RideStop {
  id?: number;
  stopName: string;
  lat: number;
  lng: number;
  sequence: number;
}

export interface DriverSummary {
  id: number;
  name: string;
  photoUrl?: string;
  gender: Gender;
  avgRating: number;
  totalRides: number;
}

export interface RideVehicle {
  id: number;
  model: string;
  numberPlate: string;
  color: string;
  seats: number;
}

export interface Ride {
  id: number;
  driver: DriverSummary;
  vehicle?: RideVehicle;
  fromLocation: string;
  toLocation: string;
  fromLat: number; fromLng: number;
  toLat: number;   toLng: number;
  departureTime: string;
  seatsTotal: number;
  seatsAvailable: number;
  pricePerSeat: number;
  acceptsPassengers: boolean;
  acceptsParcels: boolean;
  maxParcelSize?: ParcelSize;
  womenOnly: boolean;
  status: RideStatus;
  stops: RideStop[];
  createdAt: string;
}

export interface RidePostRequest {
  fromLocation: string;
  toLocation: string;
  fromLat: number; fromLng: number;
  toLat: number;   toLng: number;
  departureTime: string;     // ISO datetime
  seatsTotal: number;
  pricePerSeat: number;
  acceptsPassengers: boolean;
  acceptsParcels: boolean;
  maxParcelSize?: ParcelSize;
  womenOnly: boolean;
  vehicleId: number;
  stops: RideStop[];
}

export interface RideSearchParams {
  fromLat: number; fromLng: number;
  toLat: number;   toLng: number;
  date: string;    // ISO datetime
  seats: number;
  minPrice?: number;
  maxPrice?: number;
  womenOnly?: boolean;
  page?: number;
  size?: number;
}

export interface NominatimResult {
  place_id: number;
  display_name: string;
  lat: string;
  lon: string;
}
