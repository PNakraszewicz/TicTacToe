package softcore.tictactoe.facade;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import softcore.tictactoe.api.model.MakeMoveCommand;
import softcore.tictactoe.domain.service.GameService;

@Component
@RequiredArgsConstructor
public class GameFacade {

    private final GameService gameService;

    public void makeMove(MakeMoveCommand command) {
        gameService.makeMove(command);
    }
}
