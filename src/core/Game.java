package core;

import gui.DeathPopup;
import gui.PauseSign;
import gui.QuitButton;
import gui.ScoreCounter;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import objects.Balloon;
import objects.Barrel;

import org.lwjgl.input.Keyboard;

import aesthetics.Background;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Game implements ApplicationListener, Tickable {
	
	public static boolean hasFinishedIntro = true;
	public static final Vector2 screenSize = new Vector2(1200, 700);
	public static final Vector2 startPosition = new Vector2((Game.screenSize.x/2)-200,(Game.screenSize.y/2)-200);
	public static final Vector2 center = new Vector2((Game.screenSize.x/2),(Game.screenSize.y/2));
	public static Game activeGame;
	public static Random generator = new Random();
	public static FPSLogger logger = new FPSLogger();
	
	private Timer clock;
	private int tickCount = 0;
	
	public RainbowHippie hippie;
	public OrthographicCamera camera;
	public SpriteBatch batch;
	public boolean isPaused = false;
	
	public ArrayList<Renderable> toBeRendered;
	public ArrayList<Tickable> toBeTicked;
	public ArrayList<Tickable> pausedTicked;
	
	public PauseSign pauseSign;
	public DeathPopup deathPopup;
	public QuitButton quitButton;
	public ScoreCounter scoreCounter;
	
	public static void main(String[] args) {
		// Use the desktop configuration
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Rainbow Hippie";
		cfg.width = (int)screenSize.x;
		cfg.height = (int)screenSize.y;
		cfg.resizable = false;
		cfg.addIcon("assets/website_icon.png", FileType.Internal);
		cfg.addIcon("assets/taskbar_icon.png", FileType.Internal);
		cfg.addIcon("assets/window_icon.png", FileType.Internal);
		new LwjglApplication(new Game(), cfg);
	}
	
	public void start() {
		hippie.state = RainbowHippie.FLYING;
		hippie.lockedX = false;
		hippie.lockedY = false;
		hippie.sign.disable();
		hippie.logo.fadeAway(.1f);
		pauseSign = new PauseSign();
		quitButton = new QuitButton();
		scoreCounter = new ScoreCounter();
		Background.startMovingClouds();
		AssetManager.bgMusic.play();
		
		toBeTicked.add(this);
	}
	
	public void restart() {
		scoreCounter.reset();
		RainbowHippie.activeHippie.reset();
		for(int i = 0; i <= toBeTicked.size() - 1; i++) {
			Tickable t = toBeTicked.get(i);
			if(t instanceof Balloon || t instanceof Barrel) {
				toBeTicked.remove(t);
			}
		}
		for(int i = 0; i <= toBeRendered.size() - 1; i++) {
			Renderable r = toBeRendered.get(i);
			if(r instanceof Balloon || r instanceof Barrel) {
				toBeRendered.remove(r);
			}
		}
	}
	
	public void quit() {
		Gdx.app.exit();
	}
	
	@Override
	public void pause() {
		isPaused = true;
		AssetManager.bgMusic.pause();
	}
	
	@Override
	public void resume() {
		isPaused = false;
		AssetManager.bgMusic.play();
	}
	
	@Override
	public void create() {
		// Initialization, same for all platforms
		activeGame = this;
		camera = new OrthographicCamera();
		camera.setToOrtho(false, (int) screenSize.x, (int) screenSize.y);
		batch = new SpriteBatch();

		toBeRendered = new ArrayList<Renderable>();
		toBeTicked = new ArrayList<Tickable>();
		pausedTicked = new ArrayList<Tickable>();

		GLTexture.setEnforcePotImages(false);
		AssetManager.loadAssets();
		Background.load();
		
		// Start animation thread
		clock = new Timer();
		clock.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				for (int i = 0; i <= toBeTicked.size() - 1; i++) {
					Tickable t = toBeTicked.get(i);
					if(pauseSign != null) {
						if(isPaused) { 
							if(pausedTicked.contains(t)){
								t.tick();
							}
						} else {
							t.tick();
						}
					} else {
						t.tick();
					}
				}
			}
		}, 0, 50);

		// Create our hippie
		hippie = new RainbowHippie();
		
		RainbowRay.load();
		
		//********TESTING***********
		//Texture[] positiveCurvedRainbows = new Texture[150];
		//for (int i = 0; i != 150; i ++) {
		//	positiveCurvedRainbows[i] = RainbowRay.generateCurvedRainbow(i);
		//}
	}
	
	@Override
	public void dispose() {
		clock.cancel();
	}
	
	Pixmap map;
	int testCount = 0;
	@Override
	public void render() {
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		
		logger.log();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		for (int i = 0; i <= toBeRendered.size() - 1; i++) {
			toBeRendered.get(i).render();
		}
		/*
		testCount ++;
		if (testCount >= 10) {
			map = new Pixmap((int)(Game.screenSize.x), (int)(Game.screenSize.y), Format.RGBA8888);
			for (int y = 0; y != screenSize.y; y ++) {
				for (int x = 0; x != screenSize.x; x ++) {
					if (hippie.rainbowCollisionTest.test(x, y)) {
						map.drawPixel(x, y, Color.rgba8888(1, 0, 0, 1));
					}
				}
			}
			batch.draw(new Texture(map), 0, 0);
			testCount = 0;
		}
		*/
		batch.end();
	}
	
	@Override
	public void resize(int x, int y) {
		
	}
	
	@Override
	public void tick() {
		tickCount++;
		//The barrel spawning needs work, its pretty terrible right now
		if (generator.nextInt((999 - scoreCounter.score()*10) / 10) == 0 && !hippie.isDead) {
			if(generator.nextBoolean())
				new Barrel(generator.nextInt((int) (screenSize.y-AssetManager.barrel.getHeight())));
			else 
				new Balloon(getRandColor());
		}
	}
	
	private Color getRandColor() {
		int color = generator.nextInt(5);
		switch(color){
		case 0:
			return Color.BLACK;
		case 1:
			return Color.WHITE;
		case 2:
			return Color.RED;
		case 3:
			return Color.BLUE;
		case 4:
			return Color.GREEN;
		}
		return Color.WHITE;
	}
}
