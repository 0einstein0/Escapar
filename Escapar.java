package escapar.main;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;

import escapar.gfx.Images;
import escapar.handler.*;
import escapar.state.*;


public class Escapar implements Iterations, Runnable {
	
	//ATTRIBUTES
	
	//base
	public Window window;
	private Graphics g;
	
	// game
	public World world;

	// Technical
	private boolean running = false;
	private Thread thread;
	private BufferStrategy bs;

	// State
	public static State pause, play, mainmenu;
	public static State currentstate;

	// Input
	private KeyHandler keyhandler;
	private MouseHandler mousehandler;

	// Constructor
	public Escapar() {
		keyhandler = new KeyHandler();
		mousehandler = new MouseHandler();

		// states
		play = new GameState(world, this);
		pause = new PauseState(world, this);
		mainmenu = new MenuState(world, this);
	}
	
	//variables
	public static final int winningscore = 588;

	// working

	private void initialise() {
		window = new Window();

		// add event handlers to window
		window.getWindow().addKeyListener(keyhandler);
		window.getWindow().addMouseListener(mousehandler);
		window.getWindow().addMouseMotionListener(mousehandler);
		window.getBoard().addMouseListener(mousehandler);
		window.getBoard().addMouseMotionListener(mousehandler);

		Images.initialise();

		// set the starting currentstate
		currentstate = mainmenu;
	}

	public Window getWindow() {
		return window;
	}

	public void setWindow(Window window) {
		this.window = window;
	}

	public void run() {

		initialise();

		/* the game loop */
		int fps = 60;
		double timePerTick = 1000000000 / fps;
		double change = 0;
		long now;
		long lastTime = System.nanoTime();

		while (running) {
			now = System.nanoTime();
			change += (now - lastTime) / timePerTick;
			lastTime = now;

			if (change >= 1) {
				update();
				draw(g);
				change--;
			}
		}

		stop();
	}

	int wait = 0;
	
	@Override
	public void update() {

		// Allow user to switch between pause and play state
		if (KeyHandler.p) {
			wait++; 
			if (wait == 5) {
				if (currentstate == play)
					currentstate = pause;
				else if (currentstate == pause)
					currentstate = play;
				wait = 0;
			}
		}

		// polymorphic updating
		if (currentstate != null)
			currentstate.update();
	}

	@Override
	public void draw(Graphics x) {
		bs = window.getBoard().getBufferStrategy();
		if (bs == null) {
			window.getBoard().createBufferStrategy(3);
			return;
		}
		g = bs.getDrawGraphics();

		// Clear Screen
		g.clearRect(0, 0, Window.width, Window.height);

		// polymorphic drawing
		if (currentstate != null)
			currentstate.draw(g);

		// End Drawing!
		bs.show();
		g.dispose();
	}

	// Threading
	public synchronized void start() {
		if (running)
			return;
		running = true;
		thread = new Thread(this);
		thread.start();
	}

	public synchronized void stop() {
		if (!running)
			return;
		running = false;
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public KeyHandler getKeyHandler() {
		return keyhandler;
	}

	public void setKeyHandler(KeyHandler keyhandler) {
		this.keyhandler = keyhandler;
	}

}