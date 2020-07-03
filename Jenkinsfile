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
    GITHUB_ORG = 'mulesoft'
    GITHUB_REPO = 'amf'
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
          branch 'syaml-recovery-changes'
        }
      }
      steps {
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
          script {
            try{
              if (failedStage.isEmpty()) {
              sh '''
                  echo "about to publish in sbt"
                  sbt publish
                  echo "sbt publishing successful"
              '''
              }
            } catch(ignored) {
              failedStage = failedStage + " PUBLISH "
              unstable "Failed publication"
            }
          }
        }
      }
    }
    stage('Tag version') {
      when {
        branch 'master'
      }
      steps {
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'github-exchange', passwordVariable: 'GITHUB_PASS', usernameVariable: 'GITHUB_USER']]) {
          script {
            try{
              if (failedStage.isEmpty()) {
                sh '''#!/bin/bash
                      echo "about to tag the commit with the new version:"
                      version=$(sbt version | tail -n 1 | grep -o '[0-9].[0-9].[0-9].*')
                      url="https://${GITHUB_USER}:${GITHUB_PASS}@github.com/${GITHUB_ORG}/${GITHUB_REPO}"
                      git tag $version
                      git push $url $version
                      echo "tagging successful"
                '''
              }
            } catch(ignored) {
              failedStage = failedStage + " TAGGING "
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
