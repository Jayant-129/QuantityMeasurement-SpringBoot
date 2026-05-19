pipeline {
    agent any

    environment {
        AWS_REGION     = 'ap-south-1'
        AWS_ACCOUNT_ID = '190249219979'
        ECR_REPO       = 'quantity-measurement'
        IMAGE_TAG      = 'latest'
        ECR_URI        = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}"
        CLUSTER_NAME   = 'quantity-cluster'
    }

    stages {

        stage('Clone Repository') {
            steps {
                git branch: 'dev',
                url: 'https://github.com/Jayant-129/QuantityMeasurement-SpringBoot.git'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $ECR_REPO:$IMAGE_TAG .'
            }
        }

        stage('Tag Docker Image') {
            steps {
                sh 'docker tag $ECR_REPO:$IMAGE_TAG $ECR_URI:$IMAGE_TAG'
            }
        }

        stage('Login to ECR') {
            steps {
                sh '''
                    aws ecr get-login-password --region $AWS_REGION | \
                    docker login --username AWS --password-stdin \
                    $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
                '''
            }
        }

        stage('Push Image to ECR') {
            steps {
                sh 'docker push $ECR_URI:$IMAGE_TAG'
            }
        }

        stage('Deploy to EKS') {
            steps {
                sh '''
                    aws eks update-kubeconfig \
                        --region $AWS_REGION \
                        --name $CLUSTER_NAME

                    kubectl apply -f k8s/mysql.yaml
                    kubectl apply -f k8s/app.yaml

                    kubectl rollout status deployment/quantity-app
                '''
            }
        }
    }
}
