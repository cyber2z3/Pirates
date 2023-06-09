/*
 * Members: 
 * Lưu Hoàng Đức – ITITIU21181
 * Nguyễn Hoàng Giang – ITITIU21192
 * Nguyễn Tiến Luân – ITITIU21040
 * Đoàn Bảo Nhật Minh – ITITIU21243
 * 
 * Purpose: general properties of all enemies 
 *          and some methods: check if enemies in the air, move and turn direction
 */
package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.Constants.Directions.*;
import static utilz.Constants.*;
import static utilz.HelpMethods.*;

import java.awt.geom.Rectangle2D;

import main.Game;

public abstract class Enemy extends Entity {
	protected int enemyType;
	protected boolean firstUpdate = true;

	protected float walkSpeed = 0.35f * Game.SCALE;
	protected int walkDir = LEFT;
    protected int tileY;
    protected float attackDistance = Game.TILES_SIZE; // attack distance of enemy
    protected boolean active = true; 
    protected boolean attackChecked; 
    
    public Enemy(float x, float y, int width, int height, int enemyType) {
            super(x, y, width, height);
            this.enemyType = enemyType;            
            maxHealth = GetMaxHealth(enemyType);
            currentHealth = maxHealth;
            walkSpeed = Game.SCALE * 0.35f;
    }

    //check the first status spawn is in air or not base lvlData
    //if enemy isn't on floor => in air & cancel firstUpdate
    protected void firstUpdateCheck(int[][] lvlData){
        if (!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;
        firstUpdate = false;
    }

    //if enemy in air => make it fall
    protected void updateInAir(int[][] lvlData){
        if(CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)){
            hitbox.y += airSpeed;
            airSpeed += GRAVITY; 
        }else {
            inAir  = false; 
            hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed); 
            tileY = (int) (hitbox.y /Game.TILES_SIZE);
        }
    }

    //change move direction when it reach place can't move
    protected void move(int[][] lvlData){
        float xSpeed = 0; 

        if(walkDir == LEFT){
            xSpeed = -walkSpeed; 
        }else 
            xSpeed = walkSpeed;

        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
            if (IsFloor(hitbox, xSpeed, lvlData)){
                hitbox.x += xSpeed;
                return;
            }
                
        //change Dir
        changeWaldDir();

    }

    protected void turnTowardsPlayer(Player player){
        if (player.hitbox.x > hitbox.x){
            walkDir = RIGHT;
        } else walkDir = LEFT;
    }

    protected boolean canSeePlayer(int[][] lvlData, Player player){
        int playerTileY = (int) (player.getHitbox().y/ Game.TILES_SIZE); 

        if (playerTileY == tileY){
            if(isPlayerInRange(player)){
                if (IsSightClear(lvlData, hitbox, player.hitbox, tileY))
                    return true;              
            }
        } return false;
    }

    //check if player is in the see range of enemy
    protected boolean isPlayerInRange(Player player){
        int absValue = (int) Math.abs(player.hitbox.x - hitbox.x);
        return absValue <= attackDistance * 5; 
    }

    //check if player is in the attack range of enemy
    protected boolean isPlayerCloseForAttack(Player player){
        int absValue = (int) Math.abs(player.hitbox.x - hitbox.x);
        return absValue <= attackDistance; 
    }
    
    //reset animation tick when change state
    protected void newState(int enemyState){
        this.state = enemyState;
        aniTick = 0; 
        aniIndex = 0; 
    }

    public void hurt(int amount){
        currentHealth  -= amount; 
        if (currentHealth <0){
            newState(DEAD);
        }else newState(HIT);
    }


    protected void checkEnemyHit(Rectangle2D attackBox, Player player) {
        if(attackBox.intersects(player.hitbox)){
            player.changeHealth(-GetEnemyDmg(enemyType));
        }attackChecked = true;
    }

    protected void updateAnimationTick(){
        aniTick ++;
        if(aniTick >= ANI_SPEED){
            aniTick = 0;
            aniIndex++;
            //finish 1 animation
            if (aniIndex >= GetSpriteAmount(enemyType, state)){
                aniIndex = 0;

                switch (state){
                    case ATTACK, HIT -> state = IDLE; 
                    case DEAD -> active = false; 
                }
            }
        }
    }

    protected void changeWaldDir(){
        if (walkDir == LEFT) {
                walkDir = RIGHT; 
        }else walkDir = LEFT; 
    }

    public void resetEnemies(){
        hitbox.x = x; 
        hitbox.y = y;
        firstUpdate = true; 
        currentHealth = maxHealth;
        newState(IDLE);
        active = true; 
        airSpeed = 0; 
    }
    
    public boolean isActive(){
        return active;
    }
}
