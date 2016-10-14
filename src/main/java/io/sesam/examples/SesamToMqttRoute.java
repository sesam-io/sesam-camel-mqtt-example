package io.sesam.examples;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SesamToMqttRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        getContext().setTracing(true);
        from("sesam-local://out")
                .to("mqtt://samples?host=tcp://broker.hivemq.com:1883&publishTopicName=testtopic/2");
    }
}