package org.rsinitsyn.bot;

import org.rsinitsyn.bot.props.BotProperties;
import org.rsinitsyn.bot.props.ClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({ClientProperties.class, BotProperties.class})
@SpringBootApplication
public class GoogleInfoViberBot {

    public static void main(String[] args) {
        SpringApplication.run(GoogleInfoViberBot.class, args);
    }
}
