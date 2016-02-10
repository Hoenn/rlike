package rlike;

import java.util.List;

public class CreatureAi {
	protected Creature creature;
	
	public CreatureAi(Creature creature){
		this.creature = creature;
		this.creature.setCreatureAi(this);
	}
	
	public boolean canSee(int wx, int wy, int wz) {
        if (creature.z != wz)
            return false;
    
        if ((creature.x-wx)*(creature.x-wx) + (creature.y-wy)*(creature.y-wy) > creature.visionRadius()*creature.visionRadius())
            return false;
    
        for (Point p : new Line(creature.x, creature.y, wx, wy)){
            if (creature.tile(p.x, p.y, wz).isGround() || p.x == wx && p.y == wy)
                continue;
        
            return false;
        }
    
        return true;
    }
	public void onEnter(int x, int y, int z, Tile tile){
	    if (tile.isGround()){
	         creature.x = x;
	         creature.y = y;
	         creature.z = z;
	    } 
	}
	public void wander(){
	    int mx = (int)(Math.random() * 3) - 1;
	    int my = (int)(Math.random() * 3) - 1;
	    
	    Creature other = creature.creature(creature.x + mx, creature.y + my, creature.z);
	    
	    if (other != null && other.glyph() == creature.glyph())
	        return;
	    else
	        creature.moveBy(mx, my, 0);
	}
	public void chase(Creature target){
		if(target==null)
	    	  return;
	      List<Point> points = new Path(creature, target.x, target.y).points();
	      
	      if(points== null || points.size()==0)
	      {
	    	  return;
	      }
	      
	      int mx = points.get(0).x - creature.x;
	      int my = points.get(0).y - creature.y;
	  
	      creature.moveBy(mx, my, 0);
	}
	public Creature seekTarget() {
		List<Creature> nearby = creature.nearbyCreaturesInSight();
		if(nearby.size()>0)
			return nearby.get((int)(Math.random()*nearby.size()));
		else 
			return null;
	}
	public void onUpdate(){
	}
	
	public void onNotify(String message){
	}
	public void onLevelUp() {
		new LevelUpManager().autoLevelUp(creature);
	}
}

