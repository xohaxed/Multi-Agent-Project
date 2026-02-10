package auctions.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class BidHistoryPanel extends JPanel {

    private static final Color HEADER_BG = new Color(25, 42, 86);
    private static final Color EVEN_ROW  = new Color(248, 249, 250);
    private static final Color ODD_ROW   = Color.WHITE;
    private static final Color SUCCESS   = new Color(46, 204, 113);
    private static final Color TEXT_DARK  = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT = new Color(149, 165, 166);

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final List<BidRecord> bidRecords = new ArrayList<>();
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final JLabel countLabel;

    public BidHistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));

        // Panel header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel title = new JLabel("Bid History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_DARK);

        countLabel = new JLabel("0 bids");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(TEXT_LIGHT);

        header.add(title, BorderLayout.WEST);
        header.add(countLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] columns = {"#", "Bidder", "Amount", "Time"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(214, 234, 248));

        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(HEADER_BG);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        table.getTableHeader().setPreferredSize(new Dimension(0, 35));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);

        // Alternating row colors + styled Amount column
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component comp = super.getTableCellRendererComponent(
                    t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    comp.setBackground(row % 2 == 0 ? EVEN_ROW : ODD_ROW);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (col == 2) {
                    setForeground(SUCCESS);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    setForeground(TEXT_DARK);
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
                return comp;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        add(sp, BorderLayout.CENTER);
    }

    public void addBid(String bidder, double amount) {
        SwingUtilities.invokeLater(() -> {
            String ts = LocalTime.now().format(timeFmt);
            int num = bidRecords.size() + 1;
            tableModel.insertRow(0, new Object[]{num, bidder, String.format("$%.2f", amount), ts});
            bidRecords.add(new BidRecord(bidder, amount, ts));
            countLabel.setText(bidRecords.size() + " bid" + (bidRecords.size() != 1 ? "s" : ""));
        });
    }

    public void clearHistory() {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            bidRecords.clear();
            countLabel.setText("0 bids");
        });
    }

    public BidRecord getHighestBid() {
        return bidRecords.stream()
            .max(Comparator.comparingDouble(BidRecord::getAmount))
            .orElse(null);
    }

    // Inner class to hold bid data
    public static class BidRecord {
        private final String bidder;
        private final double amount;
        private final String time;

        public BidRecord(String bidder, double amount, String time) {
            this.bidder = bidder;
            this.amount = amount;
            this.time = time;
        }

        public String getBidder() { return bidder; }
        public double getAmount() { return amount; }
        public String getTime()   { return time; }
    }
}
