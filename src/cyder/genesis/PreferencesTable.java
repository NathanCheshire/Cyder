package cyder.genesis;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.obj.NBT;
import cyder.ui.CyderCheckBox;
import cyder.ui.CyderLabel;
import cyder.ui.CyderScrollPane;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class PreferencesTable {
    public static void main(String[] args) {
        new PreferencesTable();
    }

    PreferencesTable() {
        MyFrame window = new MyFrame();
        window.frame.setVisible(true);
    }

    public class MyFrame {
        JFrame frame;
        private JTable table;

        public MyFrame() {
            frame = new JFrame();
            frame.setSize(200, 200);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            table = new JTable();
            table.setFont(CyderFonts.defaultFontSmall);
            table.setForeground(CyderColors.navy);
            table.setModel(new MyTableModel());
            table.setFillsViewportHeight(true);
            table.getColumnModel().getColumn(0).setCellRenderer(new MyPanelCellRenderer());
            table.setRowHeight(200);
            table.setFocusable(true);

            CyderScrollPane scrollPane = new CyderScrollPane(table);
            scrollPane.setBorder(BorderFactory.createLineBorder(CyderColors.navy, 5));
            frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
            frame.setLocationRelativeTo(null);
        }
    }

    class MyTableModel extends DefaultTableModel {
        public MyTableModel() {
            super(new Object[][] {
                new Object[] { new NBT("Output border", true) },
                new Object[] { new NBT("Input border", false) },
                new Object[] { new NBT("Output fill", true) },
                new Object[] { new NBT("Input fill", true) },
                new Object[] { new NBT("Random background", false) } },
                new String[] {"Value"});
        }

        Class[] columnTypes = new Class[] {CyderCheckBox.class};

        MyTableModel(Object[][] data, Object[] columnNames) {
            super(data, columnNames);
        }

        public Class getColumnClass(int columnIndex) {
            return columnTypes[columnIndex];
        }
    }

    class MyPanelCellRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return new MyPanel((NBT)value);
        }
    }

    public class MyPanel extends JPanel {
        public MyPanel(NBT v) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JPanel labelPanel = new JPanel();
            labelPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            CyderLabel label = new CyderLabel(v.getName());
            labelPanel.add(label);
            add(labelPanel);

            //I think it's actually there but it just doesn't show

            JPanel boxPanel = new JPanel(new FlowLayout());
            CyderCheckBox chckbxSomeValue = new CyderCheckBox(v.getData());
            boxPanel.add(chckbxSomeValue);
            add(boxPanel);
            repaint();
        }
    }
}