package org.imjs_man.moodleParser.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.elasticsearch.search.SearchHit;
import org.imjs_man.moodleParser.elasticService.ElasticRestClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.imjs_man.moodleParser.entity.supporting.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ElasticRestClientService elasticRestClientService;

    @MessageMapping("/message")
    @SendTo("/chat/messages")
    public Message getMessages(Message message) {
        System.out.println(message);
        return message;
    }

//    @MessageMapping("/hello")
//    public void send(SimpMessageHeaderAccessor sha, @Payload String username) {
//        String message = "Hello from " + Objects.requireNonNull(sha.getUser()).getName();
//
//        simpMessagingTemplate.convertAndSendToUser(username, "/queue/messages", message);
//    }
    @MessageMapping("/hello")
    public void send(SimpMessageHeaderAccessor sha, @Payload String searchInput) throws Exception {


        List<Temp> hitsEntity = new ArrayList<>();
        Gson gson = new GsonBuilder().create();
        JsonObject jsonObject = gson.fromJson(searchInput, JsonObject.class);
        String queryString = jsonObject.get("message").toString();
        for (SearchHit hit:elasticRestClientService.findByQuerySync("_all" ,queryString))
        {
            jsonObject = gson.fromJson(hit.getSourceAsString(), JsonObject.class);
            String name = String.valueOf(jsonObject.get("name"));
            System.out.println(name);
            hitsEntity.add(new Temp(name,"description", String.valueOf(hit.getScore())));
        }
        System.out.println(new Gson().toJson(hitsEntity));
        simpMessagingTemplate.convertAndSendToUser(Objects.requireNonNull(sha.getUser()).getName(), "/queue/messages", gson.toJson(hitsEntity));
    }


}
class Temp {
    String name;
    String description;
    String score;

    public Temp(String name, String description, String score) {
        this.name = name;
        this.description = description;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }
}