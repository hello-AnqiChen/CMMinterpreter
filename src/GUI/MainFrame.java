package GUI;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.undo.UndoManager;

import interpretException.InterpretException;
import interpreter.Interpreter;
import lexer.Lexer;
import parser.Node;
import parser.Parser;
import semantics.SemException;
import semantics.Semantic;

public class MainFrame extends JFrame implements outputListener{
    static final int WIDTH=1600;
    static final int HEIGHT=900;
    static Font font1 = new Font("楷体", Font.PLAIN, 21);//菜单栏字体
    static Font font2 = new Font("楷体", Font.PLAIN, 24);//文本编辑区字体

    JFrame frame;
    JPanel panel;
    JToolBar bar;//按钮区
    JMenuBar menuBar;//菜单栏

    //菜单栏
    JMenuItem newItem;
    JMenuItem openItem;
    JMenuItem saveItem;
    JMenuItem exitItem;
    JMenuItem undoItem;
    JMenuItem redoItem;
    JMenuItem runItem;
    JMenuItem stopItem;
    JMenuItem aboutItem;

    //按钮
    JButton runBtn;
    JButton stopBtn;

    //输入区
    JScrollPane inPan;
    JPanel inPanel;
    //代码区
    JTextPane editor;
    JPanel inTextPan;
    //行号区
    JList lineNo;
    JPanel lineArea;

    //undo、redo
    UndoManager um;

    //输出区
    JScrollPane outPan;
    //分栏
    JTabbedPane tab;
    TextArea runPanel;
    TextArea intermediatePanel;
    TextArea lexerPanel;
    TextArea parserPanel;

    public MainFrame(){

        frame = new JFrame("CMM_Complier");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        menuBar = new JMenuBar();
        panel = new JPanel(new BorderLayout());
        frame.setContentPane(panel);
        frame.setJMenuBar(menuBar);

        //菜单栏
        JMenu menu1=new JMenu("File");
        JMenu menu3=new JMenu("Run");
        menu1.setFont(font1);
        menu3.setFont(font1);
        menuBar.add(menu1);
        menuBar.add(menu3);
        //子菜单
        newItem = new JMenuItem("New");
        openItem = new JMenuItem("Open");
        saveItem = new JMenuItem("Save");
        exitItem = new JMenuItem("Exit");
        undoItem = new JMenuItem("撤销");
        redoItem = new JMenuItem("重做");
        runItem = new JMenuItem("Run");
        stopItem = new JMenuItem("Stop");
        aboutItem = new JMenuItem("关于");
        newItem.setFont(font1);
        openItem.setFont(font1);
        saveItem.setFont(font1);
        exitItem.setFont(font1);
        undoItem.setFont(font1);
        redoItem.setFont(font1);
        runItem.setFont(font1);
        stopItem.setFont(font1);
        aboutItem.setFont(font1);
        menu1.add(newItem);
        menu1.addSeparator();
        menu1.add(openItem);
        menu1.addSeparator();
        menu1.add(saveItem);
        menu1.addSeparator();
        menu1.add(exitItem);
        menu3.add(runItem);
        menu3.addSeparator();
        menu3.add(stopItem);

        //按钮
        runBtn = new JButton();
        stopBtn = new JButton();
        runBtn.setBounds(0, 0, 30, 24);
        stopBtn.setBounds(0, 0, 30, 24);
        //根据按钮大小改变图片大小
        runBtn.setBorderPainted(false);
        stopBtn.setBorderPainted(false);
        bar = new JToolBar();
        bar.add(runBtn);
        bar.add(stopBtn);

        //输入区
        //代码区
        editor = new JTextPane();
        editor.setFont(font2);
        inTextPan = new JPanel(new BorderLayout());
        inTextPan.add(editor);
        inPanel = new JPanel(new BorderLayout());
        inPanel.add(BorderLayout.CENTER, inTextPan);
        //行号区
        lineNo = new JList();
        String[] in = new String[10000];
        for(int i=0;i<9999;i++){
            in[i] = String.format("%1$4s", i + 1);
        }
        lineNo = new JList(in);
        lineNo.setFont(new Font("楷体", Font.PLAIN, 22));
        lineNo.setForeground(Color.black);
        lineArea = new JPanel();
        lineArea.add(lineNo);
        inPanel.add(BorderLayout.WEST, lineArea);
        inPan = new JScrollPane();
        inPan.add(inPanel);
        inPan.setViewportView(inPanel);

        //输出区
        outPan = new JScrollPane();
        tab = new JTabbedPane();
        runPanel = new TextArea();
        intermediatePanel = new TextArea();
        lexerPanel = new TextArea();
        parserPanel = new TextArea();
        runPanel.setEditable(false);
        intermediatePanel.setEditable(false);
        lexerPanel.setEditable(false);
        parserPanel.setEditable(false);
        tab.setFont(font1);
        tab.add("Run", runPanel);
        tab.add("Intermediate Code", intermediatePanel);
        tab.add("Lexer", lexerPanel);
        tab.add("Parser", parserPanel);
        tab.setTabPlacement(JTabbedPane.RIGHT);
        outPan.setViewportView(tab);

        BorderLayout bord = new BorderLayout();
        panel.setLayout(bord);
        panel.add("North",bar);
        panel.add(BorderLayout.CENTER, inPan);
        frame.setVisible(true);
        frame.setSize(WIDTH, HEIGHT);


        //菜单栏点击事件
        Monitor monitor = new Monitor();
        newItem.addActionListener(monitor);
        openItem.addActionListener(monitor);
        saveItem.addActionListener(monitor);
        exitItem.addActionListener(monitor);
        undoItem.addActionListener(monitor);
        redoItem.addActionListener(monitor);
        runItem.addActionListener(monitor);
        stopItem.addActionListener(monitor);
        aboutItem.addActionListener(monitor);

        //按钮点击事件
        runBtn.addActionListener(monitor);
        stopBtn.addActionListener(monitor);
    }

