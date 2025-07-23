package softcore.tictactoe.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Testcontainers;
import softcore.tictactoe.BaseDatabaseTest;
import softcore.tictactoe.api.model.GameCreateResponse;
import softcore.tictactoe.api.model.GameDetailsResponse;
import softcore.tictactoe.api.model.MakeMoveRequest;
import softcore.tictactoe.common.enums.GameStatus;
import softcore.tictactoe.common.enums.PlayerSymbol;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class GameApiTest extends BaseDatabaseTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Test
    void shouldCreateGameMakeMoveAndGetGameDetails() {
        // CREATE GAME
        ResponseEntity<GameCreateResponse> createResponse = restTemplate.postForEntity(
                "/api/games", null, GameCreateResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        GameCreateResponse game = createResponse.getBody();
        assertThat(game).isNotNull();
        UUID gameId = game.id();
        assertThat(game.playerTurn()).isEqualTo(PlayerSymbol.X);
        assertThat(game.status()).isEqualTo(GameStatus.IN_PROGRESS);

        // MAKE MOVE
        MakeMoveRequest move = new MakeMoveRequest(1, 2, PlayerSymbol.X);
        ResponseEntity<Void> moveResponse = restTemplate.postForEntity(
                "/api/games/" + gameId + "/move", move, Void.class);

        assertThat(moveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // GET GAME DETAILS
        ResponseEntity<GameDetailsResponse> getResponse = restTemplate.getForEntity(
                "/api/games/" + gameId, GameDetailsResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        GameDetailsResponse details = getResponse.getBody();
        assertThat(details).isNotNull();
        assertThat(details.id()).isEqualTo(gameId);
        assertThat(details.playerTurn()).isEqualTo(PlayerSymbol.O);

        // Check board state
        String[][] board = details.board();
        assertThat(board).isNotNull();
        assertThat(board[0][0]).isNull();
        assertThat(board[0][1]).isNull();
        assertThat(board[1][2]).isEqualTo("X");
        assertThat(board[2][2]).isNull();
    }
}
