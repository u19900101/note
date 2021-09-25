package ppppp.evernote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("ppppp.evernote.mapper")
public class Admin {

    public static void main(String[] args) {
        SpringApplication.run(Admin.class, args);
    }




}
