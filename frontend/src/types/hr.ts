export type EmploymentStatus = 'ACTIVE' | 'ON_LEAVE' | 'TERMINATED' | 'SUSPENDED';
export type EmploymentType = 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'TEMPORARY';

/** Kodovi difoltnih rola koje seed-uje backend (DefaultRoleSeeder). */
export type RoleCode = 'ADMIN' | 'HR' | 'PROFESOR';

export interface AuthResponse {
  token: string;
  tokenType: string;
  userId: number;
  username: string;
  roleId: number | null;
}

/** GET /hr/api/auth/me */
export interface CurrentUser {
  userId: number;
  username: string;
  roleId: number | null;
  roleCode: string | null;
  roleName: string | null;
  workerId: number | null;
  workerFullName: string | null;
  workerEmail: string | null;
  permissions: string[];
}

export interface Worker {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  personalId: string;
  phone: string | null;
  hireDate: string;
  terminationDate: string | null;
  employmentStatus: EmploymentStatus;
  employmentType: EmploymentType | null;
  active: boolean;
}

export interface WorkerSummary {
  id: number;
  fullName: string;
  email: string;
  employmentStatus: EmploymentStatus;
}

export interface WorkerDetail extends Worker {
  positions: WorkerPosition[];
}

export interface WorkerCreateRequest {
  firstName: string;
  lastName: string;
  email: string;
  personalId: string;
  phone?: string | null;
  hireDate: string;
  employmentStatus?: EmploymentStatus | null;
  employmentType?: EmploymentType | null;
}

export type WorkerUpdateRequest = Partial<WorkerCreateRequest> & {
  terminationDate?: string | null;
};

export interface Position {
  id: number;
  title: string;
  salaryGrade: string | null;
  baseSalary: number | null;
  vacant: boolean;
}

export interface PositionCreateRequest {
  title: string;
  salaryGrade?: string | null;
  baseSalary?: number | null;
  isVacant?: boolean | null;
}

export type PositionUpdateRequest = Partial<PositionCreateRequest>;

export interface WorkerPosition {
  id: number;
  workerId: number;
  positionId: number;
  positionTitle: string | null;
  validFrom: string;
  validTo: string | null;
  fraction: number | null;
  isPrimary: boolean | null;
  active: boolean;
}

export interface WorkerPositionAssignRequest {
  workerId: number;
  positionId: number;
  validFrom: string;
  validTo?: string | null;
  fraction?: number | null;
  isPrimary?: boolean | null;
}

export type WorkerPositionUpdateRequest = Omit<
  Partial<WorkerPositionAssignRequest>,
  'workerId' | 'positionId'
>;

export interface User {
  id: number;
  username: string;
  workerId: number | null;
  roleId: number | null;
  roleName: string | null;
  active: boolean;
  createdAt: string;
  lastLogin: string | null;
}

export interface UserCreateRequest {
  username: string;
  password: string;
  workerId?: number | null;
  roleId?: number | null;
  isActive?: boolean | null;
}

export interface UserUpdateRequest {
  workerId?: number | null;
  roleId?: number | null;
  isActive?: boolean | null;
}

export interface Role {
  id: number;
  code: string;
  name: string;
  description: string | null;
  active: boolean;
}

export interface RoleCreateRequest {
  code: string;
  name: string;
  description?: string | null;
}

export interface RoleUpdateRequest {
  name?: string | null;
  description?: string | null;
  isActive?: boolean | null;
}

export interface Permission {
  id: number;
  code: string;
  name: string;
  module: string | null;
}

export interface PermissionCreateRequest {
  code: string;
  name: string;
  module?: string | null;
}

export interface AuditLog {
  id: number;
  entityName: string;
  entityId: number;
  action: string;
  changedBy: number | null;
  details: string | null;
  changedAt: string;
}
