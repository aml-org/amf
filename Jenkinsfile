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
    NPM_TOKEN = credentials('npm-mulesoft')
  }
  stages {
    stage('Test') {
      steps {
        sh 'sbt clean coverage test coverageReport'
      }
    }
    stage('Static Code Analysis') {
      steps {
        sh "sonar-scanner -Dsonar.host.url=${env.SONAR_SERVER_URL} -Dsonar.login=${env.SONAR_SERVER_TOKEN}"
      }
    }
    stage('Publish') {
      steps {
        sh 'sbt publish'
      }
    }
  }
}
