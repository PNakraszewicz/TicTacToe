package softcore.tictactoe.persistance.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import softcore.tictactoe.BaseDatabaseTest;
import softcore.tictactoe.domain.model.entity.Game;
import softcore.tictactoe.common.enums.GameStatus;
import softcore.tictactoe.domain.model.entity.Move;
import softcore.tictactoe.common.enums.PlayerSymbol;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MoveRepositoryIT extends BaseDatabaseTest {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MoveRepository moveRepository;

    @Test
    void shouldSaveAndLoadMovesForGame() {
        // given
        Game game = Game.builder()
                .playerTurn(PlayerSymbol.X)
                .status(GameStatus.IN_PROGRESS)
                .build();

        gameRepository.save(game);

        Move firstMove = Move.builder()
                .game(game)
                .player(PlayerSymbol.X)
                .x(0).y(0)
                .build();

        Move secondMove = Move.builder()
                .game(game)
                .player(PlayerSymbol.O)
                .x(1).y(1)
                .build();

        moveRepository.saveAll(List.of(firstMove, secondMove));

        // when
        List<Move> moves = moveRepository.findAllByGameIdOrderByCreatedAtAsc(game.getId());

        // then
        assertThat(moves).hasSize(2);
        assertThat(moves.get(0).getPlayer()).isEqualTo(PlayerSymbol.X);
        assertThat(moves.get(1).getPlayer()).isEqualTo(PlayerSymbol.O);
        assertThat(moves.get(0).getCreatedAt()).isBefore(moves.get(1).getCreatedAt());
    }
}
