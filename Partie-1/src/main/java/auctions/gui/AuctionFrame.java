package auctions.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.BiConsumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class AuctionFrame extends JFrame {

    private static final Color PRIMARY      = new Color(25, 42, 86);
    private static final Color PRIMARY_LIGHT = new Color(40, 62, 112);
    private static final Color ACCENT       = new Color(52, 152, 219);
    private static final Color SUCCESS      = new Color(46, 204, 113);
    private static final Color WARNING      = new Color(241, 196, 15);
    private static final Color DANGER       = new Color(231, 76, 60);
    private static final Color BG           = new Color(236, 240, 241);
    private static final Color CARD_BG      = Color.WHITE;
    private static final Color TEXT_DARK    = new Color(44, 62, 80);
    private static final Color TEXT_LIGHT   = new Color(149, 165, 166);

    private final JButton    startAuctionBtn;
    private final JButton    placeBidBtn;
    private final JTextField bidderField;
    private final JTextField bidField;
    private final JLabel     itemLabel;
    private final JLabel     currentPriceLbl;
    private final JLabel     timerLabel;
    private final JLabel     highestBidderLabel;
    private final JProgressBar timerBar;
    private final BidHistoryPanel bidHistoryPanel;
    private final JLabel     statusLabel;

    private Timer auctionTimer;
    private int   timeLeft;
    private Runnable startAuctionListener;
    private BiConsumer<String, Double> bidListener;

    public AuctionFrame() {
        super("English Auction System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
        setMinimumSize(new Dimension(750, 550));
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        startAuctionBtn = createStyledButton("Start New Auction", SUCCESS, Color.WHITE);
        placeBidBtn     = createStyledButton("Place Bid", ACCENT, Color.WHITE);
        placeBidBtn.setEnabled(false);

        bidderField = createStyledTextField(12);
        bidField    = createStyledTextField(8);

        itemLabel = new JLabel("No auction in progress");
        itemLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        itemLabel.setForeground(TEXT_LIGHT);

        currentPriceLbl = new JLabel("$0.00");
        currentPriceLbl.setFont(new Font("Segoe UI", Font.BOLD, 48));
        currentPriceLbl.setForeground(PRIMARY);

        highestBidderLabel = new JLabel("---");
        highestBidderLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        highestBidderLabel.setForeground(TEXT_LIGHT);

        timerLabel = new JLabel("--:--");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timerLabel.setForeground(Color.WHITE);

        timerBar = new JProgressBar(0, 60);
        timerBar.setValue(60);
        timerBar.setStringPainted(false);
        timerBar.setPreferredSize(new Dimension(200, 8));
        timerBar.setBackground(PRIMARY_LIGHT);
        timerBar.setForeground(ACCENT);
        timerBar.setBorderPainted(false);

        bidHistoryPanel = new BidHistoryPanel();

        statusLabel = new JLabel("Ready - Click 'Start New Auction' to begin");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_LIGHT);

        buildUI();
        wireEvents();
    }

    private void buildUI() {
        // === HEADER BAR ===
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerLeft.setOpaque(false);
        JLabel titleLbl = new JLabel("ENGLISH AUCTION");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);
        headerLeft.add(titleLbl);
        headerLeft.add(startAuctionBtn);

        JPanel headerRight = new JPanel();
        headerRight.setOpaque(false);
        headerRight.setLayout(new BoxLayout(headerRight, BoxLayout.Y_AXIS));
        timerLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        timerBar.setAlignmentX(Component.RIGHT_ALIGNMENT);
        headerRight.add(timerLabel);
        headerRight.add(Box.createVerticalStrut(4));
        headerRight.add(timerBar);

        header.add(headerLeft, BorderLayout.WEST);
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // === CENTER: Price Card + Bid Input ===
        JPanel centerWrapper = new JPanel(new BorderLayout(15, 15));
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(15, 20, 10, 20));

        // --- Price Card ---
        JPanel priceCard = createCard();
        priceCard.setLayout(new BoxLayout(priceCard, BoxLayout.Y_AXIS));
        priceCard.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        itemLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        currentPriceLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel priceTitleLbl = new JLabel("CURRENT PRICE");
        priceTitleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        priceTitleLbl.setForeground(TEXT_LIGHT);
        priceTitleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel bidderInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bidderInfo.setOpaque(false);
        JLabel bidderTitle = new JLabel("Highest Bidder:  ");
        bidderTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        bidderTitle.setForeground(TEXT_LIGHT);
        bidderInfo.add(bidderTitle);
        bidderInfo.add(highestBidderLabel);
        bidderInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        priceCard.add(itemLabel);
        priceCard.add(Box.createVerticalStrut(8));
        priceCard.add(priceTitleLbl);
        priceCard.add(currentPriceLbl);
        priceCard.add(Box.createVerticalStrut(5));
        priceCard.add(bidderInfo);

        // --- Bid Input Card ---
        JPanel bidCard = createCard();
        bidCard.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bidCard.setBorder(BorderFactory.createCompoundBorder(
            bidCard.getBorder(),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        JLabel nameLabel = new JLabel("Your Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameLabel.setForeground(TEXT_DARK);
        JLabel bidLabel = new JLabel("Bid ($):");
        bidLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bidLabel.setForeground(TEXT_DARK);

        bidCard.add(nameLabel);
        bidCard.add(bidderField);
        bidCard.add(bidLabel);
        bidCard.add(bidField);
        bidCard.add(placeBidBtn);

        centerWrapper.add(priceCard, BorderLayout.CENTER);
        centerWrapper.add(bidCard, BorderLayout.SOUTH);
        add(centerWrapper, BorderLayout.CENTER);

        // === BOTTOM: Bid History ===
        JPanel bottomWrapper = new JPanel(new BorderLayout());
        bottomWrapper.setOpaque(false);
        bottomWrapper.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20));
        bottomWrapper.setPreferredSize(new Dimension(0, 250));

        JScrollPane historyScroll = new JScrollPane(bidHistoryPanel);
        historyScroll.setBorder(BorderFactory.createEmptyBorder());
        bottomWrapper.add(historyScroll, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(220, 225, 230));
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        statusBar.add(statusLabel, BorderLayout.WEST);
        JLabel agentInfo = new JLabel("5 buyers connected", SwingConstants.RIGHT);
        agentInfo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        agentInfo.setForeground(TEXT_LIGHT);
        statusBar.add(agentInfo, BorderLayout.EAST);
        bottomWrapper.add(statusBar, BorderLayout.SOUTH);

        add(bottomWrapper, BorderLayout.SOUTH);
    }

    private void wireEvents() {
        startAuctionBtn.addActionListener(e -> {
            if (startAuctionListener != null) startAuctionListener.run();
        });

        placeBidBtn.addActionListener(e -> {
            if (bidListener == null) return;
            try {
                String name = bidderField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter your name.",
                        "Missing Name", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                double amt = Double.parseDouble(bidField.getText().trim());
                bidListener.accept(name, amt);
                bidField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid bid amount.",
                    "Invalid Bid", JOptionPane.ERROR_MESSAGE);
            }
        });

        bidField.addActionListener(e -> placeBidBtn.doClick());
    }

    // === Helpers ===

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btn.setOpaque(true);
        return btn;
    }

    private JTextField createStyledTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return tf;
    }

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));
        return card;
    }

    // === Public API ===

    public void setStartAuctionListener(Runnable r) {
        this.startAuctionListener = r;
    }

    public void setBidListener(BiConsumer<String, Double> l) {
        this.bidListener = l;
    }

    public void startNewAuction(String itemName, double startPrice) {
        bidHistoryPanel.clearHistory();
        itemLabel.setText("Auction: " + itemName);
        itemLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        itemLabel.setForeground(TEXT_DARK);
        currentPriceLbl.setText(String.format("$%.2f", startPrice));
        highestBidderLabel.setText("---");
        highestBidderLabel.setForeground(TEXT_LIGHT);
        bidderField.setText("");
        bidField.setText("");
        statusLabel.setText("Auction in progress: " + itemName);

        if (auctionTimer != null && auctionTimer.isRunning()) auctionTimer.stop();
        timeLeft = 60;
        timerBar.setMaximum(60);
        timerBar.setValue(60);
        timerLabel.setText("01:00");
        timerLabel.setForeground(Color.WHITE);
        timerBar.setForeground(ACCENT);
        placeBidBtn.setEnabled(true);
        startAuctionBtn.setEnabled(false);

        auctionTimer = new Timer(1000, ev -> {
            timeLeft--;
            int mins = timeLeft / 60;
            int secs = timeLeft % 60;
            timerLabel.setText(String.format("%02d:%02d", mins, secs));
            timerBar.setValue(timeLeft);

            if (timeLeft <= 10) {
                timerLabel.setForeground(DANGER);
                timerBar.setForeground(DANGER);
            } else if (timeLeft <= 20) {
                timerLabel.setForeground(WARNING);
                timerBar.setForeground(WARNING);
            }

            if (timeLeft <= 0) {
                auctionTimer.stop();
                placeBidBtn.setEnabled(false);
                startAuctionBtn.setEnabled(true);
                timerLabel.setForeground(Color.WHITE);
                timerBar.setForeground(ACCENT);

                BidHistoryPanel.BidRecord w = bidHistoryPanel.getHighestBid();
                String result;
                if (w != null) {
                    result = String.format("Winner: %s with $%.2f!", w.getBidder(), w.getAmount());
                } else {
                    result = "No bids were placed.";
                }
                statusLabel.setText("Auction ended. " + result);
                JOptionPane.showMessageDialog(this, result,
                    "Auction Ended", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        auctionTimer.start();
    }

    public void updateBid(String bidder, double amount) {
        bidHistoryPanel.addBid(bidder, amount);
        currentPriceLbl.setText(String.format("$%.2f", amount));
        highestBidderLabel.setText(bidder);
        highestBidderLabel.setForeground(SUCCESS);
        statusLabel.setText(String.format("New bid: %s placed $%.2f", bidder, amount));

        // Flash effect on price change
        Timer flash = new Timer(100, null);
        final int[] count = {0};
        flash.addActionListener(e -> {
            count[0]++;
            currentPriceLbl.setForeground(count[0] % 2 == 0 ? PRIMARY : SUCCESS);
            if (count[0] >= 6) {
                flash.stop();
                currentPriceLbl.setForeground(PRIMARY);
            }
        });
        flash.start();
    }
}
