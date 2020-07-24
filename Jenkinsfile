pipeline {
    agent any
    tools {
        maven 'maven-latest'
    }
    matrix {
    axes {
	axis {
	     name 'os'
	     }	
	 }
    }
    stages {
        stage('Build') { 
            steps {
                sh 'mvn -DskipTests clean install' 
            }
        }
	stage('Test') {
	    steps {
	    	sh 'java -jar target/benchmarks.jar GitClientFetchBenchmark -bm avgt -f 2 -foe true -rf json -rff result.json -tu s'
		jmhReport 'result.json'
            }
	}
    }
}
