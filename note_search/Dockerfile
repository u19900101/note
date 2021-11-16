FROM java:8

EXPOSE 8080

ADD note_search-0.0.1-SNAPSHOT.jar note_search.jar
RUN bash -c 'touch /note_search-0.0.1-SNAPSHOT.jar'

ENTRYPOINT ["java", "-jar", "/note_search.jar", "--spring.profiles.active=pro"]