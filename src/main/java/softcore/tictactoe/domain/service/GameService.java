package softcore.tictactoe.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softcore.tictactoe.common.exception.GameNotFoundException;
import softcore.tictactoe.api.model.MakeMoveCommand;
import softcore.tictactoe.domain.model.entity.Game;
import softcore.tictactoe.common.enums.GameStatus;
import softcore.tictactoe.domain.model.entity.Move;
import softcore.tictactoe.common.enums.PlayerSymbol;
import softcore.tictactoe.persistance.repository.GameRepository;
import softcore.tictactoe.persistance.repository.MoveRepository;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;

    private final ConcurrentHashMap<UUID, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Transactional
    public void makeMove(MakeMoveCommand moveCommand) {
        ReentrantLock lock = locks.computeIfAbsent(moveCommand.gameId(), id -> new ReentrantLock());
        boolean acquired = false;
        try {
            acquired = lock.tryLock(2, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("Timeout waiting for game lock: " + moveCommand.gameId());
            }

            Game game = gameRepository.findById(moveCommand.gameId())
                    .orElseThrow(() -> new GameNotFoundException(moveCommand.gameId()));

            validateGameState(game, moveCommand);

            Move move = Move.builder()
                    .game(game)
                    .x(moveCommand.x())
                    .y(moveCommand.y())
                    .player(moveCommand.player())
                    .build();

            moveRepository.save(move);

            game.setPlayerTurn(nextPlayer(moveCommand.player()));
            gameRepository.save(game);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted while locking game " + moveCommand.gameId(), e);
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }

    private void validateGameState(Game game, MakeMoveCommand request) {
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game already finished");
        }

        if (request.player() != game.getPlayerTurn()) {
            throw new IllegalArgumentException("It's not your turn");
        }

        boolean positionTaken = moveRepository.findAllByGameIdOrderByCreatedAtAsc(game.getId()).stream()
                .anyMatch(m -> m.getX() == request.x() && m.getY() == request.y());

        if (positionTaken) {
            throw new IllegalArgumentException("This position is already taken");
        }
    }

    private PlayerSymbol nextPlayer(PlayerSymbol current) {
        return current == PlayerSymbol.X ? PlayerSymbol.O : PlayerSymbol.X;
    }
}
