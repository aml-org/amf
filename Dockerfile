FROM eclipse-temurin:17-focal AS openjdk17
RUN mkdir -p /opt/java/openjdk17
RUN cp -r /opt/java/openjdk/* /opt/java/openjdk17/

FROM ghcr.io/aml-org/amf-ci-tools-base-image:1.4.0