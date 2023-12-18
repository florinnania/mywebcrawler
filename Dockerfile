FROM openjdk:17-jdk-alpine as build
WORKDIR /workspace/app

COPY . /workspace/app
RUN ./gradlew clean build
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/mywebcrawler-0.0.1-SNAPSHOT.jar)

FROM openjdk:17-jdk-alpine

VOLUME /tmp
ARG DEPENDENCY=/workspace/app/build/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

CMD ["java","-cp","app:app/lib/*","com.example.mywebcrawler.MywebcrawlerApplication"]