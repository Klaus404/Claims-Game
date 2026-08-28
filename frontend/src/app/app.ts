import { Component, computed, inject, OnDestroy, signal } from '@angular/core';
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
  eliminationOrder: number | null;
  bot: boolean;
}

@Component({
  selector: 'app-root',
  imports: [FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnDestroy {
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
  readonly editingLastRound = signal(false);
  readonly spectating = signal(false);
  private syncing = false;
  private readonly syncTimer = window.setInterval(() => this.syncGame(), 3000);
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
    eliminationOrder: player.eliminationOrder,
    bot: player.bot,
  })));
  readonly round = computed(() => (this.history().length || 0) + 1);
  readonly isLobby = computed(() => this.game()?.status === 'WAITING');
  readonly isOwner = computed(() => this.game()?.ownerId === this.currentPlayerId());
  readonly winnerName = computed(() => this.players().find(player => player.id === this.game()?.winnerId)?.name ?? 'The table winner');
  readonly currentPlayer = computed(() => this.players().find(player => player.id === this.currentPlayerId()));
  readonly isEliminated = computed(() => this.currentPlayer()?.eliminated ?? false);
  readonly leaderboard = computed(() => this.players().slice().sort((a, b) => {
    if (a.id === this.game()?.winnerId) return -1;
    if (b.id === this.game()?.winnerId) return 1;
    if (a.eliminated !== b.eliminated) return a.eliminated ? 1 : -1;
    return (b.eliminationOrder ?? 0) - (a.eliminationOrder ?? 0);
  }));

  constructor() {
    const savedCode = localStorage.getItem('claims-game-code');
    if (savedCode) this.loadGame(savedCode);
  }

  ngOnDestroy(): void {
    window.clearInterval(this.syncTimer);
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

  async endGame(): Promise<void> {
    const game = this.game();
    if (!game || !confirm('End this game? The active player with the lowest score will win.')) return;
    await this.request(() => firstValueFrom(this.api.end(game.joinCode)));
    this.message.set('Game ended. The winner has been recorded.');
  }

  async leaveRoom(): Promise<void> {
    const game = this.game();
    if (!game || !confirm('Leave this room?')) return;
    try { await firstValueFrom(this.api.leave(game.joinCode)); } catch { /* The local session can still be cleared if the room is unavailable. */ }
    localStorage.removeItem('claims-game-code');
    localStorage.removeItem('claims-player-id');
    this.currentPlayerId.set(''); this.game.set(null); this.history.set([]); this.hands.set({}); this.claimant.set(null); this.message.set('');
  }

  async submitRound(): Promise<void> {
    const game = this.game();
    if (!game) return;
    const scores = this.players().filter(player => !player.eliminated).map(player => ({ playerId: player.id, handPoints: player.hand }));
    if (scores.some(score => score.handPoints < 0)) return this.error.set('Hand points cannot be negative.');
    this.loading.set(true); this.error.set('');
    try {
      const openRound = this.history().find(round => round.status === 'OPEN');
      if (this.editingLastRound()) {
        await firstValueFrom(this.api.editLastRound(game.joinCode, scores, this.claimant()));
      } else if (openRound) {
        if (!openRound.id) return this.error.set('The current round is missing its ID. Refresh the game and try again.');
        await firstValueFrom(this.api.resolveRound(game.joinCode, openRound.id));
      } else {
        const round = await firstValueFrom(this.api.createRound(game.joinCode, scores, this.claimant()));
        if (!round.id) return this.error.set('The round was saved but did not return an ID. Refresh the game and try again.');
        await firstValueFrom(this.api.resolveRound(game.joinCode, round.id));
      }
      await this.refresh(game.joinCode);
      this.message.set('Round resolved and scoreboard synced.');
      this.claimant.set(null);
      this.hands.set({});
      this.editingLastRound.set(false);
    } catch (error) { this.setError(error); } finally { this.loading.set(false); }
  }

  updateHand(id: string, event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = Number(input.value);
    if (!Number.isFinite(value) || value < 0) {
      input.value = '0';
      this.error.set('Hand points must be 0 or greater.');
      return;
    }
    this.error.set('');
    this.hands.update(hands => ({ ...hands, [id]: value }));
  }

  toggleClaim(): void { this.claimant.update(current => current ? null : this.currentPlayerId() || this.players()[0]?.id || null); }
  setClaimant(event: Event): void { this.claimant.set((event.target as HTMLSelectElement).value); }

  async copyCode(): Promise<void> {
    const code = this.game()?.joinCode ?? '';
    await navigator.clipboard?.writeText(code);
    this.copied.set(true); window.setTimeout(() => this.copied.set(false), 1800);
  }

  beginEditLastRound(): void {
    const last = this.history()[this.history().length - 1];
    if (!last) return;
    this.hands.set(Object.fromEntries(last.scores.map(score => [score.playerId, score.handPoints])));
    this.claimant.set(last.claimerId);
    this.editingLastRound.set(true);
    this.message.set('Edit the hand values, then save the round.');
  }

  spectateGame(): void { this.spectating.set(true); }

  async clearLastRound(): Promise<void> {
    const game = this.game();
    if (!game || !this.history().length || !confirm('Clear the last round and restore the previous scores?')) return;
    await this.request(() => firstValueFrom(this.api.clearLastRound(game.joinCode)));
    this.hands.set({});
    this.claimant.set(null);
    this.editingLastRound.set(false);
    this.message.set('Last round cleared.');
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

  private async syncGame(): Promise<void> {
    if (this.syncing || this.loading()) return;
    const code = this.game()?.joinCode ?? localStorage.getItem('claims-game-code');
    if (!code) return;

    this.syncing = true;
    try {
      await this.refresh(code);
    } catch {
      // Background sync retries on the next interval without interrupting the current UI.
    } finally {
      this.syncing = false;
    }
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
    const response = error as { error?: { error?: string; message?: string } | string };
    const detail = typeof response.error === 'string' ? response.error : response.error?.error ?? response.error?.message;
    this.error.set(detail ?? 'Could not reach the game server.');
  }
}
