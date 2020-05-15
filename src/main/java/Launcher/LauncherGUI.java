package Launcher;

import ALC_Reasoner.ALCReasoner;
import ALC_Reasoner.ALCReasonerFactory;
import ALC_Reasoner.LoggerManager;
import ALC_Reasoner.OntologyRenderer;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.SwingUtilities;
import java.awt.Desktop;


/**
 * The type Launcher.Launcher gui.
 */
public class LauncherGUI extends JPanel implements ActionListener {
    static private final String newline = "\n";
    private final JButton openButton;
    private final JButton loadChronologicalLog;
    private final JButton loadJumpingLog;
    private final JButton startBattery;
    private final JCheckBox checkLog;
    private final JTextArea log;
    private final JFileChooser fc;
    private final OWLOntologyManager man;
    private OWLReasoner alc_chrono;
    private OWLReasoner alc_jump;
    private final OWLReasoner LOGalc_chrono;
    private final OWLReasoner LOGalc_jump;
    private final OWLReasoner NoLOGalc_chrono;
    private final OWLReasoner NoLOGalc_jump;
    private final Battery battery;
    private boolean loggerEnabled = true;
    private boolean test = false;
    /**
     * The Expression.
     */
    OWLClassExpression expression = null;

    /**
     * Instantiates a new Launcher.Launcher gui.
     */
    private LauncherGUI() {
        super(new BorderLayout());

        man = OWLManager.createOWLOntologyManager();
        battery = new Battery();
        /*TABLEAU Chronological*/
        OWLReasonerFactory factoryALC_chrono = new ALCReasonerFactory();
        alc_chrono = factoryALC_chrono.createReasoner(null);

        NoLOGalc_chrono = alc_chrono;

        /*TABLEAU Jumping*/
        OWLReasonerFactory factoryALC_jump = new ALCReasonerFactory("Jumping");
        alc_jump = factoryALC_jump.createReasoner(null);

        NoLOGalc_jump = alc_jump;

        /*TABLEAU LOGChronological*/
        OWLReasonerFactory LOGfactoryALC_chrono = new ALCReasonerFactory("LOGChronological");
        LOGalc_chrono = LOGfactoryALC_chrono.createReasoner(null);


        /*TABLEAU LOGJumping*/
        OWLReasonerFactory LOGfactoryALC_jump = new ALCReasonerFactory("LOGJumping");
        LOGalc_jump = LOGfactoryALC_jump.createReasoner(null);

        alc_chrono = LOGalc_chrono;
        alc_jump = LOGalc_jump;


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
                OWLOntology ont=null;
                try {
                    man.clearOntologies();
                    ont = man.loadOntologyFromOntologyDocument(file);



                } catch(OWLOntologyCreationException ex){
                    log.append("\nINVALID FILE"+newline);
                }
                OWLDataFactory df = man.getOWLDataFactory();
                assert ont != null;
                Optional<IRI> optIri = ont.getOntologyID().getOntologyIRI();
                assert optIri.isPresent();
                IRI iri = optIri.get();
                OWLClass flag = df.getOWLClass(iri + "#assioma");

                Set<OWLAxiom> ontologyAxiom = ont.axioms(flag).collect(Collectors.toSet());
                    /*HERMIT*/
                    ReasonerFactory factoryHermit = new ReasonerFactory();
                    OWLReasoner hermit = factoryHermit.createReasoner(ont);

                    /*Logger*/
                    if(loggerEnabled)
                        LoggerManager.setFile(file.getName().replace(".owl", ""), LauncherGUI.class, true);


                    if (ontologyAxiom.size() > 1) {
                        if(loggerEnabled)
                            LoggerManager.writeErrorLog("Invalid input concept", LauncherGUI.class);
                        throw new IllegalArgumentException("Invalid input concept");
                    }

                    OWLEquivalentClassesAxiomImpl axiom = (OWLEquivalentClassesAxiomImpl) ontologyAxiom.iterator().next();

                    Set<OWLClassExpression> expressions = axiom.classExpressions().collect(Collectors.toSet());
                    for (OWLClassExpression E : expressions) {
                        if (!E.isOWLClass()) {
                            expression = E;
                            break;
                        }
                    }

                    if (expression != null) {
                        log.setText("");
                        log.append("\n\nOpening: " + file.getName() + "." + newline);
                        String eS = expressions.toString();
                        String toDelete = eS.substring(eS.indexOf("http"), eS.indexOf("#")+1);
                        eS = eS.replaceAll(toDelete,"");
                        eS = eS.replace(", <assioma>","");
                        OWLObjectRenderer renderer = new DLSyntaxObjectRenderer();
                        renderer.setShortFormProvider(new SimpleShortFormProvider());
                        log.append("\nConcetto in input: " + renderer.render(expression) + "." + newline);
                        log.append("\nManchester Sintax: \n\n" + OntologyRenderer.render(expression) + "." + newline);
                        log.append("\n---------------- CHECK CONCEPT ----------------" + newline);

                        /*ChronologicaTableau*/
                        if(loggerEnabled)
                            LoggerManager.setFile(file.getName().replace(".owl", "") + "_Chronological", LauncherGUI.class, true);
                        long chrono_StartTime = System.currentTimeMillis();
                        boolean resultChrono = alc_chrono.isSatisfiable(expression);
                        long chrono_EndTime = System.currentTimeMillis();
                        Integer chronoIteration=((ALCReasoner)alc_chrono).getIteration();

                        log.append("\nALC (Chronological ALC_Reasoner): " + resultChrono + " (" + (chrono_EndTime - chrono_StartTime) + " milliseconds) - ("+ chronoIteration + " iterations)" + newline);
                        if(loggerEnabled)
                            LoggerManager.writeInfoLog("ALC (Chronological ALC_Reasoner): " + resultChrono, LauncherGUI.class);
                        if(resultChrono) {
                            String model = "Modello trovato: "+((ALCReasoner)alc_chrono).getModel()+ newline;
                            if(loggerEnabled)
                                LoggerManager.writeInfoLog(model, LauncherGUI.class);
                            log.append(model);
                        }
                        /*ALC_Reasoner.JumpingTableau*/
                        if(loggerEnabled)
                            LoggerManager.setFile(file.getName().replace(".owl", "") + "_Jumping", LauncherGUI.class, true);
                        long jump_StartTime = System.currentTimeMillis();
                        boolean resultJump = alc_jump.isSatisfiable(expression);
                        long jump_EndTime = System.currentTimeMillis();
                        Integer jumpIteration=((ALCReasoner)alc_jump).getIteration();
                        log.append("\nALC (Jumping ALC_Reasoner): " + resultJump + " (" + (jump_EndTime - jump_StartTime) + " milliseconds) - ("+ jumpIteration + " iterations)" + newline);
                        if(loggerEnabled)
                            LoggerManager.writeInfoLog("ALC(Jumping ALC_Reasoner): " + resultJump, LauncherGUI.class);
                        if(resultJump) {
                            String model = "Modello trovato: "+((ALCReasoner)alc_jump).getModel()+ newline;
                            if(loggerEnabled)
                                LoggerManager.writeInfoLog(model, LauncherGUI.class);
                            log.append(model);
                        }

                        /*HermiT*/
                        long hermit_StartTime = System.currentTimeMillis();
                        boolean resultHermit = hermit.isSatisfiable(expression);
                        long hermit_EndTime = System.currentTimeMillis();
                        log.append("\nHermiT: " + resultHermit + " (" + (hermit_EndTime - hermit_StartTime) + " milliseconds)" + newline);

                    }

            } else {
                log.append("Command cancelled by user." + newline);
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
                    log.append("\nNO FILE SELECTED" + newline);
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
                    log.append("\nNO FILE SELECTED" + newline);
                }
            }
            else
                JOptionPane.showMessageDialog(this, "After test there are too many files, please open manually.", "Warning ", JOptionPane.WARNING_MESSAGE);
        }
        else if (e.getSource() == startBattery){
            test = true;
            log.setText("STARTING TESTS...\n");
            try {
                String result = battery.start(loggerEnabled);
                log.append(result);
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
                alc_chrono = LOGalc_chrono;
                alc_jump = LOGalc_jump;
            }
            else{
                loadJumpingLog.setEnabled(false);
                loadChronologicalLog.setEnabled(false);
                loggerEnabled = false;
                alc_chrono = NoLOGalc_chrono;
                alc_jump = NoLOGalc_jump;
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
