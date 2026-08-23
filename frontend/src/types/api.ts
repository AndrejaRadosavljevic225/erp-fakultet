/** Omotac koji svi backend endpointi vracaju: {success, message, data, timestamp}. */
export interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}

/** Struktura paginirane liste (erp-common PageResponse). */
export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface ValidationError {
  field: string;
  message: string;
  rejectedValue?: unknown;
}

/** Telo greske iz GlobalExceptionHandler-a (erp-common ErrorResponse). */
export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path?: string;
  validationErrors?: ValidationError[];
  details?: Record<string, unknown>;
}

export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
}
