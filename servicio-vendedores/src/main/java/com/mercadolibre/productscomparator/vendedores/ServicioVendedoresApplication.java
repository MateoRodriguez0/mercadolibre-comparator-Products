package com.mercadolibre.productscomparator.vendedores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServicioVendedoresApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicioVendedoresApplication.class, args);
	}

}
