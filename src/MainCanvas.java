import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class MainCanvas extends JPanel {

	private static final long serialVersionUID = -7836613009727459920L;
	public JMenuBar menuBar;
	public JMenu main, database, help;
	public JMenuItem menuItem;
	public JLabel nameL,widthL,heightL,categoryL,typeL,radiusL,forceL,levelL,npcTypeL,npcNamesL,waterRadiusL,currentLayerL;
	public static JButton create,toggleHeight,levelB,randomB,newLayer,setEntrance,changeLayerLevel;
	public MapArea map;
	public JTextField enterName,xCoord,yCoord,level;
	public static JTextField logbookPathField;
	public static JComboBox<String> catList,typeList,npcNames,npcTypeList,locTypeList,locNames,waterTypeList,eventTypeList,eventItemTypeList,eventItemList,layerList,herbList;
	public JPanel mapOptions,heightOptions,npcOptions,locationOptions,waterOptions,eventOptions,layerOptions,herbOptions;
	public static JTextArea info,eventDescription;
	public String[][] types;
	public SpinnerModel radius,force,waterRadius,currentLayer;
	public JSpinner radiusSpinner,forceSpinner,waterRadiusSpinner,currentLayerSpinner;
	public SpringLayout layout,heightLayout,npcLayout,locationLayout,waterLayout,eventLayout,layerLayout,herbLayout;
	public String w,n,e,s;
	public JScrollPane scrollPane,eventScrollPane;
	public static JTabbedPane tabbedPane;
	public int WIDTH,HEIGHT;
	public static String name,category,type,logPath;
	public static boolean incrHeight = true,chooseEntrance = false;
	public static SAXBuilder parser = new SAXBuilder();
	public static Hashtable<String,Integer> enemies,npcs,towns,hostileAreas,qItems,equipment,items,potions,clothes,consumables,herbs;
	public static HashMap<int[],String> events;
	public static HashMap<int[],String[]> entrancePos;
	public static ArrayList<int[]> enemyPos,npcPos,townPos,hostAPos,lakePos,seaPos,herbPos;
	public static int terrainLevel = 0;
	

	public MainCanvas(){
		layout = new SpringLayout();
		setLayout(layout);

		map = new MapArea(500,500);
		
		//positional strings used
		w = SpringLayout.WEST;
		e = SpringLayout.EAST;
		n = SpringLayout.NORTH;
		s = SpringLayout.SOUTH;

		loadData();
		enemyPos = new ArrayList<int[]>();
		npcPos = new ArrayList<int[]>();
		townPos = new ArrayList<int[]>();
		hostAPos = new ArrayList<int[]>();
		seaPos = new ArrayList<int[]>();
		lakePos = new ArrayList<int[]>();
		herbPos = new ArrayList<int[]>();
		
		events = new HashMap<int[],String>();
		
		entrancePos = new HashMap<int[],String[]>();
		/**
		 * MENU
		 */

		menuBar = new JMenuBar();

		main = new JMenu("Main");
		main.getAccessibleContext().setAccessibleDescription("Make a new map");
		menuBar.add(main);

		// MAIN MENU
		
		//new map
		menuItem = new JMenuItem("Make a new map",KeyEvent.VK_M);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Construct a new map");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				// ask for save current work
				int save = -1;
				if(!map.getElevation().equalsIgnoreCase("")){
					save = JOptionPane.showConfirmDialog(null, "Save current work?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);
					if(save != JOptionPane.CANCEL_OPTION){
						if(save == JOptionPane.YES_OPTION){
							saveMap();
						}
						map.makeUniform(0);
						reset();
					}
				}
			}
		});
		main.add(menuItem);
		
		//load map
		menuItem = new JMenuItem("Load map",KeyEvent.VK_L);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L,ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Load a map from a file");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				int saveWork = -1;
				if(map.isVisible()){
					saveWork = JOptionPane.showConfirmDialog(null, "Save current map?", "Save", JOptionPane.YES_NO_CANCEL_OPTION);
					if(saveWork == JOptionPane.YES_OPTION){
						saveMap();
					}
				}
				if(saveWork != JOptionPane.CANCEL_OPTION){
					String fileName = JOptionPane.showInputDialog(null, "Give the path to the file (eg. Maps/TestMap.xml):", "Choose File..", JOptionPane.OK_CANCEL_OPTION);
					if(fileName != null && !fileName.equalsIgnoreCase("")){
						loadMap(fileName);
					}
				}
			}
		});
		main.add(menuItem);
		
		//save map
		menuItem = new JMenuItem("Save Map",KeyEvent.VK_S);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Save your current map");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				saveMap();
			}
		});
		main.add(menuItem);
		
		//new npc
		menuItem = new JMenuItem("Make a new NPC",KeyEvent.VK_N);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Construct a new NPC");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				new CreationFrame("NPC", new String[]{"ID","Name","Gender","Conv Tree ID","Sound Greet","Sound Farewell","Mental State","Description","Active Area","Importance","ReqQuestID"});
			}
		});
		main.add(menuItem);
		
		//new equipment
		menuItem = new JMenuItem("Make a new Equipment",KeyEvent.VK_E);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Construct a new Equipment");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				new CreationFrame("Equipment", new String[]{"ID","Name","Strength","Cost","Durability","Type","Weight","Treat","Amount","Logbook Path","Logbook Summary"});
			}
		});
		main.add(menuItem);
		
		//new item
		menuItem = new JMenuItem("Make a new Item",KeyEvent.VK_I);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Construct a new Item");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				new CreationFrame("Item", new String[]{"ID","Name","Description","Weight","Cost","Logbook Path","Logbook Summary"});
			}
		});
		main.add(menuItem);
		
		//new enemy
		menuItem = new JMenuItem("Make a new Enemy",KeyEvent.VK_Y);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Construct a new Enemy");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				new CreationFrame("Enemy", new String[]{"ID","Name","Level","HP","Intelligence","Gold","Strength","Dexterity","Intellect","Gear","invItemIDs","Probabilities","SpellIDs","Defense","Stationary"});
			}
		});
		main.add(menuItem);
		
		//new potion
		menuItem = new JMenuItem("Make a new Potion",KeyEvent.VK_P);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Construct a new Potion");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				new CreationFrame("Potion", new String[]{"ID","Name","Effect","Strength","Duration","Weight","Cost"});
			}
		});
		main.add(menuItem);
		
		//new clothing
		menuItem = new JMenuItem("Make new Clothing",KeyEvent.VK_T);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Construct new Clothing");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				new CreationFrame("Clothing", new String[]{"ID","Name","Description","Weight","Cost","Area","Warmth"});
			}
		});
		main.add(menuItem);
		
		//new consumable
		menuItem = new JMenuItem("Make a new Consumable",KeyEvent.VK_B);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Construct a new Consumable");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				new CreationFrame("Consumable", new String[]{"ID","Name","Description","Weight","Cost","Type","Effect","Freshness"});
			}
		});
		main.add(menuItem);
		
		/*
		 * DATABASE
		 */
		
		database = new JMenu("Database");
		database.getAccessibleContext().setAccessibleDescription("Show info from database");
		menuBar.add(database);
		
		//show npc info
		menuItem = new JMenuItem("Show NPC Database",KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Load NPC Database Info");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				showInfo("NPC","Data/NPCs.xml");
			}
		});
		database.add(menuItem);
		
		//show enemy info
		menuItem = new JMenuItem("Show Enemy Database",KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Load Enemy Database Info");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				showInfo("Enemy","Data/Enemies.xml");
			}
		});
		database.add(menuItem);
		
		//show equipment info
		menuItem = new JMenuItem("Show Equipment Database",KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Load Equipment Database Info");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				showInfo("Equipment","Data/Equipment.xml");
			}
		});
		database.add(menuItem);
		
		//show item info
		menuItem = new JMenuItem("Show Item Database",KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Load Item Database Info");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				showInfo("Item","Data/Items.xml");
			}
		});
		database.add(menuItem);
		
		//show potions info
		menuItem = new JMenuItem("Show Potion Database",KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Load Potion Database Info");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				showInfo("Potion","Data/Potions.xml");
			}
		});
		database.add(menuItem);
		
		//show clothing info
		menuItem = new JMenuItem("Show Clothes Database",KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Load Clothes Database Info");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				showInfo("Clothes","Data/Clothing.xml");
			}
		});
		database.add(menuItem);
		
		//show hostile area info
		menuItem = new JMenuItem("Show Consumables Database",KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Load Consumables Database Info");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				showInfo("Consumable","Data/Consumables.xml");
			}
		});
		database.add(menuItem);
		
		//show hostile area info
		menuItem = new JMenuItem("Show Hostile Area Database",KeyEvent.VK_O);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,ActionEvent.ALT_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Load Hostile Area Database Info");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				showInfo("Hostile Area","Data/HostileAreas.xml");
			}
		});
		database.add(menuItem);
		
		//reload data
		menuItem = new JMenuItem("Reload data",KeyEvent.VK_R);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Reload data");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				loadData();
			}
		});
		main.add(menuItem);

		//quit
		menuItem = new JMenuItem("Quit", KeyEvent.VK_Q);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Exit the program");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				int quit = JOptionPane.showConfirmDialog(null, "Really quit?","Exit",JOptionPane.YES_NO_OPTION);
				if(quit == JOptionPane.YES_OPTION){
					System.exit(0);
				}
			}
		});
		main.add(menuItem);


		// HELP MENU
		help = new JMenu("Help");
		help.getAccessibleContext().setAccessibleDescription(
				"Find information");
		menuBar.add(help);

		menuItem = new JMenuItem("Manual", KeyEvent.VK_H);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Show the manual");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				//TODO
				JOptionPane.showMessageDialog(null, "Not yet implemented :(", "Sorry", JOptionPane.ERROR_MESSAGE);
			}
		});
		help.add(menuItem);
		menuItem = new JMenuItem("Info", KeyEvent.VK_I);
		menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		menuItem.getAccessibleContext().setAccessibleDescription("Show Info");
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				JOptionPane.showMessageDialog(null, "Construction Kit Version 1.4.2\nMade by Leendert Hayen.\n2013.", "Info", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		help.add(menuItem);

		/**
		 * OPTIONS
		 */
		
		tabbedPane = new JTabbedPane();
		
		// NEW MAP OPTIONS
		mapOptions = new JPanel();
		SpringLayout optionsLayout = new SpringLayout();
		mapOptions.setLayout(optionsLayout);
		
		nameL = new JLabel("Name");
		widthL = new JLabel("Horizontal squares");
		heightL = new JLabel("Vertical squares");
		categoryL = new JLabel("Category");
		typeL = new JLabel("Type");
		
		types = new String[2][2];
		types[0][0] = "Loofbos";
		types[0][1] = "Naaldbos";
		types[1][0] = "Verlicht";
		types[1][1] = "Donker";

		enterName = new JTextField(20);
		enterName.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				name = enterName.getText().trim();
			}
		});

		xCoord = new JTextField(5);
		xCoord.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				WIDTH = Integer.parseInt(xCoord.getText().trim());
			}
		});
		yCoord = new JTextField(5);
		yCoord.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt) {
				HEIGHT = Integer.parseInt(yCoord.getText().trim());
			}
		});
		String[] categories = {"Forest","Dungeon","World map"};
		catList = new JComboBox<String>(categories);
		catList.setSelectedIndex(0);
		category = catList.getItemAt(0);
		catList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				typeList.removeAllItems();
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>)evt.getSource();
				category = (String)cb.getSelectedItem();
				int index = cb.getSelectedIndex();
				if(index >=0 && !category.equalsIgnoreCase("World map")){
					for(int j=0;j<types[index].length;j++){
						typeList.addItem(types[index][j]);
					}
					type = (String)typeList.getItemAt(0);
				}
			}
		});
		typeList = new JComboBox<String>(types[0]);
		typeList.setSelectedIndex(0);
		type = typeList.getItemAt(0);
		typeList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>)evt.getSource();
				type = (String)cb.getSelectedItem();
			}
		});

		create = new JButton("Create map");
		create.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				name = enterName.getText().trim();
				try{
					WIDTH = Integer.parseInt(xCoord.getText().trim());
					HEIGHT = Integer.parseInt(yCoord.getText().trim());
				}catch(NumberFormatException e){
					WIDTH = 0;
					HEIGHT = 0;
				}
				if(name == "" || WIDTH <1 || HEIGHT <1){
					JOptionPane.showMessageDialog(null, "Fill in the blanks first!");
				}
				else{
					map.init(WIDTH, HEIGHT);
					manageHeightOptionVisibility();
				}
			}
		});
		
		
		// HEIGHT OPTIONS
		heightOptions = new JPanel();
		heightLayout = new SpringLayout();
		heightOptions.setLayout(heightLayout);

		toggleHeight = new JButton("Height increase: " + incrHeight);
		toggleHeight.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				incrHeight=!incrHeight;
				toggleHeight.setText("Height increase: " + incrHeight);
			}
		});
		
		radiusL = new JLabel("Radius");
		forceL = new JLabel("Force");
		levelL = new JLabel("Level");
		
		// make uniform
		level = new JTextField(2);
		level.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				map.makeUniform(Integer.parseInt(level.getText().trim()));
			}
		});
		level.setText("0");
		
		
		levelB = new JButton("Uniform");
		levelB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				try{
					map.makeUniform(Integer.parseInt(level.getText().trim()));
				} catch(NumberFormatException e){}
			}
		});

		randomB = new JButton("Randomize");
		randomB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				double maxDev = Double.parseDouble(JOptionPane.showInputDialog(null, "Maximum deviation"));
				String[] modes = {"Gaussian","Uniform"};
				String mode = (String)JOptionPane.showInputDialog(null,"Choose process","Input",JOptionPane.INFORMATION_MESSAGE,null,modes,modes[0]);
				int y = 0;
				try{
					y = Integer.parseInt(level.getText().trim());
				}catch(NumberFormatException e){}

				map.randomize(y, maxDev, mode);
			}
		});
		
		// NPC OPTIONS
		
		npcOptions = new JPanel();
		npcLayout = new SpringLayout();
		npcOptions.setLayout(npcLayout);
		
		npcNamesL = new JLabel("Names");
		npcTypeL = new JLabel("Type");
		
		npcNames = new JComboBox<String>();
		
		String[] npcTypes = {"Enemies","NPCs"};
		npcTypeList = new JComboBox<String>(npcTypes);
		npcTypeList.setSelectedIndex(0);
		Enumeration<String> en = enemies.keys();
		while(en.hasMoreElements()){
			npcNames.addItem(en.nextElement());
		}
		npcTypeList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>)evt.getSource();
				int index = cb.getSelectedIndex();
				npcNames.removeAllItems();
				switch(index){
				case 0:{
					Enumeration<String> en = enemies.keys();
					while(en.hasMoreElements()){
						npcNames.addItem(en.nextElement());
					}
					break;
				}
				case 1:{
					Enumeration<String> en = npcs.keys();
					while(en.hasMoreElements()){
						npcNames.addItem(en.nextElement());
					}
					break;
				}
				}
				repaint();
			}
		});
		
		
		// CITY OPTIONS (world map)
		
		locationOptions = new JPanel();
		locationLayout = new SpringLayout();
		locationOptions.setLayout(locationLayout);
		
		String[] locTypes = {"City","Hostile Area"};
		
		locTypeList = new JComboBox<String>(locTypes);
		locNames = new JComboBox<String>();
		en = towns.keys();
		while(en.hasMoreElements()){
			locNames.addItem(en.nextElement());
		}
		locTypeList.setSelectedIndex(0);
		locTypeList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>)evt.getSource();
				int index = cb.getSelectedIndex();
				locNames.removeAllItems();
				Enumeration<String> en;
				switch(index){
				case 0: en = towns.keys();
						while(en.hasMoreElements()){
							locNames.addItem(en.nextElement());
						}
						break;
				case 1: en = hostileAreas.keys();
						while(en.hasMoreElements()){
							locNames.addItem(en.nextElement());
						}
						break;
				}
				repaint();
			}
		});
		
		// WATER OPTIONS
		
		waterOptions = new JPanel();
		waterLayout = new SpringLayout();
		waterOptions.setLayout(waterLayout);
		
		String[] waterTypes = {"Sea","Lake"};
		
		waterRadiusL = new JLabel("Radius");
		
		waterRadius = new SpinnerNumberModel((int)(Math.min(WIDTH, HEIGHT)/40),0,30,1);
		waterRadius.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				map.setWaterRadius(((Integer)waterRadius.getValue()).intValue());
			}
		});
		waterRadiusSpinner = new JSpinner(waterRadius);
		waterRadiusSpinner.setPreferredSize(new Dimension(50,20));
		
		waterTypeList = new JComboBox<String>(waterTypes);
		
		// show info about selected square
		info = new JTextArea(8,15);
		info.setDisabledTextColor(Color.black);
		info.setEditable(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);
		scrollPane = new JScrollPane(info);
		
		// DUNGEON EVENT OPTIONS

		eventOptions = new JPanel();
		eventLayout = new SpringLayout();
		eventDescription = new JTextArea(5,15);
		eventDescription.setLineWrap(true);
		eventDescription.setWrapStyleWord(true);
		eventDescription.setEditable(true);
		eventScrollPane = new JScrollPane(eventDescription);
		eventOptions.setLayout(eventLayout);
		
		String[] eventTypes = {"Text encounter","Surprise enemy","Surprise NPC","Surprise Equipment","Surprise Item","Surprise Potion","Surprise clothing","Surprise Consumable","Lost","Trap"};
		
		eventTypeList = new JComboBox<String>(eventTypes);
		
		String[] eventItemTypes = {"Enemy","NPC","Equipment","Item","Potion","Clothing","Consumable","None"};
		
		eventItemTypeList = new JComboBox<String>(eventItemTypes);
		eventItemTypeList.setSelectedIndex(0);
		eventItemList = new JComboBox<String>();
		en = enemies.keys();
		
		logbookPathField = new JTextField(20);
		logbookPathField.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				logPath = logbookPathField.getText().trim();
			}
		});
		
		while(en.hasMoreElements()){
			eventItemList.addItem(en.nextElement());
		}
		eventItemTypeList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				@SuppressWarnings("unchecked")
				JComboBox<String> cb = (JComboBox<String>)evt.getSource();
				int index = cb.getSelectedIndex();
				eventItemList.removeAllItems();
				Enumeration<String> en = null;
				switch(index){
				case 0: en = enemies.keys();break;
				case 1: en = npcs.keys();break;
				case 2: en = equipment.keys(); break;
				case 3: en = items.keys(); break;
				case 4: en = potions.keys(); break;
				case 5: en = clothes.keys(); break;
				case 6: en = consumables.keys(); break;
				}
				try{
					while(en.hasMoreElements()){
						eventItemList.addItem(en.nextElement());
					}
				}catch(NullPointerException e){
				}
				repaint();
			}
		});
		
		//LAYER OPTIONS
		
		layerOptions = new JPanel();
		layerLayout = new SpringLayout();
		layerOptions.setLayout(layerLayout);
		
		currentLayerL = new JLabel("Layer");
		
		currentLayer = new SpinnerNumberModel(terrainLevel,-5,5,1);
		currentLayer.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				terrainLevel = ((Integer)currentLayer.getValue()).intValue();
				System.out.println("terrainLevel: " + terrainLevel);
				map.repaint();
			}
		});
		currentLayerSpinner = new JSpinner(currentLayer);
		currentLayerSpinner.setPreferredSize(new Dimension(50,20));
		
		newLayer = new JButton("New Layer");
		newLayer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				int[] v = map.getNumLayerValues();
				int min = 0;
				for(int i: v){
					if(i < min){
						min = i;
					}
				}
				terrainLevel = min - 1;
				String name = JOptionPane.showInputDialog(null, "Name for new layer:", "Layer Name", JOptionPane.OK_OPTION);
				map.createLayer(terrainLevel,name);
				currentLayer.setValue(terrainLevel);
			}
		});
		
		layerList = new JComboBox<String>();
		
		layerList.addItem("0: Ground Height Map");
		
		layerList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				String[] s = layerList.getSelectedItem().toString().split(": ");
				terrainLevel = Integer.parseInt(s[0]);
				currentLayer.setValue(terrainLevel);
				map.repaint();
			}
		});
		
		setEntrance = new JButton("Add entrance");
		setEntrance.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				chooseEntrance = true;
				setEntrance.setText("Click on map");
			}
		});
		
		changeLayerLevel = new JButton("Change Level");
		changeLayerLevel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent evt){
				try{
					int newLayer = Integer.parseInt(JOptionPane.showInputDialog("Change layer to what level? (Current: " + terrainLevel + ")"));
					int oldLayer = terrainLevel;
					map.addLayer(newLayer, map.getCurrentLayerName().split(": ")[1], map.getLayer(terrainLevel));
					for(int j=0;j<layerList.getItemCount();j++){
						if(layerList.getItemAt(j).equalsIgnoreCase(map.getLayerKey(oldLayer))){
							layerList.removeItemAt(j);
							break;
						}
					}
					map.removeLayer(map.getLayerKey(oldLayer));
				} catch(NumberFormatException e){
				}
			}
		});
		
		// HERB OPTIONS
		
		herbOptions = new JPanel();
		herbLayout = new SpringLayout();
		herbOptions.setLayout(herbLayout);
		
		herbList = new JComboBox<String>();
		Enumeration<String> h = herbs.keys();
		while(h.hasMoreElements()){
			herbList.addItem(h.nextElement());
		}
		herbList.setSelectedIndex(0);
		

		// initial options panel
		mapOptions.add(nameL);
		mapOptions.add(enterName);
		mapOptions.add(widthL);
		mapOptions.add(xCoord);
		mapOptions.add(heightL);
		mapOptions.add(yCoord);
		mapOptions.add(categoryL);
		mapOptions.add(catList);
		mapOptions.add(typeL);
		mapOptions.add(typeList);
		mapOptions.add(create);
		
		optionsLayout.putConstraint(w, nameL, 20, w, mapOptions);
		optionsLayout.putConstraint(n, nameL, 10, n, mapOptions);
		optionsLayout.putConstraint(w, enterName, 20, w, mapOptions);
		optionsLayout.putConstraint(n, enterName, 10, s, nameL);
		optionsLayout.putConstraint(w, widthL, 20, w, mapOptions);
		optionsLayout.putConstraint(n, widthL, 30, s, enterName);
		optionsLayout.putConstraint(n, xCoord, 5, s, widthL);
		optionsLayout.putConstraint(w, xCoord, 20, w, mapOptions);
		optionsLayout.putConstraint(w, heightL, 30, e, widthL);
		optionsLayout.putConstraint(n, heightL, 30, s, enterName);
		optionsLayout.putConstraint(n, yCoord, 5, s, heightL);
		optionsLayout.putConstraint(w, yCoord, 30, e, widthL);
		optionsLayout.putConstraint(w, categoryL, 20, w, mapOptions);
		optionsLayout.putConstraint(n, categoryL, 20, s, xCoord);
		optionsLayout.putConstraint(w, catList, 0, w, categoryL);
		optionsLayout.putConstraint(n, catList, 10, s, categoryL);
		optionsLayout.putConstraint(w, typeL, 0, w, yCoord);
		optionsLayout.putConstraint(n, typeL, 20, s, yCoord);
		optionsLayout.putConstraint(w, typeList, 0, w, typeL);
		optionsLayout.putConstraint(n, typeList, 10, s, typeL);
		optionsLayout.putConstraint(w, create, 20, w, mapOptions);
		optionsLayout.putConstraint(n, create, 20, s, catList);

		optionsLayout.putConstraint(e, mapOptions, 40, e, typeList);
		optionsLayout.putConstraint(s, mapOptions, 30, s, create);
		
		// height panel
		heightOptions.add(toggleHeight);
		heightOptions.add(levelL);
		heightOptions.add(level);
		heightOptions.add(levelB);
		heightOptions.add(randomB);
		
		heightLayout.putConstraint(w, toggleHeight, 20, w, heightOptions);
		heightLayout.putConstraint(n, toggleHeight, 10, n, heightOptions);
		heightLayout.putConstraint(n, levelL, 150, s, toggleHeight);
		heightLayout.putConstraint(w, levelL, 20, w, heightOptions);
		heightLayout.putConstraint(n, level, 10, s,levelL);
		heightLayout.putConstraint(w, level, 20, w, heightOptions);
		heightLayout.putConstraint(w, levelB, 20, w, heightOptions);
		heightLayout.putConstraint(n, levelB, 10, s, level);
		heightLayout.putConstraint(w, randomB, 10, e, levelB);
		heightLayout.putConstraint(n, randomB, 0, n, levelB);
		heightLayout.putConstraint(e, heightOptions, 20, e, randomB);
		heightLayout.putConstraint(s, heightOptions, 20, s, randomB);
		
		// npc panel
		npcOptions.add(npcNamesL);
		npcOptions.add(npcNames);
		npcOptions.add(npcTypeL);
		npcOptions.add(npcTypeList);
		
		npcLayout.putConstraint(w, npcTypeL, 20, w, npcOptions);
		npcLayout.putConstraint(n, npcTypeL, 10, n, npcOptions);
		npcLayout.putConstraint(w, npcTypeList, 0, w, npcTypeL);
		npcLayout.putConstraint(n, npcTypeList, 10, s, npcTypeL);
		npcLayout.putConstraint(w, npcNamesL, 0, w, npcTypeL);
		npcLayout.putConstraint(n, npcNamesL, 30, s, npcTypeList);
		npcLayout.putConstraint(w, npcNames, 0, w, npcTypeL);
		npcLayout.putConstraint(n, npcNames, 10, s, npcNamesL);
		
		npcLayout.putConstraint(e, npcOptions, 20, e, npcNames);
		npcLayout.putConstraint(s, npcOptions, 20, s, npcNames);
		
		// location panel
		locationOptions.add(locTypeList);
		locationOptions.add(locNames);
		
		locationLayout.putConstraint(w, locTypeList,20,w,locationOptions);
		locationLayout.putConstraint(n, locTypeList,10,n,locationOptions);
		locationLayout.putConstraint(w, locNames, 0, w, locTypeList);
		locationLayout.putConstraint(n, locNames, 10, s, locTypeList);
		
		locationLayout.putConstraint(e, locationOptions, 20, e, locTypeList);
		locationLayout.putConstraint(s, locationOptions, 20, s, locNames);
		
		// water panel
		waterOptions.add(waterTypeList);
		waterOptions.add(waterRadiusL);
		waterOptions.add(waterRadiusSpinner);
		
		waterLayout.putConstraint(w, waterTypeList, 20, w, waterOptions);
		waterLayout.putConstraint(n, waterTypeList, 10, n, waterOptions);
		
		waterLayout.putConstraint(n, waterRadiusL, 20, s, waterTypeList);
		waterLayout.putConstraint(w, waterRadiusL, 0, w, waterTypeList);
		waterLayout.putConstraint(n, waterRadiusSpinner, 20, s, waterRadiusL);
		waterLayout.putConstraint(w, waterRadiusSpinner, 0, w, waterRadiusL);
		
		waterLayout.putConstraint(e, waterOptions, 20, e, waterTypeList);
		waterLayout.putConstraint(s, waterOptions, 20, s, waterRadiusSpinner);
		
		// event panel
		eventOptions.add(eventTypeList);
		eventOptions.add(eventScrollPane);
		eventOptions.add(eventItemList);
		eventOptions.add(eventItemTypeList);
		eventOptions.add(logbookPathField);
		
		eventLayout.putConstraint(w, eventTypeList, 20, w, eventOptions);
		eventLayout.putConstraint(n, eventTypeList, 10, n, eventOptions);
		eventLayout.putConstraint(w, eventItemTypeList, 0, w, eventTypeList);
		eventLayout.putConstraint(n, eventItemTypeList, 10, s, eventTypeList);
		eventLayout.putConstraint(w, eventItemList, 0, w, eventItemTypeList);
		eventLayout.putConstraint(n, eventItemList, 10, s, eventItemTypeList);
		eventLayout.putConstraint(w, eventScrollPane, 0, w, eventTypeList);
		eventLayout.putConstraint(n, eventScrollPane, 10, s, eventItemList);
		eventLayout.putConstraint(w, logbookPathField, 0, w, eventTypeList);
		eventLayout.putConstraint(n, logbookPathField, 10, s, eventScrollPane);
		
		eventLayout.putConstraint(e, eventOptions, 20, e, eventItemList);
		eventLayout.putConstraint(s, eventOptions, 20, s, logbookPathField);
		
		// layer panel
		layerOptions.add(currentLayerL);
		layerOptions.add(currentLayerSpinner);
		layerOptions.add(newLayer);
		layerOptions.add(layerList);
		layerOptions.add(setEntrance);
		layerOptions.add(changeLayerLevel);
		
		layerLayout.putConstraint(w, currentLayerL, 20, w, layerOptions);
		layerLayout.putConstraint(n, currentLayerL, 10, n, layerOptions);
		layerLayout.putConstraint(w, currentLayerSpinner, 10, e, currentLayerL);
		layerLayout.putConstraint(n, currentLayerSpinner, 0, n, currentLayerL);
		layerLayout.putConstraint(n, newLayer, 20, s, currentLayerL);
		layerLayout.putConstraint(w, newLayer, 0, w, currentLayerL);
		layerLayout.putConstraint(n, layerList, 20, s, newLayer);
		layerLayout.putConstraint(w, layerList, 0, w, currentLayerL);
		layerLayout.putConstraint(n, setEntrance, 20, s, layerList);
		layerLayout.putConstraint(w, setEntrance, 0, w, currentLayerL);
		layerLayout.putConstraint(n, changeLayerLevel, 20, s, setEntrance);
		layerLayout.putConstraint(w, changeLayerLevel, 0, w, currentLayerL);
		
		layerLayout.putConstraint(e, layerOptions, 20, e, layerList);
		layerLayout.putConstraint(s, layerOptions, 20, s, changeLayerLevel);
		
		// herb layout
		herbOptions.add(herbList);
		
		herbLayout.putConstraint(w, herbList, 20, w, herbOptions);
		herbLayout.putConstraint(n, herbList, 10, n, herbOptions);
		
		herbLayout.putConstraint(e, herbOptions, 20, e, herbList);
		herbLayout.putConstraint(s, herbOptions, 20, s, herbList);
		
		
		// add all optionPanels to tabbedPane
		tabbedPane.addTab("Height", heightOptions);
		tabbedPane.addTab("NPC", npcOptions);
		tabbedPane.addTab("Locations", locationOptions);
		tabbedPane.addTab("Water", waterOptions);
		tabbedPane.addTab("Event", eventOptions);
		tabbedPane.addTab("Layers", layerOptions);
		tabbedPane.addTab("Herbs", herbOptions);

		// add all components to main panel
		add(menuBar);
		add(map);
		add(tabbedPane);
		add(scrollPane);
		add(currentLayerSpinner);
		add(mapOptions);

		layout.putConstraint(n, map, 20, s, menuBar);
		layout.putConstraint(w, map, 20, w, this);
		layout.putConstraint(n, mapOptions, 20, s, menuBar);
		layout.putConstraint(w, mapOptions, 0, w, this);
		layout.putConstraint(w, tabbedPane, 30, e, map);
		layout.putConstraint(n, tabbedPane, 0, n, map);
		layout.putConstraint(w, scrollPane, 30, e, map);
		layout.putConstraint(n, scrollPane, 30, s, tabbedPane);
		layout.putConstraint(n, currentLayerSpinner, 20, s, scrollPane);
		layout.putConstraint(w, currentLayerSpinner, 0, w, scrollPane);
		layout.putConstraint(e, this, 30, e, tabbedPane);
		layout.putConstraint(s, this, 40, s, map);
		
		// only mapOptions visible
		map.setVisible(false);
		tabbedPane.setVisible(false);
		scrollPane.setVisible(false);
		currentLayerSpinner.setVisible(false);
		mapOptions.setVisible(true);
	}

	public static void updateInfo(String text){
		info.setText(text);
	}
	
	public void showInfo(String category, String fileName){
		File file = new File(fileName);
		ArrayList<String> columnNames = new ArrayList<String>();
		ArrayList<String[]> data = new ArrayList<String[]>();
		ArrayList<String> partialData = new ArrayList<String>();
		try {
			Document doc = parser.build(file);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			int j = 0;
			columnNames.add("ID");
			while(i.hasNext()){
				Element el = (Element)i.next();
				partialData.add(el.getAttributeValue("id"));
				List<?> variables = el.getChildren();
				Iterator<?> it = variables.iterator();
				while(it.hasNext()){
					Element var = (Element)it.next();
					if(j == 0){
						columnNames.add(var.getName());
					}
					partialData.add(var.getTextTrim());
				}
				data.add(partialData.toArray(new String[partialData.size()]));
				j++;
				partialData.clear();
			}
			new CreationFrame(category,data.toArray(new String[data.size()][data.get(0).length]),columnNames.toArray(new String[columnNames.size()]));
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "File not found.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	public void saveMap(){
		try{
			String[] names = name.split(" ");
			String title = names[0];
			for(int j=1;j<names.length;j++){
				title+="_" + names[j];
			}
			title = title.replace("\'", "");
			StringBuilder sb = new StringBuilder();
			sb.append("<" + title + " id=\"FILL\">\n");
			sb.append("<name>" + name + "</name>\n");
			sb.append("<category>" + category + "</category>\n");
			sb.append("<type>" + type + "</type>\n");
			sb.append("<dungeonMap>\n");
			sb.append("<heightMap>\n" + map.getElevation() + "</heightMap>\n");
			HashMap<String,boolean[][]> layers = map.getLayers();
			String[] n = new String[layers.size()];
			int[] v = new int[layers.size()];
			int index = 0;
			for(String s: layers.keySet()){
				String[] u = s.split(": ");
				v[index] = Integer.parseInt(u[0]);
				n[index] = u[1];
				index++;
			}
			for(int i=0;i<v.length;i++){
				System.out.println("v[0]: " + v[i]);
				System.out.println("n[0]: " + n[i]);
				sb.append("<layer level=\"" + v[i] + "\" name=\"" + n[i] + "\">\n" + map.getLayerMap(v[i]) + "</layer>\n");
			}
			sb.append("</dungeonMap>\n");
			sb.append("<extra>\n");
			//remove duplicates from seaPos
			System.err.println("# in SeaPos before: " + seaPos.size());
			
			System.out.println("# After:" + seaPos.size());
			
			// enemies <enemy x="" y="">ID</enemy>
			for(int j=0;j<enemyPos.size();j++){
				sb.append("<enemy x=\"" + enemyPos.get(j)[0] + "\" y=\"" + enemyPos.get(j)[1] + "\" z=\"" + enemyPos.get(j)[2] + "\">" + enemyPos.get(j)[3] + "</enemy>\n");
			}
			// quest givers <qnpc x="" y="">ID</qnpc>
			for(int j=0;j<npcPos.size();j++){
				sb.append("<npc x=\"" + npcPos.get(j)[0] + "\" y=\"" + npcPos.get(j)[1] + "\" z=\"" + npcPos.get(j)[2] + "\">" + npcPos.get(j)[3] + "</npc>\n");
			}
			for(int j=0;j<townPos.size();j++){
				sb.append("<town x=\"" + townPos.get(j)[0] + "\" y=\"" + townPos.get(j)[1] + "\" z=\"" + townPos.get(j)[2] + "\">" + townPos.get(j)[3] + "</town>\n");
			}
			for(int j=0;j<hostAPos.size();j++){
				sb.append("<hostileArea x=\"" + hostAPos.get(j)[0] + "\" y=\"" + hostAPos.get(j)[1] + "\" z=\"" + hostAPos.get(j)[2] + "\">" + hostAPos.get(j)[3] + "</hostileArea>\n");
			}
			for(int j=0;j<seaPos.size();j++){
				sb.append("<sea x=\"" + seaPos.get(j)[0] + "\" y=\"" + seaPos.get(j)[1] + "\" z=\"" + seaPos.get(j)[2] + "\">" + seaPos.get(j)[3] + "</sea>\n");
			}
			for(int j=0;j<lakePos.size();j++){
				sb.append("<lake x=\"" + lakePos.get(j)[0] + "\" y=\"" + lakePos.get(j)[1] + "\" z=\"" + lakePos.get(j)[2] + "\">" + lakePos.get(j)[3] + "</lake>\n");
			}
			for(int j=0;j<herbPos.size();j++){
				sb.append("<herb x=\"" + herbPos.get(j)[0] + "\" y=\"" + herbPos.get(j)[1] + "\" z=\"" + herbPos.get(j)[2] + "\">" + herbPos.get(j)[3] + "</herb>\n");
			}
			Set<int[]> eventPos = events.keySet();
			Iterator<int[]> i = eventPos.iterator();
			while(i.hasNext()){
				int[] el = i.next();
				String content = events.get(el);
				//content consists of "TypeName;Description;logbookPath"
				String[] contentSplit = content.split(";");
				try{
				// type is "Text Encounter:-1" or "Surprise Enemy:13"
					sb.append("<event x=\"" + el[0] + "\" y=\"" + el[1] + "\" z=\"" + el[2] + "\" type=\"" + contentSplit[0] + ":" + el[3] + "\" path=\"" + contentSplit[2] + "\">" + contentSplit[1] + "</event>\n");
				} catch(ArrayIndexOutOfBoundsException e){
					sb.append("<event x=\"" + el[0] + "\" y=\"" + el[1] + "\" z=\"" + el[2] + "\" type=\"" + contentSplit[0] + ":" + el[3] + "\">" + contentSplit[1] + "</event>\n");
				}
			}
			for(int[] pos: entrancePos.keySet()){
				sb.append("<entrance x=\"" + pos[0] + "\" y=\"" + pos[1] + "\" z1=\"" + pos[2] + "\" z2=\"" + pos[3] + "\">\n");
				sb.append("\t<text z=\"" + pos[2] + "\">" + entrancePos.get(pos)[0] + "</text>\n");
				sb.append("\t<text z=\"" + pos[3] + "\">" + entrancePos.get(pos)[1] + "</text>\n");
				sb.append("</entrance>\n");
			}
			sb.append("</extra>\n");
			sb.append("<img>FILL</img>\n");
			sb.append("</" + title + ">");
			try{
				File file = new File("Maps/" + name + ".txt");
				if(file.exists()){
					int overWrite = JOptionPane.showConfirmDialog(null, "This file already exists. Overwrite?", "Overwrite?", JOptionPane.YES_NO_CANCEL_OPTION);
					if(overWrite == JOptionPane.NO_OPTION){
						String newFileName = JOptionPane.showInputDialog(null, "Give new filename:","New Filename",JOptionPane.QUESTION_MESSAGE);
						file = new File("Maps/" + newFileName + ".txt");
					}
					else if(overWrite == JOptionPane.CANCEL_OPTION){
						sb = null;
						file = null;
						names = null;
						title = null;
						return;
					}
				}
				File parent_directory = file.getParentFile();
				
				try{
					if (!parent_directory.exists())
					{
					    parent_directory.mkdirs();
					}
				} catch(NullPointerException e){}
	
				FileWriter fstream = new FileWriter(file);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(sb.toString());
				out.close();
				JOptionPane.showMessageDialog(null, "Map written in XML at " + file.getCanonicalPath());
			}catch (Exception e){
				System.err.println("Error: " + e.getMessage());
				JOptionPane.showMessageDialog(null, "An error occured, and the map could not be saved.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch(NullPointerException e){
			return;
		}
	}
	public void reset(){
		
		enterName.setText("");
		xCoord.setText("");
		yCoord.setText("");
		typeList.removeAllItems();
		catList.setSelectedIndex(0);
		try{
			mapOptions.setVisible(true);
			map.setVisible(false);
			scrollPane.setVisible(false);
			currentLayerSpinner.setVisible(false);
			tabbedPane.setVisible(false);
			
		}catch(NullPointerException e){}
		
		enemyPos = new ArrayList<int[]>();
		npcPos = new ArrayList<int[]>();
		townPos = new ArrayList<int[]>();
		hostAPos = new ArrayList<int[]>();
		seaPos = new ArrayList<int[]>();
		lakePos = new ArrayList<int[]>();
		herbPos = new ArrayList<int[]>();
		
		events = new HashMap<int[],String>();
		
		typeList.addItem("Choose a category first");
		typeList.setSelectedIndex(0);
		
		map.init(0, 0);
		
		repaint();
	}

	public void loadMap(String fileName){
		try {
				
			Document doc = parser.build(new File(fileName));
			Element root = doc.getRootElement();
			name = root.getChildTextTrim("name");
			category = root.getChildTextTrim("category");
			type = root.getChildTextTrim("type");
			
			//LOAD THE HEIGHTMAP, AND INITIALIZE MAP
			String[] heightMap;
			try{
				heightMap = root.getChild("dungeonMap").getChildTextTrim("heightMap").split("\n");
			} catch(NullPointerException e){
				heightMap = root.getChild("heightMap").getTextTrim().split("\n"); 
			}
			WIDTH = heightMap[0].length();
			HEIGHT = heightMap.length;
			map.init(WIDTH, HEIGHT);
			for(int i=0;i<heightMap.length;i++){
				for(int j=0;j<heightMap[i].length();j++){
					map.setElevation(j, i, (double)(heightMap[i].charAt(j)-48));
				}
			}
			
			//Load the extra layers
			try{
				List<Element> layers = root.getChild("dungeonMap").getChildren("layer");
				Iterator<Element> it = layers.iterator();
				while(it.hasNext()){
					Element layer = it.next();
					boolean[][] layerMap = new boolean[WIDTH][HEIGHT];
					String[] textMap = layer.getTextTrim().split("\n");
					for(int j=0;j<textMap.length;j++){
						for(int k=0;k<textMap[j].length();k++){
							if(textMap[j].charAt(k) != 'o'){
								layerMap[k][j] = true;
							}
							else{
								layerMap[k][j] = false;
							}
						}
					}
					map.addLayer(Integer.parseInt(layer.getAttributeValue("level")), layer.getAttributeValue("name"), layerMap);
				}
			} catch(NullPointerException e){
				
			}
			
			//LOAD ALL EXTRA DATA: Enemies, npcs,...
			Element extra = root.getChild("extra");
			addObjects(extra,"enemy",enemyPos);
			addObjects(extra,"npc",npcPos);
			addObjects(extra,"town",townPos);
			addObjects(extra,"hostileArea",hostAPos);
			addObjects(extra,"sea",seaPos);
			addObjects(extra,"lake",lakePos);
			addObjects(extra,"sea",seaPos);
			addObjects(extra,"herb",herbPos);
			
			System.err.println("# in seaPos" + seaPos.size());

			List<?> event = extra.getChildren("event");
			Iterator<?> i = event.iterator();
			while(i.hasNext()){
				Element el = (Element)i.next();
				String description = el.getTextTrim();
				//full description is <event x="" y="" z="" type="">description</event>
				//type is "Text Encounter:-1" or "Surprise Enemy:13"
				//events = HashMap<int[]{x,y,z,ID}, String>
				//String consists of "TypeName;Description;logbookPath"
				String[] typeParts = el.getAttributeValue("type").split(":");
				String logbookPath = el.getAttributeValue("path");
				if(logbookPath != null){
					events.put(new int[]{Integer.parseInt(el.getAttributeValue("x")),Integer.parseInt(el.getAttributeValue("y")),
							Integer.parseInt(el.getAttributeValue("z")),Integer.parseInt(typeParts[1])}, typeParts[0] + ";" + description + ";" + logbookPath);
				}
				else{
					events.put(new int[]{Integer.parseInt(el.getAttributeValue("x")),Integer.parseInt(el.getAttributeValue("y")),
							Integer.parseInt(el.getAttributeValue("z")),Integer.parseInt(typeParts[1])}, typeParts[0] + ";" + description);
				}
			}
			
			for(Element e: (List<Element>)extra.getChildren("entrance")){
				int x = Integer.parseInt(e.getAttributeValue("x"));
				int y = Integer.parseInt(e.getAttributeValue("y"));
				int z1 = Integer.parseInt(e.getAttributeValue("z1"));
				int z2 = Integer.parseInt(e.getAttributeValue("z2"));
				String[] descriptions = new String[2];
				int index = 0;
				for(Element text: (List<Element>)e.getChildren("text")){
					descriptions[index] = text.getTextTrim();
					index++;
				}
				entrancePos.put(new int[]{x,y,z1,z2}, descriptions);
				//make sure the entrances actually have a valid square
				if(z1 != 0){
					map.getLayer(z1)[x][y] = true;
				}
				if(z2 != 0){
					map.getLayer(z2)[x][y] = true;
				}
			}
		} catch (JDOMException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Parsing error. Check XML correctness of file", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "File not found", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		manageHeightOptionVisibility();
		repaint();
	}
	public void addObjects(Element e, String childName, Collection<int[]> a){
		List<?> l = e.getChildren(childName);
		Iterator<?> i = l.iterator();
		while(i.hasNext()){
			Element el = (Element)i.next();
			try{
				a.add(new int[]{Integer.parseInt(el.getAttributeValue("x")),Integer.parseInt(el.getAttributeValue("y")),Integer.parseInt(el.getAttributeValue("z")),Integer.parseInt(el.getTextTrim())});
			} catch(NumberFormatException exc){
				a.add(new int[]{Integer.parseInt(el.getAttributeValue("x")),Integer.parseInt(el.getAttributeValue("y")),0,Integer.parseInt(el.getTextTrim())});
			}
		}
	}
	public void manageHeightOptionVisibility(){
		
		map.setBorder(BorderFactory.createTitledBorder(name + " (" + category + " - " + type + ")"));
		mapOptions.setVisible(false);
		map.setVisible(true);
		scrollPane.setVisible(true);
		currentLayerSpinner.setVisible(true);
		tabbedPane.setVisible(true);
		
		radius = new SpinnerNumberModel((int)(Math.min(WIDTH, HEIGHT)/3),0,Math.max(WIDTH, HEIGHT),1);
		radius.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				map.setRadius(((Integer)radius.getValue()).intValue());
			}
		});
		radiusSpinner = new JSpinner(radius);

		force = new SpinnerNumberModel(0.5,0,3,0.05);
		force.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e){
				map.setForce(((Double)force.getValue()).doubleValue());
			}
		});
		forceSpinner = new JSpinner(force);
		forceSpinner.setPreferredSize(new Dimension(50,20));
		
		heightOptions.add(radiusL);
		heightOptions.add(forceL);
		heightOptions.add(radiusSpinner);
		heightOptions.add(forceSpinner);
		heightLayout.putConstraint(w,radiusL,20,w,heightOptions);
		heightLayout.putConstraint(n,radiusL,20,s,toggleHeight);
		heightLayout.putConstraint(w, radiusSpinner, 0, w, radiusL);
		heightLayout.putConstraint(n, radiusSpinner, 10, s, radiusL);
		heightLayout.putConstraint(w,forceL, 0, w, radiusL);
		heightLayout.putConstraint(n, forceL, 20, s, radiusSpinner);
		heightLayout.putConstraint(w, forceSpinner, 0, w, radiusL);
		heightLayout.putConstraint(n, forceSpinner, 10, s, forceL);
	}
	public void loadData(){
		File enemyF = new File("Data/Enemies.xml");
		File npcF = new File("Data/NPCs.xml");
		File townF = new File("Data/Towns.xml");
		File hostileAF = new File("Data/HostileAreas.xml");
		File itemF = new File("Data/Items.xml");
		File equipmentF = new File("Data/Equipment.xml");
		File potionsF = new File("Data/Potions.xml");
		File clothesF = new File("Data/Clothing.xml");
		File consumablesF = new File("Data/Consumables.xml");
		File herbF = new File("Data/Herbs.xml");
		
		// load id and name into hashtable
		enemies = loadObjects(enemyF);
		npcs = loadObjects(npcF);
		towns = loadObjects(townF);
		hostileAreas = loadObjects(hostileAF);
		equipment = loadObjects(equipmentF);
		items = loadObjects(itemF);
		potions = loadObjects(potionsF);
		clothes = loadObjects(clothesF);
		consumables = loadObjects(consumablesF);
		herbs = loadObjects(herbF);

	}
	public Hashtable<String,Integer> loadObjects(File file){
		try {
			Hashtable<String,Integer> h = new Hashtable<String,Integer>();
			Document doc = parser.build(file);
			Element root = doc.getRootElement();
			List<?> objects = root.getChildren();
			Iterator<?> i = objects.iterator();
			while(i.hasNext()){
				Element el = (Element)i.next();
				h.put(el.getChild("name").getTextTrim(), Integer.parseInt(el.getAttributeValue("id")));
			}
			return h;
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Hashtable<String,Integer>();
	}
	
	
	
	class CreationFrame extends JFrame{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3388142805493372134L;
		private JTable table;
		private String[] columnNames;
		private String type;
		
		public CreationFrame(String category, String[] header){
			super(category + " Construction");
			setResizable(false);
			
			type = category;
			
			columnNames = header;
			
			JPanel panel = new JPanel();
			
			JMenuBar menuBar = new JMenuBar();

			JMenu main = new JMenu("Main");
			main.getAccessibleContext().setAccessibleDescription("Make a new " + type);
			menuBar.add(main);
			
			JMenuItem menuItem = new JMenuItem("Save " + type,KeyEvent.VK_S);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription("Save the " + type + " info");
			menuItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					for(int j=0;j<table.getRowCount();j++){
						if(table.getValueAt(j, 0) == null){
						}
						else{
							if(type.equalsIgnoreCase("NPC")){
								npcs.put(table.getValueAt(j, 1).toString(), Integer.parseInt(table.getValueAt(j, 0).toString()));
							}
							else if(type.equalsIgnoreCase("Equipment")){
								equipment.put(table.getValueAt(j, 1).toString(), Integer.parseInt(table.getValueAt(j, 0).toString()));
							}
							else if(type.equalsIgnoreCase("Item")){
								items.put(table.getValueAt(j, 1).toString(), Integer.parseInt(table.getValueAt(j, 0).toString()));
							}
							else if(type.equalsIgnoreCase("Enemy")){
								enemies.put(table.getValueAt(j, 1).toString(), Integer.parseInt(table.getValueAt(j, 0).toString()));
							}
							saveInfo(type,j);
							JOptionPane.showMessageDialog(null, type + " Info written in XML at Objects/" + type + "_" + table.getValueAt(j, 0) + ".txt\nAdded to current database.");
						}
					}
				}
			});
			main.add(menuItem);
			
			menuItem = new JMenuItem("Reset fields",KeyEvent.VK_R);
			menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,ActionEvent.CTRL_MASK));
			menuItem.getAccessibleContext().setAccessibleDescription("Clear the fields");
			menuItem.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					for(int j=0;j<columnNames.length;j++){
						table.setValueAt("", 0, j);
					}
				}
			});
			main.add(menuItem);
			
			table = new JTable(new String[10][columnNames.length],columnNames);
			table.setPreferredScrollableViewportSize(new Dimension(90*columnNames.length,180));
			JScrollPane scrollPane = new JScrollPane(table);
			table.setFillsViewportHeight(true);
			
			panel.add(menuBar);
			panel.add(scrollPane);
			
			getContentPane().add(panel);
			
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
		}
		public CreationFrame(String category, String[][] data,String[] columns){
			super(category + " Info");
			setResizable(false);
			
			columnNames = columns;
			
			JPanel panel = new JPanel();
			
			table = new JTable(data,columnNames);
			table.setPreferredScrollableViewportSize(new Dimension(90*columnNames.length,180));
			JScrollPane scrollPane = new JScrollPane(table);
			table.setFillsViewportHeight(true);
			
			panel.add(scrollPane);
			
			getContentPane().add(panel);
			
			pack();
			setLocationRelativeTo(null);
			setVisible(true);
			
		}
		public void saveInfo(String type, int row){
			StringBuilder sb = new StringBuilder();
			
			if(type.equalsIgnoreCase("NPC")){
				sb.append("<npc id=\"" + table.getValueAt(row, 0) + "\">\n");
				sb.append("<name>" + table.getValueAt(row, 1) + "</name>\n");
				sb.append("<conversation_tree_id>" + table.getValueAt(row, 2) + "</conversation_tree_id>\n");
				sb.append("<soundGreet>" + table.getValueAt(row, 3) + "</soundFarewell>\n");
				sb.append("</npc>");
			}
			else if(type.equalsIgnoreCase("Equipment")){
				sb.append("<item id=\"" + table.getValueAt(row, 0) + "\">\n");
				sb.append("<name>" + table.getValueAt(row, 1) + "</name>\n");
				sb.append("<strength>" + table.getValueAt(row, 2) + "</strength>\n");
				sb.append("<cost>" + table.getValueAt(row, 3) + "</cost>\n");
				sb.append("<durability>" + table.getValueAt(row, 4) + "</durability>\n");
				sb.append("<type>" + table.getValueAt(row, 5) + "</type>\n");
				sb.append("<weight>" + table.getValueAt(row, 6) + "</weight>\n");
				if(table.getValueAt(row, 7) != null){
					sb.append("<feat>" + table.getValueAt(row,7) + "</feat>\n");
					sb.append("<amount>" + table.getValueAt(row, 8) + "</amount>\n");
				}
				if(table.getValueAt(row, 9) != null){
					sb.append("<logbookpath>" + table.getValueAt(row,9) + "</logbookpath>\n");
					sb.append("<logbooksummary>" + table.getValueAt(row, 10) + "</logbooksummary>\n");
				}
				sb.append("</item>");
			}
			else if(type.equalsIgnoreCase("Item")){
				sb.append("<Item id=\"" + table.getValueAt(row, 0) + "\">\n");
				sb.append("<name>" + table.getValueAt(row, 1) + "</name>\n");
				sb.append("<description>" + table.getValueAt(row, 2) + "</description>\n");
				sb.append("<weight>" + table.getValueAt(row, 3) + "</weight>\n");
				sb.append("<cost>" + table.getValueAt(row, 4) + "</cost>\n");
				if(table.getValueAt(row, 5) != null){
					sb.append("<logbookpath>" + table.getValueAt(row, 5) + "</logbookpath>\n");
					sb.append("<logbooksummary>" + table.getValueAt(row, 6) + "</logbooksummary>\n");
				}
				sb.append("</Item>");
			}
			else if(type.equalsIgnoreCase("Enemy")){
				sb.append("<Enemy id=\"" + table.getValueAt(row, 0) + "\">\n");
				sb.append("<name>" + table.getValueAt(row, 1) + "</name>\n");
				sb.append("<level>" + table.getValueAt(row, 2) + "</level>\n");
				sb.append("<hp>" + table.getValueAt(row, 3) + "</hp>\n");
				sb.append("<intelligence>" + table.getValueAt(row, 4) + "</intelligence>\n");
				sb.append("<gold>" + table.getValueAt(row, 5) + "</gold>\n");
				sb.append("<strength>" + table.getValueAt(row, 6) + "</strength>\n");
				sb.append("<dexterity>" + table.getValueAt(row, 7) + "</dexterity>\n");
				sb.append("<intellect>" + table.getValueAt(row, 8) + "</intellect>\n");
				if(table.getValueAt(row, 9) != null){
					sb.append("<gear>" + table.getValueAt(row, 9) + "</gear>\n");
				}
				sb.append("<qItemID>" + table.getValueAt(row, 10) + "</qItemID>\n");
				sb.append("<chance>" + table.getValueAt(row, 11) + "</chance>\n");
				sb.append("<spellIDs>" + table.getValueAt(row, 12) + "</spellIDs>\n");
				if(table.getValueAt(row, 13) != null){
					sb.append("<defense>" + table.getValueAt(row, 13) + "</defense>\n");
				}
				sb.append("<stationary>" + table.getValueAt(row, 14) + "</stationary>\n");
				sb.append("</Enemy>");
			}
			
			try{
				File file = new File("Objects/" + type.toUpperCase() + "_" + table.getValueAt(row, 0) + ".txt");
				File parent_directory = file.getParentFile();
				
				try{
					if (!parent_directory.exists())
					{
					    parent_directory.mkdirs();
					}
				} catch(NullPointerException e){}

				FileWriter fstream = new FileWriter(file);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(sb.toString());
				out.close();
			}catch (Exception e){
				System.err.println("Error: " + e.getMessage());
			}
		}
	}
}
