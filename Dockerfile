FROM maven:3.6-alpine as BUILDER

WORKDIR /opt/uuusa
COPY pom.xml .
RUN mvn -B -e -C -T 1C org.apache.maven.plugins:maven-dependency-plugin:3.0.2:go-offline
COPY . .
RUN mvn -B -e -o -T 1C clean install

FROM openjdk:8-alpine
WORKDIR /opt/uuusa
COPY --from=builder /opt/uuusa/target/samulan-0.1.1.jar .
ADD config config
ADD taggers /opt/uuusa/data/taggers
ADD parsers /opt/uuusa/data/parsers
RUN mkdir /opt/uuusa/data/profiles
EXPOSE 8080
CMD [ "java", "-jar", "samulan-0.1.1.jar", "-springboot"]
