@Library('Jenkins_shared_library1') _
def COLOR_MAP = [
    'FAILURE' : 'danger',
    'SUCCESS' : 'good'
]
pipeline{
    agent any
    parameters {
        choice(name: 'action', choices: 'create\ndelete', description: 'Select create or destroy.')
        string(name: 'DOCKER_HUB_USERNAME', defaultValue: 'alwaystilted', description: 'Docker Hub Username')
        string(name: 'IMAGE_NAME', defaultValue: 'youtube', description: 'Docker Image Name')
    }
    tools{
        jdk 'jdk17'
        nodejs 'node16'
    }
    environment {
        SCANNER_HOME=tool 'sonar-scanner'
    }
    stages{
        stage('Cleaning Workspace'){
            steps{
                cleanWorkspace()
            }
        }
        stage('Checkout SCM'){
            when { expression { params.action == 'create'}}
            steps{
                checkoutGit('https://github.com/alwaystilted/Youtube-clone-app-ajay.git', 'main')
            }
        }
        stage('SonarQube Analysis'){
            when { expression { params.action == 'create'}}    
            steps{
                sonarqubeAnalysis()
            }
        }
        stage('QualityGate Ananlysis'){
            when { expression { params.action == 'create'}}    
            steps{
                script{
                    def credentialsId = 'Sonar-token'
                    qualityGate(credentialsId)
                }
            }
        }
        stage('NPM Build'){
            when { expression { params.action == 'create'}}    
            steps{
                npmInstall()
            }
        }
        ///*stage('OWASP FS SCAN') {
        //    steps {
        //        dependencyCheck additionalArguments: '--scan ./ --disableYarnAudit --disableNodeAudit', odcInstallation: 'DP-Check'
        //        dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
        //    }
        //}*/
        //stage('TRIVY FS SCAN') {
        //    when {expression { params.action == 'create'}}
        //    steps {
        //        sh "trivy fs . > trivyfs.txt"
        //    }
        //}
        stage("Docker Build & Push"){
            when {expression { params.action == 'create'}}
            steps{
                script{
                   withDockerRegistry(credentialsId: 'docker', toolName: 'docker'){   
                       sh "docker build --build-arg REACT_APP_RAPID_API_KEY=9a7402bf32msha2dccb44144c9f3p1f6d3cjsn24c73e8e7456 -t youtube ."
                       sh "docker tag youtube alwaystilted/youtube:latest "
                       sh "docker push alwaystilted/youtube:latest "
                    }
                }
            }
        }
        //stage("TRIVY"){
        //    when {expression { params.action == 'create'}}
        //    steps{
        //        sh "trivy image alwaystilted/youtube:latest > trivyimage.txt" 
        //    }
        //}
        ///*stage('Deploy to container'){
        //    steps{
        //        sh 'docker run -d --name youtube1 -p 3000:3000 alwaystilted/youtube:latest'
        //    }
        //}*/
        stage('Container start'){
            when { expression { params.action == 'create'}}
            steps{
                runContainer()
            }
        }
        stage('Removing container'){
            when { expression { params.action == 'delete'}}
            steps{
                removeContainer()
            }
        }
        stage('Deploying pods'){
            when { expression { params.action == 'create'}}
            steps{
                kubeDeploy()
            }
        }
        stage('Deleting pods'){
            when { expression { params.action == 'delete'}}
            steps{
                kubeDelete()
            }
        }        
    }
    post {
        always {
             echo 'Slack Notifications'
             slackSend (
                 channel: '#jenkins', 
                 color: COLOR_MAP[currentBuild.currentResult],
                 message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} \n build ${env.BUILD_NUMBER} \n More info at: ${env.BUILD_URL}"
            )
        }
    }
}