// ─────────────────────────────────────────────────────────────────
//  We'll Share — Jenkinsfile
//  Pipeline: GitHub push → Test → Build → Docker → Deploy to K8s
// ─────────────────────────────────────────────────────────────────

pipeline {

    agent any

    // ── Variables ────────────────────────────────────────────────
    environment {
        DOCKER_HUB_USER   = 'naveenvd'                         // Your Docker Hub username
        BACKEND_IMAGE     = "${DOCKER_HUB_USER}/wellshare-backend"
        FRONTEND_IMAGE    = "${DOCKER_HUB_USER}/wellshare-frontend"
        IMAGE_TAG         = "${BUILD_NUMBER}"                  // e.g. build-42
        DOCKER_CREDENTIALS = credentials('dockerhub-creds')   // Saved in Jenkins
        KUBECONFIG_FILE    = credentials('kubeconfig')         // Saved in Jenkins
    }

    // ── Triggers ─────────────────────────────────────────────────
    triggers {
        // Automatically trigger on every GitHub push
        githubPush()
    }

    stages {

        // ── Stage 1: Pull latest code ────────────────────────────
        stage('Checkout') {
            steps {
                echo '📥 Pulling code from GitHub...'
                checkout scm
            }
        }

        // ── Stage 2: Test Backend ────────────────────────────────
        stage('Test Backend') {
            steps {
                echo '🧪 Running Spring Boot tests...'
                dir('ridemate-backend') {
                    sh 'mvn test -q'
                }
            }
            post {
                always {
                    // Publish test results in Jenkins UI
                    junit 'ridemate-backend/target/surefire-reports/*.xml'
                }
            }
        }

        // ── Stage 3: Test Frontend ───────────────────────────────
        stage('Test Frontend') {
            steps {
                echo '🧪 Running Angular tests...'
                dir('ridemate-frontend') {
                    sh 'npm ci'
                    sh 'npm run test -- --watch=false --browsers=ChromeHeadless'
                }
            }
        }

        // ── Stage 4: Build Docker Images ─────────────────────────
        stage('Build Docker Images') {
            steps {
                echo '🐳 Building Docker images...'
                parallel(
                    'Backend Image': {
                        dir('ridemate-backend') {
                            sh "docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} -t ${BACKEND_IMAGE}:latest ."
                        }
                    },
                    'Frontend Image': {
                        dir('ridemate-frontend') {
                            sh "docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} -t ${FRONTEND_IMAGE}:latest ."
                        }
                    }
                )
            }
        }

        // ── Stage 5: Push to Docker Hub ──────────────────────────
        stage('Push to Docker Hub') {
            steps {
                echo '🚀 Pushing images to Docker Hub...'
                sh "echo ${DOCKER_CREDENTIALS_PSW} | docker login -u ${DOCKER_CREDENTIALS_USR} --password-stdin"
                parallel(
                    'Push Backend': {
                        sh "docker push ${BACKEND_IMAGE}:${IMAGE_TAG}"
                        sh "docker push ${BACKEND_IMAGE}:latest"
                    },
                    'Push Frontend': {
                        sh "docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}"
                        sh "docker push ${FRONTEND_IMAGE}:latest"
                    }
                )
            }
        }

        // ── Stage 6: Deploy to Kubernetes ────────────────────────
        stage('Deploy to Kubernetes') {
            steps {
                echo '☸️  Deploying to Kubernetes...'
                withKubeConfig([credentialsId: 'kubeconfig']) {
                    // Update the image tag in K8s deployments
                    sh "kubectl set image deployment/wellshare-backend backend=${BACKEND_IMAGE}:${IMAGE_TAG} -n wellshare"
                    sh "kubectl set image deployment/wellshare-frontend frontend=${FRONTEND_IMAGE}:${IMAGE_TAG} -n wellshare"

                    // Wait for rollout to complete
                    sh "kubectl rollout status deployment/wellshare-backend -n wellshare --timeout=120s"
                    sh "kubectl rollout status deployment/wellshare-frontend -n wellshare --timeout=120s"
                }
            }
        }

    }

    // ── Post Actions ─────────────────────────────────────────────
    post {
        success {
            echo '✅ Pipeline succeeded! We''ll Share is deployed.'
            // Optional: notify team via Slack/email
        }
        failure {
            echo '❌ Pipeline failed. Check the logs above.'
        }
        always {
            // Clean up local Docker images to save disk space
            sh "docker rmi ${BACKEND_IMAGE}:${IMAGE_TAG} || true"
            sh "docker rmi ${FRONTEND_IMAGE}:${IMAGE_TAG} || true"
        }
    }
}
