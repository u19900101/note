package ppppp.evernote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class Search {

    public static void main(String[] args) {
        SpringApplication.run(Search.class, args);
    }

}
