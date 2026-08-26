import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PlayerResponse {
  id: string;
  name: string;
  playingOrder: number;
  totalScore: number;
  starStreak: number;
  eliminated: boolean;
  bot: boolean;
}

export interface GameResponse {
  id: string;
  joinCode: string;
  createdAt: string;
  status: 'WAITING' | 'IN_PROGRESS' | 'FINISHED';
  ownerId: string;
  players: PlayerResponse[];
  winnerId: string | null;
}

export interface RoundScoreResponse {
  playerId: string;
  playerName: string;
  handPoints: number;
  awardedPoints: number;
  receivedStar: boolean;
  starPenalty: number;
}

export interface RoundResponse {
  id: string;
  roundNumber: number;
  createdAt: string;
  status: 'OPEN' | 'RESOLVED';
  claimerId: string | null;
  scores: RoundScoreResponse[];
}

@Injectable({ providedIn: 'root' })
export class GameApiService {
  private readonly http = inject(HttpClient);

  create(playerName: string): Observable<GameResponse> {
    return this.http.post<GameResponse>('/api/games', { playerName });
  }

  join(code: string, playerName: string): Observable<GameResponse> {
    return this.http.post<GameResponse>(`/api/games/${encodeURIComponent(code)}/players`, { playerName });
  }

  get(code: string): Observable<GameResponse> {
    return this.http.get<GameResponse>(`/api/games/${encodeURIComponent(code)}`);
  }

  start(code: string): Observable<GameResponse> {
    return this.http.post<GameResponse>(`/api/games/${encodeURIComponent(code)}/start`, {}, { headers: { 'X-Player-Id': this.playerId() } });
  }

  addBot(code: string, name: string): Observable<GameResponse> {
    return this.http.post<GameResponse>(`/api/games/${encodeURIComponent(code)}/bots`, { name }, { headers: { 'X-Player-Id': this.playerId() } });
  }

  createRound(code: string, scores: { playerId: string; handPoints: number }[], claimerId: string | null): Observable<RoundResponse> {
    return this.http.post<RoundResponse>(`/api/games/${encodeURIComponent(code)}/rounds`, { scores, claimerId }, { headers: { 'X-Player-Id': this.playerId() } });
  }

  resolveRound(code: string, roundId: string): Observable<RoundResponse> {
    return this.http.post<RoundResponse>(`/api/games/${encodeURIComponent(code)}/rounds/${roundId}/resolve`, {}, { headers: { 'X-Player-Id': this.playerId() } });
  }

  history(code: string): Observable<RoundResponse[]> {
    return this.http.get<RoundResponse[]>(`/api/games/${encodeURIComponent(code)}/rounds`);
  }

  private playerId(): string { return localStorage.getItem('claims-player-id') ?? ''; }
}
