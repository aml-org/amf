FROM ghcr.io/aml-org/amf-ci-tools-base-image:1.3.2

# Use the Sonar scanner CLI as the base image
FROM sonarsource/sonar-scanner-cli:5.0.1 AS base

# Update and install necessary packages
RUN apk update && apk upgrade && apk add --no-cache curl jq

# Copy certs from your project directory to the shared directory within the container
COPY certs/ /usr/local/share/ca-certificates/

# Import certs into the Java keystore for Sonar CLI
RUN keytool -import -trustcacerts -alias salesforce_internal_root_ca_1 -file /usr/local/share/ca-certificates/Salesforce_Internal_GIA_Root_CA_1.pem -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -noprompt
RUN keytool -import -trustcacerts -alias salesforce_internal_root_ca_4 -file /usr/local/share/ca-certificates/Salesforce_Internal_Root_CA_4.pem -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -noprompt
RUN keytool -import -trustcacerts -alias salesforce_internal_root_ca_3 -file /usr/local/share/ca-certificates/Salesforce_Internal_Root_CA_3.pem -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -noprompt

# Update CA certificates for general system use
RUN update-ca-certificates

# Copy any required scripts (adjust paths as needed)
COPY scripts/shared-functions.sh /usr/bin
COPY scripts/cli.sh /usr/bin
COPY scripts/cli-groovy-build-scripts.sh /usr/bin

# Set the entry point and user as specified
ENTRYPOINT [""]
USER 2020
