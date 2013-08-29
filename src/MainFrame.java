import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.*;


public class MainFrame extends JFrame implements WindowListener{
	
	private static final long serialVersionUID = 1L;
	private MainCanvas mainPanel;
	public MainFrame(String title){

		super(title);
		
		setResizable(false);
		
		// sets up the different GUI components
		mainPanel = new MainCanvas();
		getContentPane().add(mainPanel);
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		addWindowListener(this);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	public static void main(String[] pArgs) throws IOException,InterruptedException {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainFrame("Construction Kit v1.4.2");
            }
        });
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		int saveWork = -1;
		if(!mainPanel.map.getElevation().equalsIgnoreCase("")){
			saveWork = JOptionPane.showConfirmDialog(null, "Save work before closing?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);
			if(saveWork == JOptionPane.YES_OPTION){
				mainPanel.saveMap();
			}
		}
		if(saveWork != JOptionPane.CANCEL_OPTION){
			dispose();
			System.exit(0);
		}
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {

	}

	public void windowDeiconified(WindowEvent e) {

	}

	public void windowActivated(WindowEvent e) {

	}

	public void windowDeactivated(WindowEvent e) {

	}
}
