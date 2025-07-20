package softcore.tictactoe.api.model;

import softcore.tictactoe.common.enums.PlayerSymbol;

import java.util.UUID;

public record MakeMoveCommand(UUID gameId, int x, int y, PlayerSymbol player)  {
}
