import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import { NavComponent } from './nav/nav.component';
import { FooterComponent } from './footer/footer.component';
import { NgbModule } from "@ng-bootstrap/ng-bootstrap";
import { DomainComponent } from './domain/domain.component';
import {AppRoutingModule} from "./app-routing.module";
import { HomeComponent } from './home/home.component';
import {DomainService} from "app/service/domain.service";
import { DomainHeaderComponent } from './domain/domain-header/domain-header.component';
import {KeyMessageService} from "./service/key-message.service";

@NgModule({
  declarations: [
    AppComponent,
    NavComponent,
    FooterComponent,
    DomainComponent,
    HomeComponent,
    DomainHeaderComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    NgbModule.forRoot(),
    AppRoutingModule
  ],
  providers: [DomainService, KeyMessageService],
  bootstrap: [AppComponent]
})
export class AppModule { }
