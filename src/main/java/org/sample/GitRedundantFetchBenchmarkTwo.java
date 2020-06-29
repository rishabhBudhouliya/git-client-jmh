package org.sample;

import hudson.EnvVars;
import hudson.model.TaskListener;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.gitclient.CloneCommand;
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

public class GitRedundantFetchBenchmarkTwo {

    @State(Scope.Thread)
    public static class ClientState {

        @Param({"git", "jgit"})
        String gitExe;

        final FolderForBenchmark tmp = new FolderForBenchmark();
        File gitDir;
        GitClient gitClient;
        List<RefSpec> refSpecs = new ArrayList<>();

        /**
         * We want to create a temporary local git repository after each iteration of the benchmark, works just like
         * "before" and "after" JUnit annotations.
         */
        @Setup(Level.Iteration)
        public void doSetup() throws Exception {
            tmp.before();
            gitDir = tmp.newFolder();

            gitClient = Git.with(TaskListener.NULL, new EnvVars()).in(gitDir).using(gitExe).getClient();

            // fetching all branches
            refSpecs.add(new RefSpec("+refs/heads/*:refs/remotes/origin/*"));
            System.out.println("Do Setup for: " + gitExe);
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

    @State(Scope.Thread)
    public static class CloneRepoState {

        final FolderForBenchmark tmp = new FolderForBenchmark();

        File localRemoteDir;
        File remoteRepoDir;
        URIish urIish;
        /**
         * We test the performance of git fetch on four repositories, varying them on the basis of their
         * commit history size, number of branches and ultimately their overall size.
         * Java-logging-benchmarks: (0.034 MiB) https://github.com/stephenc/java-logging-benchmarks.git
         * Coreutils: (4.58 MiB) https://github.com/uutils/coreutils.git
         * Cairo: (93.54 MiB) https://github.com/cairoshell/cairoshell.git
         * Samba: (324.26 MiB) https://github.com/samba-team/samba.git
         */
        @Param({"https://github.com/jenkinsci/jenkins.git",
                "https://github.com/ruby/ruby.git"})
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
        public void cloneUpstreamRepo() throws Exception {
            tmp.before();
            localRemoteDir = tmp.newFolder();
            remoteRepoDir = cloneUpstreamRepositoryLocally(localRemoteDir, repoUrl);
            System.out.println("Size of cloned upstream repo: " + FileUtils.sizeOfDirectory(remoteRepoDir));
            // Coreutils is a repo sized 4.58 MiB, Cairo is 93.64 MiB and samba is 324.26 MiB
            urIish = new URIish("file://" + remoteRepoDir.getAbsolutePath());
            System.out.println("Created local upstream directory for: " + repoUrl);
        }

        @TearDown(Level.Trial)
        public void doTearDown() {
            tmp.after();
            System.out.println("Removed local upstream directory for: " + repoUrl);
        }
    }

    @Benchmark
    public void gitFetchBenchmarkWithInitialClone(ClientState jenkinsState,CloneRepoState cloneRepoState,Blackhole blackhole) throws Exception {
        CloneCommand initialClone = jenkinsState.gitClient.clone_().url(cloneRepoState.urIish.toString());
        initialClone.execute();
        System.out.println("Fetching for the first time");
        System.out.println("git client dir is: " + FileUtils.sizeOfDirectory(jenkinsState.gitDir));
        blackhole.consume(initialClone);

    }

    @Benchmark
    public void gitRedundantFetchBenchmark(ClientState jenkinsState,CloneRepoState cloneRepoState,Blackhole blackhole) throws Exception {
        jenkinsState.gitClient.clone_().url(cloneRepoState.urIish.toString()).execute();
        System.out.println("Fetching for the first time");
        System.out.println("git client dir is: " + FileUtils.sizeOfDirectory(jenkinsState.gitDir));
        FetchCommand incrementalFetch = jenkinsState.gitClient.fetch_().from(cloneRepoState.urIish, jenkinsState.refSpecs);
        incrementalFetch.execute();
        blackhole.consume(incrementalFetch);
    }
}
