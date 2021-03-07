package mygroup.voting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableCaching
@RestController
public class VotingApplication {
	
	@RequestMapping("/")
	public String home(){
		
		return ("Vá para a url api/v1/topic \n" +
				"ou para a url api/v1/vote");
	}
	
	public static void main(String[] args) {
		SpringApplication.run(VotingApplication.class, args);
		
		System.out.println( LocalDateTime.now() + " <- Hora de inicialização do servidor");
	}

}
