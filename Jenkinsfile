pipeline {
    agent any

    options {
        timeout(time: 1, unit: 'HOURS')
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Build & Test') {
            steps {
                sh './gradlew clean build -x test' // 속도를 위해 테스트 제외 빌드 예시
            }
        }

        stage('Dockerize') {
            steps {
                sh 'docker build -t baemin-api:latest .'
            }
        }

        stage('Deploy') {
            steps {
                // Rolling Update 적용 (5개의 복제본 중 하나씩 교체)
                sh 'docker-compose up -d --no-deps --scale baemin-api=5 baemin-api'
            }
        }
    }

    post {
        always { cleanWs() }
    }
}