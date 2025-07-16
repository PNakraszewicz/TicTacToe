package softcore.tictactoe.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import softcore.tictactoe.domain.model.entity.Game;

import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {
}
