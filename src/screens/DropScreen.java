package screens;

import rlike.Creature;
import rlike.Item;

public class DropScreen extends InventoryBasedScreen {
	public DropScreen(Creature player) {
        super(player);
    }

	protected String getVerb() {
        return "drop";
    }
	
	//All items are droppable
	protected boolean isAcceptable(Item item) {
        return true;
    }
	
	protected Screen use(Item item) {
        player.drop(item);
        return null;
    }

}
