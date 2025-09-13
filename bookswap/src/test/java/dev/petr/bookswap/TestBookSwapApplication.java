package dev.petr.bookswap;

import org.springframework.boot.SpringApplication;

public class TestBookSwapApplication {

	public static void main(String[] args) {
		SpringApplication.from(BookSwapApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
