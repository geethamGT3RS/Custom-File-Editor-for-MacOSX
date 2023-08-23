 
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.regex.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;


public class CodeEditor extends JFrame implements ActionListener {
    private JTabbedPane tabbedPane;
    private JFileChooser fileChooser;
    private boolean isLightMode = true;
    private JMenu viewMenu; // New view menu
    private JCheckBoxMenuItem lightModeMenuItem; // Light mode menu item
    private JCheckBoxMenuItem darkModeMenuItem; 
    private JTextPane textPane; 
    public CodeEditor() {
        setTitle("Geetu's Code Editor");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        tabbedPane = new JTabbedPane();

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        ImageIcon fileIcon = new ImageIcon("file.png");
        fileMenu.setIcon(fileIcon);
        JMenuItem openMenuItem = new JMenuItem("Open");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        JMenuItem saveasMenuItem = new JMenuItem("Save As");  
        JMenuItem newMenuItem = new JMenuItem("New"); 
        JMenuItem closeMenuItem = new JMenuItem("Close"); 
        openMenuItem.addActionListener(this);
        saveMenuItem.addActionListener(this);
        saveasMenuItem.addActionListener(this);
        newMenuItem.addActionListener(this);
        closeMenuItem.addActionListener(this);
        fileMenu.add(closeMenuItem);
        fileMenu.add(openMenuItem);
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveasMenuItem);
        fileMenu.add(newMenuItem);
        menuBar.add(fileMenu);

        viewMenu = new JMenu("View");
        ImageIcon viewIcon = new ImageIcon("view.png");
        viewMenu.setIcon(viewIcon);
        lightModeMenuItem = new JCheckBoxMenuItem("Light Mode");
        darkModeMenuItem = new JCheckBoxMenuItem("Dark Mode");
        lightModeMenuItem.addActionListener(this);
        darkModeMenuItem.addActionListener(this);
        viewMenu.add(lightModeMenuItem);
        viewMenu.add(darkModeMenuItem);
        menuBar.add(viewMenu); 

        JMenu editMenu = new JMenu("Edit");
        ImageIcon editIcon = new ImageIcon("edit.png");
        editMenu.setIcon(editIcon);
        JMenuItem increaseFontSizeMenuItem = new JMenuItem("Increase Font Size");
        JMenuItem decreaseFontSizeMenuItem = new JMenuItem("Decrease Font Size");
        increaseFontSizeMenuItem.addActionListener(this);
        decreaseFontSizeMenuItem.addActionListener(this);
        editMenu.add(increaseFontSizeMenuItem);
        editMenu.add(decreaseFontSizeMenuItem);
        menuBar.add(editMenu);

        setJMenuBar(menuBar);
        fileChooser = new JFileChooser();

        add(tabbedPane);

        setVisible(true);

