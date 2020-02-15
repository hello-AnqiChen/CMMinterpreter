package parser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GrammerAnalyzer {
    public static void main(String[] args) {
        JFrame frame = new JFrame("GrammerAnalyzer-by Ariel");
        frame.setSize(1000,700);
//        frame.setResizable(false);
        frame.setContentPane(new GrammerAnalyzer().JPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setLocation(300, 300);
        frame. setLocationRelativeTo(null);
//        frame.pack();
        frame.setVisible(true);
    }
    private JPanel JPanel;
    private JButton importButton;
    private JButton startButton;
    private JTextArea inputArea;
    private JTextArea output_g;
    private JTextArea output_e;
    private JFileChooser fileChooser;
    private final static String NEW_LINE =
            System.getProperty("line.separator");
    public GrammerAnalyzer(){
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        output_g.setText("grammer analyze");
        output_e.setText("errors analyze");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                inputArea.setText("");
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {

                    try(
                            BufferedReader input = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));)
                    {
                        String line = "";
                        while ((line = input.readLine()) != null) {
                            inputArea.append(line + NEW_LINE);
                        }
                    }
                    catch (IOException ioe) {
                        inputArea.setText("");
                        inputArea.setText("failed to import");
                    }
                }
            }
        });
        startButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                String text = inputArea.getText();
                output_e.setText("");
                output_g.setText("");
                Parser parser=new Parser(text);
                output_g.append(parser.analyze());
                output_e.append(parser.printError());
            }
        });
    }
}
