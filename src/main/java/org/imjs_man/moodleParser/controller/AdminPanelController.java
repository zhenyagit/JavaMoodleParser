package org.imjs_man.moodleParser.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminPanelController {

    @RequestMapping(value = { "/admin" }, method = RequestMethod.GET)
    public String sayHello() {
        return "adminPage";
    }
}