        applyTheme();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("New")) {
            textPane = new JTextPane();
            JScrollPane scrollPane = new JScrollPane(textPane);
        
            String untitledFileName = "Untitled";  // Default name for untitled files
            tabbedPane.addTab(untitledFileName, scrollPane);
        } else if (e.getActionCommand().equals("Save As")) { 
            saveCurrentTabContentAs();
        } else if (e.getActionCommand().equals("Close")) {
            closeCurrentTab();
        } else if (e.getActionCommand().equals("Open")) {
            int returnVal = fileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                textPane = new JTextPane();
                JScrollPane scrollPane = new JScrollPane(textPane);
                tabbedPane.addTab(selectedFile.getName(), scrollPane);

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                    StyledDocument doc = textPane.getStyledDocument();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        doc.insertString(doc.getLength(), line + "\n", null);
                    }
                    reader.close();

                    // Apply syntax highlighting based on file extension
                    String fileName = selectedFile.getName();
                    if (fileName.endsWith(".java")) {
                        JavaSyntaxHighlighter.highlight(textPane, doc);
                    }else if (fileName.endsWith(".c")) {
                        CSyntaxHighlighter.highlight(textPane, doc);
                    }
                } catch (IOException | BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        } else if (e.getActionCommand().equals("Save")) { 
            // Handle save menu item click
            int tabIndex = tabbedPane.getSelectedIndex();
            if (tabIndex != -1) {
                JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(tabIndex);
                textPane = (JTextPane) scrollPane.getViewport().getView();
                StyledDocument doc = textPane.getStyledDocument();

                File selectedFile = fileChooser.getSelectedFile();
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile));
                    writer.write(doc.getText(0, doc.getLength()));
                    writer.close();
                } catch (IOException | BadLocationException ex) {
                    ex.printStackTrace();
                }
            }
        } else if (e.getSource() == lightModeMenuItem) {
            isLightMode = true;
            applyTheme();
        } else if (e.getSource() == darkModeMenuItem) {
            isLightMode = false;
            applyTheme();
        } else if (e.getActionCommand().equals("Increase Font Size")) {
            increaseFontSize();
        } else if (e.getActionCommand().equals("Decrease Font Size")) {
            decreaseFontSize();
        }
    }
    private JTextPane getTextPaneAt(int index) {
        JScrollPane scrollPane = (JScrollPane) tabbedPane.getComponentAt(index);
        return (JTextPane) scrollPane.getViewport().getView();
    }
    private void saveCurrentTabContentAs() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex != -1) {
            JTextPane textPane = getTextPaneAt(selectedIndex);
            String content = textPane.getText();
    
            JFileChooser fileChooser = new JFileChooser();
            int returnVal = fileChooser.showSaveDialog(this);
    
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    FileWriter fileWriter = new FileWriter(selectedFile);
                    fileWriter.write(content);
                    fileWriter.close();
                    tabbedPane.setTitleAt(selectedIndex, selectedFile.getName()); // Update tab title
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    private void increaseFontSize() {
        Font currentFont = textPane.getFont();
        int newSize = currentFont.getSize() + 2; // Increase font size by 2
        Font newFont = currentFont.deriveFont((float) newSize);
        textPane.setFont(newFont);
    }
    
    private void decreaseFontSize() {
        Font currentFont = textPane.getFont();
        int newSize = Math.max(currentFont.getSize() - 2, 10); // Decrease font size by 2, but not below 10
        Font newFont = currentFont.deriveFont((float) newSize);
        textPane.setFont(newFont);
    }
    private void closeCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex != -1) {
            tabbedPane.removeTabAt(selectedIndex);
        }
    }
    private void applyTheme() {
        Color backgroundColor;
        Color textColor;

        if (isLightMode) {
            backgroundColor = Color.WHITE;
            textColor = Color.BLACK;
        } else {
            backgroundColor = Color.DARK_GRAY;
            textColor = Color.WHITE;
        }

        getContentPane().setBackground(backgroundColor);

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            Component component = tabbedPane.getComponentAt(i);
            if (component instanceof JScrollPane) {
                textPane = (JTextPane) ((JScrollPane) component).getViewport().getView();
                textPane.setBackground(backgroundColor);
                textPane.setForeground(textColor);
            }
        }
    }
    private void customizeUI() {
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());

            UIManager.getLookAndFeelDefaults().put("MenuBar:Menu[Enabled].backgroundPainter", new Color(100, 100, 100));
            UIManager.getLookAndFeelDefaults().put("MenuItem.background", new Color(100, 100, 100));
            UIManager.getLookAndFeelDefaults().put("MenuItem.foreground", Color.WHITE);


            SwingUtilities.updateComponentTreeUI(this);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {CodeEditor textEditor = new CodeEditor();
            textEditor.customizeUI();
            textEditor.setVisible(true);});
    }  
}

class SyntaxHighlighter {
    public static void setTextColor(StyledDocument doc, int start, int length, Color color) {
        Style style = doc.addStyle("TextColor", null);
        StyleConstants.setForeground(style, color);
        doc.setCharacterAttributes(start, length, style, false);
    }
}

