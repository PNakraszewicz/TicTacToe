package softcore.tictactoe.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import softcore.tictactoe.api.model.MakeMoveCommand;
import softcore.tictactoe.common.enums.GameStatus;
import softcore.tictactoe.common.enums.PlayerSymbol;
import softcore.tictactoe.common.exception.GameNotFoundException;
import softcore.tictactoe.domain.model.entity.GameEntity;
import softcore.tictactoe.domain.model.entity.MoveEntity;
import softcore.tictactoe.domain.service.GameService;
import softcore.tictactoe.persistance.repository.GameRepository;
import softcore.tictactoe.persistance.repository.MoveRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    private static final UUID GAME_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final MakeMoveCommand VALID_MOVE = new MakeMoveCommand(GAME_ID, 1, 1, PlayerSymbol.X);
    private static final MakeMoveCommand INVALID_PLAYER_MOVE = new MakeMoveCommand(GAME_ID, 1, 1, PlayerSymbol.O);
    private static final MakeMoveCommand DUPLICATE_MOVE = new MakeMoveCommand(GAME_ID, 0, 0, PlayerSymbol.X);

    @Mock
    private GameRepository gameRepository;

    @Mock
    private MoveRepository moveRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    void shouldMakeMoveAndSwitchTurn() {
        // given
        GameEntity game = inProgressGame();
        MoveEntity newMove = moveFromCommand(VALID_MOVE, game);
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(moveRepository.findAllByGameIdOrderByCreatedAtAsc(GAME_ID)).thenReturn(List.of());
        when(gameRepository.save(any())).thenReturn(game);
        when(moveRepository.save(any())).thenReturn(newMove);

        // when
        gameService.makeMove(VALID_MOVE);

        // then
        verify(moveRepository).save(argThat(move ->
                        move.getXAxis() == 1 &&
                        move.getYAxis() == 1 &&
                        move.getPlayer() == PlayerSymbol.X));

        verify(gameRepository).save(argThat(saved -> saved.getPlayerTurn() == PlayerSymbol.O));
        verify(gameRepository).save(argThat(saved -> saved.getStatus() == GameStatus.IN_PROGRESS));
    }

    @Test
    void shouldThrowWhenGameNotFound() {
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.makeMove(VALID_MOVE))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessageContaining(GAME_ID.toString());
    }

    @Test
    void shouldThrowWhenGameIsNotInProgress() {
        GameEntity finishedGame = inProgressGame();
        finishedGame.setStatus(GameStatus.DRAW);
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(finishedGame));

        assertThatThrownBy(() -> gameService.makeMove(VALID_MOVE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("finished");
    }

    @Test
    void shouldThrowWhenPlayerIsOutOfTurn() {
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(inProgressGame()));

        assertThatThrownBy(() -> gameService.makeMove(INVALID_PLAYER_MOVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not your turn");
    }

    @Test
    void shouldThrowWhenFieldIsTaken() {
        GameEntity game = inProgressGame();
        MoveEntity existingMove = MoveEntity.builder()
                .xAxis(0).yAxis(0).player(PlayerSymbol.X).build();

        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(moveRepository.findAllByGameIdOrderByCreatedAtAsc(GAME_ID)).thenReturn(List.of(existingMove));

        assertThatThrownBy(() -> gameService.makeMove(DUPLICATE_MOVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }
    @Test
    void shouldEndGameWhenPlayerWinsHorizontally() {
        GameEntity game = inProgressGame();
        MakeMoveCommand command = new MakeMoveCommand(GAME_ID, 0, 2, PlayerSymbol.X);
        MoveEntity newMove = moveFromCommand(command, game);

        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenReturn(game);
        when(moveRepository.save(any())).thenReturn(newMove);

        when(moveRepository.findAllByGameIdOrderByCreatedAtAsc(GAME_ID)).thenReturn(
                List.of(
                        move(0, 0, PlayerSymbol.X),
                        move(0, 1, PlayerSymbol.X)
                ),
                List.of(
                        move(0, 0, PlayerSymbol.X),
                        move(0, 1, PlayerSymbol.X),
                        newMove
                )
        );

        gameService.makeMove(command);

        verify(gameRepository).save(argThat(saved -> saved.getStatus() == GameStatus.X_WINS));
    }

    @Test
    void shouldEndGameWhenPlayerWinsVertically() {
        GameEntity game = inProgressGame();
        MakeMoveCommand command = new MakeMoveCommand(GAME_ID, 2, 0, PlayerSymbol.X);
        MoveEntity newMove = moveFromCommand(command, game);

        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenReturn(game);
        when(moveRepository.save(any())).thenReturn(newMove);

        when(moveRepository.findAllByGameIdOrderByCreatedAtAsc(GAME_ID)).thenReturn(
                List.of(
                        move(0, 0, PlayerSymbol.X),
                        move(1, 0, PlayerSymbol.X)
                ),
                List.of(
                        move(0, 0, PlayerSymbol.X),
                        move(1, 0, PlayerSymbol.X),
                        newMove
                )
        );

        gameService.makeMove(command);

        verify(gameRepository).save(argThat(saved -> saved.getStatus() == GameStatus.X_WINS));
    }

    @Test
    void shouldEndGameWhenPlayerWinsDiagonally() {
        GameEntity game = inProgressGame();
        MakeMoveCommand command = new MakeMoveCommand(GAME_ID, 2, 2, PlayerSymbol.X);
        MoveEntity newMove = moveFromCommand(command, game);

        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenReturn(game);
        when(moveRepository.save(any())).thenReturn(newMove);

        when(moveRepository.findAllByGameIdOrderByCreatedAtAsc(GAME_ID)).thenReturn(
                List.of(
                        move(0, 0, PlayerSymbol.X),
                        move(1, 1, PlayerSymbol.X)
                ),
                List.of(
                        move(0, 0, PlayerSymbol.X),
                        move(1, 1, PlayerSymbol.X),
                        newMove
                )
        );

        gameService.makeMove(command);

        verify(gameRepository).save(argThat(saved -> saved.getStatus() == GameStatus.X_WINS));
    }

    @Test
    void shouldEndGameWhenDraw() {
        GameEntity game = inProgressGame();
        MakeMoveCommand command = new MakeMoveCommand(GAME_ID, 2, 2, PlayerSymbol.X);
        MoveEntity newMove = moveFromCommand(command, game);

        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
        when(gameRepository.save(any())).thenReturn(game);
        when(moveRepository.save(any())).thenReturn(newMove);

        List<MoveEntity> movesBefore = List.of(
                move(0, 0, PlayerSymbol.O),
                move(0, 1, PlayerSymbol.X),
                move(0, 2, PlayerSymbol.O),
                move(1, 0, PlayerSymbol.X),
                move(1, 1, PlayerSymbol.O),
                move(1, 2, PlayerSymbol.X),
                move(2, 0, PlayerSymbol.O),
                move(2, 1, PlayerSymbol.X)
        );

        List<MoveEntity> movesAfter = new java.util.ArrayList<>(movesBefore);
        movesAfter.add(newMove);

        when(moveRepository.findAllByGameIdOrderByCreatedAtAsc(GAME_ID)).thenReturn(
                movesBefore,
                movesAfter
        );

        gameService.makeMove(command);

        verify(gameRepository).save(argThat(saved -> saved.getStatus() == GameStatus.DRAW));
    }

    @Test
    void shouldThrowWhenMovePositionOutOfBounds() {
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(inProgressGame()));

        List<MakeMoveCommand> invalidMoves = List.of(
                new MakeMoveCommand(GAME_ID, -1, 1, PlayerSymbol.X),
                new MakeMoveCommand(GAME_ID, 1, -1, PlayerSymbol.X),
                new MakeMoveCommand(GAME_ID, 3, 1, PlayerSymbol.X),
                new MakeMoveCommand(GAME_ID, 1, 3, PlayerSymbol.X)
        );

        for (MakeMoveCommand move : invalidMoves) {
            assertThatThrownBy(() -> gameService.makeMove(move))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Move position is out of bounds");
        }
    }

    private GameEntity inProgressGame() {
        return GameEntity.builder()
                .id(GAME_ID)
                .playerTurn(PlayerSymbol.X)
                .status(GameStatus.IN_PROGRESS)
                .build();
    }

    private MoveEntity moveFromCommand(MakeMoveCommand command, GameEntity game) {
        return MoveEntity.builder()
                .xAxis(command.x())
                .yAxis(command.y())
                .player(command.player())
                .game(game)
                .build();
    }

    private MoveEntity move(int x, int y, PlayerSymbol player) {
        return MoveEntity.builder()
                .xAxis(x)
                .yAxis(y)
                .player(player)
                .build();
    }
}