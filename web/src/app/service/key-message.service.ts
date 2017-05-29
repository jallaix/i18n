import {Injectable} from '@angular/core';
import {KeyMessage} from "../dto/key-message";

const KEY_MESSAGES: KeyMessage[] = [
  {
    id: '1',
    domainId: '1',
    key: 'test.one',
    languageTag: 'en',
    content: 'Test One'
  },
  {
    id: '2',
    domainId: '1',
    key: 'test.two',
    languageTag: 'en',
    content: 'Test Two'
  }];

@Injectable()
export class KeyMessageService {

  constructor() {
  }

  findMessages(domainId:string, languageTag:string, key?:string, content?:string):Promise<KeyMessage[]> {
    return Promise.resolve(KEY_MESSAGES);
  }
}
