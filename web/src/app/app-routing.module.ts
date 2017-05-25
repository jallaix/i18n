import { NgModule } from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {AppComponent} from "./app.component";
import {DomainComponent} from "./domain/domain.component";
import {HomeComponent} from "./home/home.component";

const routes: Routes = [
  { path: 'domain/:id', component: DomainComponent },
  { path: 'domain', component: DomainComponent },
  { path: '**', component: HomeComponent }
];

@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule {}
