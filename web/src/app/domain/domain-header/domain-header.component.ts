import {Component, Input, OnInit} from '@angular/core';
import {Domain} from "../../dto/domain";

@Component({
  selector: 'app-domain-header',
  templateUrl: './domain-header.component.html',
  styleUrls: ['./domain-header.component.css']
})
export class DomainHeaderComponent implements OnInit {

  @Input()
  public domain: Domain;

  public editable: boolean;

  ngOnInit() {
    if (this.domain.id)
      this.editable = false;
    else
      this.editable = true;
  }

  editDomain(): void {
    this.editable = true;
  }

  submitDomain(): void {
    this.editable = false;
  }
}
