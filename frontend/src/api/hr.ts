import { api, HR } from './client';
import type { PageParams, PageResponse } from '../types/api';
import type {
  AuditLog,
  AuthResponse,
  CurrentUser,
  EmploymentStatus,
  Permission,
  PermissionCreateRequest,
  Position,
  PositionCreateRequest,
  PositionUpdateRequest,
  Role,
  RoleCreateRequest,
  RoleUpdateRequest,
  User,
  UserCreateRequest,
  UserUpdateRequest,
  Worker,
  WorkerCreateRequest,
  WorkerDetail,
  WorkerPosition,
  WorkerPositionAssignRequest,
  WorkerPositionUpdateRequest,
  WorkerSummary,
  WorkerUpdateRequest,
} from '../types/hr';

export const authApi = {
  login: (usernameOrEmail: string, password: string) =>
    api.post<AuthResponse>(`${HR}/auth/login`, { usernameOrEmail, password }),
  me: () => api.get<CurrentUser>(`${HR}/auth/me`),
  changePassword: (oldPassword: string, newPassword: string) =>
    api.post<void>(`${HR}/auth/change-password`, { oldPassword, newPassword }),
};

export const workersApi = {
  search: (searchTerm: string | undefined, params: PageParams) =>
    api.get<PageResponse<WorkerSummary>>(`${HR}/workers`, {
      params: { ...params, searchTerm: searchTerm || undefined },
    }),
  byStatus: (status: EmploymentStatus) => api.get<WorkerSummary[]>(`${HR}/workers/status/${status}`),
  getById: (id: number) => api.get<WorkerDetail>(`${HR}/workers/${id}`),
  create: (body: WorkerCreateRequest) => api.post<Worker>(`${HR}/workers`, body),
  update: (id: number, body: WorkerUpdateRequest) => api.put<Worker>(`${HR}/workers/${id}`, body),
  remove: (id: number) => api.delete<void>(`${HR}/workers/${id}`),
};

export const positionsApi = {
  list: (params: PageParams) => api.get<PageResponse<Position>>(`${HR}/positions`, { params }),
  getById: (id: number) => api.get<Position>(`${HR}/positions/${id}`),
  create: (body: PositionCreateRequest) => api.post<Position>(`${HR}/positions`, body),
  update: (id: number, body: PositionUpdateRequest) => api.put<Position>(`${HR}/positions/${id}`, body),
  remove: (id: number) => api.delete<void>(`${HR}/positions/${id}`),
};

export const workerPositionsApi = {
  byWorker: (workerId: number) => api.get<WorkerPosition[]>(`${HR}/worker-positions/worker/${workerId}`),
  assign: (body: WorkerPositionAssignRequest) => api.post<WorkerPosition>(`${HR}/worker-positions`, body),
  update: (id: number, body: WorkerPositionUpdateRequest) =>
    api.put<WorkerPosition>(`${HR}/worker-positions/${id}`, body),
  remove: (id: number) => api.delete<void>(`${HR}/worker-positions/${id}`),
};

export const usersApi = {
  list: (params: PageParams) => api.get<PageResponse<User>>(`${HR}/users`, { params }),
  getById: (id: number) => api.get<User>(`${HR}/users/${id}`),
  create: (body: UserCreateRequest) => api.post<User>(`${HR}/users`, body),
  update: (id: number, body: UserUpdateRequest) => api.put<User>(`${HR}/users/${id}`, body),
  remove: (id: number) => api.delete<void>(`${HR}/users/${id}`),
  permissions: (userId: number) => api.get<Permission[]>(`${HR}/users/${userId}/permissions`),
};

export const rolesApi = {
  list: (params: PageParams) => api.get<PageResponse<Role>>(`${HR}/roles`, { params }),
  getById: (id: number) => api.get<Role>(`${HR}/roles/${id}`),
  create: (body: RoleCreateRequest) => api.post<Role>(`${HR}/roles`, body),
  update: (id: number, body: RoleUpdateRequest) => api.put<Role>(`${HR}/roles/${id}`, body),
  remove: (id: number) => api.delete<void>(`${HR}/roles/${id}`),
  permissions: (roleId: number) => api.get<Permission[]>(`${HR}/roles/${roleId}/permissions`),
  assignPermission: (roleId: number, permissionId: number) =>
    api.post<Permission>(`${HR}/roles/${roleId}/permissions/${permissionId}`),
  removePermission: (roleId: number, permissionId: number) =>
    api.delete<void>(`${HR}/roles/${roleId}/permissions/${permissionId}`),
};

export const permissionsApi = {
  list: (params: PageParams) => api.get<PageResponse<Permission>>(`${HR}/permissions`, { params }),
  create: (body: PermissionCreateRequest) => api.post<Permission>(`${HR}/permissions`, body),
  remove: (id: number) => api.delete<void>(`${HR}/permissions/${id}`),
};

export const auditApi = {
  list: (params: PageParams) => api.get<PageResponse<AuditLog>>(`${HR}/audit-logs`, { params }),
  forEntity: (entityName: string, entityId: number) =>
    api.get<AuditLog[]>(`${HR}/audit-logs/entity/${entityName}/${entityId}`),
};
