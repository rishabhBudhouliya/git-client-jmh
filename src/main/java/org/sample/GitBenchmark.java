///*
// * Copyright (c) 2014, Oracle America, Inc.
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *
// *  * Redistributions of source code must retain the above copyright notice,
// *    this list of conditions and the following disclaimer.
// *
// *  * Redistributions in binary form must reproduce the above copyright
// *    notice, this list of conditions and the following disclaimer in the
// *    documentation and/or other materials provided with the distribution.
// *
// *  * Neither the name of Oracle nor the names of its contributors may be used
// *    to endorse or promote products derived from this software without
// *    specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// * THE POSSIBILITY OF SUCH DAMAGE.
// */
//
//package org.sample;
//
//import hudson.EnvVars;
//import hudson.model.TaskListener;
//import org.eclipse.jgit.transport.RefSpec;
//import org.eclipse.jgit.transport.URIish;
//import org.jenkinsci.plugins.gitclient.Git;
//import org.jenkinsci.plugins.gitclient.GitClient;
//import org.jenkinsci.plugins.gitclient.InitCommand;
//import org.openjdk.jmh.annotations.*;
//import org.openjdk.jmh.infra.Blackhole;
//import org.openjdk.jmh.runner.Runner;
//import org.openjdk.jmh.runner.RunnerException;
//import org.openjdk.jmh.runner.options.Options;
//import org.openjdk.jmh.runner.options.OptionsBuilder;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//public class GitBenchmark {
//
//    @State(Scope.Thread)
//    public static class JenkinsState {
//
//        @Param({"git", "jgit"})
//        String gitExe;
//
//        List<RefSpec> refSpecs = new ArrayList<>();
//        URIish urIish;
//
//
//        final FolderForBenchmark tmp = new FolderForBenchmark();
//        File gitDir;
//        GitClient gitClient;
//
//        @Setup(Level.Iteration)
//        public void doSetup() throws Exception {
//            tmp.before();
//            gitDir = tmp.newFolder();
//            gitClient = Git.with(TaskListener.NULL, new EnvVars()).in(gitDir).using(gitExe).getClient();
//
//            urIish = new URIish("file:///tmp/experiment/TestProject.git");
//            refSpecs.add(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
//
////            gitClient.setRemoteUrl("origin", "file:///tmp/experiment/TestProject.git");
//            System.out.println("Do Setup");
//
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
//
//    }
//
//    @Benchmark
//    public void baselineInit(JenkinsState jenkinsState, Blackhole blackhole){
//        File workspaceDir = new File(jenkinsState.gitDir.getAbsolutePath());
//        blackhole.consume(workspaceDir);
//    }
//
//    @Benchmark
//    public void testGitInitMethod(JenkinsState jenkinsState, Blackhole blackhole) throws Exception {
//        InitCommand command = jenkinsState.gitClient.init_().workspace(jenkinsState.gitDir.getAbsolutePath());
//        command.execute();
//        blackhole.consume(command);
//    }
//
//    public static void main(String[] args) throws RunnerException {
//        Options options = new OptionsBuilder()
//                .include(GitBenchmark.class.getSimpleName())
//                .mode(Mode.AverageTime)
//                .warmupIterations(5)
//                .measurementIterations(5)
//                .forks(2)
//                .build();
//
//        new Runner(options).run();
//    }
//}
