package letscode.github;

import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GitHubJob {
    private final GitHub gitHub;
    private final Gui gui = new Gui();
    private final Set<Long> allPrIds = new HashSet<>();

    public GitHubJob() {
        try {
            gitHub = new GitHubBuilder()
                    .withAppInstallationToken(System.getenv("GITHUB_TOKEN"))
                    .build();
            init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void init() throws IOException {
        GHMyself myself = gitHub.getMyself();
        String login = myself.getLogin();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    boolean notifyForNewPrs = !allPrIds.isEmpty();
                    HashSet<GHPullRequest> newPrs = new HashSet<>();

                    List<RepositoryDescrtiption> repos = myself.getAllRepositories()
                            .values()
                            .stream()
                            .map(repository -> {
                                try {
                                    List<GHPullRequest> prs = repository.queryPullRequests()
                                            .list()
                                            .toList();
                                    Set<Long> prIds = prs.stream()
                                            .map(GHPullRequest::getId)
                                            .collect(Collectors.toSet());
                                    prIds.removeAll(allPrIds);
                                    allPrIds.addAll(prIds);
                                    prs.forEach(pr -> {
                                        if (prIds.contains(pr.getId())) {
                                            newPrs.add(pr);
                                        }
                                    });

                                    return new RepositoryDescrtiption(
                                            repository.getFullName(),
                                            repository,
                                            prs
                                    );
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .collect(Collectors.toList());

                    gui.setMenu(login, repos);

                    if (notifyForNewPrs) {
                        newPrs.forEach(pr -> {
                            gui.showNotification(
                                    "New PR in " + pr.getRepository().getFullName(),
                                    pr.getTitle()
                            );
                        });
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 1000, 1000);
    }
}
