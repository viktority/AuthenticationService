pipeline {
    agent any
    tools {
        maven 'maven'
    }
    stages {
        stage('Maven Version') {
            steps {
                sh 'mvn --version'
            }
        }
        stage('Java Version') {
                    steps {
                        sh 'javac -version'
                        sh 'java -version'
                    }
                }
        stage('Maven Build') {
                     steps {
                            
							sh 'mvn clean install'
                            }
                               }
        stage('SOnar Analysis') {
                             steps {

        							sh 'mvn sonar:sonar'
                                    }
                                       }
    }
    triggers {
      bitbucketPush()
    }
    post {
      failure {
        emailext body: 'Please check your last build', replyTo: 'devops@hardcorebiometric.com', subject: 'Build Failure', to: 'dev@hardcorebiometric.com'
      }
    }
}