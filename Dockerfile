# Hack for Nexus-IQ, the plugin requires Java 17
FROM eclipse-temurin:17-noble AS openjdk17

FROM eclipse-temurin:25-noble

# Hack for Nexus-IQ, the plugin requires Java 17
COPY --from=openjdk17 /opt/java/openjdk /opt/java/openjdk17

ARG USER_HOME_DIR="/root"
ENV SCALA_VERSION=2.12.21
ENV SBT_VERSION=1.12.1
ENV LANG=en_US.UTF-8
ENV LANGUAGE=en_US:en
ENV LC_ALL=en_US.UTF-8
ENV JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF8

# 2. Optimization: Combine apt-get updates and installs to reduce layers and cleanup after
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        software-properties-common \
        unzip \
        htop \
        rsync \
        openssh-client \
        jq \
        locales \
        git \
        curl \
        gnupg \
        ca-certificates && \
    echo "en_US UTF-8" >> /etc/locale.gen && \
    dpkg-reconfigure locales && \
    locale-gen en_US.UTF-8 && \
    localedef -c -i en_US -f UTF-8 en_US.UTF-8 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Verify Java installation
RUN java -version

# Install Scala from GitHub
RUN apt-get install curl --assume-yes && \
    curl -fsL "https://github.com/scala/scala/releases/download/v$SCALA_VERSION/scala-$SCALA_VERSION.tgz" | tar xfz - -C /root/

ENV PATH="/root/scala-$SCALA_VERSION/bin:${PATH}"

# 4. Install sbt from GitHub
RUN \
  curl -fsL "https://github.com/sbt/sbt/releases/download/v$SBT_VERSION/sbt-$SBT_VERSION.tgz" | tar xfz - -C /usr/local && \
  ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt && \
  sbt --allow-empty -Dsbt.rootdir=true -Djava.io.tmpdir=$HOME sbtVersion

VOLUME "$USER_HOME_DIR/.sbt"

# 5. Install Node.js 20.x
# Combined setup and install into one block
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
      apt-get install -y nodejs && \
      npm install -g npm@latest && \
      apt-get clean && \
      rm -rf /var/lib/apt/lists/*

# 6. Certificate setup for Sonar CLI
# (Ensure your 'certs/' folder exists in the same directory as this Dockerfile)
COPY certs/ /usr/local/share/ca-certificates/

# Import certs. Note: We use a loop or sequential run to ensure failure visibility
RUN keytool -import -trustcacerts -alias salesforce_internal_root_ca_1 -file /usr/local/share/ca-certificates/Salesforce_Internal_GIA_Root_CA_1.pem -cacerts -storepass changeit -noprompt && \
    keytool -import -trustcacerts -alias salesforce_internal_root_ca_4 -file /usr/local/share/ca-certificates/Salesforce_Internal_Root_CA_4.pem -cacerts -storepass changeit -noprompt && \
    keytool -import -trustcacerts -alias salesforce_internal_root_ca_3 -file /usr/local/share/ca-certificates/Salesforce_Internal_Root_CA_3.pem -cacerts -storepass changeit -noprompt && \
    update-ca-certificates

# 7. Final user config
# Create jenkins user
RUN useradd --create-home --shell /bin/bash jenkins

USER jenkins
WORKDIR /home/jenkins