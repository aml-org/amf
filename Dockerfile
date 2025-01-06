FROM ghcr.io/aml-org/amf-ci-tools-base-image:1.4.0

RUN apt-get update && \
    apt-get install -y openjdk-11-jdk

RUN mkdir -p /opt/java/openjdk11

RUN mv /usr/lib/jvm/java-11-openjdk-amd64/* /opt/java/openjdk11/

RUN rm -rf /usr/lib/jvm/java-11-openjdk-amd64

RUN /opt/java/openjdk11/bin/java -version