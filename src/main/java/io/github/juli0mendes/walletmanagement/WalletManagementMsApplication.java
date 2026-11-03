package io.github.juli0mendes.walletmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WalletManagementMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WalletManagementMsApplication.class, args);
	}
}
