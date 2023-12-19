FROM openjdk:17-jdk-alpine as build

# Install necessary dependencies
RUN apk add --no-cache \
    chromium \
    chromium-chromedriver \
    && rm -rf /var/cache/apk/*

WORKDIR /workspace/app

COPY . /workspace/app

RUN ./gradlew clean build
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/mywebcrawler-0.0.1-SNAPSHOT.jar)

# Download and extract ChromeDriver into build/dependency
RUN wget https://chromedriver.storage.googleapis.com/100.0.4896.20/chromedriver_linux64.zip -O chromedriver.zip
RUN unzip chromedriver.zip
RUN rm chromedriver.zip
RUN mv chromedriver /workspace/app/build/dependency

FROM openjdk:17-jdk-alpine

VOLUME /tmp
ARG DEPENDENCY=/workspace/app/build/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

CMD ["java","-cp","app:app/lib/*","com.example.mywebcrawler.MywebcrawlerApplication"]