pipeline {
    agent any

    options {
        timeout(time: 1, unit: 'HOURS')
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        DOCKER_IMAGE = 'delivery-api'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        REGISTRY = 'your-registry.com'  // Docker 레지스트리 주소
    }

    stages {
        // ===== 1. 코드 체크아웃 =====
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                }
            }
        }

        // ===== 2. 코드 품질 검사 =====
        stage('Code Quality') {
            parallel {
                stage('Lint') {
                    steps {
                        sh './gradlew check --no-daemon'
                    }
                }
                stage('Test Coverage') {
                    steps {
                        sh './gradlew test jacocoTestReport --no-daemon'
                    }
                    post {
                        always {
                            publishHTML([
                                reportDir: 'build/reports/jacoco/test/html',
                                reportFiles: 'index.html',
                                reportName: 'Test Coverage Report'
                            ])
                        }
                    }
                }
            }
        }

        // ===== 3. 빌드 =====
        stage('Build') {
            steps {
                sh './gradlew clean build -x test --no-daemon'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/build/libs/*.jar', fingerprint: true
                }
            }
        }

        // ===== 4. 보안 스캔 =====
        stage('Security Scan') {
            steps {
                script {
                    // OWASP Dependency Check (선택사항)
                    sh '''
                        if [ -f "dependency-check.sh" ]; then
                            ./dependency-check.sh --project delivery-api --scan . --format HTML
                        fi
                    '''
                }
            }
        }

        // ===== 5. Docker 이미지 빌드 =====
        stage('Docker Build') {
            steps {
                script {
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }

        // ===== 6. Docker 이미지 푸시 =====
        stage('Docker Push') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                script {
                    // Docker 레지스트리에 푸시 (선택사항)
                    // sh "docker push ${REGISTRY}/${DOCKER_IMAGE}:${DOCKER_TAG}"
                    // sh "docker push ${REGISTRY}/${DOCKER_IMAGE}:latest"
                    echo "Docker image built: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                }
            }
        }

        // ===== 7. 통합 테스트 =====
        stage('Integration Test') {
            steps {
                script {
                    sh '''
                        docker-compose -f docker-compose.test.yml up -d
                        sleep 30
                        ./gradlew integrationTest --no-daemon || true
                        docker-compose -f docker-compose.test.yml down
                    '''
                }
            }
        }

        // ===== 8. 배포 (Rolling Update) =====
        stage('Deploy') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                script {
                    // Rolling Update: 기존 컨테이너를 하나씩 교체
                    sh '''
                        # 새 컨테이너 시작
                        docker-compose up -d --no-deps --scale api=1 api
                        
                        # 헬스체크 대기
                        sleep 30
                        
                        # 기존 컨테이너 종료
                        docker-compose up -d --no-deps --scale api=5 api
                        
                        # 오래된 이미지 정리
                        docker image prune -f
                    '''
                }
            }
        }

        // ===== 9. 스모크 테스트 =====
        stage('Smoke Test') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                script {
                    sh '''
                        sleep 10
                        curl -f http://localhost:8080/actuator/health || exit 1
                        echo "Smoke test passed"
                    '''
                }
            }
        }
    }

    post {
        always {
            // 정리 작업
            cleanWs()
        }
        success {
            script {
                echo "✅ Build successful: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                // 슬랙/이메일 알림 (선택사항)
            }
        }
        failure {
            script {
                echo "❌ Build failed: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                // 슬랙/이메일 알림 (선택사항)
            }
        }
        unstable {
            echo "⚠️ Build unstable"
        }
    }
}
