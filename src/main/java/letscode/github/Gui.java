package letscode.github;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
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
        List<RepositoryDescrtiption> repositoriesWithPRs = new ArrayList<>();
        List<RepositoryDescrtiption> repositoriesWithoutPRs = new ArrayList<>();

        for (var repo : repos) {
            if (repo.getPrs().size() == 0) {
                repositoriesWithoutPRs.add(repo);
            } else {
                repositoriesWithPRs.add(repo);
            }
        }

        repositoriesWithPRs.forEach(
                repo -> {
                    Menu repoSM = new Menu(String.format("(%d) %s", repo.getPrs().size(), repo.getName()));
                    repositoriesMI.add(repoSM);
                });

        repositoriesMI.addSeparator();

        repositoriesWithoutPRs.forEach(
                repo -> {
                    Menu repoSM = new Menu(repo.getName());
                    repositoriesMI.add(repoSM);
                });

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
