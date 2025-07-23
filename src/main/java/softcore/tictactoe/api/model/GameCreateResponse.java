package softcore.tictactoe.api.model;

import softcore.tictactoe.common.enums.GameStatus;
import softcore.tictactoe.common.enums.PlayerSymbol;
import softcore.tictactoe.domain.model.dto.GameDto;

import java.util.UUID;

public record GameCreateResponse(UUID id, GameStatus status, PlayerSymbol playerTurn) {
    public static GameCreateResponse from(GameDto game) {
        return new GameCreateResponse(game.id(), game.status(), game.playerTurn());
    }
}
