import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {Domain} from "../../dto/domain";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {DomainHeaderLanguagesComponent} from "./domain-header-languages/domain-header-languages.component";
import {DomainService} from "../../service/domain.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-domain-header',
  templateUrl: './domain-header.component.html',
  styleUrls: ['./domain-header.component.css']
})
export class DomainHeaderComponent implements OnInit, OnChanges {

  /**
   * The input domain
   */
  @Input()
  initialDomain: Domain;

  /**
   * Domain form
   */
  domainForm: FormGroup;

  /**
   * Default language tag for the domain
   */
  defaultLanguageTag: string = "";

  /**
   * Supported language tags for the domain
   */
  supportedLanguageTags: string[] = [];

  /**
   * Supported languages and default language linked to the domain
   */
  @ViewChild("languagesInput")
  languagesInput: DomainHeaderLanguagesComponent;

  /**
   * Editable state
   */
  editable: boolean;


  /**
   * Constructor that defines the domain form.
   * @param fb Form builder
   */
  constructor(private router: Router,
              private domainService: DomainService,
              private fb: FormBuilder) {

    this.domainForm = fb.group({
      code: ['', Validators.required],
      description: ['', Validators.required]
    });
  }

  /**
   * Open the component in edit mode if no identifier is defined.
   */
  ngOnInit() {

    if (this.initialDomain.id)
      this.editable = false;
    else
      this.editable = true;
  }

  /**
   * Reset the domain form when the domain input changes.
   * @param changes
   */
  ngOnChanges(changes: SimpleChanges) {

    // Feed form with domain data
    this.domainForm.reset({
      code: this.initialDomain.code,
      description: this.initialDomain.description
    });

    this.supportedLanguageTags = this.initialDomain.supportedLanguageTags;
    this.defaultLanguageTag = this.initialDomain.defaultLanguageTag;
  }

  /**
   * Switch the component in edit mode.
   */
  editDomain() {
    this.editable = true;
  }

  /**
   * Save the domain changes.
   */
  submitDomain() {

    // Feed domain to save
    const domainToSave: Domain = {
      id : this.initialDomain.id,
      code : this.domainForm.value.code as string,
      description : this.domainForm.value.description as string,
      supportedLanguageTags : this.supportedLanguageTags,
      defaultLanguageTag : this.defaultLanguageTag
    };

    // Save the domain
    this.domainService.saveDomain(domainToSave).then(domain => {
      this.router.navigate(['/domain', domain.id]);
    });
  }

  /**
   * Indicate if domain form changes are valid.
   * @returns {boolean} {@code true} if the domain form is valid, else {@code false}
   */
  valid(): boolean {

    if (this.domainForm.valid && this.languagesInput.valid)
      return true;
    else
      return false;
  }
}
