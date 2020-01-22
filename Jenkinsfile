#!groovy
def slackChannel = '#amf-jenkins'
def failedStage = ""
def color = '#FF8C00'
def headerFlavour = "WARNING"

pipeline {
  agent {
    dockerfile true
  }
  environment {
    NEXUS = credentials('exchange-nexus')
    NEXUSIQ = credentials('nexus-iq')
  }
  stages {
    stage('Test') {
      steps {
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
          script {
            try{
              sh 'sbt -mem 4096 -Dfile.encoding=UTF-8 clean coverage test coverageReport'
            } catch (ignored) {
              failedStage = failedStage + " TEST "
              unstable "Failed tests"
            }
          }
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
            script {
              try {
                if (failedStage.isEmpty()) {
                  sh 'sbt -Dsonar.host.url=${SONAR_SERVER_URL} sonarScan'
                }
              } catch (ignored) {
                failedStage = failedStage + " COVERAGE "
                unstable "Failed coverage"
              }
            }
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
          script {
            try{
              if (failedStage.isEmpty()) {
                sh 'sbt publish'
              }
            } catch(ignored) {
              failedStage = failedStage + " PUBLISH "
              unstable "Failed publication"
            }
          }
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
          script {
            try{
              if (failedStage.isEmpty()){
                sh './gradlew nexusIq'
              }
            } catch(ignored) {
              failedStage = failedStage + " NEXUSIQ "
              unstable "Failed Nexus IQ"
            }
          }
        }
      }
    }
    stage('Trigger amf projects') {
      when {
        branch 'develop'
      }
      steps {
        script {
          try {
            if (failedStage.isEmpty()){
              echo "Starting TCKutor Applications/AMF/amfTCKutor/master"
              build job: 'application/AMF/amfTCKutor/master', wait: false

              echo "Starting TCKutor Applications/AMF/amfexamples/master"
              build job: 'application/AMF/amf-examples/snapshot', wait: false

              echo "Starting TCKutor Applications/AMF/amfinterfacetests/master"
              build job: 'application/AMF/amf-interface-tests/master', wait: false
            }
          } catch(ignored) {
            failedStage = failedStage + " TCKUTOR "
            unstable "Failed TCKUTOR job trigger"
          }
        }
      }
    }
    stage("Report to Slack") {
      when {
        anyOf {
          branch 'master'
          branch 'develop'
          branch 'release/*'
        }
      }
      steps {
        script {
          if (!failedStage.isEmpty()) {
            if (env.BRANCH_NAME == 'master') {
              color = '#FF0000'
              headerFlavour = "RED ALERT"
            } else if (env.BRANCH_NAME == 'devel') {
              color = '#FFD700'
            }
            slackSend color: color, channel: "${slackChannel}", message: ":alert: ${headerFlavour}! :alert: Build failed!. \n\tBranch: ${env.BRANCH_NAME}\n\tStage:${failedStage}\n(See ${env.BUILD_URL})\n"
            currentBuild.status = "FAILURE"
          } else if (env.BRANCH_NAME == 'master') {
            slackSend color: '#00FF00', channel: "${slackChannel}", message: ":ok_hand: Master Publish OK! :ok_hand:"
          }
        }
      }
    }
  }
}
