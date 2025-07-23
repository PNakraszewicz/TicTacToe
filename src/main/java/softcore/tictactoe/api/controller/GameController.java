package softcore.tictactoe.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softcore.tictactoe.api.model.GameCreateResponse;
import softcore.tictactoe.api.model.GameDetailsResponse;
import softcore.tictactoe.api.model.MakeMoveCommand;
import softcore.tictactoe.api.model.MakeMoveRequest;
import softcore.tictactoe.domain.model.dto.GameDto;
import softcore.tictactoe.facade.GameFacade;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameFacade gameFacade;

    @PostMapping
    public ResponseEntity<GameCreateResponse> createGame() {
        GameDto dto = gameFacade.createNewGame();
        return ResponseEntity.ok(GameCreateResponse.from(dto));
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDetailsResponse> getGame(@PathVariable UUID gameId) {
        return ResponseEntity.ok(gameFacade.getGameDetails(gameId));
    }

    @PostMapping("/{gameId}/move")
    public ResponseEntity<GameDetailsResponse> makeMove(
            @PathVariable UUID gameId,
            @RequestBody MakeMoveRequest request) {

        MakeMoveCommand command = new MakeMoveCommand(
                gameId,
                request.x(),
                request.y(),
                request.player()
        );
        return ResponseEntity.ok(gameFacade.makeMove(command));
    }
}