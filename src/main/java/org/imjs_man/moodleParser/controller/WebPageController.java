package org.imjs_man.moodleParser.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class WebPageController {

    @RequestMapping(value = { "/admin" }, method = RequestMethod.GET)
    public String showAdminPanel() {
        return "adminPanel";
    }
    @RequestMapping(value = { "/chat" }, method = RequestMethod.GET)
    public String showChat() {
        return "chatPanel";
    }
    @RequestMapping(value = { "/personal" }, method = RequestMethod.GET)
    public String showPersonalMessage() {
        return "personalMessages";
    }
}
