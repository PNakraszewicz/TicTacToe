package softcore.tictactoe.persistance.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import softcore.tictactoe.BaseDatabaseTest;
import softcore.tictactoe.domain.model.entity.Game;
import softcore.tictactoe.common.enums.GameStatus;
import softcore.tictactoe.common.enums.PlayerSymbol;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GameRepositoryIT extends BaseDatabaseTest {

    @Autowired
    private GameRepository gameRepository;

    @Test
    void shouldSaveAndLoadGame() {
        // given
        Game game = Game.builder()
                .playerTurn(PlayerSymbol.X)
                .status(GameStatus.IN_PROGRESS)
                .build();

        // when
        Game saved = gameRepository.save(game);

        // then
        Optional<Game> found = gameRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getPlayerTurn()).isEqualTo(PlayerSymbol.X);
        assertThat(found.get().getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(found.get().getCreatedAt()).isNotNull();
    }
}
