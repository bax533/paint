import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import javax.imageio.*;
import java.util.LinkedList; 
import java.util.Queue; 
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

enum Tool {
		BRUSH, FILL;
}

enum Which {
		LEFT, RIGHT;
}

class Window extends JFrame
{
    public Window() {
        super("image editor");
		setSize(500, 500);
        //setLocation(80, 80);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
	
	public void update()
	{
		SwingUtilities.updateComponentTreeUI(this);
		invalidate();
		validate();
		repaint();
	}
}

class Singleton {
private static volatile Singleton INSTANCE;

	private Singleton() {}
	
	public Window okno;
	public Tool tool = Tool.BRUSH;;
	public BufferedImage img;
	public Graphics2D imgGraphics;
	public FileButton fileButton;
	public int zoomLevel = 1;
	public Color colorL = Color.BLACK, colorR = Color.WHITE;
	public Which which = Which.LEFT;
	public PANEL paintPanel;
	public int brushSize = 1;
	
	public static Singleton getInstance() {
	
		if (INSTANCE == null)
		synchronized(Singleton.class) {
			 if (INSTANCE == null)
				 INSTANCE = new Singleton();
	}
     return INSTANCE;
	}
}

class Methods
{
    public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
	
	public static String getFileExtension(File file) {
        String extension = "";
 
        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                extension = name.substring(name.lastIndexOf("."));
            }
        } catch (Exception e) {
            extension = "";
        }
 
        return extension;
    }
	
}


class FileButton extends JButton
{
	Singleton resources = Singleton.getInstance ();
	JFileChooser chooser;
	
	
	
	public FileButton(String name)
	{
		super(name);
		chooser = new JFileChooser();
		
		FileFilter filter = new FileNameExtensionFilter("jpg png","jpg", "png");
		chooser.setDialogTitle("choose file to edit");
		chooser.setFileFilter(filter);
		//chooser.addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "png"));
		//chooser.setFileFilter(filter)
		addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					if(chooser.showOpenDialog(resources.fileButton) == JFileChooser.APPROVE_OPTION)
					{
						try {
						
							File toRead = new File( chooser.getSelectedFile().getAbsolutePath());
							if(Methods.getFileExtension(toRead).equals(".jpg") || Methods.getFileExtension(toRead).equals(".png"))
								resources.img = ImageIO.read(toRead);
							else
								Methods.infoBox("nieprawidlowy format pliku!", "error");
						} catch (IOException ex) {
						}
		
						resources.imgGraphics = resources.img.createGraphics();
						resources.zoomLevel=1;
						resources.paintPanel.setPreferredSize(new Dimension(resources.img.getWidth(), resources.img.getHeight()));
						resources.paintPanel.repaint();
						resources.okno.update();
					}
				}				
			}
		);
	}
}

class ToolBar extends JToolBar
{
	Singleton resources = Singleton.getInstance();
	
