package com.example.demo;

import com.alibaba.cloud.dubbo.annotation.DubboTransported;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "spring-cloud-alibaba-server")
public interface EchoService {
    /**
     * 集成sleuth + zipkin，并且使用dubbo协议替代feign client的时候，必须采用@DubboTransported注解，而且client和server的
     * 接口方法参数必须一一对应，并且注解也要对应上，否则RestMethodMetadata无法对上（feign如果参数对不上可能启动失败）
     *
     * 问题一：
     * 目前有一个问题就是必须在@DubboTransported注解声明使用tracing filter，比较坑爹，我也不确定是否是一个bug。
     * 经实测dubbo.consumer.filter或dubbo.provider.filter是不生效的
     *
     * 问题二：
     * 使用tracing过程中，底层初始化filter chain的时候，是通过ExtentsionLoader自动执行inject操作，inject Tracing
     * 利用的是SpringExtensionFactory，问题就在这里dubbo改造feign接口的时候，SpringExtensionFactory的CONTEXTS是一个空set，
     * 在这个时候inject的参数是null，解决方式是强制性注册context到SpringExtensionFactory
     * 详情参考{@link ExtensionLoader#injectExtension(java.lang.Object)}
     * @param message
     * @return
     */
    @RequestMapping(value = "/echo")
    @DubboTransported(filter = {"tracing"})
    String echo(@RequestParam("message") String message);
}
