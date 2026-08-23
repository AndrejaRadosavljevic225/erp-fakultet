export type BookingStatus = 'REQUESTED' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED' | 'FINISHED';
export type TeachingType = 'REGULAR' | 'EXTRA' | 'MENTORSHIP' | 'OTHER';

export interface Room {
  id: number;
  code: string;
  name: string | null;
  building: string | null;
  floor: number | null;
  roomNumber: string | null;
  capacity: number | null;
  roomType: string | null;
  computerCount: number | null;
  bookable: boolean;
  active: boolean;
}

export interface RoomCreateRequest {
  code: string;
  name?: string | null;
  building?: string | null;
  floor?: number | null;
  roomNumber?: string | null;
  capacity?: number | null;
  roomType?: string | null;
  computerCount?: number | null;
  bookable?: boolean | null;
}

export type RoomUpdateRequest = Omit<Partial<RoomCreateRequest>, 'code'> & {
  active?: boolean | null;
};

export interface Booking {
  id: number;
  roomId: number;
  roomName: string | null;
  requesterWorkerId: number;
  approvedBy: number | null;
  schoolYearId: number | null;
  startDateTime: string;
  endDateTime: string;
  durationHours: number;
  purpose: string | null;
  teachingType: TeachingType | null;
  status: BookingStatus;
  notes: string | null;
}

export interface BookingCreateRequest {
  roomId: number;
  requesterWorkerId: number;
  schoolYearId?: number | null;
  startDateTime: string;
  endDateTime: string;
  purpose?: string | null;
  teachingType?: TeachingType | null;
}

export interface BookingUpdateRequest {
  startDateTime?: string | null;
  endDateTime?: string | null;
  purpose?: string | null;
  teachingType?: TeachingType | null;
  notes?: string | null;
}

export interface AvailabilityRequest {
  roomId: number;
  startDateTime: string;
  endDateTime: string;
}

export interface AvailabilityResponse {
  roomId: number;
  startDateTime: string;
  endDateTime: string;
  available: boolean;
  conflicts: Booking[];
}

export interface SchoolYear {
  id: number;
  code: string;
  startDate: string;
  endDate: string;
  description: string | null;
}

export interface SchoolYearCreateRequest {
  code: string;
  startDate: string;
  endDate: string;
  description?: string | null;
}

export interface TeachingNorm {
  id: number;
  roleId: number;
  schoolYearId: number;
  requiredHours: number;
  description: string | null;
}

export interface TeachingNormCreateRequest {
  roleId: number;
  schoolYearId: number;
  requiredHours: number;
  description?: string | null;
}

export interface SchoolYearWorker {
  id: number;
  schoolYearId: number;
  workerId: number;
  roleId: number | null;
  normId: number | null;
}

export interface SchoolYearWorkerRequest {
  schoolYearId: number;
  workerId: number;
  roleId?: number | null;
  normId?: number | null;
}

/** Fond casova: norma vs realizovano (UC-HR-03). */
export interface TeachingReport {
  workerId: number;
  schoolYearId: number;
  requiredHours: number | null;
  realizedHours: number;
  deviation: number;
  extraHours: number;
  fulfilled: boolean;
}

/** Parametri za kalendar zauzetosti svih sala (UC-SC-05). */
export interface OccupancyParams {
  from: string;
  to: string;
  building?: string | null;
  minCapacity?: number | null;
}
