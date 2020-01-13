# spring-cloud-alibaba踩坑

### 1.使用dubbo替代FeignClient

- 必须使用@DubboTransported标注接口的方法（如果为了省事也可以直接标注在接口上面）

- 必须保证server端的RestController必须实现接口，而且参数必须和client一致（包括注解也一致），否则dubbo协议会降级成feign的http协议（也有可能项目启动不了，feign校验RestMethodMetadata过程中发现参数不符合会抛异常）



### 2.dubbo替代FeignClient之后开启zipkin链路追踪

- 必须在@DubboTransported注解里面指定filter为tracing，配置文件里面的dubbo.consumer.filter或dubbo.provider.filter是不生效的
- 注解指定filter之后还有一个问题，如果采用dubbo替换feign client的http协议，ReferenceBean在底层初始化filter chain的时候，TracingFilter的setTracing方法注入了一个null值，根本原因是ExtensionLoader委托SpringExtensionFactory获取bean对象失败（因为SpringExtensionFactory还没来得及给CONTEXTS初始化）


```java
@FeignClient(name = "spring-cloud-alibaba-server")
public interface EchoService {
    @RequestMapping(value = "/echo")
    @DubboTransported(filter = {"tracing"})
    String echo(@RequestParam("message") String message);
}
```

上面注册了filter，但是SpringExtensionFactory此刻的CONTEXTS属性还是空set，也就意味着dubbo执行注入对象操作时的参数都是null

```java
/**
 * ExtensionLoader给对象进行注入的时候，注入的对象是委托SpringExtensionFactory从context里面获取，正如下面这段代码所示
 */
@Override
@SuppressWarnings("unchecked")
public <T> T getExtension(Class<T> type, String name) {
	//SPI should be get from SpiExtensionFactory
	if (type.isInterface() && type.isAnnotationPresent(SPI.class)) {
		return null;
	}
	for (ApplicationContext context : CONTEXTS) {//默认是空set
		if (context.containsBean(name)) {
			Object bean = context.getBean(name);
			if (type.isInstance(bean)) {
				return (T) bean;
			}
		}
	}
	logger.warn("No spring extension (bean) named:" + name + ", try to find an extension (bean) of type " + type.getName());
	if (Object.class == type) {
		return null;
	}
	for (ApplicationContext context : CONTEXTS) {//默认是空set
		try {
			return context.getBean(type);
		} catch (NoUniqueBeanDefinitionException multiBeanExe) {
			logger.warn("Find more than 1 spring extensions (beans) of type " + type.getName() + ", will stop auto injection. Please make sure you have specified the concrete parameter type and there's only one extension of that type.");
		} catch (NoSuchBeanDefinitionException noBeanExe) {
			if (logger.isDebugEnabled()) {
				logger.debug("Error when get spring extension(bean) for type:" + type.getName(), noBeanExe);
			}
		}
	}
	logger.warn("No spring extension (bean) named:" + name + ", type:" + type.getName() + " found, stop get bean.");
	return null;
}
```

解决SpringExtensionFactory的CONTEXTS为空set的方式，就是以最高优先级先给SpringExtensionFactory初始化一些context

```java
@Component
@Order(value = Ordered.HIGHEST_PRECEDENCE)
public class CustomContextAware implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringExtensionFactory.addApplicationContext(applicationContext);
    }
}
```

