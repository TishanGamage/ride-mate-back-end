pipeline {

    agent any

    environment {
        APP_NAME = "ride-mate-backend"
        IMAGE_NAME = "ride-mate-backend"
        CONTAINER_NAME = "ride-mate-backend"
        APP_PORT = "8080"
        GIT_BRANCH = "main"
        REPO_URL = "https://github.com/TishanGamage/ride-mate-back-end"
    }

    stages {

        stage('Clone Repository') {
            steps {
                echo "Cloning branch: ${GIT_BRANCH}"
                git branch: "${GIT_BRANCH}",
                    url: "${REPO_URL}",
                    credentialsId: "github-finegrained-pat"
            }
        }

        stage('Build Jar') {
            steps {
                echo "Building Spring Boot JAR"
                sh "mvn clean package -DskipTests"
            }
        }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image: ${IMAGE_NAME}"
                sh "docker build -t ${IMAGE_NAME} ."
            }
        }

        stage('Stop Old Container') {
            steps {
                echo "Stopping old container if exists"
                sh """
                docker ps -q --filter 'name=${CONTAINER_NAME}' | grep -q . && docker stop ${CONTAINER_NAME} || true
                docker ps -a -q --filter 'name=${CONTAINER_NAME}' | grep -q . && docker rm ${CONTAINER_NAME} || true
                """
            }
        }

        stage('Run Container') {
            steps {
                echo "Running new container: ${CONTAINER_NAME}"
                sh """
                docker run -d --name ${CONTAINER_NAME} -p ${APP_PORT}:${APP_PORT} --env-file .env -e SPRING_PROFILES_ACTIVE=prod ${IMAGE_NAME}
                """
            }
        }
    }

    post {
        success {
            echo "✅ Deployment Successful: ${APP_NAME}"
        }
        failure {
            echo "❌ Deployment Failed: Check logs!"
        }
    }
}