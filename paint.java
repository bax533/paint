import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import javax.imageio.*;
import java.util.LinkedList; 
import java.util.Queue; 

enum Tool {
		BRUSH, FILL;
}

enum Which {
		LEFT, RIGHT;
}

class Okno extends JFrame
{
	public static Tool tool;
    public Okno() {
        super("image editor");
		tool = Tool.BRUSH;
		setSize(640, 600);
        setLocation(80, 80);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }	
}

class PANEL extends JPanel
{
	int zoomLevel = 1;
	Which which = Which.LEFT;
	private int squareX = 240, squareY=240, squareS=5;
	Point prev, next;
	
	BufferedImage img = null;
	Graphics2D imgGraphics = null;
	JLabel label = new JLabel("");
	JPanel tool = new JPanel();
	JPanel info= new JPanel();
	JToolBar toolBar = new JToolBar();  
	Color colorL = Color.BLACK, colorR = Color.WHITE;
	
	public void ustawLewy(Color c)
	{
		colorL = c;
	}
	
	public void ustawPrawy(Color c)
	{
		colorR= c;
	}
	
	void add_buttons()
	{
		info.add(label);
		JButton leftB = new JButton("L");
		JButton rightB = new JButton("R");
		
		JButton fileButton = new JButton("file");
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("choose file to edit");
		fileButton.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					if(chooser.showOpenDialog(fileButton) == JFileChooser.APPROVE_OPTION)
					{
						try {
							img = ImageIO.read(new File( chooser.getSelectedFile().getAbsolutePath() ));
						} catch (IOException ex) {
						}
		
						imgGraphics = img.createGraphics();
						zoomLevel=1;
						setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
						repaint();
					}
				}				
			}
		);
		
		
		toolBar.add(fileButton);
		
		JButton lineButton = new JButton("line");
		JButton fillButton = new JButton("fill");
		lineButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Okno.tool = Tool.BRUSH;
				}
			}
		);
		fillButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Okno.tool = Tool.FILL;
				}
			}
		);
		toolBar.add(lineButton);
		toolBar.add(fillButton);
		
		JButton colorChooser = new JButton("color");
		colorChooser.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (which == Which.LEFT){
						colorL = JColorChooser.showDialog(null, "pick color",colorL);
						leftB.setBackground(colorL);
					}
					else{
						colorR = JColorChooser.showDialog(null, "pick color",colorR);
						rightB.setBackground(colorR);
					}
				}
			}
		);		
		

		leftB.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					which = Which.LEFT;
				}
			}
		);
		leftB.setBackground(Color.BLACK);
		toolBar.add(leftB);
		
		rightB.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					which = Which.RIGHT;
				}
			}
		);
		rightB.setBackground(Color.WHITE);
		
		toolBar.add(rightB);
		
		
		toolBar.add(colorChooser);		
		JButton przybliz = new JButton();
		try {
		Image img = ImageIO.read(getClass().getResource("addicon.png"));
			przybliz.setIcon(new ImageIcon(img));
		} catch (Exception ex) {
			throw new Exception("couldn't load + icon!");
		}
		
		przybliz.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(zoomLevel<8){
						zoomLevel+=1;
						setPreferredSize(new Dimension(img.getWidth()*zoomLevel, img.getHeight()*zoomLevel));
						repaint();
					}
				}
			}
		);
		toolBar.add(przybliz);
		
		JButton oddal = new JButton();
		try {
		Image img = ImageIO.read(getClass().getResource("minusicon.png"));
			oddal.setIcon(new ImageIcon(img));
		} catch (Exception ex) {
			throw new Exception("couldn't load - icon!");
		}
		
		oddal.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(zoomLevel>1){
						zoomLevel-=1;
						setPreferredSize(new Dimension(img.getWidth()*zoomLevel, img.getHeight()*zoomLevel));
						repaint();
					}
				}
			}
		);	
		toolBar.add(oddal);
	}
	
	public PANEL()
	{
		setLayout(new BorderLayout());
		setOpaque(false);
		setBackground(Color.RED);
		
		img = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);		
		imgGraphics = img.createGraphics();	
		add(toolBar, BorderLayout.NORTH);
		add(info, BorderLayout.SOUTH);
		add_buttons();
		
		addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e) {
				switch(Okno.tool)
				{	
					case BRUSH:
						prev = new Point(e.getX()/zoomLevel, (e.getY()-50)/zoomLevel);
						break;
					case FILL:
						boolean c = false;
						if(SwingUtilities.isRightMouseButton(e)) c = true;
						fillFrom(new Point(e.getX()/zoomLevel, (e.getY()-50)/zoomLevel), c);
						repaint();
						break;
				}
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
				boolean k = false;
				if(SwingUtilities.isRightMouseButton(e)) k = true;
				
				switch(Okno.tool)
				{
					case BRUSH:
						next = new Point(e.getX()/zoomLevel, (e.getY()-50)/zoomLevel);
						line(prev, next, k);
						prev = next;
						String location = String.format("[%d, %d]", e.getX(), e.getY()-50);
						label.setText(location);
						break;
				}
			}
			public void mouseMoved(MouseEvent e)
			{	
				String location = String.format("[%d, %d]", e.getX(), e.getY());
				label.setText(location);
			}
		});
	}
	
    protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();

		int w = img.getWidth();
		int h = img.getHeight();
		g2.drawImage(img, 0, 50, w*zoomLevel, h*zoomLevel, null);
		g2.dispose();
    }  
	
	public int getIntFromColor(Color col){
		int Red = (col.getRed() << 16) & 0x00FF0000; 
		int Green = (col.getGreen() << 8) & 0x0000FF00;
		int Blue = col.getBlue() & 0x000000FF;

		return 0xFF000000 | Red | Green | Blue;
	}
	
	void line(Point a, Point b, boolean k)
	{
		if(!k)	imgGraphics.setColor(colorL);
		else 	imgGraphics.setColor(colorR);
		imgGraphics.drawLine(a.x, a.y, b.x, b.y);
		repaint();
		//System.out.println(k +" "+ which);
	}
	
	void fillFrom(Point a, boolean k)
	{
		Queue<Point> queue = new LinkedList<>();
		queue.add(a);
		boolean[][] alreadyIn = new boolean[img.getWidth()][img.getHeight()];
		int col = img.getRGB(a.x, a.y);
		while(queue.size() > 0)
		{
			Point current = queue.poll();
			if(!k)
				img.setRGB(current.x, current.y, getIntFromColor(colorL));
			else 
				img.setRGB(current.x, current.y, getIntFromColor(colorR));
			
			if(current.x>0 && img.getRGB(current.x-1, current.y) == col && !alreadyIn[current.x-1][current.y]) 				  
			{
				queue.add(new Point(current.x-1, current.y));
				alreadyIn[current.x-1][current.y] = true;
			}
			if(current.x+1<img.getWidth() && img.getRGB(current.x+1, current.y) == col && !alreadyIn[current.x+1][current.y])  
			{
				queue.add(new Point(current.x+1, current.y));
				alreadyIn[current.x+1][current.y] = true;
			}
			if(current.y>0 && img.getRGB(current.x, current.y-1) == col && !alreadyIn[current.x][current.y-1]) 				  
			{
				queue.add(new Point(current.x, current.y-1));
				alreadyIn[current.x][current.y-1] = true;
			}
			if(current.y+1<img.getHeight() && img.getRGB(current.x, current.y+1) == col && !alreadyIn[current.x][current.y+1]) 
			{
				queue.add(new Point(current.x, current.y+1));
				alreadyIn[current.x][current.y+1] = true;
			}
		}
		repaint();
	}
	
	public void dodajGuzika(JButton button)
	{
		toolBar.add(button);
		button.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					saveDialog zapis = new saveDialog(img);
				}
			}
		);
	}
}

