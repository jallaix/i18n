import {Injectable} from '@angular/core';
import {Domain} from "../dto/domain";

let domains: Domain[] = [{
  id: '1',
  code: 'I18N',
  description: 'I18n domain',
  defaultLanguageTag: 'en',
  supportedLanguageTags: ['en', 'fr']
}];

@Injectable()
export class DomainService {

  constructor() {
  }

  getDomains(): Promise<Domain[]> {
    return Promise.resolve(domains);
  }

  getDomain(id: string): Promise<Domain> {
    return Promise.resolve(domains.find(domain => domain.id == id));
  }

  saveDomain(domainToSave: Domain): Promise<Domain> {

    if (!domainToSave.id) {
      // Mock generated id
      let max = domains.map(domain => +domain.id).reduce((previous, current) => Math.max(previous, current));
      domainToSave.id = "" + (max + 1);

      domains.push(domainToSave);
    }
    else
      domains[domains.findIndex((domain, index, domains) => domain.id == domainToSave.id)] = domainToSave;

    return Promise.resolve(domainToSave);
  }
}
