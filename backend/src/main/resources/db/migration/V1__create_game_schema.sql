CREATE TABLE games (
    id UUID PRIMARY KEY,
    join_code VARCHAR(6) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE players (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL REFERENCES games(id),
    name VARCHAR(30) NOT NULL,
    playing_order INTEGER NOT NULL,
    total_score INTEGER NOT NULL DEFAULT 0,
    star_streak INTEGER NOT NULL DEFAULT 0,
    eliminated BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (game_id, playing_order)
);

CREATE TABLE rounds (
    id UUID PRIMARY KEY,
    game_id UUID NOT NULL REFERENCES games(id),
    claimer_id UUID REFERENCES players(id),
    round_number INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    UNIQUE (game_id, round_number)
);

CREATE TABLE round_player_scores (
    id UUID PRIMARY KEY,
    round_id UUID NOT NULL REFERENCES rounds(id),
    player_id UUID NOT NULL REFERENCES players(id),
    hand_points INTEGER NOT NULL,
    awarded_points INTEGER NOT NULL DEFAULT 0,
    received_star BOOLEAN NOT NULL DEFAULT FALSE,
    star_penalty INTEGER NOT NULL DEFAULT 0,
    UNIQUE (round_id, player_id)
);
