package rlike;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Creature extends Entity {
	private World world;

	public int x;
	public int y;
	public int z;
	
	public Stats stats;
	public String causeOfDeath;
	
	private boolean throwBack = false;
	public boolean checkThrowBack() {
		return throwBack;
	}
	public void setThrowBack(boolean b) {
		throwBack = b;
	}
	
	public int godModeCount = 0;
	private boolean godMode = false;
	public boolean isGodMode() {
		return godMode;
	}
	
	public int protectedCount = 0;
	private boolean protection = false;
	public boolean hasProtection() {
		return protection;
	}
	public void setProtected(boolean b) {
		protection = b;
	}
	

	private int visionRadius;

	public int visionRadius() {
		return visionRadius;
	}

	public void gainVision() {
		visionRadius += 1;
		doAction("gain sight");
	}

	private CreatureAi ai;

	public void setCreatureAi(CreatureAi ai) {
		this.ai = ai;
	}

	private int maxHp;

	public int maxHp() {
		return maxHp;
	}
	
	public void setHp(int amt) {
		hp = amt;
	}

	public void gainMaxHp() {
		maxHp += 10;
		doAction("gain constitution");
	}
	
	public int fortitudeCount = 0;
	public void gainFortitude(int amt) {
		hp += amt;
		doAction("gain absolute fortitude");
	}

	private int hp;

	public int hp() {
		return hp;
	}

	private int maxFood;

	public int maxFood() {
		return maxFood;
	}

	private int food;

	public int food() {
		return food;
	}

	private int xp;

	public int xp() {
		return xp;
	}

	private int xpToLevel;

	public int xpToLevel() {
		return xpToLevel;
	}

	private int level;

	public int level() {
		return level;
	}

	public String info() {
		return String.format("     level:%d     attack:%d     defense:%d     hp:%d", level, attackValue(),
				defenseValue(), hp);
	}

	private int attackValue;

	public int attackValue() {
		return attackValue + (weapon == null ? 0 : weapon.attackValue()) + (armor == null ? 0 : armor.attackValue());
	}

	public void gainAttackValue() {
		attackValue += 2;
		doAction("gain brawn");
	}
	public void modifyAttackValue(int amt) {
		attackValue += amt;
	}

	private int defenseValue;

	public int defenseValue() {
		return defenseValue + (weapon == null ? 0 : weapon.defenseValue()) + (armor == null ? 0 : armor.defenseValue());
	}

	public void gainDefenseValue() {
		defenseValue += 2;
		doAction("gain protection");
	}

	private Inventory inventory;

	public Inventory inventory() {
		return inventory;
	}

	private Item weapon;

	public Item weapon() {
		return weapon;
	}

	private Item armor;

	public Item armor() {
		return armor;
	}

	// Hack
	public boolean isPlayer() {
		return glyph == (char) 3;
	}

	public Creature(World world, String name, char glyph, Color color, int maxHp, int attack, int defense,
			int visionRadius) {
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
		this.level = 1;
		this.xpToLevel = (int) ((15 * level) / 2);
		this.xp = (int) (Math.random() * (xpToLevel / 2));
		this.stats = new Stats();
		causeOfDeath = "";

	}
	public void read(Item item) {
		String name = item.name;
		if(name.contains("Volume 1")) {
			godMode = true;
			modifyAttackValue(20000);
			godModeCount = 10;
			this.color = Color.YELLOW;
		} else if (name.contains("Volume 2")) {
			protection = true;
			this.color = Color.WHITE;
			protectedCount = 3;
		} else if(name.contains("Volume 3")) {
			gainFortitude(999);
			fortitudeCount = 3;
			this.color = Color.GREEN;
		}
		inventory.remove(item);
	}
	public void modifyXp(int amt) {
		xp += amt;

		notify("You %s %d xp.", amt < 0 ? "lost" : "gained", amt);

		while (xp >= xpToLevel) {
			this.stats.levelUps++;
			level++;
			xp -= xpToLevel;
			xpToLevel = (int) ((12 * level) / 2);
			modifyHp(maxHp/2, "");
			ai.onLevelUp();
			
		}

	}

	public void gainXp(Creature other) {
		modifyXp((int) (other.xp + (other.defenseValue + other.attackValue) / 2)/2);
	}

	public void pickUp() {
		Item item = world.item(x, y, z);

		if (item == null) {
			doAction("grab at ground");
		} else if (inventory.isFull()) {
			doAction("rethink that. No room in your pack");
		} else {
			world.remove(x, y, z);
			inventory.add(item);
			doAction("obtain a %s", item.name());

		}
	}

	public void drop(Item item) {
		if (world.addAtEmptySpace(item, x, y, z)) {
			doAction("drop a " + item.name());
			inventory.remove(item);
			if(hp>0)
				unequip(item);
		} else {
			notify("There's nowhere to drop the %s.", item.name());
		}
	}

	public boolean canSee(int wx, int wy, int wz) {
		return (world.creature(wx, wy, wz)!=null && world.creature(wx, wy, wz).glyph==(char)234)|| ai.canSee(wx, wy, wz);
	}

	public Tile realTile(int wx, int wy, int wz) {
		return world.tile(wx, wy, wz);
	}

	public Tile tile(int wx, int wy, int wz) {
		if (canSee(wx, wy, wz))
			return world.tile(wx, wy, wz);
		else
			return ai.rememberedTile(wx, wy, wz);
	}

	public Item item(int wx, int wy, int wz) {
		if (canSee(wx, wy, wz))
			return world.item(wx, wy, wz);
		else
			return null;
	}

	public void moveBy(int mx, int my, int mz) {
		if (mx == 0 && my == 0 && mz == 0)
			return;
		Tile tile = world.tile(x + mx, y + my, z + mz);

		if (mz == -1) {
			if (tile == Tile.STAIRS_DOWN) {
				doAction("walk up the stairs to level %d", z + mz + 1);
			} else {
				doAction("try to go up but are stopped by the cave ceiling");
				return;
			}
		} else if (mz == 1) {
			if (tile == Tile.STAIRS_UP) {
				doAction("walk down the stairs to level %d", z + mz + 1);
			} else {
				doAction("try to go down but are stopped by the cave floor");
				return;
			}
		}

		Creature other = world.creature(x + mx, y + my, z + mz);

		if (other == null) {
			if (Math.random() < .005) {
				modifyXp(1);
			}
			ai.onEnter(x + mx, y + my, z + mz, tile);
			if (Math.random() < .1)
				modifyFood(-1);
		} else {
			attack(other);
			modifyFood(-10);
		}

	}

	public void teleport(int mx, int my, int mz) {
		Tile tile = world.tile(mx, my, mz);
		Creature other = world.creature(mx, my, mz);
		this.x = mx;
		this.y = my;
		this.z = mz;

		if (other == null) {

			ai.onEnter(x, y, z, tile);
		} else {

			notify("You find yourself phased into another creature");
			notify("In a violent fit the " + other.name + " explodes");
			notify("You sustain major damage");
			other.modifyHp(-500, "having another creature phase inside you");
			this.modifyHp(-50, "phasing inside another creature");
		}
	}

	public Creature creature(int wx, int wy, int wz) {
		if (canSee(wx, wy, wz))
			return world.creature(wx, wy, wz);
		else
			return null;
	}

	public List<Creature> nearbyCreaturesInSight() {

		List<Creature> nearby = new ArrayList<Creature>();

		for (int i = x - visionRadius; i < x + visionRadius; i++) {
			for (int j = y - visionRadius; j < y + visionRadius; j++) {
				if (canSee(i, j, z) && creature(i, j, z) != null && !creature(i, j, z).equals(this)) {
					nearby.add(creature(i, j, z));
				}
			}
		}
		return nearby;

	}

	public void attack(Creature other) {
		int amount = Math.max(1, attackValue() - (2*other.defenseValue()/3));

		amount = (int) (Math.random() * amount) + amount / 4;

		doAction("attack the '%s' for %d damage", other.name, amount);

		other.modifyHp(-amount, "a "+ this.name+"'s vicious attacks");
		if (other.hp < 1) {
			this.gainXp(other);
			this.stats.kills++;
		}
			
	}

	public void modifyHp(int amount, String causeOfDeath) {
		this.causeOfDeath = causeOfDeath;
		hp += amount;
		if (fortitudeCount==0&& hp > maxHp)
			hp = maxHp;
		if (hp < 1) {
			doAction("die");
			leaveCorpse();
			world.remove(this);
		}
	}
	public void throwItem(Item item, int wx, int wy, int wz) {
        Point end = new Point(x, y, 0);
    
        for (Point p : new Line(x, y, wx, wy)){
            if (!realTile(p.x, p.y, z).isGround())
                break;
            end = p;
        }
    
        wx = end.x;
        wy = end.y;
    
        Creature c = creature(wx, wy, wz);
        
        unequip(item);

        if (c != null) {
        	if(item.name=="rock" && c.glyph == (char)234)
        		c.throwBack = true;
            throwAttack(item, c);
        }
        else {
        	doAction("throw a %s", item.name());
        }
        this.stats.itemsThrown++;
            
    
        inventory.remove(item);
        world.addAtEmptySpace(item, wx, wy, wz);
    }
	private void throwAttack(Item item, Creature other) {
        modifyFood(-10);
    
        int amount = Math.max(1, item.thrownAttackValue() - (2*other.defenseValue()/3));

		amount = (int) (Math.random() * amount) + amount / 4;
    
        doAction("throw a %s at the %s dealing %d damage", item.name(), other.name, amount);
    
        other.modifyHp(-amount, "a thrown "+item.name);
    
        if (other.hp < 1)
            gainXp(other);
    }

	private void leaveCorpse() {
		Item corpse = new Item('%', color, name + " corpse");
		corpse.modifyFoodValue(maxHp * 2);
		corpse.modifyHpValue(maxHp/2);
		corpse.modifyThrownAttackValue(5);
		world.addAtEmptySpace(corpse, x, y, z);
		for(Item item: inventory.getItems()) {
			if(item!=null)
				drop(item);
		}
	}

	public void modifyFood(int amt) {
		food += amt;
		if (food > maxFood) {
			maxFood = maxFood + food / 2;
			food = maxFood;
			notify("You've stretched your stomach It hurts so good");
			modifyHp(-15, "overeating");
		} else if (food < 1 && isPlayer()) {
			modifyHp(-100,"starvation");
		}
	}

	public void eat(Item item) {

		modifyFood(item.foodValue());
		modifyHp(item.hpValue(), "eating "+item.name+"s");
		this.stats.thingsEaten++;
		inventory.remove(item);
		unequip(item);
	}

	public void dig(int wx, int wy, int wz) {
		world.dig(wx, wy, wz);
		doAction("dig");
		this.stats.turnsSpentDigging++;
		// If no shovel
		if (weapon != null && weapon.name == "shovel")
			modifyFood(-1);
		else if (Math.random() < .4)
			modifyFood(-20);

		if (Math.random() < .01)
			modifyXp(1);
	}

	public void unequip(Item item) {
		if (item == null)
			return;

		if (item == armor) {
			doAction("remove the " + item.name());
			armor = null;
		} else if (item == weapon) {
			doAction("put away the " + item.name());
			weapon = null;
		}
	}

	public void equip(Item item) {
		if(!inventory.hasItem(item.name)) {
			if(inventory.isFull()) {
				notify("Cannot equip %s, inventory is full", item.name);
				return;
			}
			else {
				world.remove(item);
				inventory.add(item);
			}
			
		}
		if (item.attackValue() == 0 && item.defenseValue() == 0)
			return;
		// Handle unequip something equipped
		if (weapon != null && item.attackValue() == weapon.attackValue()
				&& item.defenseValue() == weapon.defenseValue()) {
			unequip(weapon);
			return;
		}
		if (armor != null && item.defenseValue() == armor.defenseValue()) {
			unequip(armor);
			return;
		}

		if (item.attackValue() >= item.defenseValue()) {
			unequip(weapon);
			doAction("wield the " + item.name());
			weapon = item;
		} else {
			unequip(armor);
			doAction("put on the " + item.name());
			armor = item;
		}
	}

	public void update() {
		ai.onUpdate();
	}

	public boolean canEnter(int wx, int wy, int wz) {
		return world.tile(wx, wy, wz).isGround() && world.creature(wx, wy, wz) == null;
	}

	public void notify(String message, Object... params) {
		ai.onNotify(String.format(message, params));
	}

	public void doAction(String message, Object... params) {
		int r = 9;
		for (int ox = -r; ox < r + 1; ox++) {
			for (int oy = -r; oy < r + 1; oy++) {
				if (ox * ox + oy * oy > r * r)
					continue;

				Creature other = world.creature(x + ox, y + oy, z);

				if (other == null)
					continue;

				if (other == this)
					other.notify("You " + message + ".", params);
				else if (other.canSee(x, y, z))
					other.notify(String.format("The %s %s.", name, makeSecondPerson(message)), params);
			}
		}
	}

	public List<Point> getSurroundingTiles() {
		Point p = new Point(x, y, z);
		return p.neighbors8();
	}

	private String makeSecondPerson(String text) {
		String[] words = text.split(" ");
		words[0] = words[0] + "s";

		StringBuilder builder = new StringBuilder();
		for (String word : words) {
			builder.append(" ");
			builder.append(word);
		}

		return builder.toString().trim();
	}
}
