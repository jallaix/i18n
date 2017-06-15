import {Component, Input, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {Domain} from "../../dto/domain";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {DomainHeaderLanguagesComponent} from "./domain-header-languages/domain-header-languages.component";
import {DomainService} from "../../service/domain.service";
import {Router} from "@angular/router";
import {NgbPopover} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-domain-header',
  templateUrl: './domain-header.component.html',
  styleUrls: ['./domain-header.component.css']
})
export class DomainHeaderComponent implements OnInit, OnChanges {

  /**
   * Error messages for each form control (initially empty)
   */
  domainFormErrors = {
    "code": "",
    "description": "",
    "languages": ""
  };

  /**
   * Validation messages for each form control
   */
  private validationMessages = {
    "code": {
      "required": "Code is required.",
      "minlength": "Code must be at least 3 characters long.",
      "maxlength": "Code cannot be more than 6 characters long.",
    },
    "description": {
      "required": "Description is required.",
      "maxlength": "Description cannot be more than 50 characters long.",
    },
    "languages": {
      "required": "At least 1 default language must be chosen."
    }
  };

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
   * Popover for code errors
   */
  @ViewChild("codeErrorsPopover")
  codeErrorsPopover: NgbPopover;

  /**
   * Popover for description errors
   */
  @ViewChild("descriptionErrorsPopover")
  descriptionErrorsPopover: NgbPopover;

  /**
   * Popover for languages errors
   */
  @ViewChild("languagesErrorsPopover")
  languagesErrorsPopover: NgbPopover;

  /**
   * Editable state
   */
  editable: boolean;


  /**
   * Constructor that defines the domain form.
   * @param router Router
   * @param domainService Domain service
   * @param fb Form builder
   */
  constructor(private router: Router,
              private domainService: DomainService,
              private fb: FormBuilder) {

    this.domainForm = fb.group({
      code: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(6)]],
      description: ['', [Validators.required, Validators.maxLength(50)]]
    });
  }

  /**
   * Open the component in edit mode if no identifier is defined.
   */
  ngOnInit() {
    this.editable = !this.initialDomain.id;
  }

  /**
   * Reset the domain form when the domain input changes.
   * @param changes
   */
  ngOnChanges(changes: SimpleChanges) {

    this.resetDomainForm();

    // Validate form input on user action
    this.domainForm.valueChanges
      .subscribe(data => this.validateInputData());
  }

  /**
   * Indicate if domain form changes are valid.
   * @returns {boolean} {@code true} if the domain form is valid, else {@code false}
   */
  get valid(): boolean {
    if (this.languagesInput && !this.languagesInput.valid)
      return false;
    return this.domainForm.valid;
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
      id: this.initialDomain.id,
      code: this.domainForm.value.code as string,
      description: this.domainForm.value.description as string,
      supportedLanguageTags: this.supportedLanguageTags,
      defaultLanguageTag: this.defaultLanguageTag
    };

    // Save the domain
    this.domainService.saveDomain(domainToSave).then(domain => {
      this.editable = false;
      this.initialDomain = domain;
      //noinspection JSIgnoredPromiseFromCall
      this.router.navigate(['/domain', domain.id]);
    });
  }

  /**
   * Reset the domain form.
   */
  resetDomain() {
    this.resetDomainForm();

    this.editable = !this.initialDomain.id;
  }

  private resetDomainForm() {

    // Reset domain form
    this.domainForm.reset({
      code: this.initialDomain.code,
      description: this.initialDomain.description
    });

    // Reset domain languages
    this.defaultLanguageTag = this.initialDomain.defaultLanguageTag;
    this.supportedLanguageTags = Object.assign([], this.initialDomain.supportedLanguageTags);
  }

  /**
   * Validate languages data.
   */
  validateLanguagesData() {

    // Define languages errors
    this.domainFormErrors["languages"] = '';
    if (this.languagesInput && !this.languagesInput.valid) {
      const messages = this.validationMessages["languages"];
      this.domainFormErrors["languages"] = messages["required"];
      this.languagesErrorsPopover.open();
    }
    else if (this.languagesErrorsPopover)
      this.languagesErrorsPopover.close();
  }

  /**
   * Validate input data.
   */
  private validateInputData() {

    if (!this.domainForm)
      return;

    // Define code errors
    this.domainFormErrors["code"] = '';
    const codeControl = this.domainForm.get("code");
    if (codeControl && codeControl.dirty && !codeControl.valid) {
      const messages = this.validationMessages["code"];
      for (const key in codeControl.errors)
        this.domainFormErrors["code"] += messages[key] + ' ';
      this.codeErrorsPopover.open();
    }
    else if (this.codeErrorsPopover)
        this.codeErrorsPopover.close();

    // Define code errors
    this.domainFormErrors["description"] = '';
    const descriptionControl = this.domainForm.get("description");
    if (descriptionControl && descriptionControl.dirty && !descriptionControl.valid) {
      const messages = this.validationMessages["description"];
      for (const key in descriptionControl.errors)
        this.domainFormErrors["description"] += messages[key] + ' ';
      this.descriptionErrorsPopover.open();
    }
    else if (this.descriptionErrorsPopover)
      this.descriptionErrorsPopover.close();
  }
}
