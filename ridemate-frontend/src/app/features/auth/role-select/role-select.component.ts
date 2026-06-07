import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router }       from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { AuthService }  from '../../../core/services/auth.service';
import { UserMode }     from '../../../core/models/auth.model';

@Component({
  selector: 'app-role-select',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule],
  templateUrl: './role-select.component.html',
  styleUrls:   ['./role-select.component.scss']
})
export class RoleSelectComponent {

  constructor(private auth: AuthService, private router: Router) {
    // If already has a mode, go straight to dashboard
    if (this.auth.getUserMode()) {
      this.router.navigate(['/dashboard']);
    }
  }

  select(mode: UserMode): void {
    this.auth.setUserMode(mode);
    this.router.navigate(['/dashboard']);
  }
}
