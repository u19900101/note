FROM java:8

EXPOSE 88

ADD note_gateway-0.0.1-SNAPSHOT.jar note_gateway.jar
RUN bash -c 'touch /note_gateway.jar'

ENTRYPOINT ["java", "-jar", "/note_gateway.jar", "--spring.profiles.active=pro"]