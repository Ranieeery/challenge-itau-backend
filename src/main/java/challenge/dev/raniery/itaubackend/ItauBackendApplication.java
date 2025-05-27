package challenge.dev.raniery.itaubackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class ItauBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ItauBackendApplication.class, args);
    }

}
