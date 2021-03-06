FROM openjdk:8-jre
VOLUME /tmp
ARG JAR_FILE
COPY ${JAR_FILE} app.jar
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone
ENTRYPOINT ["java","-XX:+UnlockExperimentalVMOptions","-XX:+UseCGroupMemoryLimitForHeap","-XX:MaxRAMFraction=2","-Djava.security.egd=file:/dev/urandom","-jar","/app.jar"]