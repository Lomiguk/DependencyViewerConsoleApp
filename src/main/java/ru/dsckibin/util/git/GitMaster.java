package ru.dsckibin.util.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import ru.dsckibin.exception.CommitNotFoundException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GitMaster {
    private final static String GIT_EXTENSION = "\\.git";
    private final String gitRepoPath;

    public GitMaster(String gitPath) {
        gitRepoPath = gitPath + GIT_EXTENSION;
    }

    public List<String> getBranches() {
        try (var repository = new FileRepository(gitRepoPath)) {
            var git = new Git(repository);
            return git
                    .branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call()
                    .stream()
                    .map(Ref::getName)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Commit> getCommits(String branch) {
        try (var repository = new FileRepository(gitRepoPath)) {
            var git = new Git(repository);
            var revCommits = git.log().add(repository.resolve(branch)).call();
            var result = new ArrayList<Commit>();

            revCommits.forEach(revCommit ->
                    result.add(new Commit(
                            revCommit.getName(),
                            String.format(
                                    "Time: %s; Message: %s",
                                    revCommit.getCommitTime(),
                                    revCommit.getShortMessage()
                            )
                    ))
            );

            return result;

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoHeadException e) {
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getDiff(String branch, String firstHash, String secondHash) {
        var firstCommit = getCommit(branch, firstHash);
        var secondCommit = getCommit(branch, secondHash);
        var result = new ArrayList<String>();
        try (var repository = new FileRepository(gitRepoPath)) {
            try (var diffFormatter = new DiffFormatter(new ByteArrayOutputStream())) {
                diffFormatter.setRepository(repository);
                diffFormatter.scan(firstCommit, secondCommit).forEach(diffEntry ->
                        result.add(diffEntry.getNewPath())
                );
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private RevCommit getCommit(String branch, String hash) {
        try (var repository = new FileRepository(gitRepoPath)) {
            var git = new Git(repository);
            var commits = git.log().add(repository.resolve(branch)).call();
            for (var commit : commits) {
                if (commit.getName().equals(hash)) {
                    return commit;
                }
            }
            throw new CommitNotFoundException(String.format("Commit %s not found", hash));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoHeadException e) {
            throw new RuntimeException(e);
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }
}
