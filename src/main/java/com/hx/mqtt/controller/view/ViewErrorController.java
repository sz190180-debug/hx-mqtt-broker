package com.hx.mqtt.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/error")
public class ViewErrorController {

    @GetMapping("/404")
    public String notFound(HttpServletRequest request, Model model) {
        // 获取原始请求URL
        String originalUri = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        model.addAttribute("originalUrl", originalUri);

        // 返回首页模板但保持错误URL
        return "error";
    }
}