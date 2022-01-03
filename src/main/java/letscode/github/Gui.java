package letscode.github;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Gui {
    private final TrayIcon trayIcon;

    public Gui() {
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit()
                    .createImage(getClass().getResource("/logo.png"));

            trayIcon = new TrayIcon(image, "GitHub helper");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("GitHub helper");
            tray.add(trayIcon);
        } catch (AWTException e) {
            throw new RuntimeException(e);
        }
    }

    public void setMenu(String login, List<RepositoryDescrtiption> repos) {
        PopupMenu popup = new PopupMenu();

        MenuItem accountMI = new MenuItem(login);
        accountMI.addActionListener(e -> openInBrowser("https://github.com/" + login));

        MenuItem notificationMI = new MenuItem("notifications");
        notificationMI.addActionListener(e -> openInBrowser("https://github.com/notifications"));

        Menu repositoriesMI = new Menu("repositories");
        repos
                .forEach(repo -> {
                    String name = repo.getPrs().size() > 0
                            ? String.format("(%d) %s", repo.getPrs().size(), repo.getName())
                            : repo.getName();

                    Menu repoSM = new Menu(name);

                    MenuItem openInBrowser = new MenuItem("Open in browser");
                    openInBrowser.addActionListener(e ->
                            openInBrowser(repo.getRepository().getHtmlUrl().toString())
                    );

                    repoSM.add(openInBrowser);

                    if (repo.getPrs().size() > 0) {
                        repoSM.addSeparator();
                    }

                    repo.getPrs()
                            .forEach(pr -> {
                                MenuItem prMI = new MenuItem(pr.getTitle());
                                prMI.addActionListener(e ->
                                        openInBrowser(pr.getHtmlUrl().toString())
                                );
                                repoSM.add(prMI);
                            });

                    repositoriesMI.add(repoSM);
                });

        int counter = 0;

        for (int i = 0; i < repos.size(); i++) {
            MenuItem item = repositoriesMI.getItem(i);

            if (repos.get(i).getPrs().size() > 0) {
                repositoriesMI.remove(item);
                repositoriesMI.insert(item, counter);
                counter++;
            }
        }

        repositoriesMI.insertSeparator(counter);
        popup.add(accountMI);
        popup.addSeparator();
        popup.add(notificationMI);
        popup.add(repositoriesMI);

        trayIcon.setPopupMenu(popup);
    }

    public void openInBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URL(url).toURI());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void showNotification(String title, String text) {
        trayIcon.displayMessage(title, text, TrayIcon.MessageType.INFO);
    }
}
