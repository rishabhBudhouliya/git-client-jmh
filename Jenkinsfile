pipeline {
    agent none
    tools {
            maven 'maven-latest'
        }
    stages {
        stage('BuildAndTest') {
            matrix {
                agent any
                axes {
                		axis {
                	     		name 'os'
                	     		values 'CentOS-8', 'Debian-10', 'FreeBSD-12', 'ppc64le', 's390x', 'windows'
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
        }
    }
}