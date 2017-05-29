import { TestBed, inject } from '@angular/core/testing';

import { KeyMessageService } from './key-message.service';

describe('KeyMessageService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [KeyMessageService]
    });
  });

  it('should ...', inject([KeyMessageService], (service: KeyMessageService) => {
    expect(service).toBeTruthy();
  }));
});
