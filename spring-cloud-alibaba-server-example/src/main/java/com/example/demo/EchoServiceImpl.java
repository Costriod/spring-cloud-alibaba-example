package com.example.demo;

import com.alibaba.fastjson.JSONObject;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@org.apache.dubbo.config.annotation.Service
public class EchoServiceImpl implements EchoService {
    @RequestMapping("/echo")
    @Override
    public String echo(@RequestParam("message") String message) {
        RpcContext serverContext = RpcContext.getServerContext();
        RpcContext context = RpcContext.getContext();
        System.out.println(context.getAttachments());
        System.out.println(serverContext.getAttachments());
        System.out.println(JSONObject.toJSONString(context.getArguments()));
        System.out.println(JSONObject.toJSONString(serverContext.getArguments()));

        return "this is message: " + message;
    }
}
