package net.minecraft.src;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GuiMultiplayerList extends GuiScreen {
    private File serverListFile = new File(System.getProperty("user.dir") + "/servers.txt");
    private List<String> serverList = new ArrayList<>();
    private GuiScreen parent;
    private boolean deleting;
    private int page = 0;
    private boolean lock = false;

    public void addToList(String server) {
        serverList.add(server);
        try {
            FileWriter writer = new FileWriter(serverListFile, true);
            writer.append(server).append('\n');
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public GuiMultiplayerList(GuiScreen parent, boolean deleting) {
        this.deleting = deleting;
        this.parent = parent;
        try {
            if (!serverListFile.exists()) {
                if (serverListFile.createNewFile()) doNothing();
            }
            else {
                FileReader reader = new FileReader(serverListFile);
                Scanner scanner = new Scanner(reader);
                while (scanner.hasNextLine()) {
                    String addr = scanner.nextLine().trim();
                    if (!addr.isEmpty()) serverList.add(addr);
                }
                scanner.close();
                reader.close();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void actionPerformed() {
        this.controlList.clear();

        if (!deleting) {
            this.controlList.add(new GuiButton(0, this.width / 4 - 50, this.height / 8 * 7 - 25, 100, 20, "Remove"));
            this.controlList.add(new GuiButton(1, this.width / 2 - 50, this.height / 8 * 7 - 25, 100, 20, "Add"));
            this.controlList.add(new GuiButton(2, this.width / 4 * 3 - 50, this.height / 8 * 7 - 25, 100, 20, "Direct connect"));
        }
        this.controlList.add(new GuiButton(3, this.width / 2 - 50, this.height / 8 * 7, 100, 20, "Back"));

        final int maxOnPage = (this.height - 120) / 25;
        System.out.println(maxOnPage);

        if (page > 0) {
            this.controlList.add(new GuiButton(4, 8, this.height / 2, 25, 20, "<<<"));
        }
        if (page * maxOnPage + 1 < serverList.size()) {
            this.controlList.add(new GuiButton(5, this.width - 8 - 25, this.height / 2, 25, 20, ">>>"));
        }

        for (int i = page * maxOnPage; i < (page + 1) * maxOnPage; i++) {
            if (serverList.size() <= i) break;
            this.controlList.add(new GuiButton(6 + i, this.width / 2 - 100, (i - page * maxOnPage) * 25 + 15 + 40, serverList.get(i)));
        }
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int mouseButton) {
        lock = false;
        super.mouseMovedOrUp(x, y, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                this.deleting = true;
                this.actionPerformed();
                break;
            case 1:
                mc.displayGuiScreen(new GuiAddServer(this));
                break;
            case 2:
                mc.displayGuiScreen(new GuiMultiplayer(this));
                break;
            case 3:
                if (deleting) {
                    deleting = false;
                    lock = true;
                    actionPerformed();
                }
                else if (!lock) mc.displayGuiScreen(parent);
                break;
            case 4:
                if (!lock) {
                    page--;
                    lock = true;
                    this.actionPerformed();
                }
                break;
            case 5:
                if (!lock) {
                    page++;
                    lock = true;
                    this.actionPerformed();
                }
                break;
            default:
                if (deleting && !lock) {
                    page = 0;
                    deleting = false;
                    lock = true;
                    serverList.remove(button.id - 6);
                    this.actionPerformed();
                    try {
                        FileWriter writer = new FileWriter(serverListFile, false);
                        for (String server : serverList) {
                            writer.append(server).append("\n");
                        }
                        writer.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (!lock) {
                    String host = serverList.get(button.id - 6);
                    final String[] split = host.split(":");
                    try {
                        this.mc.displayGuiScreen(new GuiConnecting(this.mc, split[0], (split.length > 1) ? Integer.parseInt(split[1]) : 25565));
                    } catch (NumberFormatException e) {
                        this.mc.displayGuiScreen(new GuiConnectFailed("Failed to parse URI", "Port number is invalid"));
                    }
                }
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float renderPartialTick) {
        this.drawDefaultBackground();
        this.drawCenteredString(fontRenderer, deleting ? "Delete server" : "Multiplayer", this.width / 2, 30, 0xFFFFFFFF);
        if (serverList.isEmpty()) {
            this.drawCenteredString(fontRenderer, "Empty house", this.width / 2, this.height / 2, 0xFFFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, renderPartialTick);
    }

    private void doNothing() {}
}
