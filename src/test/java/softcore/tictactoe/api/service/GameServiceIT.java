package softcore.tictactoe.api.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import softcore.tictactoe.BaseDatabaseTest;
import softcore.tictactoe.ConcurrentTestHelper;
import softcore.tictactoe.api.model.MakeMoveCommand;
import softcore.tictactoe.common.enums.GameStatus;
import softcore.tictactoe.common.enums.PlayerSymbol;
import softcore.tictactoe.domain.model.entity.GameEntity;
import softcore.tictactoe.domain.model.entity.MoveEntity;
import softcore.tictactoe.domain.service.GameService;
import softcore.tictactoe.persistance.repository.GameRepository;
import softcore.tictactoe.persistance.repository.MoveRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GameServiceIT extends BaseDatabaseTest {

    private static final int X = 0;
    private static final int Y = 0;
    private static final PlayerSymbol STARTING_PLAYER = PlayerSymbol.X;
    public static final int THREAD_COUNT = 8;

    @Autowired private GameService gameService;
    @Autowired private GameRepository gameRepository;
    @Autowired private MoveRepository moveRepository;

    @Test
    void shouldMakeMoveAndSwitchTurn() {
        // given
        UUID gameId = givenGameWith(STARTING_PLAYER, GameStatus.IN_PROGRESS);
        MakeMoveCommand command = new MakeMoveCommand(gameId, X, Y, STARTING_PLAYER);

        // when
        gameService.makeMove(command);

        // then
        thenSingleMoveSaved(gameId, X, Y, STARTING_PLAYER);
        thenGameTurnIs(gameId, PlayerSymbol.O);
    }

    @Test
    void shouldAllowOnlyOneMoveWhenExecutedConcurrently() throws Exception {
        // given
        UUID gameId = givenGameWith(STARTING_PLAYER, GameStatus.IN_PROGRESS);
        MakeMoveCommand command = new MakeMoveCommand(gameId, X, Y, STARTING_PLAYER);

        Runnable moveTask = () -> gameService.makeMove(command);

        // when
        List<Throwable> exceptions = ConcurrentTestHelper.runConcurrently(moveTask, THREAD_COUNT);

        // then
        assertThat(exceptions).hasSize(THREAD_COUNT - 1);
        assertThat(exceptions.getFirst()).isInstanceOfAny(
                IllegalArgumentException.class, IllegalStateException.class);

        List<MoveEntity> moves = moveRepository.findAllByGameIdOrderByCreatedAtAsc(gameId);
        assertThat(moves).hasSize(1);
    }

    private UUID givenGameWith(PlayerSymbol playerTurn, GameStatus status) {
        GameEntity game = GameEntity.builder()
                .playerTurn(playerTurn)
                .status(status)
                .build();
        return gameRepository.save(game).getId();
    }

    private void thenSingleMoveSaved(UUID gameId, int x, int y, PlayerSymbol player) {
        List<MoveEntity> moves = moveRepository.findAllByGameIdOrderByCreatedAtAsc(gameId);
        assertThat(moves).hasSize(1);
        MoveEntity move = moves.getFirst();
        assertThat(move.getXAxis()).isEqualTo(x);
        assertThat(move.getYAxis()).isEqualTo(y);
        assertThat(move.getPlayer()).isEqualTo(player);
    }

    private void thenGameTurnIs(UUID gameId, PlayerSymbol expectedTurn) {
        GameEntity updated = gameRepository.findById(gameId).orElseThrow();
        assertThat(updated.getPlayerTurn()).isEqualTo(expectedTurn);
    }
}
