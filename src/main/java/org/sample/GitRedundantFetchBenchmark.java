package org.sample;

import hudson.EnvVars;
import hudson.model.TaskListener;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.gitclient.FetchCommand;
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GitRedundantFetchBenchmark {

    @State(Scope.Thread)
    public static class ClientState {

        @Param({"git", "jgit"})
        String gitExe;

        final FolderForBenchmark tmp = new FolderForBenchmark();
        File gitDir;
        GitClient gitClient;
        List<RefSpec> narrowRefSpecs = new ArrayList<>();
        List<RefSpec> wideRefSpecs = new ArrayList<>();

        final FolderForBenchmark dirForRemoteClone = new FolderForBenchmark();
        File localRemoteDir;
        File remoteRepoDir;
        URIish urIish;

        @Param({"https://github.com/jenkinsci/jenkins-charm.git",
                "https://github.com/jenkinsci/parameterized-trigger-plugin.git",
                "https://github.com/jenkinsci/ec2-plugin.git",
                "https://github.com/jenkinsci/git-plugin.git"})
        String repoUrl;

        private File cloneUpstreamRepositoryLocally(File parentDir, String repoUrl) throws Exception {
            String repoName = repoUrl.split("/")[repoUrl.split("/").length - 1];
            File gitRepoDir = new File(parentDir, repoName);
            gitRepoDir.mkdir();
            GitClient cloningGitClient = Git.with(TaskListener.NULL, new EnvVars()).in(gitRepoDir).using("git").getClient();
            cloningGitClient.clone_().url(repoUrl).execute();
//            assertTrue("Unable to create git repo", gitRepoDir.exists());
            return gitRepoDir;
        }

        @Setup(Level.Trial)
        public void cloneRemoteRepo() throws Exception {
            dirForRemoteClone.before();
            localRemoteDir = dirForRemoteClone.newFolder();
            remoteRepoDir = cloneUpstreamRepositoryLocally(localRemoteDir, repoUrl);
            System.out.println("Size of cloned upstream repo: " + FileUtils.sizeOfDirectory(remoteRepoDir));
            // Coreutils is a repo sized 4.58 MiB, Cairo is 93.64 MiB and samba is 324.26 MiB
            urIish = new URIish("file://" + remoteRepoDir.getAbsolutePath());
            System.out.println("Created local upstream directory for: " + repoUrl);
        }

        @TearDown(Level.Trial)
        public void tearRemoteRepoDown() {
            tmp.after();
            System.out.println("Removed local upstream directory for: " + repoUrl);
        }

        /**
         * We want to create a temporary local git repository after each iteration of the benchmark, works just like
         * "before" and "after" JUnit annotations.
         */
        @Setup(Level.Iteration)
        public void doSetup() throws Exception {
            tmp.before();
            gitDir = tmp.newFolder();
            gitClient = Git.with(TaskListener.NULL, new EnvVars()).in(gitDir).using(gitExe).getClient();

            // fetching just master branch
            narrowRefSpecs.add(new RefSpec("+refs/heads/master:refs/remotes/origin/master"));

            // wide refspec
            wideRefSpecs.add(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));

            // initialize the test folder for git fetch
            gitClient.clone_().url(urIish.toString()).execute();
            System.out.println("Fetching for the first time");
            System.out.println("git client dir is: " + FileUtils.sizeOfDirectory(gitDir));
//            gitClient.setRemoteUrl("origin", "file:///tmp/experiment/TestProject.git");
            System.out.println("Do Setup");
        }

        @TearDown(Level.Iteration)
        public void doTearDown() {
            try {
                // making sure that git init made a git an empty repository
                File gitDir = gitClient.withRepository((repo, channel) -> repo.getDirectory());
                System.out.println(gitDir.isDirectory());
            } catch (Exception e) {
                e.getMessage();
            }
            tmp.after();
            System.out.println("Do TearDown for: " + gitExe);
        }
    }

