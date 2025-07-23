package softcore.tictactoe.persistance.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import softcore.tictactoe.BaseDatabaseTest;
import softcore.tictactoe.domain.model.entity.GameEntity;
import softcore.tictactoe.common.enums.GameStatus;
import softcore.tictactoe.domain.model.entity.MoveEntity;
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
        GameEntity game = GameEntity.builder()
                .playerTurn(PlayerSymbol.X)
                .status(GameStatus.IN_PROGRESS)
                .build();

        gameRepository.save(game);

        MoveEntity firstMove = MoveEntity.builder()
                .game(game)
                .player(PlayerSymbol.X)
                .xAxis(0).yAxis(0)
                .build();

        MoveEntity secondMove = MoveEntity.builder()
                .game(game)
                .player(PlayerSymbol.O)
                .xAxis(1).yAxis(1)
                .build();

        moveRepository.saveAll(List.of(firstMove, secondMove));

        // when
        List<MoveEntity> moves = moveRepository.findAllByGameIdOrderByCreatedAtAsc(game.getId());

        // then
        assertThat(moves).hasSize(2);
        assertThat(moves.get(0).getPlayer()).isEqualTo(PlayerSymbol.X);
        assertThat(moves.get(1).getPlayer()).isEqualTo(PlayerSymbol.O);
    }
}