class JavaSyntaxHighlighter {
    public static void highlight(JTextPane textPane, StyledDocument doc) {
        // Define syntax highlighting styles for Java
        Style defaultStyle = textPane.getStyle(StyleContext.DEFAULT_STYLE);
        Style keywordStyle = textPane.addStyle("KeywordStyle", defaultStyle);
        StyleConstants.setForeground(keywordStyle, Color.BLUE);

        // Apply styles to keywords
        String[] keywords = {
            "abstract", "boolean", "break", "class", "extends", "for", "if", "new", "return",
            "while", "public", "private", "static", "void", "int", "double"
        };
        
        for (String keyword : keywords) {
            String text = textPane.getText();
            int pos = 0;

            while ((pos = text.indexOf(keyword, pos)) >= 0) {
                SyntaxHighlighter.setTextColor(doc, pos, keyword.length(), Color.BLUE);
                pos += keyword.length();
            }
        }
    }
}
class CSyntaxHighlighter {
    public static void highlight(JTextPane textPane, StyledDocument doc) {
        // Define syntax highlighting patterns
        String keywords = "\\b(int|char|float|double|long|short|void|if|else|while|for|switch|case|break|return)\\b";
        String comments = "//.*|/\\*(.|\\R)*?\\*/ ";
        String dataTypes = "\\b([A-Za-z_]\\w*)\\b";
        String macros = "#\\w+";
        String numbers = "\\b\\d+\\.?\\d*\\b";
        String strings = "\".*?\"";
        String functions = "\\b[A-Za-z_]\\w*\\s*\\(";

        // Set font style attributes for each type
        Style keywordStyle = textPane.addStyle("KeywordStyle", null);
        StyleConstants.setForeground(keywordStyle, Color.BLUE);

        Style commentStyle = textPane.addStyle("CommentStyle", null);
        StyleConstants.setForeground(commentStyle, Color.GREEN);

        Style dataTypeStyle = textPane.addStyle("DataTypeStyle", null);
        StyleConstants.setForeground(dataTypeStyle, Color.BLUE);

        Style macroStyle = textPane.addStyle("MacroStyle", null);
        StyleConstants.setForeground(macroStyle, Color.BLUE);

        Style numberStyle = textPane.addStyle("NumberStyle", null);
        StyleConstants.setForeground(numberStyle, Color.MAGENTA);

        Style stringStyle = textPane.addStyle("StringStyle", null);
        StyleConstants.setForeground(stringStyle, Color.MAGENTA);

        Style functionStyle = textPane.addStyle("FunctionStyle", null);
        StyleConstants.setForeground(functionStyle, Color.YELLOW);

        // Combine patterns into a single pattern
        String pattern = String.format("(%s)|(%s)|(%s)|(%s)|(%s)|(%s)|(%s)", 
            keywords, comments, dataTypes, macros, numbers, strings, functions);

        Pattern compiledPattern = Pattern.compile(pattern);

        try {
            String text = doc.getText(0, doc.getLength());
            Matcher matcher = compiledPattern.matcher(text);

            while (matcher.find()) {
                if (matcher.group(1) != null) {
                    doc.setCharacterAttributes(matcher.start(1), matcher.end(1) - matcher.start(1), keywordStyle, false);
                } else if (matcher.group(2) != null) {
                    doc.setCharacterAttributes(matcher.start(2), matcher.end(2) - matcher.start(2), commentStyle, false);
                } else if (matcher.group(3) != null) {
                    doc.setCharacterAttributes(matcher.start(3), matcher.end(3) - matcher.start(3), dataTypeStyle, false);
                } else if (matcher.group(4) != null) {
                    doc.setCharacterAttributes(matcher.start(4), matcher.end(4) - matcher.start(4), macroStyle, false);
                } else if (matcher.group(5) != null) {
                    doc.setCharacterAttributes(matcher.start(5), matcher.end(5) - matcher.start(5), numberStyle, false);
                } else if (matcher.group(6) != null) {
                    doc.setCharacterAttributes(matcher.start(6), matcher.end(6) - matcher.start(6), stringStyle, false);
                } else if (matcher.group(7) != null) {
                    doc.setCharacterAttributes(matcher.start(7), matcher.end(7) - matcher.start(7), functionStyle, false);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}