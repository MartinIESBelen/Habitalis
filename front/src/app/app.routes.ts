import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login';
import { RegisterComponent } from './features/auth/register/register';
import { Home } from './features/dashboard/home/home';
import { ApartamentoDetails } from './features/dashboard/apartamento-details/apartamento-details';
import { ApartamentoEdit } from './features/dashboard/apartamento-edit/apartamento-edit';
import { ApartamentoCreate } from './features/dashboard/apartamento-create/apartamento-create';
import { VincularCodigoComponent } from './features/dashboard/vincular-codigo/vincular-codigo';
import { ReservaManualComponent } from './features/dashboard/reserva-manual/reserva-manual';
import { Balance } from './features/dashboard/finanzas/balance/balance';
import { NuevoMovimiento } from './features/dashboard/finanzas/nuevo-movimiento/nuevo-movimiento';
import { ListaContratos } from './features/dashboard/contratos/lista-contratos/lista-contratos'
import { DetallesContrato } from './features/dashboard/contratos/detalles-contrato/detalles-contrato';
import { ListaElementos } from './features/dashboard/inventario/lista-elementos/lista-elementos';
import { CrearElemento } from './features/dashboard/inventario/crear-elemento/crear-elemento';
import { ElementoDetalle } from './features/dashboard/inventario/elemento-detalle/elemento-detalle';
import { EditarElemento } from './features/dashboard/inventario/editar-elemento/editar-elemento';
import { Perfil } from './features/dashboard/perfil/perfil';
import { EditarPerfil } from './features/dashboard/perfil/editar-perfil/editar-perfil';
import { ResetPassword } from './features/dashboard/reset-password/reset-password';
import { RecuperarPassword } from './features/dashboard/recuperar-password/recuperar-password';
import { authGuard } from './core/guard/auth-guard';

export const routes: Routes = [

  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'recuperar-password', component: RecuperarPassword },
  { path: 'reset-password', component: ResetPassword },

  { path: 'vincular-vivienda', component: VincularCodigoComponent, canActivate: [authGuard] },
  { path: 'home', component: Home, canActivate: [authGuard] },
  { path: 'perfil', component: Perfil, canActivate: [authGuard] },
  { path: 'editar-cuenta', component: EditarPerfil, canActivate: [authGuard] },
  { path: 'contratos', component: ListaContratos, canActivate: [authGuard] },
  { path: 'contratos/:id', component: DetallesContrato, canActivate: [authGuard] },

  { path: 'apartamento/nuevo', component: ApartamentoCreate, canActivate: [authGuard] },
  { path: 'apartamento/:id', component: ApartamentoDetails, canActivate: [authGuard] },
  { path: 'apartamento/editar/:id', component: ApartamentoEdit, canActivate: [authGuard] },
  { path: 'apartamento/:id/reserva-manual', component: ReservaManualComponent, canActivate: [authGuard] },
  { path: 'apartamento/:id/inventario', component: ListaElementos, canActivate: [authGuard] },
  { path: 'apartamento/:id/inventario/nuevo', component: CrearElemento, canActivate: [authGuard] },
  { path: 'apartamento/:id/inventario/editar/:itemId', component: EditarElemento, canActivate: [authGuard] },
  { path: 'apartamento/:id/inventario/:itemId', component: ElementoDetalle, canActivate: [authGuard] },

  {
    path: 'finanzas',
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'balance', pathMatch: 'full' },
      { path: 'balance', component: Balance },
      { path: 'nuevo-movimiento', component: NuevoMovimiento }
    ]
  },

  { path: '', redirectTo: '/perfil', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' } // Si ponen una URL que no existe, al login.
];
