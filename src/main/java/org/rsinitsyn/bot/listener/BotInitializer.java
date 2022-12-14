package org.rsinitsyn.bot.listener;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.viber.bot.Request;
import com.viber.bot.Response;
import com.viber.bot.ViberSignatureValidator;
import com.viber.bot.api.ViberBot;
import com.viber.bot.event.incoming.IncomingConversationStartedEvent;
import com.viber.bot.event.incoming.IncomingMessageEvent;
import com.viber.bot.event.incoming.IncomingSubscribedEvent;
import com.viber.bot.event.incoming.IncomingUnsubscribeEvent;
import com.viber.bot.message.Message;
import com.viber.bot.message.TextMessage;
import com.viber.bot.profile.UserProfile;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.rsinitsyn.bot.props.BotProperties;
import org.rsinitsyn.bot.service.GoogleApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
@RequiredArgsConstructor
@Slf4j
public class BotInitializer {

    private final ViberBot viberBot;
    private final ViberSignatureValidator viberSignatureValidator;
    private final ObjectMapper objectMapper;
    private final BotProperties botProperties;
    private final GoogleApiService googleApiService;

    @Value("${externalUri}")
    private String externalUri;

    @EventListener(ContextRefreshedEvent.class)
    public void init() {
        viberBot.setWebhook(botProperties.getWebHookUri());
        viberBot.onMessageReceived(this::receiveMessage);
        viberBot.onConversationStarted(this::startConversation);
        viberBot.onSubscribe(this::subscribe);
        viberBot.onUnsubscribe(this::unsubscribe);
    }

    private void unsubscribe(IncomingUnsubscribeEvent event) {
        String userId = event.getUserId();
        log.info("Event [unsubscribe]. UserId: {}", userId);
    }

    private void subscribe(IncomingSubscribedEvent event, Response response) {
        String username = event.getUser().getName();
        String userId = event.getUser().getId();
        log.info("Event [subscribe]. Username: {}, userId: {}", username, userId);
        response.send("Спасибо за подписку!");
    }

    @SneakyThrows
    private Future<Optional<Message>> startConversation(IncomingConversationStartedEvent event) {
        return CompletableFuture.supplyAsync(() -> {
                    String userFirstName = event.getUser().getName();
                    return new TextMessage("Привет, я Бот, а тебя зовут " + userFirstName);
                })
                .thenApply(Optional::of);
    }


    private void receiveMessage(IncomingMessageEvent event, Message message, Response response) {
        if (!Optional.ofNullable(event.getEvent().getServerEventName()).orElse("other").equals("message")) {
            return;
        }
        if (!message.getType().equals("text")) {
            response.send("Я могу отвечать только на текст пока что...");
            return;
        }
        UserProfile userProfile = event.getSender();
        TextMessage userMessage = (TextMessage) message;
        log.info("Event [message], type: [text]. Username: {}, text: {}", userProfile.getName(), userMessage.getText());

        if (userMessage.getText().equals("Гугл")) {
            response.send(externalUri + "/oauth2/authorization/google");
        } else if (userMessage.getText().contains("Токен")) {
            String token = userMessage.getText().split(" ")[1];
            JsonNode userDetails = googleApiService.getUserDetails(token);
            var userEmail = userDetails.get("email").asText();
            response.send("Твой гугл эмейл - " + userEmail);
        } else {
            response.send(
                    new TextMessage("Я бот, я могу пока только повторять"),
                    new TextMessage(userMessage.getText()),
                    new TextMessage("------------------------")
            );
        }
    }

    @PostMapping(value = "/hook", produces = "application/json")
    public String incoming(@RequestBody String json,
                           @RequestHeader("X-Viber-Content-Signature") String serverSideSignature)
            throws ExecutionException, InterruptedException, IOException {
        log.info("------------------------------------------------------------");
        JsonNode jsonNode = objectMapper.readTree(json);
        log.debug("Received new event: {}, userId: {}", jsonNode.get("event"), jsonNode.get("user_id"));
        Preconditions.checkState(viberSignatureValidator.isSignatureValid(serverSideSignature, json), "Invalid signature");
        InputStream response = viberBot.incoming(Request.fromJsonString(json)).get();
        return response != null ? CharStreams.toString(new InputStreamReader(response, Charsets.UTF_16)) : null;
    }
}
