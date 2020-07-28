pipeline {
    agent any
    tools {
        maven 'mvn-3.6.0'
    }
    stages {
        stage('Build') { 
            steps {
                sh 'mvn -DskipTests clean install' 
            }
        }
	stage('Test') {
	    steps {
	    	sh 'java -jar target/benchmarks.jar GitBenchmark GitLsRemoteBenchmark -bm avgt -f 1 -foe true -rf json -rff result.json -tu ms'
		jmhReport 'result.json'
            }
	}
    }
}
