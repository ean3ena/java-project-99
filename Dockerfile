FROM eclipse-temurin:21-jdk

ARG GRADLE_VERSION=8.7

WORKDIR /java-project-99

COPY /java-project-99 .

RUN gradle installDist

CMD ./build/install/java-project-99/bin/java-project-99