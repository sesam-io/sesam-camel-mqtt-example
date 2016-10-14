==============================================
Spring Boot example with MQTT, Camel and Sesam
==============================================

This example demonstrates how to consume data from Sesam and publish it to a topic on a public `MQTT <https://en.wikipedia.org/wiki/MQTT>`_ broker. It also demonstrates how to subscribe to a MQTT topic and send that data to Sesam.
 
The example was created using the `camel-spring-boot-starter <https://camel.apache.org/spring-boot.html>`_ tool.

Camel Spring Boot Starter
=========================

The project was created using the wizard on <https://start.spring.io/>, with Apache Camel dependency.

In order to keep the application running, we need to set ``camel.springboot.main-run-controller`` to ``true`` in ``application.properties``.

The bootstrapping class ``CamelDemoApplication`` is automatically created by the Spring Boot Wizard, so we only need to add a couple route components:

::
    
    @Component
    public class SesamToMqttRoute extends RouteBuilder {
    
        @Override
        public void configure() throws Exception {
            getContext().setTracing(true);
            from("sesam-local://out")
                    .to("mqtt://samples?host=tcp://broker.hivemq.com:1883&publishTopicName=testtopic/2");
        }
    }

    @Component
    public class MqttToSesamRoute extends RouteBuilder {
    
        @Override
        public void configure() throws Exception {
            getContext().setTracing(true);
            from("mqtt://samples?host=tcp://broker.hivemq.com:1883&subscribeTopicNames=testtopic/1")
                    .to("sesam-local://in");
        }
    }


We enable route tracing with ``getContext().setTracing(true)`` so that we can see what is going on. The broker we use ``broken.hivemq.com`` is public broker that anyone can subscribe to, so don't publish any secrets.

The first route ``SesamToMqttRoute`` consumes entities from a published endpoint called ``out`` on a Sesam running on localhost. The route sends these entities to a topic called ``testtopic/2`` on ``broker.hivemq.com``.

The other route ``MqttToSesamRoute`` subscribes to a topic called ``testtopic/1`` on ``broker.hivemq.com`` and sends any messages that it receives to a http_endpoint source called ``in`` on a Sesam running on localhost.


Running the example
===================

Before you can start the application you will need to setup Sesam to run on ``http://localhost:9042``. See the `Getting started guide <https://docs.sesam.io/overview.html#edit-the-configuration-files>`_
for more information.

::

  ## upload sesam config
  $ sesam import sesam.conf.json
  ## post some entities that we can publish to MQTT
  $ curl -XPOST localhost:9042/api/receivers/out-data/entities -d @samples/messages.json -H "Content-Type: application/json"

Before you start this route, you should subscribe to this topic so that you can verify that the entities are delivered. On Ubuntu you can use `mosquitto <http://mosquitto.org/>`_ to do that:
 
::

    $ mosquitto_sub -h broker.hivemq.com -t testtopic/1
    
You can now build and run the example application with Docker:

::

    ## build the application with docker to prepare the image 
    ## (TODO fix this not working because sesam-camel is not published to Maven Central)
    $ docker run -it --rm \
           -v "$(pwd)":/opt/maven \
           -w /opt/maven \
           maven:3.2-jdk-7 \
           mvn clean install
    ## create the docker image
    $ docker build -t camel-demo .
    ## start the container
    $ docker run --net host camel-demo
     [..]
     
Once the ``SesamToMqttRoute`` kicks in, you will see the sample messages appear on the MQTT broker:

::

     ## output where you are running mosquitto_sub
     [..]
     {"_id": "sesam1","msg": "Hello world!"}
     {"_id": "sesam2","msg": "sesam.io was here"}
     
Now lets try to consume a unstructured message from MQTT into Sesam:

::

    $ mosquitto_pub -h broker.hivemq.com -t testtopic/1 -m "hei"
    ## wait until MqttToSesamRoute kicks in
    $ curl 'http://localhost:9042/api/datasets/in' | jq .
    [
      {
        "_id": "<generated guid>",
        "body": "hei"
      }
    ]

The Sesam Camel component will generate a ``_id`` for the message when the message doesn't have that. The message content will be embedded inside the ``body`` property.


What happens if we consume structured JSON from MQTT into Sesam:

::

    $ mosquitto_pub -h broker.hivemq.com -t testtopic/1 -m '{"msg": "hello"}'
    ## wait until MqttToSesamRoute kicks in
    $ curl 'http://localhost:9042/api/datasets/in' | jq .
    [
      [..]
      {
        "_id": "<generated guid>",
        "msg": "hello"
      }
    ]

If the content is structured JSON, then the component will only generate ``_id`` and set that. The other properties remain untouched.

If the content is a structured entity (JSON with ``_id``) the message is just passed "as-is":

::

    $ mosquitto_pub -h broker.hivemq.com -t testtopic/1 -m '{"_id": "foo", "msg": "hello"}'
    ## wait until MqttToSesamRoute kicks in
    $ curl 'http://localhost:9042/api/datasets/in' | jq .
    [
      [..]
      {
        "_id": "foo",
        "msg": "hello"
      }
    ]

Note the example uses `curl <https://curl.haxx.se/>`_ to send the request and `jq <https://stedolan.github.io/jq/>`_ prettify the response.
