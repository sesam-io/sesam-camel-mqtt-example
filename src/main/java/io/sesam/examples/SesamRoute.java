package io.sesam.examples;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class SesamRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("mqtt://samples?host=tcp://broker.hivemq.com:1883&subscribeTopicNames=testtopic/1")
                .to("sesam-local://in");
    }
}