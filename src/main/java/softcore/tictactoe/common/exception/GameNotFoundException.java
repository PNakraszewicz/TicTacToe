package softcore.tictactoe.common.exception;

import java.util.UUID;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(UUID id) {
        super("Game not found: " + id);
    }
}
