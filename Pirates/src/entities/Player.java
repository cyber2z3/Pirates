/*
 * Members: 
 * Lưu Hoàng Đức – ITITIU21181
 * Nguyễn Hoàng Giang – ITITIU21192
 * Nguyễn Tiến Luân – ITITIU21040
 * Đoàn Bảo Nhật Minh – ITITIU21243
 
 * Purpose: properties of player inherit from properties of entiy; 
 * 			draw player (animations turn left right, jump, ...), creat hitbox and attack box. 
 */
package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.*;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import audio.AudioPlayer;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;

public class Player extends Entity {
	private BufferedImage[][] animations;
	private boolean moving = false, attacking = false;
	private boolean left, right, jump;
	private int[][] lvlData;
	private float xDrawOffset = 21 * Game.SCALE;
	private float yDrawOffset = 4 * Game.SCALE;

	// Jumping / Gravity
	private float jumpSpeed = -2.25f * Game.SCALE;
	private float fallSpeedAfterCollision = 0.5f * Game.SCALE;

	//StatusBarUI
	private BufferedImage statusBarImg;

	private int statusBarWidth = (int) (192*Game.SCALE);
	private int statusBarHeight = (int) (58*Game.SCALE);
	private int statusBarX = (int) (10*Game.SCALE);
	private int statusBarY = (int) (10*Game.SCALE);

	private int healthBarWidth = (int) (150*Game.SCALE);
	private int healthBarHeight = (int) (4*Game.SCALE);
	private int healthBarXStart = (int) (34*Game.SCALE);
	private int healthBarYStart = (int) (14*Game.SCALE);

	private int powerBarWidth = (int) (104 * Game.SCALE);
	private int powerBarHeight = (int) (2 * Game.SCALE);
	private int powerBarXStart = (int) (44 * Game.SCALE);
	private int powerBarYStart = (int) (34 * Game.SCALE);
	private int powerWidth = powerBarWidth;
	private int powerMaxValue = 200;
	private int powerValue = powerMaxValue;

	// private int maxHealth = 100; 
	// private int currentHealth = maxHealth; //not max health
	private int healthWidth = healthBarWidth;

	// attack box => range attack of player 
	// private Rectangle2D.Float attackBox; 
	
	//flip 
	private int flipX = 0; 
	private int flipW = 1; 

	//attack system 
	private boolean attackChecked ; 
	private Playing playing; 

	private int tileY = 0;
	private boolean powerAttackActive;
	private int powerAttackTick;
	private int powerGrowSpeed = 15;
	private int powerGrowTick;
  
	public Player(float x, float y, int width, int height, Playing playing) {
		super(x, y, width, height);
		this.playing = playing;
		this.state = IDLE;
		this.maxHealth = 100;
		this.currentHealth = maxHealth;
		this.walkSpeed = Game.SCALE * 1.0f;
		loadAnimations();
		initHitbox(20, 27);
		initAttackBox();
	}

	public void setSpawn(Point spawn){
		this.x = spawn.x;
		this.y = spawn.y;
		hitbox.x = x;
		hitbox.y = y;
	}

	private void initAttackBox() {
		attackBox = new Rectangle2D.Float(x,y, (int) (20*Game.SCALE), (int) (20*Game.SCALE));
		resetAttackBox();
	}

	//update animation and their position
	public void update() {
		updateHealthBar();
		updatePowerBar();

		if(currentHealth <=0){
			if(state != DEAD) {
				state = DEAD;
				aniTick = 0;
				aniIndex = 0;
				playing.setPlayerDying(true);
				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);
			}else if(aniIndex == GetSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED -1 ){
				playing.setGameOver(true);
				playing.getGame().getAudioPlayer().stopSong();
				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
			}else
				updateAnimationTick();

				return;
			//  playing.setGameOver(true);
		}

		updateAttackBox();

		updatePos();
		if (moving){
			checkPotionTouched();
			checkSpikesTouched();
			tileY = (int)(hitbox.y/Game.TILES_SIZE);
			if (powerAttackActive){
				powerAttackTick++;
				if (powerAttackTick >= 35){
					powerAttackTick = 0;
					powerAttackActive = false;
				}
			}
		}

		if (attacking || powerAttackActive)
			checkAttack();

