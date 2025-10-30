package com.nw2.parcel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;


@RestController
@SpringBootApplication
public class ParcelApplication {
    public static void main(String[] args) {
        SpringApplication.run(ParcelApplication.class, args);
    }


}