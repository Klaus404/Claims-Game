# Claims Game

Claims is a scorekeeper for the card game. It provides a Dockerized Angular frontend, a Spring Boot backend running on Java 25, and PostgreSQL persistence.

## Rules

- Each player has five cards.
- After dealing, place one card face up. Its value must be greater than or equal to 7. This is the trump card and is worth 0 points.
- Place another card face down to begin the draw deck.
- Players take turns dropping one card from their hand.
- On a turn, a player may take either the last card dropped by the previous player or the top card from the face-down deck.
- The card taken replaces the card dropped, keeping five cards in the player's hand.
- A player may call `claims` when their hand has fewer than 10 points.
- When claims is called, everybody reveals their hand.
- If the caller has the lowest hand:
  - Everybody receives the points in their hand.
  - The caller receives a `*`.
- If another player has fewer points than the caller:
  - The caller receives 50 points.
  - The player with the lowest hand receives a `*`.
  - Everybody else receives 0 points.
- If the lowest hand is tied, the next tied player in playing order receives the `*`.
- A player who receives a third consecutive `*` immediately loses 50 points.
- A round where a player does not receive a `*` resets that player's streak.
- A player who reaches exactly 200 points wins the game.
- A player who goes above 200 points is eliminated.
- If elimination leaves one player, that player wins.
- Games support 2 to 6 players.

## Ownership And Bots

- The player who creates a game is its owner.
- Only the owner can start a game, add bots, enter hand totals, and resolve rounds.
- Human players can join using the game code.
- The owner can add named bot players so a game can start without every player connecting.
- Game and player state is persisted in PostgreSQL.

## Run With Docker

Requirements: Docker and Docker Compose.

```bash
docker compose up --build
```

Open the frontend at <http://localhost:4200>.

The backend is available at <http://localhost:8080> and PostgreSQL at port `5432`.

To stop the stack:

```bash
docker compose down
```

The named PostgreSQL volume preserves game data between restarts. Remove it only when you intentionally want a clean database:

```bash
docker compose down -v
```

## Local Development

Backend:

```bash
cd backend
./gradlew test
./gradlew bootRun
```

Frontend:

```bash
cd frontend
npm install
npm run build
npm start
```

The frontend uses `/api` and expects the backend to be available on port `8080` during local development.

## API Overview

- `POST /api/games` - create a game.
- `POST /api/games/{code}/players` - join a game.
- `POST /api/games/{code}/bots` - add a bot as the owner.
- `GET /api/games/{code}` - retrieve game state.
- `POST /api/games/{code}/start` - start a game as the owner.
- `POST /api/games/{code}/rounds` - enter a round as the owner.
- `POST /api/games/{code}/rounds/{roundId}/resolve` - resolve a round as the owner.
- `GET /api/games/{code}/rounds` - retrieve round history.

Owner-only requests use the `X-Player-Id` header for the creating player's ID.
