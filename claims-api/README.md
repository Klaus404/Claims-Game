# Claims API Bruno Collection

Open the `claims-api` directory in Bruno and select the `local` environment.

After creating a game, copy its `joinCode` and owner player `id` into `gameCode` and `ownerId`. Use the player IDs from the game response in the round request bodies. Set `roundId` from the response returned by `Create Round`.

The collection targets the backend directly at `http://localhost:8080` by default. Change `baseUrl` when using another host.
