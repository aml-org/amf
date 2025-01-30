FROM eclipse-temurin:17-focal AS openjdk17

FROM ghcr.io/aml-org/amf-ci-tools-base-image:1.4.0

COPY --from=openjdk17 /opt/java/openjdk /opt/java/openjdk17