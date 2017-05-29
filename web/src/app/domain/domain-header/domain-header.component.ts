import {Component, Input, OnInit} from '@angular/core';
import {ActivatedRoute, Params} from "@angular/router";
import 'rxjs/add/operator/switchMap';
import {Domain} from "../../dto/domain";
import {DomainService} from "../../service/domain.service";

@Component({
  selector: 'app-domain-header',
  templateUrl: './domain-header.component.html',
  styleUrls: ['./domain-header.component.css']
})
export class DomainHeaderComponent implements OnInit {

  @Input()
  public domain: Domain;
  public editable: boolean;
  public languageTags:string[] = ["es", "de", "zh"];

  constructor(private route: ActivatedRoute,
              private domainService:DomainService) {
  }

  ngOnInit() {
    if (this.domain.id)
      this.editable = false;
    else
      this.editable = true;
  }

  editDomain():void {
    this.editable = true;
  }

  submitDomain():void {
    this.editable = false;
  }

  addLanguageTag(languageTag:string):void {

    if (!this.domain.availableLanguageTags)
      this.domain.availableLanguageTags = new Array;

    this.domain.availableLanguageTags.push(languageTag);
    this.domain.availableLanguageTags.sort();
    this.languageTags.splice(this.languageTags.indexOf(languageTag), 1);
  }
}
