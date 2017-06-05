import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DomainHeaderLanguagesComponent } from './domain-header-languages.component';

describe('DomainHeaderLanguagesComponent', () => {
  let component: DomainHeaderLanguagesComponent;
  let fixture: ComponentFixture<DomainHeaderLanguagesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DomainHeaderLanguagesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DomainHeaderLanguagesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
