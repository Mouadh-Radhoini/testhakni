import { ServiceCategory } from './models';

export interface OpenRequest {
  id: string;
  title: string;
  description: string;
  category: ServiceCategory;
  city: string;
  budget: number | null;
  preferredDate: string | null;
}
export interface Proposal {
  price: number;
  proposedDate: string;
  description: string;
}
export interface Offer extends Proposal {
  id: string;
  requestId: string;
  providerId: string;
  businessName: string;
  requestTitle: string;
  status: 'SUBMITTED' | 'ACCEPTED' | 'REJECTED' | 'CANCELLED';
  createdAt: string;
}
export type BookingStatus = 'BOOKED' | 'IN_PROGRESS' | 'AWAITING_CONFIRMATION' | 'COMPLETED';
export interface Booking {
  id: string;
  requestId: string;
  offerId: string;
  customerId: string;
  providerId: string;
  serviceId: string | null;
  title: string;
  address: string | null;
  price: number;
  proposedDate: string;
  status: BookingStatus;
  reviewed: boolean;
  history: { status: BookingStatus; actorId: string; at: string }[];
}
export interface ReviewEntry {
  id: string;
  rating: number;
  comment: string;
  createdAt: string;
}
export interface Review extends ReviewEntry {
  bookingId: string;
  providerId: string;
  serviceId: string | null;
  hidden: boolean;
}
export interface Reviews {
  count: number;
  average: number | null;
  reviews: ReviewEntry[];
}
export interface Notice {
  id: string;
  recipientId: string;
  type: string;
  referenceId: string;
  message: string;
  createdAt: string;
  readAt: string | null;
}
export interface Report {
  id: string;
  reporterId: string;
  targetType: 'SERVICE' | 'REVIEW';
  targetId: string;
  reason: string;
  status: 'OPEN' | 'RESOLVED' | 'DISMISSED';
  createdAt: string;
  moderatorId: string | null;
  decision: string | null;
}

export const offerLabels = {
  SUBMITTED: 'En attente',
  ACCEPTED: 'Acceptée',
  REJECTED: 'Refusée',
  CANCELLED: 'Annulée',
};
export const bookingLabels: Record<BookingStatus, string> = {
  BOOKED: 'Réservée',
  IN_PROGRESS: 'En cours',
  AWAITING_CONFIRMATION: 'À confirmer',
  COMPLETED: 'Terminée',
};
