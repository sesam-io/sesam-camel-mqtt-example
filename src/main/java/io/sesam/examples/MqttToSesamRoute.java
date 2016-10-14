package io.sesam.examples;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MqttToSesamRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        getContext().setTracing(true);
        from("mqtt://samples?host=tcp://broker.hivemq.com:1883&subscribeTopicNames=testtopic/1")
                .to("sesam-local://in");
    }
}