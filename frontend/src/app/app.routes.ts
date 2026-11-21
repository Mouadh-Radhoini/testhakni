import { Routes } from '@angular/router';

import { authGuard, guestGuard, roleGuard } from './core/guards/auth.guards';

const T = ' - Local Services Marketplace';

/** Toutes les pages sont chargees a la demande (lazy loading). FE-01 */
export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/home/home').then((m) => m.Home), title: 'Accueil' + T },

  // Authentification (FE-02)
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/login').then((m) => m.Login),
    title: 'Connexion' + T,
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register-choice').then((m) => m.RegisterChoice),
    title: 'Inscription' + T,
  },
  {
    path: 'register/customer',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register-customer').then((m) => m.RegisterCustomerPage),
    title: 'Inscription client' + T,
  },
  {
    path: 'register/provider',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/auth/register-provider').then((m) => m.RegisterProviderPage),
    title: 'Inscription prestataire' + T,
  },

  // Compte (FE-04)
  {
    path: 'account',
    canActivate: [authGuard],
    loadComponent: () => import('./features/account/account').then((m) => m.Account),
    title: 'Mon compte' + T,
  },

  // Catalogue public (FE-06)
  {
    path: 'services',
    loadComponent: () => import('./features/catalog/service-search').then((m) => m.ServiceSearchPage),
    title: 'Trouver un service' + T,
  },
  {
    path: 'services/:id',
    loadComponent: () => import('./features/catalog/service-detail').then((m) => m.ServiceDetail),
    title: 'Détail du service' + T,
  },
  {
    path: 'providers/:id',
    loadComponent: () => import('./features/catalog/provider-public').then((m) => m.ProviderPublicPage),
    title: 'Prestataire' + T,
  },

  { path: 'provider/requests', canActivate: [roleGuard('PROVIDER')], loadComponent: () => import('./features/marketplace/requests').then(m => m.OpenRequests) },
  { path: 'provider/offers', canActivate: [roleGuard('PROVIDER')], loadComponent: () => import('./features/marketplace/offers').then(m => m.OffersPage) },
  { path: 'requests/:id/offers', canActivate: [roleGuard('CUSTOMER')], loadComponent: () => import('./features/marketplace/offers').then(m => m.OffersPage) },
  { path: 'bookings', canActivate: [roleGuard('CUSTOMER','PROVIDER')], loadComponent: () => import('./features/marketplace/bookings').then(m => m.BookingsPage) },
  { path: 'bookings/:id', canActivate: [roleGuard('CUSTOMER','PROVIDER')], loadComponent: () => import('./features/marketplace/bookings').then(m => m.BookingsPage) },
  { path: 'notifications', canActivate: [authGuard], loadComponent: () => import('./features/marketplace/notifications').then(m => m.NotificationsPage) },
  { path: 'admin/dashboard', canActivate: [roleGuard('ADMIN')], data: {section:'dashboard'}, loadComponent: () => import('./features/marketplace/admin').then(m => m.AdminPage) },
  { path: 'admin/users', canActivate: [roleGuard('ADMIN')], data: {section:'users'}, loadComponent: () => import('./features/marketplace/admin').then(m => m.AdminPage) },
  { path: 'admin/services', canActivate: [roleGuard('ADMIN')], data: {section:'services'}, loadComponent: () => import('./features/marketplace/admin').then(m => m.AdminPage) },
  { path: 'admin/reviews', canActivate: [roleGuard('ADMIN')], data: {section:'reviews'}, loadComponent: () => import('./features/marketplace/admin').then(m => m.AdminPage) },
  { path: 'admin/reports', canActivate: [roleGuard('ADMIN')], data: {section:'reports'}, loadComponent: () => import('./features/marketplace/admin').then(m => m.AdminPage) },

  // Espace prestataire (FE-04 / FE-05)
  {
    path: 'provider',
    canActivate: [roleGuard('PROVIDER')],
    children: [
      {
        path: 'profile',
        loadComponent: () => import('./features/provider/provider-profile-edit').then((m) => m.ProviderProfileEdit),
        title: 'Profil professionnel' + T,
      },
      {
        path: 'services',
        loadComponent: () => import('./features/provider/my-services').then((m) => m.MyServices),
        title: 'Mes services' + T,
      },
      {
        path: 'services/new',
        loadComponent: () => import('./features/provider/service-form').then((m) => m.ServiceForm),
        title: 'Nouveau service' + T,
      },
      {
        path: 'services/:id/edit',
        loadComponent: () => import('./features/provider/service-form').then((m) => m.ServiceForm),
        title: 'Modifier le service' + T,
      },
      { path: '', pathMatch: 'full', redirectTo: 'services' },
    ],
  },

  // Demandes du client (FE-06)
  {
    path: 'requests',
    canActivate: [roleGuard('CUSTOMER')],
    loadComponent: () => import('./features/requests/my-requests').then((m) => m.MyRequests),
    title: 'Mes demandes' + T,
  },
  {
    path: 'requests/new',
    canActivate: [roleGuard('CUSTOMER')],
    loadComponent: () => import('./features/requests/request-form').then((m) => m.RequestForm),
    title: 'Nouvelle demande' + T,
  },

  { path: 'forbidden', loadComponent: () => import('./features/errors/forbidden').then((m) => m.Forbidden) },
  {
    path: '**',
    loadComponent: () => import('./features/errors/not-found').then((m) => m.NotFound),
    title: 'Page introuvable' + T,
  },
];
