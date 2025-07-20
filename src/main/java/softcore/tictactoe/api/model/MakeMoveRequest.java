package softcore.tictactoe.api.model;

import softcore.tictactoe.common.enums.PlayerSymbol;

public record MakeMoveRequest(int x, int y, PlayerSymbol player) {
}
