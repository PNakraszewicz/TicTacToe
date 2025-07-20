package softcore.tictactoe.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import softcore.tictactoe.common.exception.GameNotFoundException;
import softcore.tictactoe.api.model.MakeMoveCommand;
import softcore.tictactoe.domain.model.entity.Game;
import softcore.tictactoe.common.enums.GameStatus;
import softcore.tictactoe.domain.model.entity.Move;
import softcore.tictactoe.common.enums.PlayerSymbol;
import softcore.tictactoe.domain.service.GameService;
import softcore.tictactoe.persistance.repository.GameRepository;
import softcore.tictactoe.persistance.repository.MoveRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    private static final UUID GAME_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final MakeMoveCommand VALID_MOVE = new MakeMoveCommand(GAME_ID, 1, 1, PlayerSymbol.X);
    private static final MakeMoveCommand INVALID_PLAYER_MOVE = new MakeMoveCommand(GAME_ID,1, 1, PlayerSymbol.O);
    private static final MakeMoveCommand DUPLICATE_MOVE = new MakeMoveCommand(GAME_ID, 0, 0, PlayerSymbol.X);

    @Mock
    private GameRepository gameRepository;
    @Mock private MoveRepository moveRepository;

    @InjectMocks
    private GameService gameService;

    @Test
    void shouldMakeMoveAndSwitchTurn() {
        // given
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(copyGame()));
        when(moveRepository.findAllByGameIdOrderByCreatedAtAsc(GAME_ID)).thenReturn(List.of());

        // when
        gameService.makeMove(VALID_MOVE);

        // then
        verify(moveRepository).save(argThat(move ->
                move.getX() == 1 && move.getY() == 1 && move.getPlayer() == PlayerSymbol.X));

        verify(gameRepository).save(argThat(savedGame ->
                savedGame.getPlayerTurn() == PlayerSymbol.O));
    }

    @Test
    void shouldThrowWhenGameNotFound() {
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.makeMove(VALID_MOVE))
                .isInstanceOf(GameNotFoundException.class)
                .hasMessageContaining(GAME_ID.toString());
    }

    @Test
    void shouldThrowWhenGameAlreadyFinished() {
        Game finishedGame = copyGame();
        finishedGame.setStatus(GameStatus.DRAW);
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(finishedGame));

        assertThatThrownBy(() -> gameService.makeMove(VALID_MOVE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("finished");
    }

    @Test
    void shouldThrowWhenMoveIsOutOfTurn() {
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(copyGame()));

        assertThatThrownBy(() -> gameService.makeMove(INVALID_PLAYER_MOVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not your turn");
    }

    @Test
    void shouldThrowWhenFieldIsAlreadyTaken() {
        Move existingMove = Move.builder().x(0).y(0).player(PlayerSymbol.X).build();
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(copyGame()));
        when(moveRepository.findAllByGameIdOrderByCreatedAtAsc(GAME_ID)).thenReturn(List.of(existingMove));

        assertThatThrownBy(() -> gameService.makeMove(DUPLICATE_MOVE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    void shouldOnlyAllowOneMoveWhenCalledConcurrently() throws Exception {
        when(gameRepository.findById(GAME_ID)).thenAnswer(inv -> Optional.of(copyGame()));
        when(moveRepository.findAllByGameIdOrderByCreatedAtAsc(GAME_ID)).thenReturn(List.of());

        Callable<String> task1 = () -> tryMove(VALID_MOVE);
        Callable<String> task2 = () -> tryMove(VALID_MOVE);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        List<Future<String>> results = executor.invokeAll(List.of(task1, task2));
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        long successCount = results.stream()
                .map(this::safeGet)
                .filter("OK"::equals)
                .count();

        assertThat(successCount).isEqualTo(1);
    }

    private Game copyGame() {
        return Game.builder()
                .id(GAME_ID)
                .playerTurn(PlayerSymbol.X)
                .status(GameStatus.IN_PROGRESS)
                .build();
    }

    private String tryMove(MakeMoveCommand command) {
        try {
            gameService.makeMove(command);
            return "OK";
        } catch (Exception e) {
            return "EX";
        }
    }

    private String safeGet(Future<String> future) {
        try {
            return future.get();
        } catch (Exception e) {
            return "EX";
        }
    }
}
