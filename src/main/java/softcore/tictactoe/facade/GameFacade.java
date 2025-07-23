package softcore.tictactoe.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import softcore.tictactoe.api.model.GameDetailsResponse;
import softcore.tictactoe.api.model.MakeMoveCommand;
import softcore.tictactoe.domain.model.dto.GameDto;
import softcore.tictactoe.domain.service.GameService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GameFacade {

    private final GameService gameService;

    public GameDto createNewGame() {
        return gameService.createNewGame();
    }

    public GameDetailsResponse makeMove(MakeMoveCommand command) {
        GameDto game = gameService.makeMove(command);
        return GameDetailsResponse.from(game);
    }

    public GameDetailsResponse getGameDetails(UUID gameId) {
        GameDto game = gameService.getGame(gameId);
        return GameDetailsResponse.from(game);
    }
}
