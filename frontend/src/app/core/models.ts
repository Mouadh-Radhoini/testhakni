/** Types partages, alignes sur les DTO du backend Spring Boot. */

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
  priceType: PriceType;
  status: ServiceStatus;
  createdAt: string;
  updatedAt: string;
  provider: ProviderSummary | null;
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
  services: Service[];
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

export const PRICE_TYPE_LABELS: Record<PriceType, string> = {
  FIXED: 'Prix fixe',
  HOURLY: 'Par heure',
  ON_QUOTE: 'Sur devis',
};

export const REQUEST_STATUS_LABELS: Record<RequestStatus, string> = {
  OPEN: 'Ouverte',
  IN_PROGRESS: 'En cours',
  COMPLETED: 'Terminée',
  CANCELLED: 'Annulée',
};

export const SERVICE_STATUS_LABELS: Record<ServiceStatus, string> = {
  ACTIVE: 'Actif',
  INACTIVE: 'Désactivé',
  SUSPENDED: 'Suspendu (admin)',
};
