package ru.dsckibin.util.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import ru.dsckibin.exception.CommitNotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitMaster {
    private final File workTree;

    public GitMaster(String gitPath) {
        workTree = new File(gitPath);
        try (var ignored = openRepository()) {
            // Validate the repository once and fail with a useful message.
        } catch (IOException e) {
            throw new IllegalArgumentException("not a Git repository: " + gitPath, e);
        }
    }

    public List<String> getBranches() {
        try (var git = new Git(openRepository())) {
            return git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call()
                    .stream()
                    .map(Ref::getName)
                    .toList();
        } catch (IOException | GitAPIException e) {
            throw new IllegalStateException("failed to read Git branches", e);
        }
    }

    public List<Commit> getCommits(String branch) {
        try (var git = new Git(openRepository())) {
            var repository = git.getRepository();
            var branchId = repository.resolve(branch);
            if (branchId == null) {
                throw new IllegalArgumentException("branch not found: " + branch);
            }

            var result = new ArrayList<Commit>();
            for (var commit : git.log().add(branchId).call()) {
                result.add(new Commit(
                        commit.getName(),
                        String.format("Time: %s; Message: %s",
                                commit.getCommitTime(), commit.getShortMessage())
                ));
            }
            return result;
        } catch (IOException | GitAPIException e) {
            throw new IllegalStateException("failed to read Git history", e);
        }
    }

    public List<String> getDiff(String branch, String firstHash, String secondHash) {
        var firstCommit = getCommit(branch, firstHash);
        var secondCommit = getCommit(branch, secondHash);
        var result = new ArrayList<String>();

        try (var repository = openRepository();
             var formatter = new DiffFormatter(new ByteArrayOutputStream())) {
            formatter.setRepository(repository);
            formatter.scan(firstCommit, secondCommit)
                    .forEach(entry -> result.add(entry.getNewPath()));
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("failed to calculate Git diff", e);
        }
    }

    private RevCommit getCommit(String branch, String hash) {
        try (var git = new Git(openRepository())) {
            var repository = git.getRepository();
            var branchId = repository.resolve(branch);
            if (branchId == null) {
                throw new IllegalArgumentException("branch not found: " + branch);
            }
            for (var commit : git.log().add(branchId).call()) {
                if (commit.getName().equals(hash)) {
                    return commit;
                }
            }
            throw new CommitNotFoundException("Commit " + hash + " not found");
        } catch (IOException | GitAPIException e) {
            throw new IllegalStateException("failed to find Git commit", e);
        }
    }

    private Repository openRepository() throws IOException {
        var builder = new FileRepositoryBuilder().findGitDir(workTree);
        if (builder.getGitDir() == null) {
            throw new IOException(".git directory not found");
        }
        return builder.build();
    }
}