class saveDialog extends JDialog
{
	public saveDialog(BufferedImage toSave)
	{
		super(new JFrame());
		setSize(300, 300);
		
		JPanel mainPanel = new JPanel();
		
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		setLocation(400, 400);
		JPanel messagePane = new JPanel();
		messagePane.add(new JLabel("NAME FILE"));
		mainPanel.add(messagePane);
		
		JTextField nazwa = new JTextField("ENTER FILE NAME");
		JPanel poleTekstowe = new JPanel();
		
		String nazwafileu = nazwa.getText();
		
		poleTekstowe.add(nazwa);
		mainPanel.add(poleTekstowe);
	
		JPanel buttonPane = new JPanel();
		JButton saveButton = new JButton("SAVE");
		
		saveButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					File outputfile = new File(nazwa.getText());
					
					try{
						ImageIO.write(toSave, "png", outputfile);
					} catch (IOException ex) {
					}
				}
			}
		);
		
		buttonPane.add(saveButton);
		mainPanel.add(buttonPane);
		getContentPane().add(mainPanel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setVisible(true);
	}
}

public class paint
{
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
				Okno okno = new Okno();
				Container contentPane = okno.getContentPane();	
				contentPane.setLayout(new BorderLayout());
				PANEL panel = new PANEL();
				JScrollPane skrol = new JScrollPane();
                skrol.setPreferredSize(new Dimension(300,300));

				JButton test = new JButton("SAVE");
				panel.dodajGuzika(test);
				
				skrol.setViewportView(panel);
				contentPane.add(skrol);
            }
        });
	}
}