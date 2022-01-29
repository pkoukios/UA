FROM openjdk:11-jdk
ARG NAME
ADD $NAME app.jar
VOLUME /tmp
VOLUME /certificates
COPY certs/* /

RUN ls $JAVA_HOME
RUN keytool -import -trustcacerts -noprompt -keystore $JAVA_HOME/lib/security/cacerts -alias ua-unified -file ua-unified-cert.pem -storepass changeit

ENTRYPOINT ["java", "-jar", "app.jar"]
