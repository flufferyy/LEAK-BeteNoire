package me.yaimsputnik5.cope.gui;

import com.intellij.uiDesigner.core.*;
import me.yaimsputnik5.cope.BeteNoireInstance;
import me.yaimsputnik5.cope.Info;
import me.yaimsputnik5.cope.InputData;
import me.yaimsputnik5.utils.InvalidRangeException;
import me.yaimsputnik5.utils.Log;
import me.yaimsputnik5.versionChecker.VersionChecker;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainWindow extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static DefaultTableModel dtm;
    private BeteNoireInstance beteNoireInstance;
    private Thread instanceThread;
    private InstanceRunnable instanceRunnable;
    private Point initialClick;
    private final JFrame meMyselfAndI;
    private ScheduledExecutorService schedulerProgressBarService;

    public MainWindow() {
        setUndecorated(true);
        meMyselfAndI = this;
        VersionChecker.checkNewVersion();
        me.setText("Bête Noire Control Panel" + " | ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setContentPane(pannello);

        URL url = ClassLoader.getSystemResource("icon.png");
        Toolkit kit = Toolkit.getDefaultToolkit();
        Image img = kit.createImage(url);
        setIconImage(img);

        progressBar1.setString("Bête Noire Is Idle...");
        setVisible(true);
        setupTable();

        startButton.addActionListener(e -> {
            InputData i;

            try {
                i = new InputData(getArgsFromInputMask());
            } catch (InvalidRangeException invalidRangeException) {
                if (Confirm.requestConfirm("ERROR! Check IP/CIDR Range! Would You Like To See An Example Conf?"))
                    exampleConf();
                return;
            } catch (NumberFormatException ex) {
                if (Confirm.requestConfirm("ERROR! Check Port(s)! Would You Like To See An Example Conf?"))
                    exampleConf();
                return;
            }
            running(i);
            beteNoireInstance = null;
        });
        stopButton.addActionListener(e -> idle());
        saveResultsButton.addActionListener(e -> {
            if (resultsTable.getRowCount() <= 0) {
                MessageWindow.showMessage("ERROR! SAVE FAILED!", "No Servers Found To Export!");
                return;
            }
            try {
                saveToFile();
            } catch (IOException ex) {
                Log.log_to_file(ex.toString(), "log.txt");
            }

        });
        me.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://github.com/Yaimsputnik5/BeteNoire"));
                    } catch (IOException | URISyntaxException ex) {
                        Log.log_to_file(ex.toString(), "log.txt");
                    }
                }
            }
        });
        exitButton.addActionListener(e -> {
            if (instanceRunnable != null) instanceRunnable.stop();
            System.exit(0);

        });
        toolbar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);
            }
        });
        toolbar.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                //super.mouseDragged(e);
                // get location of Window
                int thisX = meMyselfAndI.getLocation().x;
                int thisY = meMyselfAndI.getLocation().y;

                // Determine how much the mouse moved since the initial click
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;

                // Move window to this position
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                meMyselfAndI.setLocation(X, Y);
            }
        });
    }

    public void idle() {
        instanceRunnable.stop();
        instanceThread = null;
        schedulerProgressBarService.shutdown();
        beteNoireInstance = null;
        progressBar1.setString("100%");
        progressBar1.setValue(100);

        stopButton.setEnabled(false);
        ipRangeTextField.setEnabled(true);
        portRangeTextField.setEnabled(true);
        threadTextField.setEnabled(true);
        timeoutTextField.setEnabled(true);
        startButton.setEnabled(true);
        stateLabel.setText("Bête Noire Is Idle...");
        stateLabel.setForeground(Color.green.darker().darker());
        doAllCheckBox.setEnabled(true);
        pingCheckBox.setEnabled(true);
        motdText.setEnabled(true);
        minPlayersText.setEnabled(true);
        versionText.setEnabled(true);
    }

    private void running(InputData i) {
        beteNoireInstance = new BeteNoireInstance(i);
        dtm.setRowCount(0);
        Info.serverFound = 0;
        Info.serverNotFilteredFound = 0;

        instanceRunnable = new InstanceRunnable(beteNoireInstance, this);
        instanceThread = new Thread(instanceRunnable);
        instanceThread.start();
        schedulerProgressBarService = Executors.newScheduledThreadPool(1);
        schedulerProgressBarService.scheduleAtFixedRate(new ProgressBarRunnable(progressBar1, beteNoireInstance),
                0,
                TimeUnit.SECONDS.toSeconds(1),
                TimeUnit.SECONDS);
        ipRangeTextField.setEnabled(false);
        portRangeTextField.setEnabled(false);
        threadTextField.setEnabled(false);
        timeoutTextField.setEnabled(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        stateLabel.setText("Bête Noire Is Running...");
        stateLabel.setForeground(Color.red);
        doAllCheckBox.setEnabled(false);
        pingCheckBox.setEnabled(false);
        motdText.setEnabled(false);
        minPlayersText.setEnabled(false);
        versionText.setEnabled(false);
    }

    private void saveToFile() throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(meMyselfAndI);
        File file;
        if (option == JFileChooser.APPROVE_OPTION) {
            file = fileChooser.getSelectedFile();
        } else return;
        if (!file.createNewFile()) {
            MessageWindow.showMessage("ERROR! Failed To Save!", "Failed To Create File! Try One Of These Steps: 1. Try Again | 2. Restart Bête Noire | 3. Run BêteNoire.jar As Administrator!");
            return;
        }
        PrintWriter os = new PrintWriter(file);
        for (int row = 0; row < resultsTable.getRowCount(); row++) {
            for (int col = 0; col < resultsTable.getColumnCount(); col++) {
                os.print(resultsTable.getValueAt(row, col));
                os.print(" - ");
            }
            os.println();
        }
        os.close();
    }

    public void exampleConf() {
        //ipRangeTextField.setText("192.168.1.*");
        //portRangeTextField.setText("25565-25577");
        //threadTextField.setText("50");
        //timeoutTextField.setText("1000");
        ipRangeTextField.setText("139.99.125.*");
        portRangeTextField.setText("25565-25577");
        threadTextField.setText("1000");
        timeoutTextField.setText("99999");
    }

    private String[] getArgsFromInputMask() {

        String command = "-range " + ipRangeTextField.getText() + " " +
                "-ports " + portRangeTextField.getText() + " " +
                "-th " + threadTextField.getText() + " " +
                "-ti " + timeoutTextField.getText();

        if (!pingCheckBox.isSelected()) command += " -noping";
        if (doAllCheckBox.isSelected()) command += " -all";
        if (!versionText.getText().isEmpty()) command += " -ver " + versionText.getText();
        if (!motdText.getText().isEmpty()) command += " -motd " + motdText.getText();
        if (!minPlayersText.getText().isEmpty()) command += " -on " + minPlayersText.getText();

        return command.split(" ");
    }

    private void setupTable() {
        dtm = new MyTableModel();

        resultsTable.setModel(dtm);
        TableColumnModel columnModel = resultsTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(5);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(70);
        columnModel.getColumn(3).setPreferredWidth(100);
        columnModel.getColumn(4).setPreferredWidth(136);
        columnModel.getColumn(5).setPreferredWidth(453);

        resultsTable.setSelectionBackground(Color.white);
        resultsTable.setSelectionForeground(Color.black);
        resultsTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table = (JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);
                if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    String ip = table.getModel().getValueAt(row, 1).toString();
                    String port = table.getModel().getValueAt(row, 2).toString();
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection(ip + ":" + port), null);
                }
            }
        });
        resultsTable.getTableHeader().setReorderingAllowed(false);
    }


    private JPanel pannello;
    private JLabel ipRangeLabel;
    private JTextField ipRangeTextField;
    private JTextField portRangeTextField;
    private JTextField timeoutTextField;
    private JLabel timeoutLabel;
    private JLabel portRangeLabel;
    private JButton stopButton;
    private JTable resultsTable;
    public JProgressBar progressBar1;
    private JLabel stateLabel;
    private JToolBar toolbar;
    private JLabel me;
    private JButton saveResultsButton;
    private JButton exitButton;
    private JCheckBox pingCheckBox;
    private JCheckBox doAllCheckBox;
    private JTextField threadTextField;
    private JButton startButton;
    private JLabel threadsLabel;
    private JTextField motdText;
    private JTextField minPlayersText;
    private JTextField versionText;

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        pannello = new JPanel();
        pannello.setLayout(new GridLayoutManager(7, 9, new Insets(0, 0, 0, 0), -1, -1));
        pannello.setBackground(new Color(-14605013));
        pannello.setEnabled(false);
        pannello.setForeground(new Color(-5524801));
        ipRangeLabel = new JLabel();
        ipRangeLabel.setBackground(new Color(-5524801));
        ipRangeLabel.setForeground(new Color(-5524801));
        ipRangeLabel.setText("IP Address/CIDR");
        pannello.add(ipRangeLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        ipRangeTextField = new JTextField();
        ipRangeTextField.setBackground(new Color(-14605013));
        ipRangeTextField.setForeground(new Color(-5524801));
        ipRangeTextField.setToolTipText("IP Address OR CIDR Range Goes Here!");
        pannello.add(ipRangeTextField, new GridConstraints(1, 1, 1, 8, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        portRangeTextField = new JTextField();
        portRangeTextField.setBackground(new Color(-14605013));
        portRangeTextField.setForeground(new Color(-5524801));
        portRangeTextField.setToolTipText("Example: 25565-25577");
        pannello.add(portRangeTextField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        timeoutTextField = new JTextField();
        timeoutTextField.setBackground(new Color(-14605013));
        timeoutTextField.setForeground(new Color(-5524801));
        timeoutTextField.setText("");
        timeoutTextField.setToolTipText("Best Timeout Option Is 500-800");
        pannello.add(timeoutTextField, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        timeoutLabel = new JLabel();
        timeoutLabel.setBackground(new Color(-5524801));
        timeoutLabel.setForeground(new Color(-5524801));
        timeoutLabel.setText("Timout");
        pannello.add(timeoutLabel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        portRangeLabel = new JLabel();
        portRangeLabel.setBackground(new Color(-5524801));
        portRangeLabel.setForeground(new Color(-5524801));
        portRangeLabel.setText("Port OR Port Range!");
        pannello.add(portRangeLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        stopButton = new JButton();
        stopButton.setBackground(new Color(-14605013));
        stopButton.setEnabled(false);
        stopButton.setFocusable(false);
        stopButton.setForeground(new Color(-1));
        stopButton.setText("Stop Bête Noire");
        pannello.add(stopButton, new GridConstraints(4, 3, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        scrollPane1.setBackground(new Color(-14605013));
        scrollPane1.setForeground(new Color(-5524801));
        scrollPane1.setVisible(true);
        pannello.add(scrollPane1, new GridConstraints(6, 0, 1, 9, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        scrollPane1.setBorder(BorderFactory.createTitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        resultsTable = new JTable();
        resultsTable.setAutoResizeMode(4);
        resultsTable.setBackground(new Color(-14605013));
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setForeground(new Color(-5524801));
        resultsTable.setGridColor(new Color(-5524801));
        resultsTable.setSelectionForeground(new Color(-10855846));
        resultsTable.setVisible(true);
        scrollPane1.setViewportView(resultsTable);
        progressBar1 = new JProgressBar();
        progressBar1.setBackground(new Color(-14605013));
        progressBar1.setForeground(new Color(-5524801));
        pannello.add(progressBar1, new GridConstraints(5, 0, 1, 9, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        stateLabel = new JLabel();
        stateLabel.setBackground(new Color(-5524801));
        stateLabel.setForeground(new Color(-5524801));
        stateLabel.setText("Bête Noire Is Idle...");
        pannello.add(stateLabel, new GridConstraints(4, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        toolbar = new JToolBar();
        toolbar.setBackground(new Color(-14605013));
        toolbar.setFloatable(false);
        pannello.add(toolbar, new GridConstraints(0, 0, 1, 9, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 20), null, 0, false));
        me = new JLabel();
        Font meFont = this.$$$getFont$$$(null, -1, 16, me.getFont());
        if (meFont != null) me.setFont(meFont);
        me.setText("  Bête Noire Control Panel  ");
        toolbar.add(me);
        saveResultsButton = new JButton();
        saveResultsButton.setBackground(new Color(-1));
        saveResultsButton.setFocusable(false);
        saveResultsButton.setForeground(new Color(-5524801));
        saveResultsButton.setOpaque(false);
        saveResultsButton.setText("Save Scan");
        toolbar.add(saveResultsButton);
        exitButton = new JButton();
        exitButton.setText("|  Exit Bête Noire Control Panel");
        toolbar.add(exitButton);
        threadTextField = new JTextField();
        threadTextField.setBackground(new Color(-14605013));
        threadTextField.setForeground(new Color(-5524801));
        threadTextField.setToolTipText("Number Of Threads Here - 500 Threads Recommended");
        pannello.add(threadTextField, new GridConstraints(2, 3, 1, 6, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        startButton = new JButton();
        startButton.setAutoscrolls(true);
        startButton.setBackground(new Color(-14605013));
        startButton.setFocusable(false);
        startButton.setForeground(new Color(-5524801));
        startButton.setText("Start");
        startButton.setToolTipText("Start Project Copenheimer!");
        pannello.add(startButton, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        threadsLabel = new JLabel();
        threadsLabel.setBackground(new Color(-5524801));
        threadsLabel.setForeground(new Color(-5524801));
        threadsLabel.setText("Threads");
        pannello.add(threadsLabel, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JLabel label1 = new JLabel();
        label1.setBackground(new Color(-5524801));
        label1.setForeground(new Color(-5524801));
        label1.setText("MOTD");
        pannello.add(label1, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        motdText = new JTextField();
        motdText.setBackground(new Color(-14605013));
        motdText.setForeground(new Color(-5524801));
        pannello.add(motdText, new GridConstraints(3, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setBackground(new Color(-5524801));
        label2.setForeground(new Color(-5524801));
        label2.setText("Server Version");
        pannello.add(label2, new GridConstraints(3, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        final JLabel label3 = new JLabel();
        label3.setBackground(new Color(-5524801));
        label3.setForeground(new Color(-5524801));
        label3.setText("Minimal Players");
        pannello.add(label3, new GridConstraints(3, 7, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
        minPlayersText = new JTextField();
        minPlayersText.setBackground(new Color(-14605013));
        minPlayersText.setForeground(new Color(-5524801));
        pannello.add(minPlayersText, new GridConstraints(3, 8, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        versionText = new JTextField();
        versionText.setBackground(new Color(-14605013));
        versionText.setForeground(new Color(-5524801));
        pannello.add(versionText, new GridConstraints(3, 5, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        doAllCheckBox = new JCheckBox();
        doAllCheckBox.setBackground(new Color(-14605013));
        doAllCheckBox.setFocusable(false);
        doAllCheckBox.setText("Check All Information");
        pannello.add(doAllCheckBox, new GridConstraints(4, 6, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        pingCheckBox = new JCheckBox();
        pingCheckBox.setBackground(new Color(-14605013));
        pingCheckBox.setFocusable(false);
        pingCheckBox.setText("Check For ICMP Ping");
        pannello.add(pingCheckBox, new GridConstraints(4, 7, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {
            resultName = currentFont.getName();
        } else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
                resultName = fontName;
            } else {
                resultName = currentFont.getName();
            }
        }
        Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
        boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
        Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
        return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return pannello;
    }

}
