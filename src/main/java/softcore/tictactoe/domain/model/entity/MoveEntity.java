package softcore.tictactoe.domain.model.entity;

import jakarta.persistence.*;
import lombok.*;
import softcore.tictactoe.common.enums.PlayerSymbol;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "move")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerSymbol player;

    @Column(name = "x_axis", nullable = false)
    private int xAxis;

    @Column(name = "y_axis", nullable = false)
    private int yAxis;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}

