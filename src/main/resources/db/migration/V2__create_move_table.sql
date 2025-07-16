CREATE TABLE IF NOT EXISTS game (
                        id UUID PRIMARY KEY,
                        game_id UUID NOT NULL,
                        player VARCHAR(5) NOT NULL,
                        x_axis INT NOT NULL,
                        y_axis INT NOT NULL,
                        created_at TIMESTAMP NOT NULL
);