package com.example.demo;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "spring-cloud-alibaba-server")
public interface HelloFeignClient {
    @RequestMapping(value = "/echoFeign")
    String echo(@RequestParam("message")String message);
}
