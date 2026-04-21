import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { RegisterComponent } from './features/auth/register/register';
import {Home} from './features/dashboard/home/home';
import { ApartamentoDetails } from './features/dashboard/apartamento-details/apartamento-details';
import { ApartamentoEdit } from './features/dashboard/apartamento-edit/apartamento-edit';
import {ApartamentoCreate} from './features/dashboard/apartamento-create/apartamento-create';
import {VincularCodigoComponent} from './features/dashboard/vincular-codigo/vincular-codigo';
import {ReservaManualComponent} from './features/dashboard/reserva-manual/reserva-manual';

export const routes: Routes = [

  { path: 'login', component: LoginComponent },
  { path: 'vincular-vivienda', component: VincularCodigoComponent },
  {path: 'home', component: Home},
  { path: 'apartamento/nuevo', component: ApartamentoCreate },
  { path: 'apartamento/:id', component: ApartamentoDetails },
  { path: 'apartamento/editar/:id', component: ApartamentoEdit },
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  {path: 'register', component:RegisterComponent},
  {path: 'apartamento/:id/reserva-manual', component: ReservaManualComponent},
  { path: '**', redirectTo: '/login' }
];
