import { api, SCHEDULE } from './client';
import type { PageParams, PageResponse } from '../types/api';
import type {
  AvailabilityRequest,
  AvailabilityResponse,
  Booking,
  BookingCreateRequest,
  BookingStatus,
  BookingUpdateRequest,
  OccupancyParams,
  Room,
  RoomCreateRequest,
  RoomUpdateRequest,
  SchoolYear,
  SchoolYearCreateRequest,
  SchoolYearWorker,
  SchoolYearWorkerRequest,
  TeachingNorm,
  TeachingNormCreateRequest,
  TeachingReport,
} from '../types/schedule';

export const roomsApi = {
  list: (params: PageParams) => api.get<PageResponse<Room>>(`${SCHEDULE}/rooms`, { params }),
  getById: (id: number) => api.get<Room>(`${SCHEDULE}/rooms/${id}`),
  create: (body: RoomCreateRequest) => api.post<Room>(`${SCHEDULE}/rooms`, body),
  update: (id: number, body: RoomUpdateRequest) => api.put<Room>(`${SCHEDULE}/rooms/${id}`, body),
  remove: (id: number) => api.delete<void>(`${SCHEDULE}/rooms/${id}`),
};

export const bookingsApi = {
  getById: (id: number) => api.get<Booking>(`${SCHEDULE}/bookings/${id}`),
  byRoom: (roomId: number, params: PageParams) =>
    api.get<PageResponse<Booking>>(`${SCHEDULE}/bookings/room/${roomId}`, { params }),
  byWorker: (workerId: number, params: PageParams) =>
    api.get<PageResponse<Booking>>(`${SCHEDULE}/bookings/worker/${workerId}`, { params }),
  byStatus: (status: BookingStatus, params: PageParams) =>
    api.get<PageResponse<Booking>>(`${SCHEDULE}/bookings/status/${status}`, { params }),
  /** Zauzetost jedne sale u intervalu. */
  occupancy: (roomId: number, from: string, to: string) =>
    api.get<Booking[]>(`${SCHEDULE}/bookings/room/${roomId}/occupancy`, { params: { from, to } }),
  /** Zauzetost svih sala u intervalu, uz filtere po zgradi i kapacitetu (UC-SC-05). */
  occupancyAll: (params: OccupancyParams) =>
    api.get<Booking[]>(`${SCHEDULE}/bookings/occupancy`, {
      params: {
        from: params.from,
        to: params.to,
        building: params.building || undefined,
        minCapacity: params.minCapacity || undefined,
      },
    }),
  checkAvailability: (body: AvailabilityRequest) =>
    api.post<AvailabilityResponse>(`${SCHEDULE}/bookings/availability`, body),
  create: (body: BookingCreateRequest) => api.post<Booking>(`${SCHEDULE}/bookings`, body),
  update: (id: number, body: BookingUpdateRequest) => api.put<Booking>(`${SCHEDULE}/bookings/${id}`, body),
  approve: (id: number, approvedBy?: number | null) =>
    api.post<Booking>(`${SCHEDULE}/bookings/${id}/approve`, undefined, {
      params: approvedBy ? { approvedBy } : undefined,
    }),
  reject: (id: number, approvedBy?: number | null) =>
    api.post<Booking>(`${SCHEDULE}/bookings/${id}/reject`, undefined, {
      params: approvedBy ? { approvedBy } : undefined,
    }),
  cancel: (id: number) => api.post<Booking>(`${SCHEDULE}/bookings/${id}/cancel`),
};

export const schoolYearsApi = {
  list: (params: PageParams) => api.get<PageResponse<SchoolYear>>(`${SCHEDULE}/school-years`, { params }),
  getById: (id: number) => api.get<SchoolYear>(`${SCHEDULE}/school-years/${id}`),
  create: (body: SchoolYearCreateRequest) => api.post<SchoolYear>(`${SCHEDULE}/school-years`, body),
  remove: (id: number) => api.delete<void>(`${SCHEDULE}/school-years/${id}`),
};

export const teachingApi = {
  norms: (params: PageParams) => api.get<PageResponse<TeachingNorm>>(`${SCHEDULE}/teaching/norms`, { params }),
  createNorm: (body: TeachingNormCreateRequest) => api.post<TeachingNorm>(`${SCHEDULE}/teaching/norms`, body),
  removeNorm: (id: number) => api.delete<void>(`${SCHEDULE}/teaching/norms/${id}`),
  assignments: (schoolYearId: number) =>
    api.get<SchoolYearWorker[]>(`${SCHEDULE}/teaching/assignments/year/${schoolYearId}`),
  assign: (body: SchoolYearWorkerRequest) => api.post<SchoolYearWorker>(`${SCHEDULE}/teaching/assignments`, body),
  removeAssignment: (id: number) => api.delete<void>(`${SCHEDULE}/teaching/assignments/${id}`),
  /** Fond casova jednog nastavnika za jednu skolsku godinu (UC-HR-03). */
  report: (workerId: number, schoolYearId: number) =>
    api.get<TeachingReport>(`${SCHEDULE}/teaching/report`, { params: { workerId, schoolYearId } }),
  reportByYear: (schoolYearId: number) =>
    api.get<TeachingReport[]>(`${SCHEDULE}/teaching/report/year/${schoolYearId}`),
};
