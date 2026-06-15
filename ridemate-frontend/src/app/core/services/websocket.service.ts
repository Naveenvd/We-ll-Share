import { Injectable, OnDestroy } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Observable, Subject } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

/**
 * Thin wrapper around @stomp/stompjs that:
 *  - Authenticates via JWT passed as a STOMP connect header (read lazily
 *    at each (re)connect via beforeConnect — fixes the "token captured at
 *    construction time" bug for post-login sessions)
 *  - Connects lazily when the first subscription is requested
 *  - Stores the StompSubscription returned by client.subscribe() and
 *    calls stompSub.unsubscribe() when the Angular consumer unsubscribes —
 *    prevents accumulating dangling broker subscriptions (B4 fix)
 *  - Disconnects when the service is destroyed (app teardown)
 */
@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {

  private client!: Client;
  private connected = false;
  private connectCallbacks: (() => void)[] = [];

  constructor(private auth: AuthService) {
    this.initClient();
  }

  // ── Initialise STOMP client ──────────────────────────────────────

  private initClient(): void {
    this.client = new Client({
      // SockJS factory — called each (re)connect
      webSocketFactory: () => new SockJS(environment.wsUrl),

      // B5 fix: start with empty headers; beforeConnect fills them fresh
      // so that a post-login WebSocket connection gets the correct JWT.
      connectHeaders: {},

      reconnectDelay: 5000,

      // B5 fix: read the token lazily right before each connection attempt
      beforeConnect: async () => {
        const token = this.auth.getToken();
        this.client.connectHeaders = { Authorization: `Bearer ${token ?? ''}` };
      },

      onConnect: () => {
        this.connected = true;
        // Flush any pending subscription callbacks
        this.connectCallbacks.forEach(cb => cb());
        this.connectCallbacks = [];
      },

      onStompError: (frame) => {
        console.error('[WS] STOMP error:', frame.headers['message']);
      }
    });
  }

  // ── Connect / disconnect ─────────────────────────────────────────

  connect(): void {
    if (!this.client.active) {
      this.client.activate();
    }
  }

  disconnect(): void {
    if (this.client.active) {
      this.client.deactivate();
      this.connected = false;
    }
  }

  // ── Subscribe to a topic and get an Observable ───────────────────

  /**
   * @param topic  e.g. '/topic/booking.42'
   * @returns Observable<T> that emits parsed JSON payloads.
   *
   * B4 fix: the returned Observable's teardown logic calls
   * stompSub.unsubscribe() so the STOMP broker subscription is
   * released when the Angular component unsubscribes — no leak.
   */
  subscribe<T>(topic: string): Observable<T> {
    const subject = new Subject<T>();
    let stompSub: StompSubscription | undefined;

    const doSubscribe = () => {
      stompSub = this.client.subscribe(topic, (msg: IMessage) => {
        try {
          subject.next(JSON.parse(msg.body) as T);
        } catch {
          console.error('[WS] JSON parse error:', msg.body);
        }
      });
    };

    if (this.connected) {
      doSubscribe();
    } else {
      // Queue until connected
      this.connectCallbacks.push(doSubscribe);
      this.connect();
    }

    // Wrap in a proper Observable so that unsubscribe() tears down
    // both the RxJS chain and the STOMP broker subscription.
    return new Observable<T>(observer => {
      const rxSub = subject.subscribe(observer);
      return () => {
        rxSub.unsubscribe();
        subject.complete();
        stompSub?.unsubscribe();   // B4: release server-side subscription
      };
    });
  }

  // ── Publish a message ────────────────────────────────────────────

  /**
   * @param destination  e.g. '/app/chat.booking.42'
   * @param body         Object to JSON-serialise and send
   */
  publish(destination: string, body: object): void {
    if (!this.connected) {
      this.connect();
      this.connectCallbacks.push(() =>
        this.client.publish({ destination, body: JSON.stringify(body) })
      );
    } else {
      this.client.publish({ destination, body: JSON.stringify(body) });
    }
  }

  // ── Cleanup ──────────────────────────────────────────────────────

  ngOnDestroy(): void {
    this.disconnect();
  }
}
