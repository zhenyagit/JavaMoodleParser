package org.imjs_man.moodleParser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MoodleParserApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoodleParserApplication.class, args);
    }
}
