// ── SOS ─────────────────────────────────────────────────────────────────

export interface SosRequest {
  latitude:  number | null;
  longitude: number | null;
  message?:  string;
  bookingId?: number;
  parcelId?:  number;
}

export interface SosAlertResponse {
  id:           number;
  userId:       number;
  userName:     string;
  latitude:     number | null;
  longitude:    number | null;
  message:      string | null;
  bookingId:    number | null;
  parcelId:     number | null;
  acknowledged: boolean;
  createdAt:    string;
}

// ── Report ────────────────────────────────────────────────────────────────

/** Predefined categories for reporting. */
export type ReportReason =
  | 'DANGEROUS_DRIVING'
  | 'INAPPROPRIATE_BEHAVIOR'
  | 'FRAUD'
  | 'HARASSMENT'
  | 'NO_SHOW'
  | 'OTHER';

export const REPORT_REASONS: { value: ReportReason; label: string }[] = [
  { value: 'DANGEROUS_DRIVING',       label: 'Dangerous driving'       },
  { value: 'INAPPROPRIATE_BEHAVIOR',  label: 'Inappropriate behaviour'  },
  { value: 'FRAUD',                   label: 'Fraud / scam'             },
  { value: 'HARASSMENT',              label: 'Harassment'               },
  { value: 'NO_SHOW',                 label: 'No-show'                  },
  { value: 'OTHER',                   label: 'Other'                    },
];

export interface ReportRequest {
  reportedUserId: number;
  reason:         ReportReason;
  details?:       string;
  bookingId?:     number;
  parcelId?:      number;
}

export interface ReportResponse {
  id:           number;
  reporterId:   number;
  reporterName: string;
  reportedId:   number;
  reportedName: string;
  reason:       string;
  details:      string | null;
  bookingId:    number | null;
  parcelId:     number | null;
  resolution:   string | null;
  resolved:     boolean;
  createdAt:    string;
}

// ── Block ─────────────────────────────────────────────────────────────────

export interface BlockedUserResponse {
  id:                 number;
  blockedUserId:      number;
  blockedUserName:    string;
  blockedUserPhotoUrl: string | null;
  createdAt:          string;
}

// ── History ───────────────────────────────────────────────────────────────

export type HistoryItemType = 'RIDE_DRIVER' | 'RIDE_PASSENGER' | 'PARCEL_SENDER' | 'PARCEL_DRIVER';

export interface HistoryItem {
  id:                   number;
  type:                 HistoryItemType;
  status:               string;
  fromLocation:         string;
  toLocation:           string;
  eventTime:            string;

  /** Populated for RIDE_DRIVER */
  earnings?:            number;

  /** Populated for RIDE_PASSENGER */
  amount?:              number;

  /** Populated for PARCEL_SENDER */
  price?:               number;

  counterPartyName?:    string;
  counterPartyPhotoUrl?: string;
}
