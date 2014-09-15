package edduarte.protbox.ui;

import edduarte.protbox.core.Constants;
import edduarte.protbox.core.synchronization.Sync;
import edduarte.protbox.ui.listeners.OnMouseClick;
import edduarte.protbox.ui.window.NewRegistryWindow;
import org.jdesktop.swingx.VerticalLayout;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * @author Eduardo Duarte (<a href="mailto:emod@ua.pt">emod@ua.pt</a>)
 * @version 2.0
 */

public class TrayApplet extends JDialog {

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TrayApplet.class);
    private static TrayApplet instance;
    public final TrayIcon trayIcon;
    public final JPanel instanceList;
    private JLabel statusText;

    private TrayApplet() {
        super();
        this.setLayout(null);

        statusText = new JLabel("");

//        final Integer clickInterval = (Integer) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
        final int clickInterval = 100;

        this.trayIcon = new TrayIcon(TrayStatus.OKAY.trayIcon);
        this.status(TrayStatus.LOADING, "Loading");
        this.trayIcon.setImageAutoSize(true);
        this.trayIcon.addMouseListener(new TrayClickListener(this, clickInterval));


        statusText.setLayout(null);
        statusText.setBounds(10, 5, 240, 30);
        statusText.setFont(new Font(Constants.FONT, Font.PLAIN, 12));
        statusText.setForeground(Color.DARK_GRAY);
        this.add(statusText);


        JLabel close = new JLabel("Exit Application");
        close.setLayout(null);
        close.setBounds(195, 219, 100, 30);
        close.setFont(new Font(Constants.FONT, Font.PLAIN, 12));
        close.setForeground(Color.gray);
        close.addMouseListener((OnMouseClick) e -> {
            Sync.stop();
            System.exit(1);
        });
        this.add(close);


        instanceList = new JPanel();
        instanceList.setLayout(new VerticalLayout());
        instanceList.setMinimumSize(new Dimension(310, 200));
        instanceList.setFont(new Font(Constants.FONT, Font.PLAIN, 12));
        instanceList.setForeground(Color.black);
        instanceList.setBackground(Color.white);
        instanceList.setOpaque(true);
        JScrollPane scrollList = new JScrollPane(instanceList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollList.setOpaque(true);
        scrollList.setBackground(Color.white);
        scrollList.setBorder(null);
        scrollList.setBounds(0, 41, 310, 174);
        this.add(scrollList);


        JLabel addFolder = new JLabel("Monitor folder...");
        addFolder.setLayout(null);
        addFolder.setBounds(30, 219, 150, 30);
        addFolder.setFont(new Font(Constants.FONT, Font.PLAIN, 12));
        addFolder.setForeground(Color.gray);
        addFolder.setIcon(new ImageIcon(Constants.getAsset("add.png")));
        addFolder.addMouseListener((OnMouseClick) e -> NewRegistryWindow.start(false));
        this.add(addFolder);


        JLabel separator = new JLabel(new ImageIcon(Constants.getAsset("separator.png")));
        separator.setLayout(null);
        separator.setBounds(0, 40, 310, 2);
        this.add(separator);

        JLabel lowerPanelBg = new JLabel(new ImageIcon(Constants.getAsset("lower_side.png")));
        lowerPanelBg.setLayout(null);
        lowerPanelBg.setBounds(0, 215, 310, 41);
        this.add(lowerPanelBg);


        this.addWindowFocusListener(new WindowFocusListener() {
            private boolean gained = false;

            @Override
            public void windowGainedFocus(WindowEvent e) {
                gained = true;
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                if (gained) {
                    dispose();
                }
            }
        });

        this.setVisible(false);
        this.setSize(312, 257);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setUndecorated(true);
        this.getContentPane().setBackground(Color.white);
        this.setResizable(false);
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));

        if (Constants.verbose) logger.info("All swing elements were built");
    }

    public static TrayApplet getInstance() {
        if (instance == null) {
            instance = new TrayApplet();
        } else {
            instance.setVisible(true);
            instance.toFront();
        }
        return instance;
    }

    public void status(TrayStatus status, String extraInfo) {
        trayIcon.setImage(status.trayIcon);
        statusText.setIcon(new ImageIcon(status.dialogIcon));

        String msg = status.msg.replaceAll("<EXTRA>", extraInfo);
        trayIcon.setToolTip("Protbox 1.031\n" + msg);
        statusText.setText(msg);
    }

    public void baloon(String caption, String msg, TrayIcon.MessageType type) {
        trayIcon.displayMessage(caption, msg, type);
    }


    public static enum TrayStatus {
        OKAY(Constants.getAsset("box.png"), Constants.getAsset("done.png"), "Up to date and secure"),
        LOADING(Constants.getAsset("vfl3Wt7C.gif"), Constants.getAsset("sync.png"), "<EXTRA>..."),
        UPDATING(Constants.getAsset("vfl3Wt7C.gif"), Constants.getAsset("sync.png"), "Updating (<EXTRA>)...");

        private BufferedImage trayIcon;
        private BufferedImage dialogIcon;
        private String msg;

        TrayStatus(BufferedImage trayIcon, BufferedImage dialogIcon, String message) {
            this.trayIcon = trayIcon;
            this.dialogIcon = dialogIcon;
            this.msg = message;
        }
    }

    private class TrayClickListener extends MouseAdapter implements ActionListener {
        JDialog frame;
        int delay;
        boolean wasDoubleClick;

        public TrayClickListener(JDialog frame, int delay) {
            this.frame = frame;
            this.delay = delay;
            wasDoubleClick = false;
        }

        private Rectangle getScreenBoundsAt(Point pos) {
            GraphicsDevice gd = getGraphicsDeviceAt(pos);
            Rectangle bounds = null;

            if (gd != null) {
                bounds = gd.getDefaultConfiguration().getBounds();
                Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());

                bounds.x += insets.left;
                bounds.y += insets.top;
                bounds.width -= (insets.left + insets.right);
                bounds.height -= (insets.top + insets.bottom);
            }
            return bounds;
        }

        private GraphicsDevice getGraphicsDeviceAt(Point pos) {
            GraphicsDevice device = null;

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice lstGDs[] = ge.getScreenDevices();

            java.util.List<GraphicsDevice> lstDevices = new ArrayList<>(lstGDs.length);

            for (GraphicsDevice gd : lstGDs) {
                GraphicsConfiguration gc = gd.getDefaultConfiguration();
                Rectangle screenBounds = gc.getBounds();

                if (screenBounds.contains(pos)) {
                    lstDevices.add(gd);
                }
            }

            if (lstDevices.size() == 1) {
                device = lstDevices.get(0);
            }
            return device;
        }

        @Override
        public void mouseClicked(final MouseEvent e) {
            Timer timer = new Timer(delay, evt -> {
                if (wasDoubleClick) {
                    wasDoubleClick = false; // reset flag
                } else {
                    // SINGLE CLICK ACTION
                    Point pos = e.getLocationOnScreen();
                    Rectangle screen = getScreenBoundsAt(pos);

                    if (pos.x + frame.getWidth() > screen.x + screen.width) {
                        pos.x = screen.x + screen.width - frame.getWidth();
                    }
                    if (pos.x < screen.x) {
                        pos.x = screen.x;
                    }

                    if (pos.y + frame.getHeight() > screen.y + screen.height) {
                        pos.y = screen.y + screen.height - frame.getHeight();
                    }
                    if (pos.y < screen.y) {
                        pos.y = screen.y;
                    }

                    frame.setLocation(pos);
                    frame.setVisible(true);
                }
            });
            timer.setRepeats(false);
            timer.start();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Do nothing, the action performed events are done within the Timer
        }
    }
}

