/**
 * OmniGame AI — TypeScript Type Definitions
 *
 * Mirrors the backend DTO contracts for type-safe API consumption.
 * These interfaces are the single source of truth for all frontend
 * data structures.
 */

// ── Game Catalog ─────────────────────────────────────────────────

export interface Game {
  id: string;
  name: string;
  slug: string;
  coverUrl: string | null;
  description: string | null;
  entityCount: number;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;       // current page (0-indexed)
  size: number;         // page size
  first: boolean;
  last: boolean;
}

// ── Game Entities (EAV) ──────────────────────────────────────────

export interface GameEntity {
  id: string;
  name: string;
  type: EntityType;
  description: string | null;
  downloadUrl: string | null;
  authorName: string | null;
  downloadCount: number;
  securityAudited: boolean;
  attributeValues: EntityAttributeValue[];
  createdAt: string;
}

export type EntityType = 'MOD' | 'PATCH' | 'ASSET' | 'TOOL';

export interface EntityAttributeValue {
  id: string;
  attributeName: string;
  attributeDataType: AttributeDataType;
  value: string;
}

export type AttributeDataType = 'STRING' | 'INTEGER' | 'BOOLEAN' | 'FLOAT';

// ── The Collector (AI Chat) ──────────────────────────────────────

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp?: number;
}

export interface ChatRequest {
  gameSlug: string;
  message: string;
  conversationHistory?: ChatMessage[];
}

// ── User / Auth ──────────────────────────────────────────────────

export interface User {
  id: string;
  email: string;
  displayName: string | null;
  avatarUrl: string | null;
  subscriptionTier: 'FREE' | 'PREMIUM';
}
