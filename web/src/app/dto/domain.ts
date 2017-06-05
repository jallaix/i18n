export class Domain {

  constructor(public id?: string,
              public code?: string,
              public description?: string,
              public defaultLanguageTag?: string,
              public supportedLanguageTags?: string[]) {
  }
}
