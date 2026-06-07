export interface ReviewRequest {
  reviewedUserId: number;
  rating:         number;   // 1–5
  comment?:       string;
  bookingId?:     number;
  parcelId?:      number;
}

export interface ReviewResponse {
  id:               number;
  reviewerId:       number;
  reviewerName:     string;
  reviewerPhotoUrl: string | null;
  reviewedId:       number;
  reviewedName:     string;
  rating:           number;
  comment:          string | null;
  bookingId:        number | null;
  parcelId:         number | null;
  createdAt:        string;
}
