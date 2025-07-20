package softcore.tictactoe.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import softcore.tictactoe.domain.model.entity.Move;

import java.util.List;
import java.util.UUID;

public interface MoveRepository extends JpaRepository<Move, UUID> {
    List<Move> findAllByGameIdOrderByCreatedAtAsc(UUID gameId);
}

