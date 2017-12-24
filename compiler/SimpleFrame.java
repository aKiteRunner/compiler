package compiler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class SimpleFrame extends JFrame {
    //打开的文件的名字
    private String fileName;
    //保存的文件的名字
    private String saveName;
    //左文本区域
    private JTextArea jTextAreal;
    //右文本区域
    private JTextArea jTextArear;
    //每个单词名

    public SimpleFrame() {
        super();
        Toolkit toolkit;
        Dimension screenSize;
        JPanel jPanel;
        JScrollPane jScrollPanel;
        JScrollPane jScrollPaner;
        JPanel buttonJPanel;
        JButton submit;
        JMenuBar jMenuBar;
        int width;
        int height;
        int w;
        int h;
        toolkit = Toolkit.getDefaultToolkit();
        screenSize = toolkit.getScreenSize();
        setSize(1080, 720);
        width = screenSize.width;
        height = screenSize.height;
        w = getWidth();
        h = getHeight();
        setLocation((width - w) / 2, (height - h) / 2);
        setTitle("PL0");
        jPanel = (JPanel) getContentPane();
        setResizable(false);
        jScrollPanel = new JScrollPane(jTextAreal = new JTextArea("源代码: " + "\n", 10, 33));
        jScrollPaner = new JScrollPane(jTextArear = new JTextArea("词法分析: " + "\n", 10, 33));
        jTextArear.setTabSize(4);
        jTextArear.setFont(new Font("微软雅黑", Font.PLAIN, 17));
        jTextAreal.setTabSize(4);
        jTextAreal.setFont(new Font("微软雅黑", Font.PLAIN, 17));
        buttonJPanel = new JPanel();
        submit = new JButton("词法分析");
        submit.addActionListener(new start());
        buttonJPanel.add(submit);
        jPanel.add(jScrollPanel, BorderLayout.WEST);
        jPanel.add(jScrollPaner, BorderLayout.EAST);
        jPanel.add(buttonJPanel, BorderLayout.SOUTH);
        jMenuBar = createJMenuBar();
        setJMenuBar(jMenuBar);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jTextAreal.setEditable(false);
        jTextArear.setEditable(false);
        setVisible(true);
    }

    public class start implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (fileName != null) {
                    Lexer lexer = new Lexer(fileName);
                    lexer.lex();
                    jTextArear.setText(String.format("%-24s%-24s%-24s\n", "单词", "类别", "值"));
                    for (int i = 0; i < lexer.getTable().size(); ++i) {
                        Token token = lexer.getTable().get(i);
                        jTextArear.append(String.format("%-24s%-24s%-24s\n", token.name, token.symbol, token.name));
                    }
                }
            } catch (FileNotFoundException e1) {
                jTextArear.setText("文件不存在: " + fileName);
            } catch (IOException e1) {
                jTextArear.setText(e1.toString());
            } catch (LexException e1) {
                jTextArear.setText("源代码出现错误\n");
                jTextArear.append(e1.toString());
            }
        }
    }

    private JMenuBar createJMenuBar() {
        JMenuBar jMenuBar = new JMenuBar();
        JMenu fMenu = new JMenu("file");
        JMenuItem oMenu = new JMenuItem("Open");
        JMenuItem eMenu = new JMenuItem("Exit");
        oMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jTextAreal.setText("源代码: \n");
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle("词法分析: ");
                int ret = jFileChooser.showOpenDialog(null);
                //如果选择了文件
                if (JFileChooser.APPROVE_OPTION == ret) {
                    FileReader fileReader = null;
                    BufferedReader bufferedReader = null;
                    String line;
                    fileName = jFileChooser.getSelectedFile().toString();
                    File infile = new File(fileName);
                    try {
                        fileReader = new FileReader(fileName);
                        bufferedReader = new BufferedReader(fileReader);
                        while ((line = bufferedReader.readLine()) != null) {
                            jTextAreal.append(line + "\n");
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    } finally {
                        try {
                            bufferedReader.close();
                            fileReader.close();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }

            }
        });
        eMenu.addActionListener((e) -> {
            SimpleFrame.this.dispose();
        });
        fMenu.add(oMenu);
        fMenu.addSeparator();
        fMenu.addSeparator();
        fMenu.add(eMenu);
        fMenu.addSeparator();
        jMenuBar.add(fMenu);
        return jMenuBar;
    }

    public static void main(String[] args) {
        new SimpleFrame();
    }
}
