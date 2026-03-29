package io.github.juli0mendes.walletmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"io.github.juli0mendes.walletmanagement", "com.walletmanagement"})
public class WalletManagementMsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WalletManagementMsApplication.class, args);
	}

}
