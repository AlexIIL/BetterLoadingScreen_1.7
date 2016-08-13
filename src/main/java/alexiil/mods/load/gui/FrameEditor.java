package alexiil.mods.load.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.StringUtils;

import alexiil.mods.load.json.EType;
import alexiil.mods.load.json.ImageRender;

@SuppressWarnings("serial")
public class FrameEditor extends JFrame {
    private JPanel contentPane, settingsPanel;
    private JList<String> renderList;
    private DefaultListModel<String> renderListInternal;

    private final GuiPreview gui;
    private ImageRender[] renders;

    /** Create the frame. */
    public FrameEditor(GuiPreview preview) {
        this.gui = preview;
        renders = gui.getImageData();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 450, 300);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        renderList = new JList<String>();
        renderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        renderList.setLayoutOrientation(JList.VERTICAL);
        renderList.setModel(renderListInternal = new DefaultListModel<String>());

        renderList.addListSelectionListener(new ListSelectionListener() {
            @Override                                      // TODO (JDK8): Convert to lambda
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                settingsPanel.removeAll();
                int index = renderList.getSelectedIndex();
                if (index == -1) {
                    settingsPanel.removeAll();
                }
                else {
                    settingsPanel.add(handle(renders[index]));
                }
                settingsPanel.invalidate();
                settingsPanel.repaint();
            }
        });

        JScrollPane scrollPane = new JScrollPane(renderList);

        contentPane.add(scrollPane, BorderLayout.WEST);

        settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        contentPane.add(settingsPanel, BorderLayout.CENTER);

        resetRenders();
    }

    private void resetRenders() {
        settingsPanel.removeAll();
        renderListInternal.clear();

        for (ImageRender render : renders) {
            if (StringUtils.isEmpty(render.comment)) {
                render.comment = "<unnamed>";
            }
            renderListInternal.addElement(render.comment);
            // settingsPanel.add(handle(render));
        }
    }

    private JPanel handle(final ImageRender render) {
        JPanel panel = new JPanel();
        panel.setBorder(LineBorder.createGrayLineBorder());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField comment = new JTextField(20);
        comment.setText(render.comment);
        panel.add(comment);

        JComboBox<EType> type = new JComboBox<EType>(EType.values());
        type.setSelectedItem(render.type);
        panel.add(type);

        JPanel colour = new JPanel();
        colour.setLayout(new BoxLayout(colour, BoxLayout.X_AXIS));
        {
            JButton colourChanger = new JButton("Change colour");
            colour.add(colourChanger);

            final JPanel colourPreview = new JPanel();
            colourPreview.setSize(100, 100);
            colourPreview.setBackground(new Color(render.getColour()));
            colour.add(colourPreview);

            colourChanger.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Color c = JColorChooser.showDialog(null, "Choose a colour", new Color(render.getColour()));
                    colourPreview.setBackground(c);
                    render.setColour(c);
                }
            });
        }

        if (render.type == EType.STATIC_TEXT) {
            JTextField toDisplay = new JTextField(20);
        }

        panel.add(colour);

        return panel;
    }
}
