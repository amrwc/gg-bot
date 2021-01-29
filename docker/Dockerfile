FROM openjdk:11-jre-buster

EXPOSE 8000
EXPOSE 8080
RUN addgroup --system spring
RUN adduser --system --no-create-home --ingroup spring spring

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
