FROM maven:3.6-alpine as DEPS

WORKDIR /opt/uuusa
COPY pom.xml .
RUN mvn -B -e -C org.apache.maven.plugins:maven-dependency-plugin:3.0.2:go-offline

FROM maven:3.6-alpine as BUILDER
WORKDIR /opt/uuusa
COPY --from=deps /root/.m2 /root/.m2
COPY --from=deps /opt/uuusa/pom.xml .
COPY src src
RUN mvn -B -e -o clean install -DskipTests=true

FROM openjdk:8-alpine
WORKDIR /opt/uuusa
COPY --from=builder /opt/uuusa/target/samulan-0.1.1.jar .
ADD config config
ADD data/taggers /opt/uuusa/data/taggers
ADD data/parsers /opt/uuusa/data/parsers
ADD entrypoint.sh .
RUN mkdir /opt/uuusa/data/profiles
EXPOSE 8080
CMD [ "./entrypoint.sh" ]
# CMD [ "java", "-jar", "samulan-0.1.1.jar", "-springboot"]


FROM node:alpine as B
WORKDIR /app
COPY package.json /app/
COPY package-lock.json /app
RUN npm install
COPY . /app/
RUN npm run-script build

FROM nginx:alpine
COPY --from=B /app .
EXPOSE 5000
CMD serve -p 5000 -s /app/build