		updateAnimationTick();
		setAnimation();
	}

	private void checkSpikesTouched() {
		playing.checkSpikesTouched(this);
	}

	private void checkPotionTouched() {
		playing.checkPotionTouched(hitbox);
	}

	private void checkAttack() {
		if(attackChecked || aniIndex !=1){
			return; 
		}
		attackChecked = true; 

		if (powerAttackActive)
			attackChecked = false;

		playing.checkEnemyHit(attackBox); 
		playing.checkObjectHit(attackBox);
		playing.getGame().getAudioPlayer().playAttackSound();
	}

	private void updateAttackBox(){
		//attackBox x = hitbox.x (Start) + width of hitbox + 10*Scale
		if (right && left){
			if (flipW == 1){
				attackBox.x = hitbox.x + hitbox.width + (int) (Game.SCALE * 10);
			}else{
				attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
			}
		}else if (right || powerAttackActive && flipW == 1){
			attackBox.x = hitbox.x + hitbox.width + (int)(10*Game.SCALE);
		}else if (left || powerAttackActive && flipW == -1){
			attackBox.x = hitbox.x - hitbox.width - (int)(10*Game.SCALE);
		}

		//attack box 
		attackBox.y = hitbox.y + + (int)(10*Game.SCALE); 
	}

	private void updateHealthBar() {
		healthWidth = (int) ((currentHealth / (float) maxHealth) * healthBarWidth);
	}

	private void updatePowerBar(){
		powerWidth = (int) ((powerValue / (float) powerMaxValue) * powerBarWidth);

		powerGrowTick++;
		if (powerGrowTick >= powerGrowSpeed){
			powerGrowTick = 0;
			changePower(1);
		}
	}

	//help to draw
	public void render(Graphics g, int lvlOffset) {
		g.drawImage(animations[state][aniIndex], (int) (hitbox.x - xDrawOffset) - lvlOffset + flipX, (int) (hitbox.y - yDrawOffset), width*flipW, height, null);

		drawUI(g);
	}

	private void drawUI(Graphics g) {
		//background ui
		g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);

		//draw current health 
		g.setColor(Color.red);
		g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);
	
		//power bar
		g.setColor(Color.yellow);
		g.fillRect(powerBarXStart + statusBarX, powerBarYStart + statusBarY, powerWidth, powerBarHeight);
	}

	private void updateAnimationTick() {
		aniTick++;
		if (aniTick >= ANI_SPEED) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= GetSpriteAmount(state)) {
				aniIndex = 0;
				attacking = false;
				attackChecked = false; 
			}
		}
	}

	private void setAnimation() {
		int startAni = state;

		if (moving)
			state = RUNNING;
		else
			state = IDLE;

		if (inAir) {
			if (airSpeed < 0)
				state = JUMP;
			else
				state = FALLING;
		}

		if (powerAttackActive){
			state = ATTACK;
			aniIndex = 1;
			aniTick = 0;
			return;
		}

		if (attacking){
			state = ATTACK;
			if(startAni != ATTACK){
				aniIndex = 1; //index of attack is at 1
				aniTick = 0;
				return; 
			}
		}
		if (startAni != state)
			resetAniTick();
	}

	private void resetAniTick() {
		aniTick = 0;
		aniIndex = 0;
	}

	private void updatePos() {
		moving = false;
		float xSpeed = 0;

		//check jump
		if (jump) 
			jump();

		// if (!left && !right && !inAir)
		// 	return;

		if(!inAir)
			if (!powerAttackActive)
				if ((!left && !right) || (left && right))
					return;

		//check left or right and move
		if (left && !right){
			xSpeed -= walkSpeed;
			flipX = width;
			flipW = -1;
		}
		if (right && !left){
			xSpeed += walkSpeed;
			flipX = 0; 
			flipW = 1; 
		}

		if (powerAttackActive){
			if (!left && !right || (left && right)){
				if (flipW == -1)
					xSpeed = -walkSpeed;
				else 
					xSpeed = walkSpeed;
			}
			xSpeed *= 3;
		}

		//check inAir
		if (!inAir)
			if (!IsEntityOnFloor(hitbox, lvlData))
				inAir = true;

		if (inAir && !powerAttackActive) {
			if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
				hitbox.y += airSpeed;
				airSpeed += GRAVITY;
				updateXPos(xSpeed);
			} else {
				hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
				if (airSpeed > 0){
					//reset inAir
					inAir = false;
					airSpeed = 0;
				}else
					airSpeed = fallSpeedAfterCollision;
				updateXPos(xSpeed);
			}
		} else
			updateXPos(xSpeed);
		moving = true;
	}

	private void jump(){
		if (inAir)
		return;
		playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
	inAir = true;
	airSpeed = jumpSpeed;
	}

	private void updateXPos(float xSpeed) {
		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
			hitbox.x += xSpeed;
		} else {
			hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
			if (powerAttackActive){
				powerAttackActive = false;
				powerAttackTick = 0;
			}
		}

	}

	public void changeHealth(int value){
		currentHealth += value; 

		if (currentHealth < 0){
			currentHealth = 0; 
			//gameOver();
		}else if (currentHealth > 100){
			currentHealth = maxHealth;
		}
	}

	public void kill() {
		currentHealth = 0;
	}

	private void changePower(int value){
		powerValue += value;
		if (powerValue >= powerMaxValue)
			powerValue = powerMaxValue;
		else if (powerValue <= 0)
			powerValue = 0;
	}

	private void loadAnimations() {

		BufferedImage img = LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS);

		animations = new BufferedImage[7][8];
		for (int j = 0; j < animations.length; j++)
			for (int i = 0; i < animations[j].length; i++)
				animations[j][i] = img.getSubimage(i * 64, j * 40, 64, 40);

		statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
			
	}

	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}

	public void resetDirBooleans() {
		left = false;
		right = false;
	}

	public void setAttacking(boolean attacking) {
		this.attacking = attacking;
	}

	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public boolean isRight() {
		return right;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	public void setJump(boolean jump) {
		this.jump = jump;
	}

	public void resetAll() {
		resetDirBooleans();
		inAir = false; 
		attacking = false; 
		moving = false; 
		airSpeed = 0f;
		state = IDLE;
		currentHealth = maxHealth;

		hitbox.x = x; 
		hitbox.y = y;
		resetAttackBox();

		if(!IsEntityOnFloor(hitbox, lvlData)){
			inAir = true;
		}
	}

	private void resetAttackBox(){
		if (flipW == 1){
			attackBox.x = hitbox.x + hitbox.width + (int) (Game.SCALE * 10);
		}else{
			attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
		}
	}

	public int getTileY(){
		return tileY;
	}

	public void powerAttack() {
		if (powerAttackActive)
			return;
		if (powerValue >= 60){
			powerAttackActive = true;
			changePower(-60);
		}
	}

}