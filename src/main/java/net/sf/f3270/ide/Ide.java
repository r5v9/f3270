package net.sf.f3270.ide;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import net.sf.f3270.Parameter;

public class Ide {

    private JFrame jFrame = null;
    private JPanel jContentPane = null;
    private JLabel labelCommands = null;
    private JPanel commandsPanel = null;
    private JList listCommands = null;
    private JPanel commandPanel = null;
    private JPanel commandPanel1 = null;
    private JComboBox comboBoxCommand = null;
    private JComboBox comboBoxN = null;
    private JButton buttonAdd = null;
    private JPanel commandPanel2 = null;
    private JLabel labelLabel = null;
    private JTextField textFieldLabel = null;
    private JLabel labelMode = null;
    private JComboBox comboBoxMode = null;
    private JLabel labelValue = null;
    private JTextField textFieldValue = null;
    private JLabel labelAssert = null;
    private JComboBox comboBoxAssert = null;
    private JLabel labelSkip = null;
    private JComboBox comboBoxSkip = null;
    private JLabel labelMatch = null;
    private JComboBox comboBoxMatch = null;
    private JScrollPane scrollPaneListCommands = null;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
                Ide application = new Ide();
                application.getJFrame().setVisible(true);
            }
        });
    }

    private JFrame getJFrame() {
        if (jFrame == null) {
            jFrame = new JFrame();
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            // jFrame.setSize(500, 500);
            jFrame.setContentPane(getJContentPane());
            jFrame.setTitle("Application");

            Toolkit tk = Toolkit.getDefaultToolkit();
            Dimension screenSize = tk.getScreenSize();
            int screenHeight = screenSize.height;
            int screenWidth = screenSize.width;
            jFrame.setSize(screenWidth / 3, screenHeight * 3 / 5);
            jFrame.setLocation((screenWidth - jFrame.getWidth()) / 2, (screenHeight - jFrame.getHeight()) / 2);

            comboBoxCommand.setSelectedIndex(0);
        }
        return jFrame;
    }

    private JPanel getJContentPane() {
        if (jContentPane == null) {
            labelCommands = new JLabel();
            labelCommands.setText("Commands");
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getCommandsPanel(), BorderLayout.CENTER);
            jContentPane.add(getCommandPanel(), BorderLayout.SOUTH);
        }
        return jContentPane;
    }

    private JPanel getCommandsPanel() {
        if (commandsPanel == null) {
            GridBagConstraints constraintsCommandsList = new GridBagConstraints();
            constraintsCommandsList.fill = GridBagConstraints.BOTH;
            constraintsCommandsList.gridx = 0;
            constraintsCommandsList.gridy = 1;
            constraintsCommandsList.weightx = 1.0;
            constraintsCommandsList.weighty = 1.0;

            GridBagConstraints constraintsLabelCommands = new GridBagConstraints();
            constraintsLabelCommands.gridx = 0;
            constraintsLabelCommands.gridy = 0;
            constraintsLabelCommands.insets = new Insets(0, 0, 5, 0);
            constraintsLabelCommands.anchor = GridBagConstraints.WEST;

            commandsPanel = new JPanel();
            commandsPanel.setLayout(new GridBagLayout());
            commandsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            commandsPanel.add(labelCommands, constraintsLabelCommands);
            commandsPanel.add(getScrollPaneListCommands(), constraintsCommandsList);
        }
        return commandsPanel;
    }

    private JScrollPane getScrollPaneListCommands() {
        if (scrollPaneListCommands == null) {
            scrollPaneListCommands = new JScrollPane(getListCommands());
            scrollPaneListCommands.setBorder(new LineBorder(Color.gray));
        }
        return scrollPaneListCommands;

    }

    private JList getListCommands() {
        if (listCommands == null) {
            listCommands = new JList();
            listCommands.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            listCommands.setModel(getListCommandsListModel());
            listCommands.setCellRenderer(getCommandsCellRenderer());
        }
        return listCommands;
    }

    private ListCellRenderer getCommandsCellRenderer() {
        return new ListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Command command = (Command) list.getModel().getElementAt(index);
                return command.toPanel(list, isSelected);
            }
        };
    }

    @SuppressWarnings("serial")
    private ListModel getListCommandsListModel() {
        return new AbstractListModel() {
            // private static List<Command> commands = new ArrayList<Command>();

            public int getSize() {
                return 20;
            }

            public Object getElementAt(int i) {
                return new Command("write", new Parameter("label", "whatever"), new Parameter("value", "something"));
            }
        };
    }

    private JPanel getCommandPanel() {
        if (commandPanel == null) {
            commandPanel = new JPanel();
            commandPanel.setLayout(new BoxLayout(getCommandPanel(), BoxLayout.Y_AXIS));
            commandPanel.setBorder(BorderFactory.createTitledBorder("Command"));
            commandPanel.add(getCommandPanel1(), null);
            commandPanel.add(getCommandPanel2(), null);
        }
        return commandPanel;
    }

    private JPanel getCommandPanel1() {
        if (commandPanel1 == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(FlowLayout.LEFT);
            commandPanel1 = new JPanel();
            commandPanel1.setLayout(flowLayout);
            commandPanel1.add(getComboBoxCommand());
            commandPanel1.add(getComboBoxN());
            commandPanel1.add(getButtonAdd());
        }
        return commandPanel1;
    }

    private JComboBox getComboBoxCommand() {
        if (comboBoxCommand == null) {
            comboBoxCommand = new JComboBox();
            comboBoxCommand.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"Write", "Verify", "Enter",
                    "PF", "PA", "SysReq", "Clear"}));
            comboBoxCommand.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    comboBoxCommandActionPerformed(e);
                }
            });
        }
        return comboBoxCommand;
    }

    private void comboBoxCommandActionPerformed(java.awt.event.ActionEvent evt) {
        String command = (String) comboBoxCommand.getSelectedItem();
        if (command.equals("Write")) {
            comboBoxN.setEnabled(false);
            labelLabel.setEnabled(true);
            textFieldLabel.setEnabled(true);
            labelMode.setEnabled(true);
            comboBoxMode.setEnabled(true);
            labelValue.setEnabled(true);
            textFieldValue.setEnabled(true);
            labelAssert.setEnabled(false);
            comboBoxAssert.setEnabled(false);
            labelSkip.setEnabled(true);
            comboBoxSkip.setEnabled(true);
            labelMatch.setEnabled(true);
            comboBoxMatch.setEnabled(true);
        } else if (command.equals("Verify")) {
            comboBoxN.setEnabled(false);
            labelLabel.setEnabled(true);
            textFieldLabel.setEnabled(true);
            labelMode.setEnabled(true);
            comboBoxMode.setEnabled(true);
            labelValue.setEnabled(true);
            textFieldValue.setEnabled(true);
            labelAssert.setEnabled(true);
            comboBoxAssert.setEnabled(true);
            labelSkip.setEnabled(true);
            comboBoxSkip.setEnabled(true);
            labelMatch.setEnabled(true);
            comboBoxMatch.setEnabled(true);
        } else if (command.equals("Enter") || command.equals("SysReq") || command.equals("Clear")) {
            comboBoxN.setEnabled(false);
            labelLabel.setEnabled(false);
            textFieldLabel.setEnabled(false);
            labelMode.setEnabled(false);
            comboBoxMode.setEnabled(false);
            labelValue.setEnabled(false);
            textFieldValue.setEnabled(false);
            labelAssert.setEnabled(false);
            comboBoxAssert.setEnabled(false);
            labelSkip.setEnabled(false);
            comboBoxSkip.setEnabled(false);
            labelMatch.setEnabled(false);
            comboBoxMatch.setEnabled(false);
        } else if (command.equals("PF") || command.equals("PA")) {
            comboBoxN.setEnabled(true);
            labelLabel.setEnabled(false);
            textFieldLabel.setEnabled(false);
            labelMode.setEnabled(false);
            comboBoxMode.setEnabled(false);
            labelValue.setEnabled(false);
            textFieldValue.setEnabled(false);
            labelAssert.setEnabled(false);
            comboBoxAssert.setEnabled(false);
            labelSkip.setEnabled(false);
            comboBoxSkip.setEnabled(false);
            labelMatch.setEnabled(false);
            comboBoxMatch.setEnabled(false);
        }
    }

    private JComboBox getComboBoxN() {
        if (comboBoxN == null) {
            comboBoxN = new JComboBox();
            comboBoxN
                    .setModel(new javax.swing.DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7",
                            "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22",
                            "23", "24"}));
        }
        return comboBoxN;
    }

    private JButton getButtonAdd() {
        if (buttonAdd == null) {
            buttonAdd = new JButton("Add");
        }
        return buttonAdd;
    }

    private JPanel getCommandPanel2() {
        if (commandPanel2 == null) {

            GridBagConstraints constraintsLabelLabel = new GridBagConstraints();
            constraintsLabelLabel.gridx = 0;
            constraintsLabelLabel.anchor = GridBagConstraints.EAST;
            constraintsLabelLabel.insets = new Insets(0, 0, 0, 5);
            constraintsLabelLabel.gridy = 0;

            GridBagConstraints constraintsTextFieldLabel = new GridBagConstraints();
            constraintsTextFieldLabel.fill = GridBagConstraints.HORIZONTAL;
            constraintsTextFieldLabel.gridx = 1;
            constraintsTextFieldLabel.gridy = 0;
            constraintsTextFieldLabel.weightx = 1.0;

            GridBagConstraints constraintsLabelMode = new GridBagConstraints();
            constraintsLabelMode.gridx = 2;
            constraintsLabelMode.gridy = 0;
            constraintsLabelMode.anchor = GridBagConstraints.EAST;
            constraintsLabelMode.insets = new Insets(0, 0, 0, 5);

            GridBagConstraints constraintsComboBoxMode = new GridBagConstraints();
            constraintsComboBoxMode.fill = GridBagConstraints.HORIZONTAL;
            constraintsComboBoxMode.gridx = 3;
            constraintsComboBoxMode.gridy = 0;
            constraintsComboBoxMode.anchor = GridBagConstraints.EAST;

            GridBagConstraints constraintsLabelValue = new GridBagConstraints();
            constraintsLabelValue.gridx = 0;
            constraintsLabelValue.gridy = 1;
            constraintsLabelValue.anchor = GridBagConstraints.EAST;
            constraintsLabelValue.insets = new Insets(0, 0, 0, 5);

            GridBagConstraints constraintsTextFieldValue = new GridBagConstraints();
            constraintsTextFieldValue.fill = GridBagConstraints.HORIZONTAL;
            constraintsTextFieldValue.gridx = 1;
            constraintsTextFieldValue.gridy = 1;

            GridBagConstraints constraintsLabelAssert = new GridBagConstraints();
            constraintsLabelAssert.gridx = 2;
            constraintsLabelAssert.gridy = 1;
            constraintsLabelAssert.insets = new Insets(0, 5, 0, 5);

            GridBagConstraints constraintsComboBoxAssert = new GridBagConstraints();
            constraintsComboBoxAssert.fill = GridBagConstraints.NONE;
            constraintsComboBoxAssert.gridx = 3;
            constraintsComboBoxAssert.gridy = 1;

            GridBagConstraints constraintsLabelSkip = new GridBagConstraints();
            constraintsLabelSkip.gridx = 0;
            constraintsLabelSkip.gridy = 2;
            constraintsLabelSkip.anchor = GridBagConstraints.EAST;
            constraintsLabelSkip.insets = new Insets(0, 0, 0, 5);

            GridBagConstraints constraintsComboBoxSkip = new GridBagConstraints();
            constraintsComboBoxSkip.fill = GridBagConstraints.HORIZONTAL;
            constraintsComboBoxSkip.gridx = 1;
            constraintsComboBoxSkip.gridy = 2;
            constraintsComboBoxSkip.weightx = 1.0;
            constraintsComboBoxSkip.gridwidth = 3;

            GridBagConstraints constraintsLabelMatch = new GridBagConstraints();
            constraintsLabelMatch.gridx = 0;
            constraintsLabelMatch.gridy = 3;
            constraintsLabelMatch.anchor = GridBagConstraints.EAST;
            constraintsLabelMatch.insets = new Insets(0, 0, 0, 5);

            GridBagConstraints constraintsComboBoxMatch = new GridBagConstraints();
            constraintsComboBoxMatch.fill = GridBagConstraints.HORIZONTAL;
            constraintsComboBoxMatch.gridx = 1;
            constraintsComboBoxMatch.gridy = 3;
            constraintsComboBoxMatch.weightx = 1.0;
            constraintsComboBoxMatch.gridwidth = 3;

            labelLabel = new JLabel();
            labelLabel.setText("After Label");

            labelMode = new JLabel();
            labelMode.setText("Mode");

            labelValue = new JLabel();
            labelValue.setText("Value");

            labelAssert = new JLabel();
            labelAssert.setText("Assert");

            labelSkip = new JLabel();
            labelSkip.setText("Skip");

            labelMatch = new JLabel();
            labelMatch.setText("Match");

            commandPanel2 = new JPanel();
            commandPanel2.setLayout(new GridBagLayout());
            commandPanel2.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
            commandPanel2.add(labelLabel, constraintsLabelLabel);
            commandPanel2.add(getTextFieldLabel(), constraintsTextFieldLabel);
            commandPanel2.add(labelMode, constraintsLabelMode);
            commandPanel2.add(getComboBoxMode(), constraintsComboBoxMode);
            commandPanel2.add(labelValue, constraintsLabelValue);
            commandPanel2.add(getTextFieldValue(), constraintsTextFieldValue);
            commandPanel2.add(labelAssert, constraintsLabelAssert);
            commandPanel2.add(getComboBoxAssert(), constraintsComboBoxAssert);
            commandPanel2.add(labelSkip, constraintsLabelSkip);
            commandPanel2.add(getComboBoxSkip(), constraintsComboBoxSkip);
            commandPanel2.add(labelMatch, constraintsLabelMatch);
            commandPanel2.add(getComboBoxMatch(), constraintsComboBoxMatch);
        }
        return commandPanel2;
    }

    private JTextField getTextFieldLabel() {
        if (textFieldLabel == null) {
            textFieldLabel = new JTextField();
        }
        return textFieldLabel;
    }

    private JComboBox getComboBoxMode() {
        if (comboBoxMode == null) {
            comboBoxMode = new JComboBox();
            comboBoxMode.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"Exact", "Exact (trim)", "Regex",
                    "Contains"}));
        }
        return comboBoxMode;
    }

    private JTextField getTextFieldValue() {
        if (textFieldValue == null) {
            textFieldValue = new JTextField();
        }
        return textFieldValue;
    }

    private JComboBox getComboBoxAssert() {
        if (comboBoxAssert == null) {
            comboBoxAssert = new JComboBox();
            comboBoxAssert.setModel(new javax.swing.DefaultComboBoxModel(new String[] {"Equals", "Contains",
                    "Not contains"}));
        }
        return comboBoxAssert;
    }

    private JComboBox getComboBoxSkip() {
        if (comboBoxSkip == null) {
            comboBoxSkip = new JComboBox();
        }
        return comboBoxSkip;
    }

    private JComboBox getComboBoxMatch() {
        if (comboBoxMatch == null) {
            comboBoxMatch = new JComboBox();
        }
        return comboBoxMatch;
    }

}
