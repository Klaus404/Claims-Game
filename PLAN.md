# Claims Scorekeeper Plan

## Scope

- Angular frontend using the latest available Angular release.
- Spring Boot backend using Java 25.
- PostgreSQL persistence with Flyway migrations.
- Games support 2 to 6 players.
- Players join through a generated game code; accounts are not required.
- In-progress games survive application restarts.

## Backend First

1. Define the game domain: `Game`, `Player`, `Round`, and `RoundPlayerScore`.
2. Add database migrations and repository persistence.
3. Implement game creation, joining, starting, reconnection, and game completion.
4. Implement transactional round scoring and claims resolution.
5. Expose REST APIs for games, rounds, scores, and history.
6. Add validation and consistent error responses.
7. Test normal scoring, claims, ties, star streaks, elimination, and winner detection.

## Scoring Rules

- Each player submits the points in their five-card hand.
- A player can call claims when their hand is below 10 points.
- If the caller has the lowest hand, everyone receives their hand total and the caller receives `*`.
- If another player has fewer points, the caller receives 50 points, the lowest player receives `*`, and everyone else receives 0.
- When the lowest score is tied, the next tied player in playing order receives `*`.
- A third consecutive `*` immediately applies a 50-point deduction.
- A round where a player does not receive `*` resets that player's star streak.
- A player reaching 200 or more points is eliminated.
- The last remaining player wins.

## REST API

- `POST /api/games` - create a game.
- `POST /api/games/{code}/players` - join a game.
- `GET /api/games/{code}` - retrieve current game state.
- `POST /api/games/{code}/start` - start a game.
- `POST /api/games/{code}/rounds` - submit a round's hand totals.
- `POST /api/games/{code}/rounds/{roundId}/resolve` - resolve the round and claim.
- `GET /api/games/{code}/rounds` - retrieve round history.

## Frontend

1. Create and join game screens.
2. Lobby with player order and start control.
3. Round entry and claim declaration screens.
4. Live scoreboard with points, star streaks, and eliminated players.
5. Round history and game-over winner screen.

## Verification

- Backend unit and integration tests.
- Angular component and service tests.
- Production frontend build.
- Full-stack local run with Docker Compose.
