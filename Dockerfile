FROM gradle:8.9-jdk21

WORKDIR /

COPY / .

RUN ./gradlew installDist

CMD ./build/install/app/bin/app

ENV SPRING_PROFILES_ACTIVE=prod