#!groovy
@Library('amf-jenkins-library') _

def slackChannel = '#amf-jenkins'
def failedStage = ""
def color = '#FF8C00'
def headerFlavour = "WARNING"

pipeline {
  options {
    timeout(time: 30, unit: 'MINUTES')
  }
  agent {
    dockerfile {
      filename 'Dockerfile'
      label 'gn-8-16-1'
      registryCredentialsId 'dockerhub-pro-credentials'
    }
  }
  environment {
    NEXUS = credentials('exchange-nexus')
    NEXUSIQ = credentials('nexus-iq')
    GITHUB_ORG = 'aml-org'
    GITHUB_REPO = 'amf'
  }
  stages {
    stage('Test') {
      steps {
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'XTerm']) {
          script {
            try{
              sh 'sbt -mem 6144 -Dfile.encoding=UTF-8 clean coverage test coverageReport'
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
        anyOf {
          branch 'master'
          branch 'support/*'
        }
      }
      steps {
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'github-salt', passwordVariable: 'GITHUB_PASS', usernameVariable: 'GITHUB_USER']]) {
          script {
            try{
              if (failedStage.isEmpty()) {
                def version = sbtArtifactVersion("apiContractJVM")
                tagCommitToGithub(version)
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
          branch 'master'
          branch 'support/*'
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
        anyOf {
          branch 'develop'
          branch 'release/*'
        }
      }
      steps {
        script {
          try {
            if (failedStage.isEmpty()){
              echo "Starting TCKutor Applications/AMF/amfTCKutor/master"
              build job: 'application/AMF/amfTCKutor/master', wait: false

              echo "Starting Amf Examples Applications/AMF/amfexamples/master"
              build job: 'application/AMF/amf-examples/snapshot', wait: false

              echo "Starting Amf Interface Tests Applications/AMF/amfinterfacetests/master"
              build job: 'application/AMF/amf-interface-tests/master', wait: false

              if (env.BRANCH_NAME == 'develop') {
                build job: "application/AMF/amf-metadata/${env.BRANCH_NAME}", wait: false
              } else {
                echo "Skipping Amf Metadata Tests Build Trigger as env.BRANCH_NAME is not master or develop"
              }
              def newAmfVersion = sbtArtifactVersion("apiContractJVM")
              echo "Starting ApiQuery hook API-Query/api-query-amf-integration/master with amf version: ${newAmfVersion}"
              build job: "API-Query-new/api-query-amf-integration/master", wait: false, parameters: [[$class: 'StringParameterValue', name: 'AMF_NEW_VERSION', value: newAmfVersion]]
            }
          } catch(ignored) {
            failedStage = failedStage + " JOBS TRIGGER "
            unstable "Failed triggering downstream jobs"
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
            } else if (env.BRANCH_NAME == 'develop') {
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

Boolean isDevelop() {
  env.BRANCH_NAME == "develop"
}
