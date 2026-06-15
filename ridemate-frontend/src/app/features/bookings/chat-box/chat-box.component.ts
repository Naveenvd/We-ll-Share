import {
  Component, OnInit, OnDestroy, Input,
  ViewChild, ElementRef, AfterViewChecked
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { Subscription } from 'rxjs';

import { ApiService } from '../../../core/services/api.service';
import { WebSocketService } from '../../../core/services/websocket.service';
import { ChatMessage } from '../../../core/models/booking.model';

@Component({
  selector: 'app-chat-box',
  standalone: true,
  imports: [
    CommonModule, FormsModule,
    MatCardModule, MatButtonModule, MatIconModule,
    MatInputModule, MatFormFieldModule
  ],
  templateUrl: './chat-box.component.html',
  styleUrls: ['./chat-box.component.scss']
})
export class ChatBoxComponent implements OnInit, AfterViewChecked, OnDestroy {

  @Input({ required: true }) bookingId!: number;
  @Input({ required: true }) currentUserId!: number;

  @ViewChild('messagesEnd') private messagesEnd!: ElementRef;

  messages: ChatMessage[] = [];
  newText  = '';
  sending  = false;

  private wsSub!: Subscription;
  private scrollNeeded = false;

  constructor(
    private api: ApiService,
    private ws:  WebSocketService
  ) {}

  ngOnInit(): void {
    this.api.getChatHistory(this.bookingId).subscribe({
      next: msgs => {
        this.messages = msgs;
        this.scrollNeeded = true;
      }
    });

    this.api.markChatRead(this.bookingId).subscribe();

    this.wsSub = this.ws.subscribe<ChatMessage>(
      `/topic/booking.${this.bookingId}`
    ).subscribe(msg => {
      this.messages.push(msg);
      if (msg.senderId !== this.currentUserId) {
        this.api.markChatRead(this.bookingId).subscribe();
      }
      this.scrollNeeded = true;
    });
  }

  ngAfterViewChecked(): void {
    if (this.scrollNeeded) {
      this.scrollToBottom();
      this.scrollNeeded = false;
    }
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
  }

  send(): void {
    const text = this.newText.trim();
    if (!text || this.sending) return;
    this.sending = true;
    this.newText = '';
    this.ws.publish(`/app/chat.booking.${this.bookingId}`, { text });
    this.sending = false;
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  isMine(msg: ChatMessage): boolean {
    return msg.senderId === this.currentUserId;
  }

  private scrollToBottom(): void {
    try {
      this.messagesEnd.nativeElement.scrollIntoView({ behavior: 'smooth' });
    } catch { /* element not yet rendered */ }
  }
}
