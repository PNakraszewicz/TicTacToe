package softcore.tictactoe.api.model;

import softcore.tictactoe.common.enums.GameStatus;
import softcore.tictactoe.common.enums.PlayerSymbol;
import softcore.tictactoe.domain.model.dto.GameDto;

import java.util.UUID;
public record GameDetailsResponse(
        UUID id,
        GameStatus status,
        PlayerSymbol playerTurn,
        String[][] board
) {
    public static GameDetailsResponse from(GameDto dto) {
        String[][] board = new String[3][3];

        dto.moves().forEach(move -> {
            int x = move.getXAxis();
            int y = move.getYAxis();
            board[x][y] = move.getPlayer().name();
        });

        return new GameDetailsResponse(
                dto.id(),
                dto.status(),
                dto.playerTurn(),
                board
        );
    }
}