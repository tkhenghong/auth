FROM adoptopenjdk/openjdk11:latest as build

COPY --chown=gradle:gradle . /home/gradle/src

WORKDIR /home/gradle/src

RUN ./gradlew build --no-daemon
# ----------------------
FROM adoptopenjdk/openjdk11:alpine-jre

RUN mkdir /app

# RUN addgroup --system javauser && adduser --system --group javauser
RUN addgroup --system javauser && adduser -S -s /bin/false -G javauser javauser

COPY --from=build /home/gradle/src/build/libs/*.jar /app/spring-boot-application.jar

WORKDIR /app

RUN chown -R javauser:javauser /app

USER javauser

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-Djava.security.egd=file:/dev/./urandom","-jar","/app/spring-boot-application.jar"]
