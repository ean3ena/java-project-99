FROM eclipse-temurin:21-jdk

ARG GRADLE_VERSION=8.7

WORKDIR /

COPY / .

RUN gradle installDist

CMD ./build/install/app/bin/app