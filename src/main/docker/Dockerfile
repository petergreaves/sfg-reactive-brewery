FROM openjdk:11-jre-slim as builder
#ENV project.build.finalName=foo
WORKDIR application
ADD sfg-reactive-brewery-0.0.1-SNAPSHOT.jar sfg-reactive-brewery-0.0.1-SNAPSHOT.jar
#ADD maven/${project.build.finalName}.jar ${project.build.finalName}.jar
RUN java -Djarmode=layertools -jar sfg-reactive-brewery-0.0.1-SNAPSHOT.jar extract

FROM openjdk:11-jre-slim
LABEL PROJECT_NAME=sfg-reactive-brewery \
      PROJECT=brewery-ms

EXPOSE 8080

WORKDIR application
COPY --from=builder application/dependencies/ ./
COPY --from=builder application/spring-boot-loader/ ./
COPY --from=builder application/snapshot-dependencies/ ./
COPY --from=builder application/application/ ./
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "org.springframework.boot.loader.JarLauncher"]