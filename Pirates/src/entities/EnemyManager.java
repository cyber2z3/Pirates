/*
 * Members: 
 * Lưu Hoàng Đức – ITITIU21181
 * Nguyễn Hoàng Giang – ITITIU21192
 * Nguyễn Tiến Luân – ITITIU21040
 * Đoàn Bảo Nhật Minh – ITITIU21243
 * 
 * Purpose: (similarity with level manager) to control and load enemy image and check enemy hit
 * 
 */

package entities;
//similarity with level manager

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import gamestates.Playing;
import utilz.LoadSave;
import static utilz.Constants.EnemyConstants.*;

public class EnemyManager {
    
    private Playing playing;
    private BufferedImage[][] crabbyArr;
    private ArrayList<Crabby> crabbies = new ArrayList<>(); 

    public EnemyManager(Playing playing){
        this.playing = playing; 
        loadEnemyImgs();
    }

    public void loadEnemies(levels.Level newLevel) {
        crabbies = newLevel.getCrabs();
    }

    public void update(int[][] lvlData, Player player){
        boolean isAnyActive = false;
        for (Crabby c: crabbies){
            if (c.isActive()){
                c.update(lvlData, player);
                isAnyActive = true;
            }
        }
        if (!isAnyActive)
            playing.setLevelCompleted(true);;
    }

    public void draw(Graphics g, int xLvlOffset){
        drawCrabs(g, xLvlOffset);
    }

    //draw Crabs
    public void drawCrabs(Graphics g, int xLvlOffset){
        for (Crabby c: crabbies)
            if (c.isActive()){
            g.drawImage(crabbyArr[c.getEnemyState()][c.getAniIndex()], (int) c.getHitbox().x - xLvlOffset -CRABBY_DRAWOFFSET_X + c.flipX(),(int) c.getHitbox().y - CRABBY_DRAWOFFSET_Y,  CRABBY_WIDTH*c.flipW(), CRABBY_HEIGHT, null);
            }
    }

    public void checkEnemyHit(Rectangle2D.Float attackBox){
        for(Crabby c: crabbies)
            if (c.isActive()){
                if (c.getCurrentHealth()>0)
                    if (attackBox.intersects(c.getHitbox())){
                        c.hurt(10);
                        return;
                    }
            }
    }

    private void loadEnemyImgs(){
        crabbyArr = new BufferedImage[5][9];
        BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.CRABBY_SPRITE);
        for (int i =0; i<crabbyArr.length; i++)
            for (int j=0; j<crabbyArr[i].length; j++)
                crabbyArr[i][j] = temp.getSubimage(j* CRABBY_WIDTH_DEFAULT, i*CRABBY_HEIGHT_DEFAULT, CRABBY_WIDTH_DEFAULT, CRABBY_HEIGHT_DEFAULT);
    }

    public void resetAllEnemies() {
        for(Crabby c: crabbies){
            c.resetEnemies();
        }
    }
}