	public ToolBar()
	{
		JButton leftB = new JButton("L");
		JButton rightB = new JButton("R");
		JButton lineButton = new JButton("line");
		JButton fillButton = new JButton("fill");
		JButton colorChooser = new JButton("color");
		JButton przybliz = new JButton();
		FileButton fileButton = new FileButton("file");
		JButton oddal = new JButton();
		
		JLabel lineSize = new JLabel("1");
		JButton addSize = new JButton("+");
		JButton minusSize = new JButton("-");
		JButton saveButton = new JButton("SAVE");
		
		lineButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					resources.tool = Tool.BRUSH;
				}
			}
		);
		
		
		
		addSize.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					resources.brushSize += 1;
					lineSize.setText(Integer.toString(resources.brushSize));
				}
			}
		);
		
		minusSize.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e){
					if(resources.brushSize > 1)
						resources.brushSize -= 1;
					lineSize.setText(Integer.toString(resources.brushSize));
				}
			}
		);
		
		fillButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					resources.tool = Tool.FILL;
				}
			}
		);
		
		
		
		colorChooser.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (resources.which == Which.LEFT){
						resources.colorL = JColorChooser.showDialog(null, "pick color",resources.colorL);
						leftB.setBackground(resources.colorL);
					}
					else{
						resources.colorR = JColorChooser.showDialog(null, "pick color",resources.colorR);
						rightB.setBackground(resources.colorR);
					}
				}
			}
		);		
		

		leftB.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					resources.which = Which.LEFT;
				}
			}
		);
		leftB.setBackground(Color.BLACK);
		
		
		rightB.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					resources.which = Which.RIGHT;
				}
			}
		);
		rightB.setBackground(Color.WHITE);
		
		
		
		try {
		Image imgg = ImageIO.read(getClass().getResource("addicon.png"));
			przybliz.setIcon(new ImageIcon(imgg));
		} catch (Exception ex) {
			//throw new Exception("couldn't load + icon!");
		}
		
		przybliz.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(resources.zoomLevel<8){
						resources.zoomLevel+=1;
						resources.paintPanel.setPreferredSize(new Dimension(resources.img.getWidth()*resources.zoomLevel, resources.img.getHeight()*resources.zoomLevel));
						resources.paintPanel.repaint();
						resources.okno.update();
					}
				}
			}
		);
		
		
		
		try {
		Image img = ImageIO.read(getClass().getResource("minusicon.png"));
			oddal.setIcon(new ImageIcon(img));
		} catch (Exception ex) {
			//throw new Exception("couldn't load - icon!");
		}
		
		oddal.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if(resources.zoomLevel>1){
						resources.zoomLevel-=1;
						resources.paintPanel.setPreferredSize(new Dimension(resources.img.getWidth()*resources.zoomLevel, resources.img.getHeight()*resources.zoomLevel));
						resources.paintPanel.repaint();
						resources.okno.update();
					}
				}
			}
		);	
		
		saveButton.addActionListener(
			new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					saveDialog zapis = new saveDialog(resources.img);
				}
			}
		);
		
		add(fileButton);
		add(lineButton);
		add(addSize);
		add(minusSize);
		add(lineSize);
		add(fillButton);
		add(leftB);
		add(rightB);
		add(colorChooser);		
		add(przybliz);
		add(oddal);
		add(saveButton);
	}
}

class Painter
{
	Singleton resources = Singleton.getInstance();
	public void line(Point a, Point b, boolean k)
	{
		if(!k)	resources.imgGraphics.setColor(resources.colorL);
		else 	resources.imgGraphics.setColor(resources.colorR);
		resources.imgGraphics.setStroke(new BasicStroke(resources.brushSize));
		resources.imgGraphics.drawLine(a.x, a.y, b.x, b.y);
		resources.paintPanel.repaint();
	}
	
	public void fillFrom(Point a, boolean k)
	{
		Queue<Point> queue = new LinkedList<>();
		queue.add(a);
		boolean[][] alreadyIn = new boolean[resources.img.getWidth()][resources.img.getHeight()];
		int col = resources.img.getRGB(a.x, a.y);
		while(queue.size() > 0)
		{
			Point current = queue.poll();
			if(!k)
				resources.img.setRGB(current.x, current.y, getIntFromColor(resources.colorL));
			else 
				resources.img.setRGB(current.x, current.y, getIntFromColor(resources.colorR));
			
			if(current.x>0 && resources.img.getRGB(current.x-1, current.y) == col && !alreadyIn[current.x-1][current.y]) 				  
			{
				queue.add(new Point(current.x-1, current.y));
				alreadyIn[current.x-1][current.y] = true;
			}
			if(current.x+1<resources.img.getWidth() && resources.img.getRGB(current.x+1, current.y) == col && !alreadyIn[current.x+1][current.y])  
			{
				queue.add(new Point(current.x+1, current.y));
				alreadyIn[current.x+1][current.y] = true;
			}
			if(current.y>0 && resources.img.getRGB(current.x, current.y-1) == col && !alreadyIn[current.x][current.y-1]) 				  
			{
				queue.add(new Point(current.x, current.y-1));
				alreadyIn[current.x][current.y-1] = true;
			}
			if(current.y+1<resources.img.getHeight() && resources.img.getRGB(current.x, current.y+1) == col && !alreadyIn[current.x][current.y+1]) 
			{
				queue.add(new Point(current.x, current.y+1));
				alreadyIn[current.x][current.y+1] = true;
			}
		}
		resources.paintPanel.repaint();
	}
	
