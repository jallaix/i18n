import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DomainHeaderComponent } from './domain-header.component';

describe('DomainHeaderComponent', () => {
  let component: DomainHeaderComponent;
  let fixture: ComponentFixture<DomainHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DomainHeaderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DomainHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
