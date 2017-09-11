#!groovy

pipeline {
  agent {
    docker {
      image 'mulesoft/maelstrom-sbtnode-builder:v0.2.3'
      registryUrl 'https://devdocker.mulesoft.com:18078'
      registryCredentialsId 'quay-docker-registry'
    }
  }
  environment {
    NEXUS = credentials('nexus')
    SONAR = credentials('sonar-mulesoft')
    SONAR_URL = 'http://es.sandbox.msap.io/sonar'
  }
  stages {
    stage('Test') {
      steps {
        sh 'sbt clean coverage test coverageReport'
      }
    }
    stage('Sonar') {
      steps {
        sh 'sbt sonar'
      }
    }
    stage('Publish') {
      steps {
        sh 'sbt publish'
      }
    }
  }
}
