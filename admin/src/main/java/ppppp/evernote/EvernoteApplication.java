package ppppp.evernote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("ppppp.evernote.mapper")
public class EvernoteApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvernoteApplication.class, args);
    }




}
