package softcore.tictactoe;

import org.springframework.boot.SpringApplication;

public class TestTicTacToeApplication {

	public static void main(String[] args) {
		SpringApplication.from(TicTacToeApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
