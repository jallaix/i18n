import { Injectable } from '@angular/core';
import {Domain} from "../dto/domain";

const DOMAINS:Domain[] = [{
  id:'1',
  code:'I18N',
  description:'I18n domain',
  defaultLanguageTag: 'en',
  availableLanguageTags:['en', 'fr']
}];

@Injectable()
export class DomainService {

  constructor() { }

  getDomains():Promise<Domain[]> {
    return Promise.resolve(DOMAINS);
  }

  getDomain(id:string):Promise<Domain> {
    return Promise.resolve(DOMAINS[0]);
  }
}
