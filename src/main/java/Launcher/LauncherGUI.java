package Launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;


/**
 * The type Launcher.Launcher gui.
 */
public class LauncherGUI extends JPanel implements ActionListener {
    private final JButton openButton;
    private final JButton loadChronologicalLog;
    private final JButton loadJumpingLog;
    private final JButton startBattery;
    private final JCheckBox checkLog;
    private final JTextArea log;
    private final JFileChooser fc;

    private final Battery battery;
    private boolean loggerEnabled = true;
    private boolean test = false;

    /**
     * Instantiates a new Launcher.Launcher gui.
     */
    private LauncherGUI() {
        super(new BorderLayout());

        battery = new Battery();

        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        fc = new JFileChooser();

        URL iconOpen = getClass().getClassLoader().getResource("images/Open16.gif");
        assert iconOpen != null;
        openButton = new JButton("Open", new ImageIcon(iconOpen));
        openButton.setPreferredSize(new Dimension(120, 30));
        openButton.addActionListener(this);

        URL iconChronoLog = getClass().getClassLoader().getResource("images/chronoLog.png");
        assert iconChronoLog != null;
        loadChronologicalLog = new JButton("ChronoLog", new ImageIcon(iconChronoLog));
        loadChronologicalLog.setPreferredSize(new Dimension(120, 30));
        loadChronologicalLog.addActionListener(this);

        URL iconJumpLog = getClass().getClassLoader().getResource("images/jumpLog.png");
        assert iconJumpLog != null;
        loadJumpingLog = new JButton("JumpLog", new ImageIcon(iconJumpLog));
        loadJumpingLog.setPreferredSize(new Dimension(120, 30));
        loadJumpingLog.addActionListener(this);

        URL iconTest = getClass().getClassLoader().getResource("images/test.png");
        assert iconTest != null;
        startBattery = new JButton("Test", new ImageIcon(iconTest));
        startBattery.setPreferredSize(new Dimension(120, 30));
        startBattery.addActionListener(this);

        checkLog = new JCheckBox("Log");
        checkLog.setSelected(true);
        checkLog.addActionListener(this);


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(checkLog);
        buttonPanel.add(startBattery);
        buttonPanel.add(openButton);
        buttonPanel.add(loadChronologicalLog);
        buttonPanel.add(loadJumpingLog);


        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
        setPreferredSize( new Dimension( 640, 480 ) );
    }

    public void actionPerformed(ActionEvent e) {


        if (e.getSource() == openButton) {
            test = false;
            int returnVal = fc.showOpenDialog(LauncherGUI.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                try{

                    log.setText(battery.single(file, loggerEnabled));

                }
                catch(Exception ex){
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error ", JOptionPane.ERROR_MESSAGE);
                }

            }
            else {
                log.append("Command cancelled by user." + "\n");
            }
            log.setCaretPosition(log.getDocument().getLength());

        }
        else if (e.getSource() == loadChronologicalLog) {
            if(!test) {
                try {
                    File file = fc.getSelectedFile();
                    String fileLog = file.getName().replace(".owl", "") + "_Chronological.log";

                    File Log = new File("LOG/Launcher.LauncherGUI/" + fileLog);

                    Desktop desktop = Desktop.getDesktop();
                    if (Log.exists()) {
                        try {
                            desktop.open(Log);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                } catch (Exception ex) {
                    log.append("\nNO FILE SELECTED" + "\n");
                }
            }
            else
                JOptionPane.showMessageDialog(this, "After test there are too many files, please open manually.", "Warning ", JOptionPane.WARNING_MESSAGE);
        }
        else if (e.getSource() == loadJumpingLog) {
            if(!test) {
                try {
                    File file = fc.getSelectedFile();
                    String fileLog = file.getName().replace(".owl", "") + "_Jumping.log";

                    File Log = new File("LOG/Launcher.LauncherGUI/" + fileLog);

                    Desktop desktop = Desktop.getDesktop();
                    if (Log.exists()) {
                        try {
                            desktop.open(Log);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                } catch (Exception ex) {
                    log.append("\nNO FILE SELECTED" + "\n");
                }
            }
            else
                JOptionPane.showMessageDialog(this, "After test there are too many files, please open manually.", "Warning ", JOptionPane.WARNING_MESSAGE);
        }
        else if (e.getSource() == startBattery){
            test = true;
            log.setText("STARTING TESTS...\n");
            try {

                log.append(battery.start(loggerEnabled));
            }
            catch (Exception ex){
                log.append(ex.getMessage());
            }
        }
        else if(e.getSource() == checkLog){

            if(checkLog.isSelected()){
                loadJumpingLog.setEnabled(true);
                loadChronologicalLog.setEnabled(true);
                loggerEnabled = true;

            }
            else{
                loadJumpingLog.setEnabled(false);
                loadChronologicalLog.setEnabled(false);
                loggerEnabled = false;

            }

        }
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("ALC_REASONER - Manfredi - Santangelo - Scalella");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Add content to the window.
        frame.add(new LauncherGUI());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(() -> {
            //Turn off metal's use of bold fonts
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            createAndShowGUI();
        });
    }
}
