import {Component, Input, OnInit, QueryList, ViewChildren} from '@angular/core';
import {ActivatedRoute, Params} from "@angular/router";
import 'rxjs/add/operator/switchMap';
import {Domain} from "../../dto/domain";
import {DomainService} from "../../service/domain.service";
import {NgbPopover} from "@ng-bootstrap/ng-bootstrap";
import {forEach} from "@angular/router/src/utils/collection";

@Component({
  selector: 'app-domain-header',
  templateUrl: './domain-header.component.html',
  styleUrls: ['./domain-header.component.css']
})
export class DomainHeaderComponent implements OnInit {

  @Input()
  public domain: Domain;
  public editable: boolean;
  public languageTags: string[] = ["es", "de", "zh"];
  public selectedLanguageTag: string;
  @ViewChildren('languageTagPopover') popovers: QueryList<NgbPopover>;

  constructor(private route: ActivatedRoute,
              private domainService: DomainService) {
  }

  ngOnInit() {
    if (this.domain.id)
      this.editable = false;
    else
      this.editable = true;
  }

  editDomain(): void {
    this.editable = true;
  }

  submitDomain(): void {
    this.editable = false;
  }

  addLanguageTag(languageTag: string): void {

    if (!this.domain.availableLanguageTags)
      this.domain.availableLanguageTags = new Array;

    this.domain.availableLanguageTags.push(languageTag);
    this.domain.availableLanguageTags.sort();
    this.languageTags.splice(this.languageTags.indexOf(languageTag), 1);
  }

  showLanguageTagOptions(languageTagPopover: NgbPopover, languageTag: string): void {

    // No option available for the default language tag
    if (languageTag == this.domain.defaultLanguageTag)
      return;

    // Same language tag that was previously shown => toggle popover
    if (languageTag == this.selectedLanguageTag) {

      if (languageTagPopover.isOpen())
        languageTagPopover.close();
      else
        languageTagPopover.open();
    }

    // Other selected language tag => close the opened popover and open a new one
    else {
      let openedPopover = this.popovers.find((popover, i, popovers) => popover.isOpen());
      if (openedPopover)
        openedPopover.close()

      this.selectedLanguageTag = languageTag;

      languageTagPopover.open();
    }
  }

  chooseDefaultLanguageTag(languageTag: string): void {

    this.domain.defaultLanguageTag = languageTag;

    this.popovers.find((popover, i, popovers) => popover.isOpen()).close();
  }

  removeLanguageTag(languageTag: string): void {

    this.domain.availableLanguageTags = this.domain.availableLanguageTags.filter((tag, i, tags) => tag != languageTag);
    this.languageTags.push(languageTag);

    this.popovers.find((popover, i, popovers) => popover.isOpen()).close();
  }
}
