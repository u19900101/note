FROM java:8

EXPOSE 8080

ADD note_admin-0.0.1-SNAPSHOT.jar note_admin.jar
RUN bash -c 'touch /note_admin-0.0.1-SNAPSHOT.jar'

ENTRYPOINT ["java", "-jar", "/note_admin.jar", "--spring.profiles.active=pro"]