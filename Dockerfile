FROM amazoncorretto:24-alpine

RUN apk add --no-cache bash openssh-client

LABEL org.opencontainers.image.title="Itau Backend"
LABEL org.opencontainers.image.description="Backend application for Itau project"

ENV MAVEN_HOME=/usr/share/maven

COPY --from=maven:3.9.9-amazoncorretto-21 ${MAVEN_HOME} ${MAVEN_HOME}
COPY --from=maven:3.9.9-amazoncorretto-21 /usr/local/bin/mvn-entrypoint.sh /usr/local/bin/mvn-entrypoint.sh
COPY --from=maven:3.9.9-amazoncorretto-21 /usr/share/maven/ref/settings-docker.xml /usr/share/maven/ref/settings-docker.xml

RUN ln -s ${MAVEN_HOME}/bin/mvn /usr/bin/mvn

ARG MAVEN_VERSION=3.9.9
ARG USER_HOME_DIR="/root"
ENV MAVEN_CONFIG="$USER_HOME_DIR/.m2"

WORKDIR /app

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

EXPOSE 8080

CMD ["mvn", "spring-boot:run"]