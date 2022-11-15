#!groovy
@Library('amf-jenkins-library') _

import groovy.transform.Field

def SLACK_CHANNEL = '#amf-jenkins'
def PRODUCT_NAME = "AMF"
def lastStage = ""
def color = '#FF8C00'
def headerFlavour = "WARNING"
@Field TCKUTOR_JOB = "application/AMF/amfTCKutor/master"
@Field INTERFACES_JOB = "application/AMF/amf-interface-tests/master"
@Field METADATA_JOB = "application/AMF/amf-metadata/develop"
@Field API_QUERY_JOB = "API-Query-new/api-query-amf-integration/master"
@Field APB_JOB = "APB/apb/develop"
@Field EXAMPLES_JOB = "application/AMF/examples/master"

pipeline {
    options {
        timeout(time: 30, unit: 'MINUTES')
        ansiColor('xterm')
    }
    agent {
        dockerfile {
            registryCredentialsId 'dockerhub-pro-credentials'
            registryCredentialsId 'github-salt'
            registryUrl 'https://ghcr.io'
            label 'gn-8-16-1'
        }
    }
    environment {
        NEXUS = credentials('exchange-nexus')
        NEXUSIQ = credentials('nexus-iq')
        GITHUB_ORG = 'aml-org'
        GITHUB_REPO = 'amf-core'
        BUILD_NUMBER = "${env.BUILD_NUMBER}"
        BRANCH_NAME = "${env.BRANCH_NAME}"
        NPM_TOKEN = credentials('aml-org-bot-npm-token')
        NPM_CONFIG_PRODUCTION = true
        CURRENT_VERSION = sh(script: "cat dependencies.properties | grep \"version\" | cut -d '=' -f 2", returnStdout: true)
    }
    stages {
        stage('Test') {
            steps {
                script {
                    lastStage = env.STAGE_NAME
                    sh 'sbt -mem 6144 -Dfile.encoding=UTF-8 clean coverage test coverageAggregate'
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
                        sh 'sbt -Dsonar.host.url=${SONAR_SERVER_URL} -Dsonar.login=${SONAR_SERVER_TOKEN} sonarScan'
                    }
                }
            }
        }
        stage('Build JS Package') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
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
                    branch 'master'
                    branch 'develop'
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
                }
            }
            steps {
                script {
                    lastStage = env.STAGE_NAME

                    echo "Starting TCKutor $TCKUTOR_JOB"
                    build job: TCKUTOR_JOB, wait: false

                    echo "Starting Amf Interface Tests $INTERFACES_JOB"
                    build job: INTERFACES_JOB, wait: false

                    echo "Starting $METADATA_JOB"
                    build job: METADATA_JOB, wait: false

                    echo "Starting $APB_JOB"
                    build job: APB_JOB, wait: false

                    echo "Starting $EXAMPLES_JOB"
                    build job: EXAMPLES_JOB, wait: false

                    def newAmfVersion = sbtArtifactVersion("apiContractJVM")
                    echo "Starting ApiQuery hook $API_QUERY_JOB with amf version: ${newAmfVersion}"
                    build job: API_QUERY_JOB, wait: false, parameters: [[$class: 'StringParameterValue', name: 'AMF_NEW_VERSION', value: newAmfVersion]]
                }
            }
        }
    }
    post {
        unsuccessful {
            failureSlackNotify(lastStage, SLACK_CHANNEL, PRODUCT_NAME)
        }
        success {
            successSlackNotify(SLACK_CHANNEL, PRODUCT_NAME)
        }
    }
}
