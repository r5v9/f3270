package net.sf.f3270.ide;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import net.sf.f3270.Parameter;

class Command {

    protected String command;
    protected Parameter[] parameters;

    Command(String command, Parameter... parameters) {
        this.command = command;
        this.parameters = parameters;
    }

    final JPanel toPanel(JList list, boolean isSelected) {
        FlowLayout layout = new FlowLayout(FlowLayout.LEFT);
        layout.setHgap(0);
        layout.setVgap(3);
        JPanel panel = new JPanel(layout);

        panel.setBackground(list.getBackground());

        if (isSelected) {
            panel.setBorder(new LineBorder(Color.gray));
        } else {
            panel.setBorder(new LineBorder(list.getBackground()));
        }
        panel.setOpaque(true);

        addLabel(panel, " ", Color.white);
        addLabel(panel, command, Color.black);
        addLabel(panel, "(", Color.gray);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            addLabel(panel, parameter.getName(), new Color(128, 0, 0));
            addLabel(panel, "=", Color.gray);
            addLabel(panel, parameter.getValue(), Color.blue);
            if (i != parameters.length - 1) {
                addLabel(panel, ", ", Color.gray);
            }
        }
        addLabel(panel, ")", Color.gray);
        return panel;
    }

    private void addLabel(JPanel panel, String text, Color color) {
        JLabel label = new JLabel();
        label.setText(text);
        label.setForeground(color);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setVerticalAlignment(SwingConstants.CENTER);
        panel.add(label);
    }

}
