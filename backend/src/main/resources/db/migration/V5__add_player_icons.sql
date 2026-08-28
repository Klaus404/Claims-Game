ALTER TABLE players ADD COLUMN icon VARCHAR(30);
UPDATE players SET icon = CASE playing_order % 10
    WHEN 0 THEN 'hedgehog'
    WHEN 1 THEN 'sloth'
    WHEN 2 THEN 'tiger'
    WHEN 3 THEN 'parrot'
    WHEN 4 THEN 'elephant'
    WHEN 5 THEN 'fox'
    WHEN 6 THEN 'chick'
    WHEN 7 THEN 'dog'
    WHEN 8 THEN 'turtle'
    ELSE 'lion'
END
WHERE icon IS NULL;
ALTER TABLE players ALTER COLUMN icon SET NOT NULL;