	int getIntFromColor(Color col){
		int Red = (col.getRed() << 16) & 0x00FF0000; 
		int Green = (col.getGreen() << 8) & 0x0000FF00;
		int Blue = col.getBlue() & 0x000000FF;

		return 0xFF000000 | Red | Green | Blue;
	}	
}

class PANEL extends JPanel
{
	Singleton resources = Singleton.getInstance ();
	private int squareX = 240, squareY=240, squareS=5;
	Point prev, next;
	
	Painter painter;
	
	JLabel label = new JLabel("");
	//JPanel tool = new JPanel();
	JPanel info= new JPanel();
	//JToolBar toolBar = new JToolBar();  
	
	
	public void ustawLewy(Color c)
	{
		resources.colorL = c;
	}
	
	public void ustawPrawy(Color c)
	{
		resources.colorR= c;
	}
	
	public PANEL()
	{
		painter = new Painter();
		setLayout(new FlowLayout());
		setOpaque(false);
		setBackground(Color.RED);
		setPreferredSize(new Dimension(400,400));
		resources.img = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
		resources.imgGraphics = resources.img.createGraphics();	
		
		addMouseListener(new MouseAdapter() {
			
			public void mousePressed(MouseEvent e) {
				switch(resources.tool)
				{	
					case BRUSH:
						prev = new Point(e.getX()/resources.zoomLevel, (e.getY())/resources.zoomLevel);
						break;
					case FILL:
						boolean c = false;
						if(SwingUtilities.isRightMouseButton(e)) c = true;
						painter.fillFrom(new Point(e.getX()/resources.zoomLevel, (e.getY())/resources.zoomLevel), c);
						break;
				}
			}
		});
		
		addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent e) {
				boolean k = false;
				if(SwingUtilities.isRightMouseButton(e)) k = true;
				
				switch(resources.tool)
				{
					case BRUSH:
						next = new Point(e.getX()/resources.zoomLevel, (e.getY())/resources.zoomLevel);
						painter.line(prev, next, k);
						prev = next;
						String location = String.format("[%d, %d]", e.getX(), e.getY());
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

		int w = resources.img.getWidth();
		int h = resources.img.getHeight();
		g2.drawImage(resources.img, 0, 0, w*resources.zoomLevel, h*resources.zoomLevel, null);
		g2.dispose();
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
					if(Methods.getFileExtension(outputfile).equals(".png") || Methods.getFileExtension(outputfile).equals(".jpg")){
						try{
							ImageIO.write(toSave, "image", outputfile);
						} catch (IOException ex) {
						}
					}
					else
					{
						Methods.infoBox("niewłasciwy format pliku!\nwybierz .png lub .jpg", "error");
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
				Singleton resources = Singleton.getInstance ();
				resources.okno = new Window();
				resources.paintPanel = new PANEL();
				resources.okno.getContentPane().setLayout (new FlowLayout());  
				
				JScrollPane skrol = new JScrollPane(resources.paintPanel,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);//resources.paintPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

				skrol.setPreferredSize(new Dimension(400,400));
				
				resources.okno.getContentPane().add(skrol);
				
				ToolBar toolBar = new ToolBar();
				
				JFrame tools = new JFrame();
				tools.add(toolBar);
				tools.setSize(350, 100);
				tools.setVisible(true);

            }
        });
	}
}