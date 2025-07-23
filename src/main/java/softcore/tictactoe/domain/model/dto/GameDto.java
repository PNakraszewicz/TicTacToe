package softcore.tictactoe.domain.model.dto;

import softcore.tictactoe.common.enums.GameStatus;
import softcore.tictactoe.common.enums.PlayerSymbol;
import softcore.tictactoe.domain.model.entity.GameEntity;
import softcore.tictactoe.domain.model.entity.MoveEntity;

import java.util.List;
import java.util.UUID;

public record GameDto(
        UUID id,
        GameStatus status,
        PlayerSymbol playerTurn,
        List<MoveEntity> moves
) {
    public static GameDto fromEntity(GameEntity game) {
        return new GameDto(
                game.getId(),
                game.getStatus(),
                game.getPlayerTurn(),
                null
        );
    }

    public static GameDto fromEntity(GameEntity game, List<MoveEntity> moves) {
        return new GameDto(
                game.getId(),
                game.getStatus(),
                game.getPlayerTurn(),
                moves
        );
    }
}
