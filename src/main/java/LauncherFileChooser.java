
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


public class LauncherFileChooser extends JPanel
        implements ActionListener {
    static private final String newline = "\n";
    JButton openButton;
    JButton loadLog;
    JTextArea log;
    JFileChooser fc;

    public LauncherFileChooser() {
        super(new BorderLayout());


        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);


        fc = new JFileChooser();


        openButton = new JButton("Open", new ImageIcon("images/Open16.gif"));
        openButton.setPreferredSize(new Dimension(200, 30));
        openButton.addActionListener(this);

        loadLog = new JButton("Log", new ImageIcon("images/Log1.png"));
        loadLog.setPreferredSize(new Dimension(200, 30));
        loadLog.addActionListener(this);


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);
        buttonPanel.add(loadLog);


        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
        setPreferredSize( new Dimension( 640, 480 ) );
    }

    public void actionPerformed(ActionEvent e) {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = null;
        OWLClassExpression expression = null;

        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(LauncherFileChooser.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                try {
                    ont = man.loadOntologyFromOntologyDocument(file);
                    OWLDataFactory df = man.getOWLDataFactory();
                    Optional<IRI> optIri = ont.getOntologyID().getOntologyIRI();
                    assert optIri.isPresent();
                    IRI iri = optIri.get();
                    OWLClass flag = df.getOWLClass(iri + "#assioma");
                    Set<OWLAxiom> ontologyAxiom = ont.axioms(flag).collect(Collectors.toSet());

                    /*TABLEAU Chronological*/
                    OWLReasonerFactory factoryALC_chrono = new ALCReasonerFactory();
                    OWLReasoner alc_chrono = factoryALC_chrono.createReasoner(null);

                    /*HERMIT*/
                    ReasonerFactory factoryHermit = new ReasonerFactory();
                    OWLReasoner hermit = factoryHermit.createReasoner(ont);

                    /*Logger*/
                    LoggerManager.setFile(file.getName().replace(".owl", ""), LauncherFileChooser.class);


                    if (ontologyAxiom.size() > 1) {
                        LoggerManager.writeErrorLog("Invalid input concept", Launcher.class);
                        throw new IllegalArgumentException("Invalid input concept");
                    }

                    OWLEquivalentClassesAxiomImpl axiom = (OWLEquivalentClassesAxiomImpl) ontologyAxiom.iterator().next();

                    Set<OWLClassExpression> expressions = axiom.classExpressions().collect(Collectors.toSet());
                    //OWLClassExpression expression = null;
                    for (OWLClassExpression E : expressions) {
                        if (!E.isOWLClass()) {
                            expression = E;
                            break;
                        }
                    }

                    if (expression != null) {

                        log.append("\n\nOpening: " + file.getName() + "." + newline);
                        //log.append("Concetto in input:: " + expression.toString() + "." + newline);
                        log.append("\nManchester Sintax: \n\n" + OntologyRenderer.render(expression) + "." + newline);
                        log.append("\n---------------- CHECK CONCEPT ----------------" + newline);
                        /*ChronologicaTableau*/
                        LoggerManager.setFile(file.getName().replace(".owl", "") + "_Chronological", LauncherFileChooser.class);
                        long chrono_StartTime = System.currentTimeMillis();
                        boolean resultChrono = alc_chrono.isSatisfiable(expression);
                        long chrono_EndTime = System.currentTimeMillis();
                        System.out.println("\nALC (Chronological Tableau): " + resultChrono + " (" + (chrono_EndTime - chrono_StartTime) + " milliseconds )"); //+ chronoIteration + " iterazioni");
                        log.append("\nALC (Chronological Tableau): " + resultChrono + " (" + (chrono_EndTime - chrono_StartTime) + " milliseconds )" + newline);
                        LoggerManager.writeInfoLog("ALC (Chronological Tableau): " + resultChrono, LauncherFileChooser.class);

                        /*HermiT*/
                        long hermit_StartTime = System.currentTimeMillis();
                        boolean resultHermit = hermit.isSatisfiable(expression);
                        long hermit_EndTime = System.currentTimeMillis();
                        System.out.println("HermiT: " + resultHermit + " (" + (hermit_EndTime - hermit_StartTime) + " milliseconds)");
                        log.append("\nHermiT: " + resultHermit + " (" + (hermit_EndTime - hermit_StartTime) + " milliseconds)" + newline);
                        LoggerManager.writeInfoLog("HermiT: " + resultHermit, LauncherFileChooser.class);

                    }
                } catch(Exception ex){
                    System.out.println("INVALID FILE...");
                    log.append("\nINVALID FILE"+newline);
                }
            } else {
                log.append("Command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());

        } else if (e.getSource() == loadLog) {
            try {
                File file = fc.getSelectedFile();
                String fileLog = file.getName().replace(".owl", "") + "_Chronological.log";

                File Log = new File("LOG/LauncherFileChooser/" + fileLog);

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
        frame.add(new LauncherFileChooser());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws Exception{
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                createAndShowGUI();
            }
        });
    }
}
