#!groovy
@Library('amf-jenkins-library') _

def SLACK_CHANNEL = '#amf-jenkins'
def PRODUCT_NAME = "AMF"
def lastStage = ""
def color = '#FF8C00'
def headerFlavour = "WARNING"

pipeline {
  options {
    timeout(time: 30, unit: 'MINUTES')
    ansiColor('xterm')
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
    NPM_TOKEN = credentials('aml-org-bot-npm-token')
    NPM_CONFIG_PRODUCTION = true
  }
  stages {
    stage('Test') {
      steps {
        script {
          lastStage = env.STAGE_NAME
          sh 'sbt -mem 6144 -Dfile.encoding=UTF-8 clean coverage test coverageReport'
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
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'sonarqube-official', passwordVariable: 'SONAR_SERVER_TOKEN', usernameVariable: 'SONAR_SERVER_URL']]) {
          script {
            lastStage = env.STAGE_NAME
            sh 'sbt -Dsonar.host.url=${SONAR_SERVER_URL} sonarScan'
          }
        }
      }
    }
    stage('Build JS Package') {
      when {
        anyOf {
          branch 'master'
          branch 'develop'
          branch 'release/*'
        }
      }
      steps {
        script {
          lastStage = env.STAGE_NAME
          sh 'chmod +x js-build.sh'
          sh './js-build.sh'
        }
      }
    }
    stage('Publish JVM Artifact') {
      when {
        anyOf {
          branch 'master'
          branch 'develop'
          branch 'release/*'
        }
      }
      steps {
        script {
          lastStage = env.STAGE_NAME
          sh 'sbt publish'
        }
      }
    }
    stage("Publish JS Package") {
      when {
        anyOf {
          branch 'master'
          branch 'develop'
          branch 'release/*'
        }
      }
      steps {
        script {
          lastStage = env.STAGE_NAME
          // They are separate commands because we want an earlyExit in case one of them doesnt end with exit code 0
          sh 'chmod +x ./scripts/setup-npmrc.sh'
          sh './scripts/setup-npmrc.sh'
          sh 'chmod +x ./js-publish.sh'
          sh './js-publish.sh'
        }
      }
    }
    stage('Tag version') {
      when {
        anyOf {
          branch 'master'
          branch 'support/*'
          branch 'release/*'
        }
      }
      steps {
        withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'github-salt', passwordVariable: 'GITHUB_PASS', usernameVariable: 'GITHUB_USER']]) {
          script {
            lastStage = env.STAGE_NAME
            def version = sbtArtifactVersion("apiContractJVM")
            tagCommitToGithub(version)
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
        script {
          lastStage = env.STAGE_NAME
          sh './gradlew nexusIq'
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
          lastStage = env.STAGE_NAME
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
      }
    }
  }
  post {
    unsuccessful {
      script {
        if (isMaster() || isDevelop()) {
          sendBuildErrorSlackMessage(lastStage, SLACK_CHANNEL, PRODUCT_NAME)
        } else {
          echo "Unsuccessful build: skipping slack message notification as branch is not master or develop"
        }
      }
    }
    success {
      script {
      echo "SUCCESSFULL BUILD"
        if (isMaster()) {
          sendSuccessfulSlackMessage(lastStage, SLACK_CHANNEL, PRODUCT_NAME)
        } else {
          echo "Successful build: skipping slack message notification as branch is not master"
        }
      }
    }
  }
}

Boolean isDevelop() {
  env.BRANCH_NAME == "develop"
}

Boolean isMaster() {
  env.BRANCH_NAME == "master"
}

def sendBuildErrorSlackMessage(String lastStage, String slackChannel, String productName) {
  def color = '#FF8C00'
  def headerFlavour = 'WARNING'
  if (isMaster()) {
    color = '#FF0000'
    headerFlavour = "RED ALERT"
  } else if (isDevelop()) {
    color = '#FFD700'
  }
  def message = """:alert: ${headerFlavour}! :alert: Build failed!.
                  |Branch: ${env.BRANCH_NAME}
                  |Stage: ${lastStage}
                  |Build URL: ${env.BUILD_URL}""".stripMargin().stripIndent()
  slackSend color: color, channel: "${slackChannel}", message: message
}

def sendSuccessfulSlackMessage(String slackChannel) {
  slackSend color: '#00FF00', channel: "${slackChannel}", message: ":ok_hand: ${productName} Master Publish OK! :ok_hand:"
}
