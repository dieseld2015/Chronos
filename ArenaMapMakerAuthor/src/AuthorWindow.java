import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSplitPane;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JComboBox;
public class AuthorWindow extends JPanel implements ActionListener {

	private JFrame frame;
	private AuthorPanel authorPanel;
	private MapPanel mapPanel;
	private String[] authorActions = {"Click here to start drawing","Draw Outline(Solid Wall)","Split Room(Transparent Wall)"}; 
	
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					AuthorWindow window = new AuthorWindow();
					window.frame.setTitle("ArenaMapMaker");
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public AuthorWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(Color.BLACK);
		
		JSplitPane splitPane = new JSplitPane();
		frame.getContentPane().add(splitPane, BorderLayout.CENTER);
		splitPane.setDividerLocation(300);
		//Author panel holds the buttons for authors use
		authorPanel = new AuthorPanel();
		splitPane.setLeftComponent(authorPanel);
		
		JComboBox<String> comboBox = new JComboBox<String>(authorActions);
		comboBox.addActionListener(this);
		authorPanel.add(comboBox);
		
		
		//button resets the map
		JButton btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mapPanel.clear();
				comboBox.setSelectedItem(authorActions[0]);
			}
		});
		authorPanel.add(btnClear);
		//button allows author to undo last action.
		//ctrl+z is preferred design
		JButton undoButton = new JButton("Undo");
		undoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mapPanel.undo();
			}
		});
		authorPanel.add(undoButton);
		
		JButton placeStart = new JButton("Place Start Point");
		placeStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mapPanel.placePlayerStart();
			}
		});
		authorPanel.add(placeStart);
		
		JButton start = new JButton("Start Playing");
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(mapPanel.playerPos!=null) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							PlayerWindow window = new PlayerWindow(mapPanel);
							window.frame.setVisible(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				mapPanel.startGame();
				frame.setVisible(false);
				}
			}
		});
		authorPanel.add(start);
		
		//mapPanel holds the graphics of the map
		mapPanel = new MapPanel();
		
		splitPane.setRightComponent(mapPanel);
		
		frame.setBounds(200, 200, 800, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JComboBox<String> cb = (JComboBox<String>)e.getSource();
		switch(cb.getSelectedItem().toString()) {
		case "Draw Outline(Solid Wall)": mapPanel.paintRooms(); break;
		case "Split Room(Transparent Wall)": mapPanel.paintWalls(); break;
		}
	}
}
