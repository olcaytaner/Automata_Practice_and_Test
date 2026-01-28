package UserInterface;

import java.awt.Font;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import common.TestRunner;

/**
 * A modal dialog that displays test execution progress.
 * Encapsulates the progress UI logic extracted from AbstractAutomatonPanel.
 */
public class TestProgressDialog extends JDialog {

    private final JLabel statusLabel;
    private final JProgressBar progressBar;
    private final JLabel progressLabel;
    private final JButton cancelButton;
    private SwingWorker<?, ?> worker;

    /**
     * Creates a new test progress dialog.
     *
     * @param owner The parent window
     * @param timeoutSeconds Total timeout in seconds for display
     */
    public TestProgressDialog(Window owner, long timeoutSeconds) {
        super(owner, "Running Tests...", ModalityType.APPLICATION_MODAL);

        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        statusLabel = new JLabel("Initializing...");
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setAlignmentX(CENTER_ALIGNMENT);

        progressLabel = new JLabel("0% - Preparing tests...");
        progressLabel.setAlignmentX(CENTER_ALIGNMENT);
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel timeoutDisplayLabel = new JLabel("Total timeout: " + timeoutSeconds + " seconds for all tests");
        timeoutDisplayLabel.setAlignmentX(CENTER_ALIGNMENT);
        timeoutDisplayLabel.setFont(new Font("Arial", Font.PLAIN, 10));

        cancelButton = new JButton("Cancel");
        cancelButton.setAlignmentX(CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> {
            if (worker != null) {
                worker.cancel(true);
            }
            dispose();
        });

        progressPanel.add(statusLabel);
        progressPanel.add(Box.createVerticalStrut(8));
        progressPanel.add(progressBar);
        progressPanel.add(Box.createVerticalStrut(5));
        progressPanel.add(progressLabel);
        progressPanel.add(Box.createVerticalStrut(8));
        progressPanel.add(timeoutDisplayLabel);
        progressPanel.add(Box.createVerticalStrut(15));
        progressPanel.add(cancelButton);

        setContentPane(progressPanel);
        setSize(350, 180);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    }

    /**
     * Sets the SwingWorker to cancel when dialog is cancelled.
     *
     * @param worker The worker to associate with this dialog
     */
    public void setWorker(SwingWorker<?, ?> worker) {
        this.worker = worker;
    }

    /**
     * Updates the progress display with test progress information.
     *
     * @param progress The test progress update
     */
    public void updateProgress(TestRunner.TestProgress progress) {
        int percentage = progress.getProgressPercentage();
        progressBar.setValue(percentage);

        // Format input for display
        String inputDisplay = progress.getCurrentInput().isEmpty() ? "\u03B5" : progress.getCurrentInput();
        if (inputDisplay.length() > 20) {
            inputDisplay = inputDisplay.substring(0, 17) + "...";
        }

        if (progress.isCompleted()) {
            String result = progress.isPassed() ? "\u2713" : "\u2717";
            statusLabel.setText(String.format("Test %d/%d: %s %s",
                progress.getCurrentTest(), progress.getTotalTests(), inputDisplay, result));
            progressLabel.setText(String.format("%d%% - Test %d of %d completed",
                percentage, progress.getCurrentTest(), progress.getTotalTests()));
        } else {
            statusLabel.setText(String.format("Running test %d/%d: %s",
                progress.getCurrentTest(), progress.getTotalTests(), inputDisplay));
            progressLabel.setText(String.format("%d%% - Running test %d of %d",
                percentage, progress.getCurrentTest(), progress.getTotalTests()));
        }
    }

    /**
     * Closes this dialog (disposes it).
     */
    public void close() {
        dispose();
    }

    /**
     * Gets the cancel button for adding additional listeners if needed.
     *
     * @return The cancel button
     */
    public JButton getCancelButton() {
        return cancelButton;
    }
}
