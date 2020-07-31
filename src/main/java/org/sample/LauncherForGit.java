package org.sample;

import hudson.Launcher;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class LauncherForGit {

    protected hudson.EnvVars env = new hudson.EnvVars();
    protected TaskListener listener = TaskListener.NULL;
    protected File repo;

    LauncherForGit(File gitDir) throws Exception {
        repo = gitDir;
    }

    String launchCommand(String... args) throws IOException, InterruptedException {
        return launchCommand(false, args);
    }

    String launchCommand(boolean ignoreError, String... args) throws IOException, InterruptedException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int st = new Launcher.LocalLauncher(listener).launch().pwd(repo).cmds(args).
                envs(env).stdout(out).join();
        String s = out.toString();
        if (!ignoreError) {
            if (s == null || s.isEmpty()) {
                s = StringUtils.join(args, ' ');
            }
//            assertThat(s, 0, st); /* Reports full output of failing commands */
        }
        return s;
    }

}
