#!groovy

pipeline {
  agent {
    dockerfile true
  }
  environment {
    NEXUS = credentials('exchange-nexus')
  }
  stages {
    stage('Test') {
      steps {
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
          withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'sonarqube-official', passwordVariable: 'SONAR_SERVER_TOKEN', usernameVariable: 'SONAR_SERVER_URL']]) {
            sh 'sbt -mem 4096 -Dfile.encoding=UTF-8 clean coverage test coverageReport sonarMe'
          }
        }
      }
    }
    stage('Publish') {
      when {
        anyOf {
          branch 'master'
          branch 'build/develop'
          branch 'release/*'
          branch 'support/*'
        }
      }
      steps {
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
          sh 'sbt publish'
        }
      }
    }
    stage('Trigger amf projects') {
      when {
        branch 'build/develop'
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