package org.rsinitsyn.bot.config;

import com.viber.bot.ViberSignatureValidator;
import com.viber.bot.api.ViberBot;
import com.viber.bot.profile.BotProfile;
import org.rsinitsyn.bot.props.BotProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BotConfig {

    @Autowired
    private BotProperties botProperties;

    @Bean
    public ViberBot viberBot() {
        return new ViberBot(new BotProfile(botProperties.getName(), botProperties.getAvatar()), botProperties.getToken());
    }

    @Bean
    public ViberSignatureValidator viberSignatureValidator() {
        return new ViberSignatureValidator(botProperties.getToken());
    }
}
