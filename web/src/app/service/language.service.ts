import { Injectable } from '@angular/core';

const AVAILABLE_LANGUAGE_TAGS = ["en", "es", "de", "fr", "zh"];

@Injectable()
export class LanguageService {

  constructor() { }

  findLanguageTags():Promise<string[]> {
    return Promise.resolve(AVAILABLE_LANGUAGE_TAGS);
  }
}
