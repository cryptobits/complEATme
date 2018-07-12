/*
 * Created by: Dane Hart
 * For CS493 Capstone Project
 * Regis University
 * 
 *  June 2017
 */

package complEATme.UI;

import javax.swing.JDialog;
import javax.swing.JProgressBar;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;

/**
 * The LoadingDialog class provides a simple dialog box with an infinite
 * progress bar to show that the program is doing something and has not
 * frozen. The dialog box does not actually know if the program is 
 * still functioning, but provides the user with an indication that
 * something should be happening. The dialog box is designed to be
 * dismissed programmatically and is best used with a disabled
 * close button.
 */
public class LoadingDialog extends JDialog {

	private static final long serialVersionUID = -8860521195173212280L;

	/**
	 * This constructor creates a basic dialog box with a progress bar.
	 * 
	 * @param appName string name of the application to display
	 * @param message string of message to display
	 */
	public LoadingDialog(String appName, String message) {
		setBounds(100, 100, 448, 138);
		
		JProgressBar progressBar = new JProgressBar();
		getContentPane().add(progressBar, BorderLayout.SOUTH);
		progressBar.setIndeterminate(true);
		
		JLabel lblCompleteme = new JLabel(appName);
		lblCompleteme.setFont(new Font("Tahoma", Font.PLAIN, 40));
		lblCompleteme.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblCompleteme, BorderLayout.NORTH);
		
		JLabel lblAQueryIs = new JLabel(message);
		lblAQueryIs.setHorizontalAlignment(SwingConstants.CENTER);
		lblAQueryIs.setFont(new Font("Tahoma", Font.PLAIN, 16));
		getContentPane().add(lblAQueryIs, BorderLayout.CENTER);

	}

}
