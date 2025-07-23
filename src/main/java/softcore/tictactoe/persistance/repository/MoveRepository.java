package softcore.tictactoe.persistance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import softcore.tictactoe.domain.model.entity.MoveEntity;

import java.util.List;
import java.util.UUID;

public interface MoveRepository extends JpaRepository<MoveEntity, UUID> {
    List<MoveEntity> findAllByGameIdOrderByCreatedAtAsc(UUID gameId);
}

