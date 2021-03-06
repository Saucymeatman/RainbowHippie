package objects;

import java.util.Random;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import core.AssetManager;
import core.Game;
import core.RainbowHippie;
import core.Renderable;
import core.Tickable;

public class Barrel implements Renderable, Tickable {
	
	public static final Vector2 BARREL_SIZE = new Vector2(AssetManager.barrel.getWidth(),AssetManager.barrel.getHeight());
	public static final Random generator = new Random();
	
	public float rotation, torque, velocity;
	public Vector2 location;
	public Rectangle boundingBox;
	public Sprite sprite;
	
	public Barrel(float y) {
		location = new Vector2(Game.screenSize.x+50,y);
		boundingBox = new Rectangle(0,0, BARREL_SIZE.x, BARREL_SIZE.y);
		sprite = new Sprite(AssetManager.barrel);
		Game.activeGame.toBeTicked.add(this);
		Game.activeGame.toBeRendered.add(this);
		
		//Rotational random small offset, looks better
		sprite.setOrigin(randomInt(30, 50), randomInt(50, 100));
		torque = randomInt(7, 20);
		velocity = randomInt(10, 40);
	}
	
	public void dispose() {
		Game.activeGame.toBeTicked.remove(this);
		Game.activeGame.toBeRendered.remove(this);
	}
	
	public boolean isActive() {
		return Game.activeGame.toBeTicked.contains(this) && Game.activeGame.toBeRendered.contains(this);
	}
	
	@Override
	public void render() {
		sprite.setPosition(location.x, location.y);
		sprite.setRotation(rotation);
		sprite.draw(Game.activeGame.batch);
	}
	
	@Override
	public void tick() {
		//Update location
		location.x -= velocity;
		
		//Update bounding box
		boundingBox.setCenter(location.x+BARREL_SIZE.x/2, location.y+BARREL_SIZE.y/2);
		
		//Update rotation
		rotation += torque;
		
		//Collision check
		if (boundingBox.overlaps(RainbowHippie.activeHippie.boundingBox)) {
			dispose();
			RainbowHippie.activeHippie.die();
		}
		
		//Dispose if out of screen
		if (location.x < -100) {
			dispose();
		}
	}
	
	public static int randomInt(int min, int max) {
		return generator.nextInt(max-min) + min;
	}
	
	public static float random(float min, float max) {
		return generator.nextInt((int)max-(int)min) + min + generator.nextFloat();
	}
}
