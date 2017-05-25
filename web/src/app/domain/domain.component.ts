import { Component, OnInit } from '@angular/core';
import {ActivatedRoute, Params} from "@angular/router";
import 'rxjs/add/operator/switchMap';
import { Observable } from 'rxjs/Observable';
import {Domain} from "../dto/domain";
import {DomainService} from "../service/domain.service";

@Component({
  selector: 'app-domain',
  templateUrl: './domain.component.html',
  styleUrls: ['./domain.component.css']
})
export class DomainComponent implements OnInit {

  public domain: Domain;
  public editable: boolean;
  public languageTags:string[] = ["es", "de", "zh"];

  constructor(private route: ActivatedRoute,
              private domainService:DomainService) {
  }

  ngOnInit() {
    this.route.params
      .switchMap(params => {
        if (params['id'])
          return this.domainService.getDomain(params['id']);
        else
          return Promise.resolve(new Domain());
      })
      .subscribe(domain => {
        this.domain = domain;
        if (domain.id)
          this.editable = false;
        else
          this.editable = true;
      });
  }

  editDomain():void {
    this.editable = true;
  }

  submitDomain():void {
    this.editable = false;
  }
}
