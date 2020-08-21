pipeline {
    agent any
    tools {
        maven 'maven'
    }
    stages {
        stage('Maven Version') {
            steps {
                bat 'mvn --version'
            }
        }
        stage('Java Version') {
                    steps {
                        bat 'javac -version'
                        bat 'java -version'
                    }
                }
        stage('Maven Build and Sonar Anaylysis') {
                     steps {
                            
							bat 'mvn clean package sonar:sonar'
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