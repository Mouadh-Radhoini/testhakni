/** Types partages, alignes sur les DTO du backend Spring Boot. */
import { labelTable } from './i18n/labels';

export type Role = 'CUSTOMER' | 'PROVIDER' | 'ADMIN';
export type ServiceCategory =
  | 'PLOMBERIE'
  | 'ELECTRICITE'
  | 'MENAGE'
  | 'JARDINAGE'
  | 'PEINTURE'
  | 'BRICOLAGE'
  | 'DEMENAGEMENT'
  | 'INFORMATIQUE'
  | 'COURS_PARTICULIERS'
  | 'AUTRE';
export type PriceType = 'FIXED' | 'HOURLY' | 'ON_QUOTE';
export type ServiceStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
export type RequestStatus = 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
export type SortOption = 'NEWEST' | 'PRICE_ASC' | 'PRICE_DESC';

export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  city: string | null;
  role: Role;
  status: 'ACTIVE' | 'SUSPENDED';
  createdAt: string;
  avatarUrl?: string | null;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface Category {
  code: ServiceCategory;
  label: string;
}

export interface ProviderSummary {
  providerId: string;
  businessName: string;
  city: string;
  yearsOfExperience: number | null;
  available: boolean;
}

export interface Service {
  id: string;
  providerId: string;
  title: string;
  description: string;
  category: ServiceCategory;
  categoryLabel: string;
  city: string;
  price: number | null;
  /** null quand le service est verrouille (visiteur sans compte). */
  priceType: PriceType | null;
  status: ServiceStatus;
  createdAt: string;
  updatedAt: string;
  provider: ProviderSummary | null;
  /** true pour un visiteur sans compte : prix, description et prestataire masques par le serveur. */
  locked?: boolean;
}

export interface ServicePayload {
  title: string;
  description: string;
  category: ServiceCategory;
  city: string;
  price: number | null;
  priceType: PriceType;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ServiceSearch {
  q?: string;
  category?: ServiceCategory | '';
  city?: string;
  minPrice?: number | null;
  maxPrice?: number | null;
  sort?: SortOption;
  page?: number;
  size?: number;
}

export interface ProviderProfile {
  id: string;
  userId: string;
  businessName: string;
  bio: string | null;
  categories: ServiceCategory[];
  skills: string[];
  city: string;
  serviceArea: string | null;
  yearsOfExperience: number | null;
  available: boolean;
  photoUrls?: string[];
}

export interface ProviderPublic {
  providerId: string;
  displayName: string;
  businessName: string;
  bio: string | null;
  categories: ServiceCategory[];
  skills: string[];
  city: string;
  serviceArea: string | null;
  yearsOfExperience: number | null;
  available: boolean;
  memberSince: string;
  averageRating: number | null;
  reviewCount: number;
  /** Prestations terminees : l'experience du prestataire sur la plateforme. */
  completedJobs?: number;
  services: Service[];
  avatarUrl: string | null;
  photoUrls: string[];
}

export interface ServiceRequest {
  id: string;
  customerId: string;
  serviceId: string | null;
  title: string;
  description: string;
  category: ServiceCategory;
  categoryLabel: string;
  city: string;
  address: string | null;
  preferredDate: string | null;
  budget: number | null;
  status: RequestStatus;
  createdAt: string;
}

/** Format d'erreur unique renvoye par l'API (ApiError cote Java). */
export interface ApiError {
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: Record<string, string>;
}

export const PRICE_TYPE_LABELS: Record<PriceType, string> = labelTable('priceType', ['FIXED', 'HOURLY', 'ON_QUOTE'] as const);

export const REQUEST_STATUS_LABELS: Record<RequestStatus, string> = labelTable('status.request', ['OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'] as const);

export const SERVICE_STATUS_LABELS: Record<ServiceStatus, string> = labelTable('status.service', ['ACTIVE', 'INACTIVE', 'SUSPENDED'] as const);
