#!groovy

pipeline {
    agent any
    stages {
        stage ('Build') {
            steps {
                sh 'mvn -version'
                sh 'sbt compile'
            }
        }
        stage ('Test') {
            steps {
                sh 'sbt test'
            }
        }
    }
}
