FROM java:8-jre-alpine

ADD target/camel-demo-0.0.1-SNAPSHOT.jar /srv/

ENTRYPOINT ["java", "-jar", "/srv/camel-demo-0.0.1-SNAPSHOT.jar"]

