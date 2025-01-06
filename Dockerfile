FROM ghcr.io/aml-org/amf-ci-tools-base-image:1.4.0

FROM eclipse-temurin:17-focal AS openjdk17
RUN mkdir -p /opt/java/openjdk17
RUN cp -r /usr/lib/jvm/java-17-openjdk-amd64/* /opt/java/openjdk17/