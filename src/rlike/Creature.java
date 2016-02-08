package rlike;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Creature extends Entity{
	private World world;
	
	public int x;
	public int y;
	public int z;
	
	private int visionRadius;
	public int visionRadius() { return visionRadius; }


	private CreatureAi ai;
	public void setCreatureAi(CreatureAi ai) { this.ai = ai; }
	
	private int maxHp;
	public int maxHp() { return maxHp; }
	
	private int hp;
	public int hp() { return hp; }
	
	private int maxFood;
	public int maxFood() { return maxFood; }
	
	private int food;
	public int food() { return food; }
	

	
	private int attackValue;
	public int attackValue() {
	    return attackValue
	     + (weapon == null ? 0 : weapon.attackValue())
	     + (armor == null ? 0 : armor.attackValue());
	  }
	
	private int defenseValue;
	public int defenseValue() {
		    return defenseValue
		     + (weapon == null ? 0 : weapon.defenseValue())
		     + (armor == null ? 0 : armor.defenseValue());
		  }	
	private Inventory inventory;
	public Inventory inventory() { return inventory; }
	
	private Item weapon;
	public Item weapon() { return weapon; }

	private Item armor;
	public Item armor() { return armor; }
	
	//Hack
	public boolean isPlayer() {
		return glyph == (char)3;
	}
	
	public Creature(World world, String name,  char glyph, Color color, int maxHp, int attack, int defense, int visionRadius){
		super(glyph, color, name);
		this.world = world;
		this.maxHp = maxHp;
		this.hp = maxHp;
		this.attackValue = attack;
		this.defenseValue = defense;
		this.inventory = new Inventory(15);
		this.visionRadius = visionRadius;
		this.maxFood = 1000;
		this.food = 750;
	}
	public void pickUp() {
		Item item = world.item(x, y,  z);

		if(item == null) {
			doAction("grab at ground");
		}
		else if(inventory.isFull()) {	
			doAction("rethink that. No room in your pack");
		}
		else
		{
			world.remove(x, y, z);
			inventory.add(item);
			doAction("pickup a %s", item.name());

		}
	}
	
	public void drop(Item item){
	    if (world.addAtEmptySpace(item, x, y, z)){
	         doAction("drop a " + item.name());
	         inventory.remove(item);
	         unequip(item);
	    } else {
	         notify("There's nowhere to drop the %s.", item.name());
	    }
	}
	public boolean canSee(int wx, int wy, int wz)
	{
		return ai.canSee(wx, wy ,wz);
	}
	public Tile tile(int wx, int wy, int wz)
	{
		return world.tile(wx, wy, wz);
	}
	public void moveBy(int mx, int my, int mz){
		if (mx==0 && my==0 && mz==0)
		    return;
		Tile tile = world.tile(x+mx, y+my, z+mz);
		
		if (mz == -1){
			if (tile == Tile.STAIRS_DOWN) {
				doAction("walk up the stairs to level %d", z+mz+1);
			} else {
				doAction("try to go up but are stopped by the cave ceiling");
				return;
			}
		} else if (mz == 1){
			if (tile == Tile.STAIRS_UP) {
				doAction("walk down the stairs to level %d", z+mz+1);
			} else {
				doAction("try to go down but are stopped by the cave floor");
				return;
			}
		}
		
		Creature other = world.creature(x+mx, y+my, z+mz);
		
		if (other == null)
		{
			ai.onEnter(x+mx, y+my, z+mz, tile);
			if (Math.random() < .1) 
				modifyFood(-1);
		}
		else
		{
			attack(other);
			modifyFood(-10);
		}
		
		
	}
	public Creature creature(int wx, int wy, int wz) {
	    return world.creature(wx, wy, wz);
	}
	public List<Creature> nearbyCreaturesInSight() {
		
		List<Creature> nearby = new ArrayList<Creature>();

		for(int i = x - visionRadius; i < x + visionRadius; i ++) {
			for(int j = y - visionRadius; j < y + visionRadius; j++) {
				if(canSee(i,j,z) && creature(i,j,z)!= null && !creature(i,j,z).equals(this)) {
					nearby.add(creature(i,j,z));
				}
			}
		}
		return nearby;
		
	}
	public void attack(Creature other){
		int amount = Math.max(0, attackValue() - other.defenseValue());
		
		amount = (int)(Math.random() * amount) + amount/4;
		
		doAction("attack the '%s' for %d damage", other.name, amount);
		
		other.modifyHp(-amount);
	}

	public void modifyHp(int amount) { 
		hp += amount;
		
		if (hp < 1) {
			doAction("die");
			leaveCorpse();
			world.remove(this);
		}
	}
	private void leaveCorpse() {
		Item corpse = new Item('%', color, name+ " corpse");
		corpse.modifyFoodValue(maxHp*2);
		world.addAtEmptySpace(corpse, x, y, z);
	}
	public void modifyFood(int amt) {
		food += amt;
		if(food>maxFood) {
			maxFood = maxFood+food/2;
			food = maxFood;
			notify("You've stretched your stomach It hurts so good");
			modifyHp(-15);
		}
		else if(food<1 && isPlayer()) {
			modifyHp(-100);
		}
	}
	public void eat(Item item) {
		
		modifyFood(item.foodValue());
		inventory.remove(item);
		unequip(item);
	}
	public void dig(int wx, int wy, int wz) {
		world.dig(wx, wy, wz);
		doAction("dig");		
		//If no shovel
		if(!inventory.hasItem("shovel"))
			modifyFood(-20);
		else
			if (Math.random() < .4) 
				modifyFood(-1);
			
	}
	
	public void unequip(Item item){
	      if (item == null)
	         return;
	  
	      if (item == armor){
	          doAction("remove the " + item.name());
	          armor = null;
	      } else if (item == weapon) {
	          doAction("put away the " + item.name());
	          weapon = null;
	      }
	  }
	public void equip(Item item){
	      if (item.attackValue() == 0 && item.defenseValue() == 0)
	          return;
	      //Handle unequip something equipped
	      if(weapon!=null && item.attackValue() == weapon.attackValue() && item.defenseValue()==weapon.defenseValue()) {
	    	  unequip(weapon);
	    	  return;
	      }
	      if(armor!=null && item.defenseValue()==armor.defenseValue()) {
	    	  unequip(armor);
	    	  return;
	      }
	  
	      if (item.attackValue() >= item.defenseValue()){
	          unequip(weapon);
	          doAction("wield the " + item.name());
	          weapon = item;
	      } else {
	          unequip(armor);
	          doAction("put on the " + item.name());
	          armor = item;
	      }
	  }
	public void update(){
		ai.onUpdate();
	}

	public boolean canEnter(int wx, int wy, int wz) {
		return world.tile(wx, wy, wz).isGround() && world.creature(wx, wy, wz) == null;
	}

	public void notify(String message, Object ... params){
		ai.onNotify(String.format(message, params));
	}
	
	public void doAction(String message, Object ... params){
	    int r = 9;
	    for (int ox = -r; ox < r+1; ox++){
	        for (int oy = -r; oy < r+1; oy++){
	            if (ox*ox + oy*oy > r*r)
	                continue;
	         
	            Creature other = world.creature(x+ox, y+oy, z);
	         
	            if (other == null)
	                continue;
	         
	            if (other == this)
	                other.notify("You " + message + ".", params);
	            else if (other.canSee(x, y, z))
	                other.notify(String.format("The %s %s.", name, makeSecondPerson(message)), params);
	         }
	    }
	}

	
	private String makeSecondPerson(String text){
		String[] words = text.split(" ");
		words[0] = words[0] + "s";
		
		StringBuilder builder = new StringBuilder();
		for (String word : words){
			builder.append(" ");
			builder.append(word);
		}
		
		return builder.toString().trim();
	}
}
