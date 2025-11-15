import { message } from "antd";

export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
}

export interface RequestOptions extends RequestInit {
  params?: Record<string, string | number | boolean | null | undefined>;
}

export const SERVER_URL: string =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export const BASE_URL = `${SERVER_URL}/api`;

function buildUrl(url: string, params?: Record<string, string | number | boolean | null | undefined>): string {
  const fullUrl = `${BASE_URL}${url}`;

  if (!params || Object.keys(params).length === 0) {
    return fullUrl;
  }

  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined) {
      searchParams.append(key, String(value));
    }
  });

  const queryString = searchParams.toString();
  return queryString ? `${fullUrl}?${queryString}` : fullUrl;
}

async function handleResponse<T>(response: Response): Promise<ApiResponse<T>> {
  if (!response.ok) {

    throw new Error(`HTTP error! status: ${response.status}`);
  }

  const data: ApiResponse<T> = await response.json();

  if (data.code !== 200) {
    message.error(data.message || "Request failed");
    throw new Error(data.message || "Request failed");
  }

  return data;
}

async function request<T = unknown>(
  url: string,
  options: RequestOptions = {}
): Promise<T> {
  const { params, headers, ...restOptions } = options;

  const fullUrl = buildUrl(url, params);

  const defaultHeaders = new Headers(headers);
  if (!(restOptions.body instanceof FormData) && !defaultHeaders.has("Content-Type")) {
    defaultHeaders.set("Content-Type", "application/json");
  }

  try {
    const response = await fetch(fullUrl, {
      ...restOptions,
      headers: defaultHeaders,
    });

    const apiResponse = await handleResponse<T>(response);
    return apiResponse.data;
  } catch (error) {

    if (error instanceof Error) {
      throw error;
    }
    throw new Error("Network request failed");
  }
}

export function get<T = unknown>(
  url: string,
  params?: Record<string, string | number | boolean | null | undefined>,
  options?: Omit<RequestOptions, "method" | "body" | "params">
): Promise<T> {
  return request<T>(url, {
    ...options,
    method: "GET",
    params,
  });
}

export function post<T = unknown>(
  url: string,
  data?: unknown,
  options?: Omit<RequestOptions, "method" | "body">
): Promise<T> {
  return request<T>(url, {
    ...options,
    method: "POST",
    body: data === undefined ? undefined : JSON.stringify(data),
  });
}

export function put<T = unknown>(
  url: string,
  data?: unknown,
  options?: Omit<RequestOptions, "method" | "body">
): Promise<T> {
  return request<T>(url, {
    ...options,
    method: "PUT",
    body: data === undefined ? undefined : JSON.stringify(data),
  });
}

export function patch<T = unknown>(
  url: string,
  data?: unknown,
  options?: Omit<RequestOptions, "method" | "body">
): Promise<T> {
  return request<T>(url, {
    ...options,
    method: "PATCH",
    body: data === undefined ? undefined : JSON.stringify(data),
  });
}

export function del<T = unknown>(
  url: string,
  params?: Record<string, string | number | boolean | null | undefined>,
  options?: Omit<RequestOptions, "method" | "body" | "params">
): Promise<T> {
  return request<T>(url, {
    ...options,
    method: "DELETE",
    params,
  });
}

export default {
  get,
  post,
  put,
  patch,
  delete: del,
};
