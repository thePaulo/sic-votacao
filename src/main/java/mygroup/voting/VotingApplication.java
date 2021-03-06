package mygroup.voting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableCaching
public class VotingApplication {

	public static void main(String[] args) {
		SpringApplication.run(VotingApplication.class, args);
		System.out.println( LocalDateTime.now() + " <- server starting time zzzzz");

	}

}
