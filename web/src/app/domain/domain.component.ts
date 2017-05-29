import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Params} from "@angular/router";
import 'rxjs/add/operator/switchMap';
import {Domain} from "../dto/domain";
import {DomainService} from "../service/domain.service";
import {KeyMessage} from "../dto/key-message";
import {KeyMessageService} from "../service/key-message.service";

@Component({
  selector: 'app-domain',
  templateUrl: './domain.component.html',
  styleUrls: ['./domain.component.css']
})
export class DomainComponent implements OnInit {

  public domain: Domain;

  public editableKeyMessage: KeyMessage;
  public messageEditable: boolean;
  public messages: KeyMessage[];

  constructor(private route: ActivatedRoute,
              private domainService: DomainService,
              private keyMessageService: KeyMessageService) {
  }

  ngOnInit() {
    this.route.params
      .switchMap(params => {
        if (params['id'])
          return this.domainService.getDomain(params['id']);
        else
          return Promise.resolve(new Domain());
      })
      .subscribe(domain => {
        this.domain = domain;
      });
  }

  searchMessages(): void {
    this.keyMessageService.findMessages(this.domain.id, this.domain.defaultLanguageTag)
      .then(messages => this.messages = messages);
  }

  displayNewMessage(): void {
    this.messageEditable = true;
    this.editableKeyMessage = new KeyMessage();
  }
}
