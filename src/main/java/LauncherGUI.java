
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.SwingUtilities;
import java.awt.Desktop;


public class LauncherGUI extends JPanel
        implements ActionListener {
    static private final String newline = "\n";
    JButton openButton;
    JButton loadChronologicalLog;
    JButton loadJumpingLog;
    JTextArea log;
    JFileChooser fc;
    OWLOntologyManager man;
    OWLClassExpression expression;
    OWLReasonerFactory factoryALC_chrono;
    OWLReasonerFactory factoryALC_jump;
    OWLReasoner alc_chrono;
    OWLReasoner alc_jump;
    public LauncherGUI() {
        super(new BorderLayout());

        man = OWLManager.createOWLOntologyManager();

        /*TABLEAU Chronological*/
        factoryALC_chrono = new ALCReasonerFactory();
        alc_chrono = factoryALC_chrono.createReasoner(null);

        /*TABLEAU Jumping*/
        factoryALC_jump = new ALCReasonerFactory("Jumping");
        alc_jump = factoryALC_chrono.createReasoner(null);

        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);


        fc = new JFileChooser();


        openButton = new JButton("Open", new ImageIcon("images/Open16.gif"));
        openButton.setPreferredSize(new Dimension(200, 30));
        openButton.addActionListener(this);

        loadChronologicalLog = new JButton("ChronoLog", new ImageIcon("images/chronoLog.png"));
        loadChronologicalLog.setPreferredSize(new Dimension(200, 30));
        loadChronologicalLog.addActionListener(this);

        loadJumpingLog = new JButton("JumpLog", new ImageIcon("images/jumpLog.png"));
        loadJumpingLog.setPreferredSize(new Dimension(200, 30));
        loadJumpingLog.addActionListener(this);


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);
        buttonPanel.add(loadChronologicalLog);
        buttonPanel.add(loadJumpingLog);


        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
        setPreferredSize( new Dimension( 640, 480 ) );
    }

    public void actionPerformed(ActionEvent e) {


        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(LauncherGUI.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                try {
                    man.clearOntologies();
                    OWLOntology ont = man.loadOntologyFromOntologyDocument(file);
                    OWLDataFactory df = man.getOWLDataFactory();
                    Optional<IRI> optIri = ont.getOntologyID().getOntologyIRI();
                    assert optIri.isPresent();
                    IRI iri = optIri.get();
                    OWLClass flag = df.getOWLClass(iri + "#assioma");
                    Set<OWLAxiom> ontologyAxiom = ont.axioms(flag).collect(Collectors.toSet());

                    /*HERMIT*/
                    ReasonerFactory factoryHermit = new ReasonerFactory();
                    OWLReasoner hermit = factoryHermit.createReasoner(ont);

                    /*Logger*/
                    LoggerManager.setFile(file.getName().replace(".owl", ""), LauncherGUI.class);


                    if (ontologyAxiom.size() > 1) {
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
                        log.append("Concetto in input:: " + expression.toString() + "." + newline);
                        log.append("\nManchester Sintax: \n\n" + OntologyRenderer.render(expression) + "." + newline);
                        log.append("\n---------------- CHECK CONCEPT ----------------" + newline);

                        /*ChronologicaTableau*/
                        LoggerManager.setFile(file.getName().replace(".owl", "") + "_Chronological", LauncherGUI.class);
                        long chrono_StartTime = System.currentTimeMillis();
                        boolean resultChrono = alc_chrono.isSatisfiable(expression);
                        long chrono_EndTime = System.currentTimeMillis();
                        Integer chronoIteration=((ALCReasoner)alc_chrono).getIteration();

                        System.out.println("\nALC (Chronological Tableau): " + resultChrono + " (" + (chrono_EndTime - chrono_StartTime) + " milliseconds) - ("+ chronoIteration + " iterations)");
                        log.append("\nALC (Chronological Tableau): " + resultChrono + " (" + (chrono_EndTime - chrono_StartTime) + " milliseconds) - ("+ chronoIteration + " iterations)" + newline);
                        LoggerManager.writeInfoLog("ALC (Chronological Tableau): " + resultChrono, LauncherGUI.class);

                        /*JumpingTableau*/
                        LoggerManager.setFile(file.getName().replace(".owl", "") + "_Jumping", LauncherGUI.class);
                        long jump_StartTime = System.currentTimeMillis();
                        boolean resultJump = alc_jump.isSatisfiable(expression);
                        long jump_EndTime = System.currentTimeMillis();
                        Integer jumpIteration=((ALCReasoner)alc_jump).getIteration();
                        System.out.println("ALC(Jumping Tableau): " + resultJump + " ("+(jump_EndTime - jump_StartTime) + " milliseconds) - ("+jumpIteration + " iterations)");
                        log.append("\nALC (Jumping Tableau): " + resultJump + " (" + (jump_EndTime - jump_StartTime) + " milliseconds) - ("+ jumpIteration + " iterations)" + newline);
                        LoggerManager.writeInfoLog("ALC(Jumping Tableau): " + resultJump, LauncherGUI.class);
                        if(resultJump) {
                            String model = "Modello trovato: "+((ALCReasoner)alc_jump).getModel();
                            LoggerManager.writeInfoLog(model, Launcher.class);
                        }

                        /*HermiT*/
                        long hermit_StartTime = System.currentTimeMillis();
                        boolean resultHermit = hermit.isSatisfiable(expression);
                        long hermit_EndTime = System.currentTimeMillis();
                        System.out.println("HermiT: " + resultHermit + " (" + (hermit_EndTime - hermit_StartTime) + " milliseconds)");
                        log.append("\nHermiT: " + resultHermit + " (" + (hermit_EndTime - hermit_StartTime) + " milliseconds)" + newline);
                        LoggerManager.writeInfoLog("HermiT: " + resultHermit, LauncherGUI.class);

                    }
                } catch(Exception ex){
                    System.out.println("INVALID FILE...");
                    log.append("\nINVALID FILE"+newline);
                }
            } else {
                log.append("Command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());

        } else if (e.getSource() == loadChronologicalLog) {
            try {
                File file = fc.getSelectedFile();
                String fileLog = file.getName().replace(".owl", "") + "_Chronological.log";

                File Log = new File("LOG/LauncherGUI/" + fileLog);

                Desktop desktop = Desktop.getDesktop();
                if (Log.exists()) {
                    try {
                        desktop.open(Log);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                System.out.println("NO FILE SELECTED");
                log.append("\nNO FILE SELECTED"+newline);
            }
        } else if (e.getSource() == loadJumpingLog) {
            try {
                File file = fc.getSelectedFile();
                String fileLog = file.getName().replace(".owl", "") + "_Jumping.log";

                File Log = new File("LOG/LauncherGUI/" + fileLog);

                Desktop desktop = Desktop.getDesktop();
                if (Log.exists()) {
                    try {
                        desktop.open(Log);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            } catch (Exception ex) {
                System.out.println("NO FILE SELECTED");
                log.append("\nNO FILE SELECTED"+newline);
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