//    @State(Scope.Thread)
//    public static class CloneRepoState {
//
//        final FolderForBenchmark tmp = new FolderForBenchmark();
//
//        File localRemoteDir;
//        File remoteRepoDir;
//        URIish urIish;
//        /**
//         * We test the performance of git fetch on four repositories, varying them on the basis of their
//         * commit history size, number of branches and ultimately their overall size.
//         * Java-logging-benchmarks: (0.034 MiB) https://github.com/stephenc/java-logging-benchmarks.git
//         * Coreutils: (4.58 MiB) https://github.com/uutils/coreutils.git
//         * Cairo: (93.54 MiB) https://github.com/cairoshell/cairoshell.git
//         * Samba: (324.26 MiB) https://github.com/samba-team/samba.git
//         */
//        @Param({"https://github.com/freedesktop/cairo.git",
//                "https://github.com/samba-team/samba.git"})
//        String repoUrl;
//
//        private File cloneUpstreamRepositoryLocally(File parentDir, String repoUrl) throws Exception {
//            String repoName = repoUrl.split("/")[repoUrl.split("/").length - 1];
//            File gitRepoDir = new File(parentDir, repoName);
//            gitRepoDir.mkdir();
//            GitClient cloningGitClient = Git.with(TaskListener.NULL, new EnvVars()).in(gitRepoDir).using("git").getClient();
//            cloningGitClient.clone_().url(repoUrl).execute();
////            assertTrue("Unable to create git repo", gitRepoDir.exists());
//            return gitRepoDir;
//        }
//
//        @Setup(Level.Trial)
//        public void cloneUpstreamRepo() throws Exception {
//            tmp.before();
//            localRemoteDir = tmp.newFolder();
//            remoteRepoDir = cloneUpstreamRepositoryLocally(localRemoteDir, repoUrl);
//            // Coreutils is a repo sized 4.58 MiB, Cairo is 93.64 MiB and samba is 324.26 MiB
//            urIish = new URIish("file://" + remoteRepoDir.getAbsolutePath());
//            System.out.println("Created local upstream directory for: " + repoUrl);
//        }
//
//        @TearDown(Level.Trial)
//        public void doTearDown() {
//            tmp.after();
//            System.out.println("Removed local upstream directory for: " + repoUrl);
//        }
//    }

    @Benchmark
    public void gitFetchBenchmarkRedundantWithWideRefSpec(ClientState jenkinsState, Blackhole blackhole) throws Exception {
        FetchCommand incrementalFetch = jenkinsState.gitClient.fetch_().from(jenkinsState.urIish, jenkinsState.wideRefSpecs);
        incrementalFetch.execute();
        blackhole.consume(incrementalFetch);
    }

//    @Benchmark
//    public void gitFetchBenchmarkBaselineWithNarrowRefSpec(ClientState jenkinsState, CloneRepoState cloneRepoState, Blackhole blackhole) throws Exception {
//        FetchCommand fetch = jenkinsState.gitClient.fetch_().from(cloneRepoState.urIish, jenkinsState.narrowRefSpecs);
//        fetch.execute();
//        blackhole.consume(fetch);
//    }

//    @Benchmark
//    public void gitFetchBenchmarkWithHonorRefSpec(ClientState jenkinsState, CloneRepoState cloneRepoState, Blackhole blackhole) throws Exception {
//        FetchCommand fetch = jenkinsState.gitClient.fetch_().from(cloneRepoState.urIish, jenkinsState.narrowRefSpecs);
//        fetch.execute();
//        FetchCommand incrementalFetch = jenkinsState.gitClient.fetch_().from(cloneRepoState.urIish, jenkinsState.narrowRefSpecs);
//        incrementalFetch.execute();
//        blackhole.consume(fetch);
//        blackhole.consume(incrementalFetch);
//    }

//    @Benchmark
//    public void gitFetchBenchmarkWithoutHonorRefSpec(ClientState jenkinsState, CloneRepoState cloneRepoState, Blackhole blackhole) throws Exception {
//        FetchCommand fetch = jenkinsState.gitClient.fetch_().from(cloneRepoState.urIish, jenkinsState.wideRefSpecs);
//        fetch.execute();
//        FetchCommand incrementalFetch = jenkinsState.gitClient.fetch_().from(cloneRepoState.urIish, jenkinsState.wideRefSpecs);
//        incrementalFetch.execute();
//        blackhole.consume(fetch);
//        blackhole.consume(incrementalFetch);
//    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(GitRedundantFetchBenchmark.class.getSimpleName())
                .mode(Mode.AverageTime)
                .warmupIterations(5)
                .measurementIterations(5)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(2)
                .shouldFailOnError(true)
                .shouldDoGC(true)
                .build();

        new Runner(options).run();
    }
}