package softcore.tictactoe.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softcore.tictactoe.api.model.MakeMoveCommand;
import softcore.tictactoe.common.enums.GameStatus;
import softcore.tictactoe.common.enums.PlayerSymbol;
import softcore.tictactoe.common.exception.GameNotFoundException;
import softcore.tictactoe.domain.model.dto.GameDto;
import softcore.tictactoe.domain.model.entity.GameEntity;
import softcore.tictactoe.domain.model.entity.MoveEntity;
import softcore.tictactoe.persistance.repository.GameRepository;
import softcore.tictactoe.persistance.repository.MoveRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class GameService {

    private static final int LOCK_TIMEOUT_SECONDS = 2;

    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;

    private final ConcurrentHashMap<UUID, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Transactional
    public GameDto createNewGame() {
        GameEntity newGame = GameEntity.builder()
                .status(GameStatus.IN_PROGRESS)
                .playerTurn(PlayerSymbol.X)
                .build();

        GameEntity saved = gameRepository.save(newGame);
        return GameDto.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public GameDto getGame(UUID gameId) {
        GameEntity gameEntity = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
        var moves = moveRepository.findAllByGameIdOrderByCreatedAtAsc(gameId);
        return GameDto.fromEntity(gameEntity, moves);
    }

    @Transactional
    public GameDto makeMove(MakeMoveCommand command) {
        ReentrantLock lock = locks.computeIfAbsent(command.gameId(), id -> new ReentrantLock());
        boolean acquired = false;

        try {
            acquired = lock.tryLock(LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("Timeout while waiting for game lock: " + command.gameId());
            }
            return processMove(command);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted while locking game: " + command.gameId(), e);
        } finally {
            if (acquired) {
                lock.unlock();
            }
        }
    }

    private GameDto processMove(MakeMoveCommand command) {
        GameEntity game = gameRepository.findById(command.gameId())
                .orElseThrow(() -> new GameNotFoundException(command.gameId()));

        validateGameState(game, command);

        MoveEntity move = MoveEntity.builder()
                .game(game)
                .xAxis(command.x())
                .yAxis(command.y())
                .player(command.player())
                .build();

        MoveEntity newMove = moveRepository.save(move);
        var allMoves = moveRepository.findAllByGameIdOrderByCreatedAtAsc(game.getId());

        if (hasPlayerWon(allMoves, newMove)) {
            game.setStatus(GameStatus.valueOf(newMove.getPlayer() + "_WINS"));
        } else if (allMoves.size() == 9) {
            game.setStatus(GameStatus.DRAW);
        } else {
            game.setPlayerTurn(nextPlayer(newMove.getPlayer()));
        }

        GameEntity updatedGame = gameRepository.save(game);
        return GameDto.fromEntity(updatedGame, allMoves);
    }

    private void validateGameState(GameEntity game, MakeMoveCommand command) {
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Game already finished");
        }

        if (command.player() != game.getPlayerTurn()) {
            throw new IllegalArgumentException("It's not your turn");
        }

        if (command.x() < 0 || command.x() > 2 || command.y() < 0 || command.y() > 2) {
            throw new IllegalArgumentException("Move position is out of bounds");
        }

        boolean positionTaken = moveRepository.findAllByGameIdOrderByCreatedAtAsc(game.getId()).stream()
                .anyMatch(m -> m.getXAxis() == command.x() && m.getYAxis() == command.y());

        if (positionTaken) {
            throw new IllegalArgumentException("This position is already taken");
        }
    }

    private boolean hasPlayerWon(List<MoveEntity> moves, MoveEntity lastMove) {
        PlayerSymbol player = lastMove.getPlayer();
        int x = lastMove.getXAxis();
        int y = lastMove.getYAxis();

        List<MoveEntity> playerMoves = moves.stream()
                .filter(m -> m.getPlayer() == player)
                .toList();

        boolean rowWin = playerMoves.stream().filter(m -> m.getXAxis() == x).count() == 3;

        boolean colWin = playerMoves.stream().filter(m -> m.getYAxis() == y).count() == 3;

        boolean isOnMainDiagonal = x == y;
        boolean mainDiagonalWin = isOnMainDiagonal &&
                playerMoves.stream().filter(m -> m.getXAxis() == m.getYAxis()).count() == 3;

        boolean isOnAntiDiagonal = x + y == 2;
        boolean antiDiagonalWin = isOnAntiDiagonal &&
                playerMoves.stream().filter(m -> m.getXAxis() + m.getYAxis() == 2).count() == 3;

        return rowWin || colWin || mainDiagonalWin || antiDiagonalWin;
    }

    private PlayerSymbol nextPlayer(PlayerSymbol current) {
        return current == PlayerSymbol.X ? PlayerSymbol.O : PlayerSymbol.X;
    }
}
