import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import { GameApiService, GameResponse, RoundResponse } from './game-api.service';

interface PlayerView {
  id: string;
  name: string;
  initials: string;
  score: number;
  hand: number;
  streak: number;
  color: string;
  current: boolean;
  eliminated: boolean;
  bot: boolean;
}

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly api = inject(GameApiService);
  readonly game = signal<GameResponse | null>(null);
  readonly history = signal<RoundResponse[]>([]);
  readonly playerName = signal('');
  readonly botName = signal('');
  readonly joinCodeInput = signal('');
  readonly currentPlayerId = signal(localStorage.getItem('claims-player-id') ?? '');
  readonly copied = signal(false);
  readonly claimant = signal<string | null>(null);
  readonly hands = signal<Record<string, number>>({});
  readonly loading = signal(false);
  readonly error = signal('');
  readonly message = signal('');
  readonly players = computed<PlayerView[]>(() => (this.game()?.players ?? []).map((player, index) => ({
    id: player.id,
    name: player.name,
    initials: player.name.slice(0, 2).toUpperCase(),
    score: player.totalScore,
    hand: this.hands()[player.id] ?? 0,
    streak: player.starStreak,
    color: ['#d9a84e', '#e87e62', '#7f9be8', '#9cbf78', '#ca83bc', '#7db9b0'][index % 6],
    current: player.id === this.currentPlayerId(),
    eliminated: player.eliminated,
    bot: player.bot,
  })));
  readonly round = computed(() => (this.history().length || 0) + 1);
  readonly isLobby = computed(() => this.game()?.status === 'WAITING');
  readonly isOwner = computed(() => this.game()?.ownerId === this.currentPlayerId());

  constructor() {
    const savedCode = localStorage.getItem('claims-game-code');
    if (savedCode) this.loadGame(savedCode);
  }

  async createGame(): Promise<void> {
    if (!this.playerName().trim()) return this.error.set('Enter your name first.');
    await this.request(() => firstValueFrom(this.api.create(this.playerName().trim())), true);
  }

  async joinGame(): Promise<void> {
    if (!this.playerName().trim() || !this.joinCodeInput().trim()) return this.error.set('Enter your name and game code.');
    await this.request(() => firstValueFrom(this.api.join(this.joinCodeInput().trim(), this.playerName().trim())), true);
  }

  async startGame(): Promise<void> {
    if (this.game()) await this.request(() => firstValueFrom(this.api.start(this.game()!.joinCode)));
  }

  async addBot(): Promise<void> {
    const game = this.game();
    if (!game || !this.botName().trim()) return this.error.set('Enter a name for the bot.');
    await this.request(() => firstValueFrom(this.api.addBot(game.joinCode, this.botName().trim())));
    this.botName.set('');
  }

  async submitRound(): Promise<void> {
    const game = this.game();
    if (!game) return;
    const scores = this.players().filter(player => !player.eliminated).map(player => ({ playerId: player.id, handPoints: player.hand }));
    if (scores.some(score => score.handPoints < 0)) return this.error.set('Hand points cannot be negative.');
    this.loading.set(true); this.error.set('');
    try {
      const openRound = this.history().find(round => round.status === 'OPEN');
      if (openRound) {
        await firstValueFrom(this.api.resolveRound(game.joinCode, openRound.id));
      } else {
        const round = await firstValueFrom(this.api.createRound(game.joinCode, scores, this.claimant()));
        await firstValueFrom(this.api.resolveRound(game.joinCode, round.id));
      }
      await this.refresh(game.joinCode);
      this.message.set('Round resolved and scoreboard synced.');
      this.claimant.set(null);
      this.hands.set({});
    } catch (error) { this.setError(error); } finally { this.loading.set(false); }
  }

  updateHand(id: string, event: Event): void {
    const value = Math.max(0, Number((event.target as HTMLInputElement).value) || 0);
    this.hands.update(hands => ({ ...hands, [id]: value }));
  }

  toggleClaim(): void { this.claimant.update(current => current ? null : this.currentPlayerId() || this.players()[0]?.id || null); }
  setClaimant(event: Event): void { this.claimant.set((event.target as HTMLSelectElement).value); }

  async copyCode(): Promise<void> {
    const code = this.game()?.joinCode ?? '';
    await navigator.clipboard?.writeText(code);
    this.copied.set(true); window.setTimeout(() => this.copied.set(false), 1800);
  }

  private async loadGame(code: string): Promise<void> {
    await this.request(() => firstValueFrom(this.api.get(code)));
    await this.refresh(code);
  }

  private async refresh(code: string): Promise<void> {
    const [game, history] = await Promise.all([firstValueFrom(this.api.get(code)), firstValueFrom(this.api.history(code))]);
    this.game.set(game); this.history.set(history);
    localStorage.setItem('claims-game-code', game.joinCode);
    const knownPlayer = game.players.find(player => player.id === this.currentPlayerId());
    if (knownPlayer) localStorage.setItem('claims-player-id', knownPlayer.id);
  }

  private async request(request: () => Promise<GameResponse>, identify = false): Promise<void> {
    this.loading.set(true); this.error.set('');
    try {
      const game = await request();
      this.game.set(game);
      if (identify) {
        const player = game.players.find(candidate => candidate.name.toLowerCase() === this.playerName().trim().toLowerCase());
        if (player) { this.currentPlayerId.set(player.id); localStorage.setItem('claims-player-id', player.id); }
      }
      localStorage.setItem('claims-game-code', game.joinCode);
      this.history.set(await firstValueFrom(this.api.history(game.joinCode)));
    } catch (error) { this.setError(error); } finally { this.loading.set(false); }
  }

  private setError(error: unknown): void {
    const response = error as { error?: { error?: string } };
    this.error.set(response.error?.error ?? 'Could not reach the game server.');
  }
}