    /**
     * 实时输出执行结果
     * @param output 解释执行部分输出内容
     */
    @Override
    public void output(String output) {
        runPanel.append(output);
        panel.add(BorderLayout.EAST, outPan);
        panel.updateUI();
    }

    /**
     * 处理菜单栏、按钮点击事件
     */
    class Monitor implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            Object o = e.getSource();
            if(o == newItem)
                newFile();
            else if (o == openItem){
                try {
                    openFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else if(o == saveItem){
                try {
                    saveFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else if(o == exitItem){
                int n = JOptionPane.showConfirmDialog(null,
                        "确定要退出吗?",
                        "退出",JOptionPane.OK_CANCEL_OPTION);
                if(n == 0)
                    System.exit(0);
            } else if(o == undoItem){
                if(um.canUndo())
                    um.undo();
            } else if(o == redoItem){
                if(um.canRedo())
                    um.redo();
            } else if(o == runItem | o == runBtn){
                run();
            } else if(o == stopItem | o == stopBtn) {
                stop();
            } else if(o == aboutItem)
                JOptionPane.showMessageDialog(null,
                        "CMM解释器\nAll rights reserved.",
                        "关于", JOptionPane.PLAIN_MESSAGE);
        }
    }

    /**
     * 新建文件
     */
    private void newFile(){
        if(!editor.getText().equals("")){
            int n = JOptionPane.showConfirmDialog(null,
                    "是否保存更改?",
                    "CMM解释器",JOptionPane.YES_NO_CANCEL_OPTION);
            if(n == 0){
                try {
                    saveFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            else if(n == 1){
                editor.setText("");
            }
        }
    }

    /**
     * 打开文件
     */
    private void openFile() throws IOException {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.showOpenDialog(inTextPan);
        String fileName = null;
        fileName = jFileChooser.getSelectedFile().getPath();
        File file = new File(fileName);
        int fLength;
        fLength = (int) file.length();
        int num = 0;
        FileReader fReader = new FileReader(file);
        char[] date = new char[fLength];
        while(fReader.ready())
        {
            num += fReader.read(date, num, fLength - num);
        }

        fReader.close();
        editor.setText(new String(date, 0,num));
    }

    /**
     * 保存文件
     */
    private void saveFile() throws IOException {
        if(editor.getText().equals(""))
            JOptionPane.showMessageDialog(null,
                    "文件内容不能为空！",
                    "错误",JOptionPane.OK_OPTION);
        else {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.showSaveDialog(inTextPan);
            String filePath=jFileChooser.getSelectedFile().getAbsolutePath();
            FileWriter fw = new FileWriter(filePath);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            pw.print(editor.getText());
            bw.close();
            fw.close();
        }
    }

    /**
     * 运行
     */
    private void run(){
        runPanel.setText("");
        intermediatePanel.setText("");
        lexerPanel.setText("");
        parserPanel.setText("");
        runPanel.setFont(new Font("楷体", Font.PLAIN, 23));
        runPanel.setForeground(new Color(0x000000));

        if(!editor.getText().equals("")) {
            Lexer lexer = new Lexer(editor.getText());
            Parser parser = new Parser(editor.getText());
            parser.parse();
            if(parser.errors.isEmpty()){

                Semantic semantic = new Semantic(parser.getTree());
                try {
                    semantic.semAnalyze();
                    intermediatePanel.setText(semantic.getCodes());
                    Interpreter interpreter = new Interpreter();
                    interpreter.registerListener(this);
                    interpreter.interpret(semantic);
                } catch (SemException e) {
                    runPanel.setText(e.getMessage());
                    runPanel.setFont(new Font("楷体", Font.PLAIN, 23));
                    runPanel.setForeground(new Color(0xEE2C2C));
                    intermediatePanel.setText(semantic.getCodes());
                } catch (InterpretException e){
                    runPanel.setText(e.getMessage());
                    runPanel.setFont(new Font("楷体", Font.PLAIN, 23));
                    runPanel.setForeground(new Color(0xEE2C2C));
                }
                lexerPanel.setText(lexer.getResult());
                parserPanel.setText(parser.printTree());
            }else{
                runPanel.setText(parser.printError());
                runPanel.setFont(new Font("楷体", Font.PLAIN, 23));
                runPanel.setForeground(new Color(0xEE2C2C));
                intermediatePanel.setText("");
                lexerPanel.setText(lexer.getResult());
                parserPanel.setText(parser.getTree().displayTree(0));
            }
        }

        panel.add(BorderLayout.CENTER, inPan);
        panel.add(BorderLayout.EAST, outPan);

        panel.updateUI();
    }

    /**
     * 停止运行
     */
    private void stop(){
        panel.remove(outPan);
        panel.add(BorderLayout.CENTER, inPan);
        panel.updateUI();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    //UIManager.setLookAndFeel("org.jvnet.substance.skin.SubstanceDustLookAndFeel");
                    JFrame.setDefaultLookAndFeelDecorated(true);
                    JDialog.setDefaultLookAndFeelDecorated(true);
                    new MainFrame();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Something went wrong!");
                }
            }
        });
    }
}