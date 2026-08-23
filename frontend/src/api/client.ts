import axios, { AxiosError, type AxiosRequestConfig } from 'axios';
import type { ApiResponse, ErrorResponse } from '../types/api';

const BASE_URL = import.meta.env.VITE_API_URL ?? '';

export const TOKEN_KEY = 'erp.token';

export function getStoredToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function setStoredToken(token: string | null) {
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
  } else {
    localStorage.removeItem(TOKEN_KEY);
  }
}

export const http = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

http.interceptors.request.use((config) => {
  const token = getStoredToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/** Poziva se kada backend odbije token — postavlja ga AuthProvider. */
let onUnauthorized: (() => void) | null = null;

export function setUnauthorizedHandler(handler: (() => void) | null) {
  onUnauthorized = handler;
}

http.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorResponse>) => {
    const status = error.response?.status;
    // 401 = token istekao/nevalidan. 403 bez tokena takodje znaci "nisi prijavljen"
    // (Spring vraca 403 za anonimni pristup zasticenom resursu).
    const notLoggedIn = status === 401 || (status === 403 && !getStoredToken());
    if (notLoggedIn) {
      onUnauthorized?.();
    }
    return Promise.reject(error);
  },
);

/** Skida {success, data} omotac i vraca samo `data`. */
async function unwrap<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const response = await promise;
  return response.data.data;
}

export const api = {
  get: <T>(url: string, config?: AxiosRequestConfig) => unwrap<T>(http.get<ApiResponse<T>>(url, config)),
  post: <T>(url: string, body?: unknown, config?: AxiosRequestConfig) =>
    unwrap<T>(http.post<ApiResponse<T>>(url, body, config)),
  put: <T>(url: string, body?: unknown, config?: AxiosRequestConfig) =>
    unwrap<T>(http.put<ApiResponse<T>>(url, body, config)),
  delete: <T>(url: string, config?: AxiosRequestConfig) =>
    unwrap<T>(http.delete<ApiResponse<T>>(url, config)),
};

/** Ljudski citljiva poruka iz bilo koje greske (validacione poruke se spajaju). */
export function errorMessage(error: unknown, fallback = 'Došlo je do greške'): string {
  if (axios.isAxiosError<ErrorResponse>(error)) {
    const body = error.response?.data;
    if (body?.validationErrors?.length) {
      return body.validationErrors.map((v) => v.message).join('; ');
    }
    if (body?.message) {
      return body.message;
    }
    if (error.code === 'ERR_NETWORK') {
      return 'Backend nije dostupan. Proveri da li je API gateway pokrenut na ' + (BASE_URL || 'istom origin-u');
    }
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}

/** Mapa polje -> poruka, za prikaz greske uz konkretno polje forme. */
export function fieldErrors(error: unknown): Record<string, string> {
  if (axios.isAxiosError<ErrorResponse>(error) && error.response?.data.validationErrors) {
    return Object.fromEntries(error.response.data.validationErrors.map((v) => [v.field, v.message]));
  }
  return {};
}

export const HR = '/hr/api';
export const SCHEDULE = '/schedule/api';
