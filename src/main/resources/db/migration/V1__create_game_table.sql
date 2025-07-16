CREATE TABLE IF NOT EXISTS game (
                        id UUID PRIMARY KEY,
                        status VARCHAR(20) NOT NULL,
                        created_at TIMESTAMP NOT NULL,
                        updated_at TIMESTAMP,
                        player_turn VARCHAR(5) NOT NULL
);