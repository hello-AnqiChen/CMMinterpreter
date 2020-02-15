package lexer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LexicalAnalyzer {

    public static void main(String[] args) {
        JFrame frame = new JFrame("LexicalAnalyzer-by Ariel");
        frame.setSize(1000,700);
//        frame.setResizable(false);
        frame.setContentPane(new LexicalAnalyzer().JPanel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setLocation(300, 300);
        frame. setLocationRelativeTo(null);
//        frame.pack();
        frame.setVisible(true);
    }

    private JPanel JPanel1;
    private JButton importButton;
    private JButton startButton;
    private JTextArea inputArea;
    private JTextArea outputArea;
    private JFileChooser fileChooser;
    private final static String NEW_LINE =
            System.getProperty("line.separator");
    public LexicalAnalyzer(){
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
                outputArea.setText("");
                Lexer lexer = new Lexer(text);
                outputArea.append(lexer.getResult());

            }
        });
    }
}
