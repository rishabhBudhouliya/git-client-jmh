//package org.sample;
//
//import hudson.EnvVars;
//import hudson.model.Run;
//import hudson.model.TaskListener;
//import org.eclipse.jgit.transport.RefSpec;
//import org.eclipse.jgit.transport.URIish;
//import org.jenkinsci.plugins.gitclient.FetchCommand;
//import org.jenkinsci.plugins.gitclient.Git;
//import org.jenkinsci.plugins.gitclient.GitClient;
//import org.openjdk.jmh.annotations.*;
//import org.openjdk.jmh.infra.Blackhole;
//import org.openjdk.jmh.results.format.ResultFormatType;
//import org.openjdk.jmh.runner.Runner;
//import org.openjdk.jmh.runner.RunnerException;
//import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
//import org.openjdk.jmh.runner.options.Options;
//import org.openjdk.jmh.runner.options.OptionsBuilder;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//public class GitBranchBenchmark {
//
//    @State(Scope.Thread)
//    public static class JenkinsState {
//
//        @Param({"git", "jgit"})
//        String gitExe;
//
//        @Param({"file:///tmp/experiment/test4/java-logging-benchmarks.git",
//                "file:///tmp/experiment/test2/coreutils.git",
//                "file:///tmp/experiment/test/cairo.git",
//                "file:///tmp/experiment/test3/samba.git"})
//        String repoUrl;
//
//        final FolderForBenchmark tmp = new FolderForBenchmark();
//        File gitDir;
//        GitClient gitClient;
//        List<RefSpec> refSpecs  = new ArrayList<>();
//        URIish urIish;
//
//        @Setup(Level.Iteration)
//        public void doSetup() throws Exception {
//            tmp.before();
//            gitDir = tmp.newFolder();
//            gitClient = Git.with(TaskListener.NULL, new EnvVars()).in(gitDir).using(gitExe).getClient();
//
//            // Coreutils is a repo sized 4.58 MiB, Cairo is 93.64 MiB and samba is 324.26 MiB
//            urIish = new URIish(repoUrl);
//
//            // fetching just master branch
//            refSpecs.add(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
//
//            // initialize the test folder for git fetch
//            gitClient.init();
//
////            gitClient.setRemoteUrl("origin", "file:///tmp/experiment/TestProject.git");
//            System.out.println("Do Setup");
//        }
//
//        @TearDown(Level.Iteration)
//        public void doTearDown() {
//            try {
//                File gitDir = gitClient.withRepository((repo, channel) -> repo.getDirectory());
//                System.out.println(gitDir.isDirectory());
//            } catch (Exception e){
//                e.getMessage();
//            }
//            tmp.after();
//            System.out.println("Do TearDown");
//        }
//    }
//
//    @Benchmark
//    @BenchmarkMode({Mode.AverageTime, Mode.SingleShotTime})
//    public void gitFetchBenchmark(JenkinsState jenkinsState, Blackhole blackhole) throws Exception {
//        FetchCommand fetch = jenkinsState.gitClient.fetch_().from(jenkinsState.urIish, jenkinsState.refSpecs);
//        fetch.execute();
//        blackhole.consume(fetch);
//    }
//
//    public static void main(String[] args) throws RunnerException {
//        Options options = new OptionsBuilder()
//                .include(GitBranchBenchmark.class.getSimpleName())
//                .mode(Mode.AverageTime)
//                .warmupIterations(5)
//                .measurementIterations(5)
//                .timeUnit(TimeUnit.MILLISECONDS)
//                .forks(5)
//                .shouldFailOnError(true)
//                .shouldDoGC(true)
//                .build();
//
//        new Runner(options).run();
//    }
//}