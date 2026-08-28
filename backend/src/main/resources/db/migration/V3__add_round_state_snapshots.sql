ALTER TABLE round_player_scores ADD COLUMN previous_total_score INTEGER;
ALTER TABLE round_player_scores ADD COLUMN previous_star_streak INTEGER;
ALTER TABLE round_player_scores ADD COLUMN previous_eliminated BOOLEAN;
