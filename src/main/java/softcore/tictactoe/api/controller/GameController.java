package softcore.tictactoe.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import softcore.tictactoe.api.model.MakeMoveCommand;
import softcore.tictactoe.api.model.MakeMoveRequest;
import softcore.tictactoe.facade.GameFacade;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameFacade gameFacade;

    @PostMapping("/{gameId}/moves")
    public ResponseEntity<Void> makeMove(
            @PathVariable UUID gameId,
            @RequestBody MakeMoveRequest request) {

        MakeMoveCommand command = new MakeMoveCommand(
                gameId,
                request.x(),
                request.y(),
                request.player()
        );
        gameFacade.makeMove(command);
        return ResponseEntity.ok().build();
    }
}