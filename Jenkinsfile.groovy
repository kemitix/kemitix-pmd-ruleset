final String mvn = "mvn --batch-mode --update-snapshots"

pipeline {
    agent any
    stages {
        stage('no SNAPSHOT in master') {
            // checks that the pom version is not a snapshot when the current branch is master
            // TODO: also check for SNAPSHOT when is a pull request with master as the target branch
            when {
                expression {
                    (env.GIT_BRANCH == 'master') &&
                            (readMavenPom(file: 'pom.xml').version).contains("SNAPSHOT") }
            }
            steps {
                error("Build failed because SNAPSHOT version")
            }
        }
        stage('Build') {
            steps {
                withMaven(maven: 'maven 3.5.2', jdk: 'JDK 9') {
                    sh 'mvn clean install'
                }
            }
        }
        stage('Archiving') {
            steps {
                archiveArtifacts '**/classes/**/*.xml'
            }
        }
        stage('Deploy') {
            when { expression { (env.GIT_BRANCH == 'master') } }
            steps {
                withMaven(maven: 'maven 3.5.2', jdk: 'JDK 1.8') {
                    sh "${mvn} deploy --activate-profiles release -DskipTests=true"
                }
            }
        }
    }
}
