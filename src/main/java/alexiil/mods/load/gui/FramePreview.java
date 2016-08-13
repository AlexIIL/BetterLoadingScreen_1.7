package alexiil.mods.load.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

@SuppressWarnings("serial")
public class FramePreview extends JFrame {
    private JPanel contentPane;

    private JTextField textField;

    /** Create the frame. */
    public FramePreview(final GuiPreview gui) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        setTitle("Preview editor");
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel pnlButtons = new JPanel();
        contentPane.add(pnlButtons, BorderLayout.SOUTH);
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JButton btnEditor = new JButton("Open Editor");
        pnlButtons.add(btnEditor);
        btnEditor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FrameEditor editor = new FrameEditor(gui);
                editor.setVisible(true);
            }
        });

        JButton btnClose = new JButton("Exit");
        pnlButtons.add(btnClose);
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gui.close();
            }
        });

        JPanel pnlVariables = new JPanel();
        pnlVariables.setLayout(new BoxLayout(pnlVariables, BoxLayout.Y_AXIS));
        contentPane.add(pnlVariables, BorderLayout.CENTER);

        JPanel pnlTextField = new JPanel();
        pnlVariables.add(pnlTextField);

        textField = new JTextField();
        textField.setColumns(30);
        textField.setToolTipText("message to display");
        textField.setText("Random Text");
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                gui.debugText = textField.getText();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                gui.debugText = textField.getText();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                gui.debugText = textField.getText();
            }
        });
        pnlTextField.add(textField);

        final JScrollBar scrollBar = new JScrollBar();
        scrollBar.setValue(20);
        scrollBar.setOrientation(JScrollBar.HORIZONTAL);
        scrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                gui.debugPercent = scrollBar.getValue() / 100f;
            }
        });
        pnlVariables.add(scrollBar);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                gui.close();
            }
        });
    }
}
