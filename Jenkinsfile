#!groovy

pipeline {
  sh "git clean -fxd"
  agent {
    dockerfile {
      additionalBuildArgs '--no-cache'
    }
  }
  environment {
    NEXUS = credentials('exchange-nexus')
    NEXUSIQ = credentials('nexus-iq')
  }
  stages {
    stage('Test') {
      steps {
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
          sh 'sbt -mem 4096 -Dfile.encoding=UTF-8 clean coverage test coverageReport'
        }
      }
    }
    stage('Coverage') {
      when {
        anyOf {
          branch 'master'
          branch 'develop'
        }
      }
      steps {
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
          withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'sonarqube-official', passwordVariable: 'SONAR_SERVER_TOKEN', usernameVariable: 'SONAR_SERVER_URL']]) {
            sh 'sbt -Dsonar.host.url=${SONAR_SERVER_URL} sonarScan'
          }
        }
      }
    }
    stage('Publish') {
      when {
        anyOf {
          branch 'master'
          branch 'develop'
          branch 'release/*'
        }
      }
      steps {
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
          sh 'sbt publish'
        }
      }
    }
    stage('Nexus IQ') {
      when {
        anyOf {
          branch 'develop'
        }
      }
      steps {
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
          sh './gradlew nexusIq'
        }
      }
    }
    stage('Trigger amf projects') {
      when {
        branch 'develop'
      }
      steps {
        echo "Starting TCKutor Applications/AMF/amfTCKutor/master"
        build job: 'application/AMF/amfTCKutor/master', wait: false

        echo "Starting TCKutor Applications/AMF/amfexamples/master"
        build job: 'application/AMF/amf-examples/snapshot', wait: false

        echo "Starting TCKutor Applications/AMF/amfinterfacetests/master"
        build job: 'application/AMF/amf-interface-tests/master', wait: false
      }
    }
  }
}
