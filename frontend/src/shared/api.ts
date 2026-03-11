/**
 * OmniGame AI — API Client SDK
 *
 * Typed fetch wrapper with JWT auth token injection and error handling.
 * Provides methods for all backend API endpoints.
 */

import type { Game, PageResponse, ChatRequest } from './types';

const API_BASE = '/api/v1';

// ── Auth Token Management ────────────────────────────────────────

let authToken: string | null = null;

export function setAuthToken(token: string | null): void {
  authToken = token;
}

export function getAuthToken(): string | null {
  return authToken;
}

// ── Base Fetch Wrapper ───────────────────────────────────────────

async function apiFetch<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...((options.headers as Record<string, string>) || {}),
  };

  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }

  const response = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ detail: 'Unknown error' }));
    throw new ApiError(response.status, error.detail || error.title || 'Request failed');
  }

  return response.json() as Promise<T>;
}

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

// ── Game Endpoints ───────────────────────────────────────────────

export async function fetchGames(
  page = 0,
  size = 20,
  sort = 'name'
): Promise<PageResponse<Game>> {
  return apiFetch<PageResponse<Game>>(
    `/games?page=${page}&size=${size}&sort=${sort}`
  );
}

export async function searchGames(
  query: string,
  page = 0,
  size = 20
): Promise<PageResponse<Game>> {
  return apiFetch<PageResponse<Game>>(
    `/games/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`
  );
}

export async function fetchGameBySlug(slug: string): Promise<Game> {
  return apiFetch<Game>(`/games/${slug}`);
}

// ── The Collector (RAG Chat) ─────────────────────────────────────

/**
 * Streams a chat response from The Collector via SSE.
 * Returns an async generator that yields text tokens as they arrive.
 */
export async function* streamCollectorChat(
  request: ChatRequest
): AsyncGenerator<string, void, undefined> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };

  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }

  const response = await fetch(`${API_BASE}/collector/chat`, {
    method: 'POST',
    headers,
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    throw new ApiError(response.status, 'Chat request failed');
  }

  const reader = response.body?.getReader();
  if (!reader) throw new Error('No response body');

  const decoder = new TextDecoder();

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    const chunk = decoder.decode(value, { stream: true });
    // SSE format: "data:token\n\n"
    const lines = chunk.split('\n');
    for (const line of lines) {
      if (line.startsWith('data:')) {
        const token = line.slice(5);
        if (token.trim()) {
          yield token;
        }
      }
    }
  }
}
