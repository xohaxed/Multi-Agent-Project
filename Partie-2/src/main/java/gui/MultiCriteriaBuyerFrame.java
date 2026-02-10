package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class MultiCriteriaBuyerFrame extends JFrame {

    private static final Color PRIMARY    = new Color(25, 42, 86);
    private static final Color ACCENT     = new Color(52, 152, 219);
    private static final Color SUCCESS    = new Color(46, 204, 113);
    private static final Color WARNING    = new Color(241, 196, 15);
    private static final Color PURPLE     = new Color(155, 89, 182);
    private static final Color BG         = new Color(236, 240, 241);
    private static final Color CARD_BG    = Color.WHITE;
    private static final Color TEXT_DARK  = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT = new Color(149, 165, 166);

    private JTextArea logArea;
    private JButton requestButton;
    private JButton migrateContainerButton;
    private JButton migratePlatformButton;
    private JLabel statusLabel;
    private DefaultTableModel offerTableModel;
    private JTable offerTable;
    private JLabel bestSupplierLabel;
    private JLabel bestScoreLabel;
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    public MultiCriteriaBuyerFrame() {
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Multi-Criteria Supplier Selection System");
        setSize(960, 720);
        setMinimumSize(new Dimension(800, 550));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        // === HEADER ===
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel titleLbl = new JLabel("MULTI-CRITERIA SUPPLIER SELECTION");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        requestButton          = createStyledButton("Request Offers", SUCCESS, Color.WHITE);
        migrateContainerButton = createStyledButton("Migrate Container", ACCENT, Color.WHITE);
        migratePlatformButton  = createStyledButton("Migrate Platform", PURPLE, Color.WHITE);
        btnPanel.add(requestButton);
        btnPanel.add(migrateContainerButton);
        btnPanel.add(migratePlatformButton);

        header.add(titleLbl, BorderLayout.WEST);
        header.add(btnPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // === CENTER ===
        JPanel centerWrapper = new JPanel(new BorderLayout(15, 15));
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        // --- Left: Offers Table Card ---
        JPanel offersCard = createCard();
        offersCard.setLayout(new BorderLayout());

        JPanel offersHeader = new JPanel(new BorderLayout());
        offersHeader.setBackground(CARD_BG);
        offersHeader.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        JLabel offersTitle = new JLabel("Supplier Offers Comparison");
        offersTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        offersTitle.setForeground(TEXT_DARK);
        offersHeader.add(offersTitle, BorderLayout.WEST);
        offersCard.add(offersHeader, BorderLayout.NORTH);

        String[] cols = {"Supplier", "Price ($)", "Quality", "Delivery (days)", "Score"};
        offerTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        offerTable = new JTable(offerTableModel);
        offerTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        offerTable.setRowHeight(38);
        offerTable.setShowGrid(false);
        offerTable.setIntercellSpacing(new Dimension(0, 0));
        offerTable.setSelectionBackground(new Color(214, 234, 248));
        offerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        offerTable.getTableHeader().setBackground(PRIMARY);
        offerTable.getTableHeader().setForeground(Color.WHITE);
        offerTable.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Custom renderer for alternating rows + styled score column
        offerTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component comp = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    comp.setBackground(row % 2 == 0 ? new Color(248, 249, 250) : Color.WHITE);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (col == 4) {
                    setForeground(SUCCESS);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else if (col == 0) {
                    setForeground(PRIMARY);
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    setForeground(TEXT_DARK);
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }
                setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                return comp;
            }
        });

        JScrollPane tableSp = new JScrollPane(offerTable);
        tableSp.setBorder(BorderFactory.createEmptyBorder());
        offersCard.add(tableSp, BorderLayout.CENTER);

        // --- Right Panel: Best Supplier + Criteria Weights ---
        JPanel rightPanel = new JPanel(new BorderLayout(0, 15));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(280, 0));

        // Best supplier card
        JPanel bestCard = createCard();
        bestCard.setLayout(new BoxLayout(bestCard, BoxLayout.Y_AXIS));
        bestCard.setBorder(BorderFactory.createCompoundBorder(
            bestCard.getBorder(),
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));

        JLabel bestTitle = new JLabel("BEST SUPPLIER");
        bestTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bestTitle.setForeground(TEXT_LIGHT);
        bestTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        bestSupplierLabel = new JLabel("---");
        bestSupplierLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        bestSupplierLabel.setForeground(PRIMARY);
        bestSupplierLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bestScoreLabel = new JLabel("");
        bestScoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bestScoreLabel.setForeground(SUCCESS);
        bestScoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bestCard.add(bestTitle);
        bestCard.add(Box.createVerticalStrut(6));
        bestCard.add(bestSupplierLabel);
        bestCard.add(Box.createVerticalStrut(4));
        bestCard.add(bestScoreLabel);

        // Criteria weights card
        JPanel weightsCard = createCard();
        weightsCard.setLayout(new BorderLayout());
        weightsCard.setBorder(BorderFactory.createCompoundBorder(
            weightsCard.getBorder(),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        JLabel weightsTitle = new JLabel("Evaluation Weights");
        weightsTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        weightsTitle.setForeground(TEXT_DARK);
        weightsCard.add(weightsTitle, BorderLayout.NORTH);

        JPanel barsPanel = new JPanel();
        barsPanel.setBackground(CARD_BG);
        barsPanel.setLayout(new BoxLayout(barsPanel, BoxLayout.Y_AXIS));
        barsPanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        addWeightBar(barsPanel, "Quality", 40, ACCENT);
        addWeightBar(barsPanel, "Price", 40, SUCCESS);
        addWeightBar(barsPanel, "Delivery", 20, WARNING);

        weightsCard.add(barsPanel, BorderLayout.CENTER);

        // Info card
        JPanel infoCard = createCard();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBorder(BorderFactory.createCompoundBorder(
            infoCard.getBorder(),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));

        JLabel infoTitle = new JLabel("AGENTS");
        infoTitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoTitle.setForeground(TEXT_LIGHT);
        infoTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel infoDetail = new JLabel("3 Sellers + 1 Buyer");
        infoDetail.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoDetail.setForeground(TEXT_DARK);
        infoDetail.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoCard.add(infoTitle);
        infoCard.add(Box.createVerticalStrut(4));
        infoCard.add(infoDetail);

        rightPanel.add(bestCard, BorderLayout.NORTH);
        rightPanel.add(weightsCard, BorderLayout.CENTER);
        rightPanel.add(infoCard, BorderLayout.SOUTH);

        centerWrapper.add(offersCard, BorderLayout.CENTER);
        centerWrapper.add(rightPanel, BorderLayout.EAST);
        add(centerWrapper, BorderLayout.CENTER);

        // === BOTTOM: Log + Status ===
        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setOpaque(false);
        bottomWrapper.setBorder(BorderFactory.createEmptyBorder(0, 20, 12, 20));
        bottomWrapper.setPreferredSize(new Dimension(0, 180));

        JPanel logCard = createCard();
        logCard.setLayout(new BorderLayout());

        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setBackground(CARD_BG);
        logHeader.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel logTitle = new JLabel("Activity Log");
        logTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logTitle.setForeground(TEXT_DARK);
        logHeader.add(logTitle, BorderLayout.WEST);
        logCard.add(logHeader, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(248, 249, 250));
        logArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JScrollPane logSp = new JScrollPane(logArea);
        logSp.setBorder(BorderFactory.createEmptyBorder());
        logCard.add(logSp, BorderLayout.CENTER);

        bottomWrapper.add(logCard, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBackground(new Color(220, 225, 230));
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_LIGHT);
        statusBar.add(statusLabel);
        bottomWrapper.add(statusBar, BorderLayout.SOUTH);

        add(bottomWrapper, BorderLayout.SOUTH);
    }

    private void addWeightBar(JPanel parent, String name, int weight, Color color) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel lbl = new JLabel(name + " (" + weight + "%)");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_DARK);
        lbl.setPreferredSize(new Dimension(120, 20));

        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(weight);
        bar.setStringPainted(false);
        bar.setBackground(new Color(230, 235, 240));
        bar.setForeground(color);
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(120, 10));

        row.add(lbl, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        parent.add(row);
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setOpaque(true);
        return btn;
    }

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));
        return card;
    }

    // === Public API ===

    public void appendLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + LocalTime.now().format(timeFmt) + "] " + text + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void setStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }

    public void addOffer(String supplier, double price, double quality, double delivery, double score) {
        SwingUtilities.invokeLater(() -> {
            offerTableModel.addRow(new Object[]{
                supplier,
                String.format("$%.2f", price),
                String.format("%.1f / 100", quality),
                String.format("%.1f", delivery),
                String.format("%.3f", score)
            });
        });
    }

    public void clearOffers() {
        SwingUtilities.invokeLater(() -> {
            offerTableModel.setRowCount(0);
            bestSupplierLabel.setText("---");
            bestSupplierLabel.setForeground(PRIMARY);
            bestScoreLabel.setText("");
        });
    }

    public void setBestSupplier(String name, double score) {
        SwingUtilities.invokeLater(() -> {
            bestSupplierLabel.setText(name);
            bestSupplierLabel.setForeground(SUCCESS);
            bestScoreLabel.setText("Score: " + String.format("%.3f", score));
            // Highlight winner row
            for (int i = 0; i < offerTableModel.getRowCount(); i++) {
                if (offerTableModel.getValueAt(i, 0).equals(name)) {
                    offerTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        });
    }

    public void addRequestListener(ActionListener l)          { requestButton.addActionListener(l); }
    public void addMigrateContainerListener(ActionListener l)  { migrateContainerButton.addActionListener(l); }
    public void addMigratePlatformListener(ActionListener l)   { migratePlatformButton.addActionListener(l); }
}
