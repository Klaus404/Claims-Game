ALTER TABLE games ADD COLUMN owner_id UUID;
ALTER TABLE games ADD COLUMN winner_id UUID;
ALTER TABLE players ADD COLUMN bot BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE games g
SET owner_id = (
    SELECT p.id FROM players p
    WHERE p.game_id = g.id
    ORDER BY p.playing_order
    LIMIT 1
)
WHERE g.owner_id IS NULL;
