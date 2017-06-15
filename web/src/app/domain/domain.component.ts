import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import 'rxjs/add/operator/switchMap';
import {Domain} from "../dto/domain";
import {DomainService} from "../service/domain.service";
import {KeyMessage} from "../dto/key-message";
import {KeyMessageService} from "../service/key-message.service";

/**
 * The domain component manages CRUD operations about basic domain properties as well as its linked key and entity messages.
 */
@Component({
  selector: 'app-domain',
  templateUrl: './domain.component.html',
  styleUrls: ['./domain.component.css']
})
export class DomainComponent implements OnInit {

  /**
   * Domain managed by the component
   */
  public domain: Domain;

  // TODO To refactor
  public editableKeyMessage: KeyMessage;
  public messageEditable: boolean;
  public messages: KeyMessage[];


  /**
   * Constructor with injected services.
   * @param route The activated route
   * @param domainService The service that manages domain entities
   * @param keyMessageService The service that manages key message entities
   */
  constructor(private route: ActivatedRoute,
              private domainService: DomainService,
              private keyMessageService: KeyMessageService) {
  }

  /**
   * When the component is initialized, get the domain to display from the activated route or show an empty one.
   */
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

  // TODO refactor
  searchMessages(): void {
    this.keyMessageService.findMessages(this.domain.id, this.domain.defaultLanguageTag)
      .then(messages => this.messages = messages);
  }

  // TODO refactor
  displayNewMessage(): void {
    this.messageEditable = true;
    this.editableKeyMessage = new KeyMessage();
  }
}
