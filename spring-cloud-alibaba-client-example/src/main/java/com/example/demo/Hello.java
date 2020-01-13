package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Hello {
    @Autowired
    private EchoService echoService;

    @Autowired
    private HelloFeignClient client;

    @RequestMapping("/echo")
    public String echo(String message) {
        return echoService.echo(message);
    }

    @RequestMapping("/echoFeign")
    public String echoFeign(String message) {
        return client.echo(message);
    }
}
