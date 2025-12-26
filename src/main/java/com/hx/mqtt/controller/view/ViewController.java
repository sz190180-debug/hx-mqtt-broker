package com.hx.mqtt.controller.view;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ViewController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    @GetMapping("/map")
    public String map() {
        return "map";
    }

    @GetMapping("/config")
    public String config() {
        return "config";
    }

    @GetMapping("/user")
    public String configUser() {
        return "user";
    }

    @GetMapping("/amr")
    public String amr() {
        return "amr";
    }

    @GetMapping("/template")
    public String template() {
        return "template";
    }

    @GetMapping("/warehouse")
    public String warehouse() {
        return "warehouse";
    }

    @GetMapping("/machine")
    public String machine() {
        return "machine";
    }
}