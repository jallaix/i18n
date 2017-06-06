import {
  Component, ElementRef, EventEmitter, HostListener, Input, OnInit, Output, QueryList,
  ViewChildren
} from '@angular/core';
import {NgbPopover} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-domain-header-languages',
  templateUrl: './domain-header-languages.component.html',
  styleUrls: ['./domain-header-languages.component.css']
})
export class DomainHeaderLanguagesComponent implements OnInit {

  /**
   * List of language tags available for selection
   */
  public availableLanguageTags: string[];

  /**
   * List of selected language tags
   */
  @Input()
  public supportedLanguageTags: string[];

  /**
   * Event emitter for supported language tags
   * @type {EventEmitter<string[]>}
   */
  @Output()
  public supportedLanguageTagsChange = new EventEmitter<string[]>();

  /**
   * Default language tag
   */
  @Input()
  defaultLanguageTag: string;

  /**
   * Event emitter for default language tag
   * @type {EventEmitter<string>}
   */
  @Output()
  public defaultLanguageTagChange = new EventEmitter<string>();

  /**
   * List of popovers, there are as many popovers as selected language tags
   */
  @ViewChildren('languageTagPopover')
  public popovers: QueryList<NgbPopover>;

  /**
   * Language tag currently selected for operations (remove, set to default)
   */
  public selectedLanguageTag: string;


  /**
   * Constructor.
   * @param el Element reference
   */
  constructor(private el: ElementRef) {
  }

  /**
   * Set available language tags.
   */
  ngOnInit() {
    // TODO : call the LanguageService to get data
    this.availableLanguageTags = ["es", "de", "zh"]

    if (!this.supportedLanguageTags)
      this.supportedLanguageTags = [];
  }


  /**
   * Show language tag operations (remove, set to default)
   * @param languageTagPopover Popover assigned to the selected language
   * @param languageTag Selected language tag
   */
  public showLanguageTagOperations(languageTagPopover: NgbPopover, languageTag: string): void {

    // No operation available for the default language tag
    if (languageTag == this.defaultLanguageTag)
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
      this.closeOpenedPopover();

      this.selectedLanguageTag = languageTag;

      languageTagPopover.open();
    }
  }

  /**
   * Add a language tag to the list of supported tags and remove the language tag from the list of available tags.
   * @param languageTag The language tag to add
   */
  public addLanguageTag(languageTag: string): void {

    // Add the language tag to the list of supported tags
    this.supportedLanguageTags.push(languageTag);
    this.supportedLanguageTags.sort();
    this.supportedLanguageTagsChange.emit(this.supportedLanguageTags);

    // Remove the language tag from the list of available tags
    this.availableLanguageTags.splice(this.availableLanguageTags.indexOf(languageTag), 1);
  }

  /**
   * Choose the default language tag amongst supported tags.
   * @param languageTag The default language to set
   */
  public chooseDefaultLanguageTag(languageTag: string): void {

    this.defaultLanguageTag = languageTag;
    this.defaultLanguageTagChange.emit(this.defaultLanguageTag);

    this.closeOpenedPopover();
  }

  /**
   * Remove the chosen language tag.
   * @param languageTag The language tag to remove
   */
  public removeLanguageTag(languageTag: string): void {

    // Remove the language tag from the list of supported tags
    this.supportedLanguageTags = this.supportedLanguageTags.filter((tag, i, tags) => tag != languageTag);
    this.supportedLanguageTagsChange.emit(this.supportedLanguageTags);

    // Add the language tag to the list of available tags
    this.availableLanguageTags.push(languageTag);

    this.closeOpenedPopover();
  }

  /**
   * Close the opened popover when the user presses the 'Esc' key.
   * @param event Key event
   */
  @HostListener('keyup.esc', ['$event'])
  public closePopoverFromEsc(event: any): void {

    // Close popover if Esc is presses inside the component
    if (this.el.nativeElement.contains(event.target)) {
      this.closeOpenedPopover();
    }
  }

  /**
   * Close the opened popover when the user clicks occurs outside of the component.
   * @param event Mouse event
   */
  @HostListener('document:click', ['$event'])
  public closePopoverFromOutsideClick(event: any): void {

    // Close popover if the mouse click occurs outside of the component
    if (!this.el.nativeElement.contains(event.target)) {
      this.closeOpenedPopover();
    }
  }

  /**
   * Indicate if the language selection is valid.
   * @returns {boolean} {@code true} if a default language tag is defined, else {@code false}
   */
  public get valid(): boolean {

    if (this.defaultLanguageTag)
      return true;
    else
      return false;
  }


  /**
   * Close the currently opened popover.
   */
  private closeOpenedPopover() {

    let openedPopover = this.popovers.find((popover, i, popovers) => popover.isOpen());
    if (openedPopover)
      openedPopover.close();
  }
}
