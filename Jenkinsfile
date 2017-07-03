#!groovy

pipeline {
    agent any
    stages {
        stage ('Build') {
            steps {
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
