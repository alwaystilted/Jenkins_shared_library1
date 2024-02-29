def call() {
    sh 'trivy image alwaystilted/youtube:latest > trivyimage.txt'
}