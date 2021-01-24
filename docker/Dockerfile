##############################################################################
############################# Build the project ##############################
##############################################################################
FROM gradle:jdk11 AS gradle_container

RUN mkdir -p /home/gradle/project/src/main
COPY build.gradle /home/gradle/project
COPY settings.gradle /home/gradle/project
COPY src/main /home/gradle/project/src/main

WORKDIR /home/gradle/project
RUN chown gradle:gradle --recursive .

# Mount the cache from the previous builds. If the cache volume doesn't exist,
# create it before running `docker build`:
# docker volume create --name gradle-cache
VOLUME gradle-cache /home/gradle/.gradle

USER gradle
RUN gradle build -x test

##############################################################################
############################## Run the project ###############################
##############################################################################
FROM openjdk:11-jre-buster AS renameme

EXPOSE 8000
EXPOSE 8080
RUN addgroup --system spring && adduser --system --no-create-home --ingroup spring spring

WORKDIR /home/project
COPY docker/entrypoint.sh entrypoint.sh
COPY --from=gradle_container /home/gradle/project/build/libs/*.jar app.jar
RUN chown spring:spring --recursive .
RUN chmod 0554 entrypoint.sh

ARG debug="false"
ARG suspend="false"
ENV DEBUG="${debug}"
ENV SUSPEND="${suspend}"

USER spring
ENTRYPOINT ["./entrypoint.sh"]
