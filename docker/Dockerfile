FROM openjdk:11.0-jre-slim-buster

EXPOSE 8000
EXPOSE 8080
RUN addgroup --gid 10001 --system spring
RUN adduser --ingroup spring --no-create-home --system --uid 10000 spring

WORKDIR /home/project
COPY docker/entrypoint.sh entrypoint.sh
RUN chown spring:spring --recursive .
RUN chmod 0554 entrypoint.sh

ARG debug="false"
ARG suspend="false"
ENV DEBUG="${debug}"
ENV SUSPEND="${suspend}"

USER spring
ENTRYPOINT ["./entrypoint.sh"]
