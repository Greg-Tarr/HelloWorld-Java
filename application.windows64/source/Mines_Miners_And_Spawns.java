import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Mines_Miners_And_Spawns extends PApplet {

//Mines
//Miners
//Spawn
spawn Spawn1;

public void setup() {
  //size(500, 500);
  
  frameRate(1000);
  Spawn1 = new spawn();
}

public void draw() {
  background(255);
  Spawn1.drawObjects();
  Spawn1.createObjects();
  
}
class mine {
  float x, y, resource = 200;
  spawn spawn;
  
  mine(spawn s) {
    this.x = random(width);
    this.y = random(height);
    this.spawn = s;
    while(dist(this.x, this.y, this.spawn.x, this.spawn.y) <  this.spawn.innerRadius) {
      this.x = random(width);
      this.y = random(height);
    }
    
  }
}

class miner {
  float x, y, birthtime, w = 10, energy, energyCapacity = 50, target;
  boolean goingToSpawn = false;
  mine mineTarget;  

  spawn spawn;
  miner(spawn s) {
    this.spawn = s;
    this.x = spawn.x;
    this.birthtime = frameCount;
    this.y = spawn.y;
  }
  
  //Move To Coords
  public void moveto(Object target) {
    for(miner miner : this.spawn.miners) {
      if(dist(miner.x, miner.y, this.x, this.y) < (this.w * 2 + miner.w * 2) && miner != this) {
        int number; if(random(1)<0.5f){number=2;}else{number=-2;}
        this.x += this.w * number;
        this.y += this.w * number;
      }

    }
    if(target.getClass() == mine.class) {
      mine m = (mine) target;
      float d = (float)Math.toDegrees(Math.atan2((m.x - this.x),(m.y - this.y)));
      d = (float)(d + Math.ceil( -d / 360 ) * 360);
      float X = x + (sin(radians(d)) * 20);
      float Y = y + (cos(radians(d)) * 20);
      
      this.x = X;
      this.y = Y;
    }
    
    if(target.getClass() == spawn.class) {
      spawn s = (spawn) target;
      float d = (float)Math.toDegrees(Math.atan2((this.x - s.x),(this.y - s.y)));
      d = (float)(d + Math.ceil( -d / 360 ) * 360) + 180;
      
      float X = x + (sin(radians(d)) * 20);
      float Y = y + (cos(radians(d)) * 20);
      
      this.x = X;
      this.y = Y;
    }
  }
  
  public void think() {

    if(this.energy >= this.energyCapacity && goingToSpawn == true) {
      //Go to spawn
      moveto(this.spawn);
      
      if(dist(this.spawn.x, this.spawn.y, this.x, this.y) < this.w + this.spawn.energyAvailable / 20) {
        //Take energy
        this.spawn.energyAvailable += energyCapacity;
        this.energy -= energyCapacity;
        this.goingToSpawn = true;
      }
    } else {
      //Go to mine
      
      boolean goToClosest = false;
      try {
        if(goToClosest) {
            //First go through all the mines and find the wealthiest
            if(this.spawn.mines.size() >= 1) {
              mine closestMine = this.spawn.mines.get(0);
              for(mine mine : this.spawn.mines) {
                if(dist(mine.x, mine.y, this.x, this.y) < dist(closestMine.x, closestMine.y, this.x, this.y)) {
                  closestMine = mine;
                }
              }
            //Go to closest mine;
            moveto(closestMine);
            
            if(dist(closestMine.x, closestMine.y, this.x, this.y) < this.w && closestMine.resource > 0) {
              //Take energy
              closestMine.resource -= energyCapacity;
              this.energy = energyCapacity;
              this.goingToSpawn = true;
            }
          }
        } else {
          //Go to random mine
          if(this.spawn.mines.size() >= 1) {
            if(this.target == -1) {
              this.target = (int)random(this.spawn.mines.size() - 1);
              this.mineTarget = this.spawn.mines.get((int)target);
            }
            if(this.mineTarget == null || this.spawn.mines.get((int)target) != this.mineTarget) {
              this.target = -1;
            } else {
              moveto(this.spawn.mines.get((int)target));
              
              if(dist(this.spawn.mines.get((int)target).x, this.spawn.mines.get((int)target).y, this.x, this.y) < this.w && this.spawn.mines.get((int)target).resource > 0) {
                //Take energy
                this.spawn.mines.get((int)target).resource -= energyCapacity;
                this.energy = energyCapacity;
                this.goingToSpawn = true;
              }
            }
          }
        }
      } catch (Exception e) {
      }
    }
  }
}

class spawn {
  ArrayList<miner> miners = new ArrayList<miner>();
  ArrayList<mine> mines = new ArrayList<mine>();
  
  float x = width / 2, y = height / 2, energyAvailable, innerRadius = 100;
  
  spawn() {
    this.energyAvailable = 301;
  }

  public void createObjects() {
    if(this.energyAvailable > 300 && miners.size() < 7) {
      println("Spawning a miner");
      this.energyAvailable -= 300;
      miners.add(new miner(this));
    }
    
    if((frameCount % 30 == 0 && mines.size() < 10) || mines.size() < miners.size()) {
      println("Spawning a mine");
      mines.add(new mine(this));
    }
    
    if(frameCount % 10 == 0 && this.energyAvailable > 500) {
      this.energyAvailable -= 100;
    }
  }
  
  public void drawObjects() {
    fill(255);
    ellipse(this.x, this.y, this.energyAvailable / 10, this.energyAvailable / 10);
    fill(this.energyAvailable / 2, this.energyAvailable / 2, 0);
    ellipse(this.x, this.y, this.energyAvailable / 20, this.energyAvailable / 20);
    
    for(mine mine : mines) {
      if(mine.resource < 40) {
        mines.remove(mine); 
        mine = null;
        break;
      }
      fill(mine.resource * (255 / mine.resource), mine.resource * (255 / mine.resource), 0);
      ellipse(mine.x, mine.y, mine.resource / 10, mine.resource / 10);
    }
    for(miner miner : miners) {
      if(frameCount - miner.birthtime > 500) {
        miners.remove(miner);
        miner = null;
        break;
      }
      miner.think();
      fill(255);
      ellipse(miner.x, miner.y, miner.w + miner.energy / 5, miner.w + miner.energy / 5);
      fill(miner.energy * (255 / miner.energyCapacity), miner.energy * (255 / miner.energyCapacity), 0);
      ellipse(miner.x, miner.y, miner.w + miner.energy / 10, miner.w + miner.energy / 10);
    }
  }
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Mines_Miners_And_Spawns" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
