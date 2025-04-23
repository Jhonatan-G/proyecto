package com.niyovi.proyecto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProyectoApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ProyectoApplication.class);
        app.setAllowCircularReferences(true);
        app.run(args);
    }
}
