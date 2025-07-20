package softcore.tictactoe.api.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import softcore.tictactoe.BaseDatabaseTest;
import softcore.tictactoe.api.model.MakeMoveCommand;
import softcore.tictactoe.domain.model.entity.Game;
import softcore.tictactoe.common.enums.GameStatus;
import softcore.tictactoe.domain.model.entity.Move;
import softcore.tictactoe.common.enums.PlayerSymbol;
import softcore.tictactoe.domain.service.GameService;
import softcore.tictactoe.persistance.repository.GameRepository;
import softcore.tictactoe.persistance.repository.MoveRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GameServiceIT extends BaseDatabaseTest {

    private static final int X_POSITION = 0;
    private static final int Y_POSITION = 0;
    private static final PlayerSymbol STARTING_PLAYER = PlayerSymbol.X;
    private static final PlayerSymbol NEXT_PLAYER = PlayerSymbol.O;

    @Autowired
    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MoveRepository moveRepository;

    @Test
    void shouldPersistMoveAndSwitchPlayerTurn() {
        // given
        UUID gameId = saveGame(STARTING_PLAYER, GameStatus.IN_PROGRESS);
        MakeMoveCommand moveCommand = new MakeMoveCommand(gameId, X_POSITION, Y_POSITION, STARTING_PLAYER);

        // when
        gameService.makeMove(moveCommand);

        // then
        Game game = gameRepository.findById(gameId).orElseThrow();
        assertSingleMove(game.getId());
        assertThat(game.getPlayerTurn()).isEqualTo(PlayerSymbol.O);
    }
    @Test
    void shouldAllowOnlyOneMoveWhenExecutedConcurrently() throws Exception {
        // given
        UUID gameId = saveGame(PlayerSymbol.X, GameStatus.IN_PROGRESS);
        MakeMoveCommand moveCommand = new MakeMoveCommand(gameId, 1, 1, PlayerSymbol.X);

        Callable<String> task = () -> tryMakeMove(moveCommand);
        List<Future<String>> futures = submitConcurrentTasks(task, 2);

        // when
        List<String> results = collectResults(futures);
        long successCount = results.stream().filter("OK"::equals).count();
        List<Move> savedMoves = moveRepository.findAllByGameIdOrderByCreatedAtAsc(gameId);

        // then
        assertThat(successCount)
                .withFailMessage("Only one move should succeed")
                .isEqualTo(1);

        assertThat(savedMoves)
                .withFailMessage("Only one move should be saved")
                .hasSize(1);

        Move move = savedMoves.getFirst();
        assertThat(move.getX()).isEqualTo(1);
        assertThat(move.getY()).isEqualTo(1);
        assertThat(move.getPlayer()).isEqualTo(PlayerSymbol.X);
    }

    private UUID saveGame(PlayerSymbol playerTurn, GameStatus status) {
        Game game = Game.builder()
                .playerTurn(playerTurn)
                .status(status)
                .build();
        return gameRepository.save(game).getId();
    }

    private void assertSingleMove(UUID gameId) {
        List<Move> moves = moveRepository.findAllByGameIdOrderByCreatedAtAsc(gameId);
        assertThat(moves)
                .hasSize(1)
                .first()
                .satisfies(move -> {
                    assertThat(move.getX()).isEqualTo(X_POSITION);
                    assertThat(move.getY()).isEqualTo(Y_POSITION);
                    assertThat(move.getPlayer()).isEqualTo(STARTING_PLAYER);
                });
    }

    private List<Future<String>> submitConcurrentTasks(Callable<String> task, int count) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(count);
        List<Future<String>> futures = executor.invokeAll(List.of(task, task));
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
        return futures;
    }

    private List<String> collectResults(List<Future<String>> futures) {
        return futures.stream()
                .map(this::safeGet)
                .toList();
    }

    private String tryMakeMove(MakeMoveCommand command) {
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
