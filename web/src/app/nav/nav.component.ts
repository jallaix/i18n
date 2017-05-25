import { Component, OnInit } from '@angular/core';
import {Domain} from "../dto/domain";
import {DomainService} from "../service/domain.service";

@Component({
  selector: 'app-nav',
  templateUrl: './nav.component.html',
  styleUrls: ['./nav.component.css']
})
export class NavComponent implements OnInit {

  public domains:Domain[];
  public selectedDomains:Domain;

  constructor(private domainService:DomainService) {
  }

  ngOnInit() {
    this.domainService.getDomains().then(domains => this.domains = domains);
  }
}
