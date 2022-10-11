
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;

import ClientContext.Antiban;
import simple.api.actions.SimpleObjectActions;
import simple.api.filters.SimpleSkills;
import simple.api.script.Category;
import simple.api.script.Script;
import simple.api.script.ScriptManifest;
import simple.api.script.interfaces.SimplePaintable;
import simple.api.wrappers.SimpleGameObject;
import simple.api.wrappers.SimpleSceneObject;

// Credits to FVZ

@ScriptManifest(author = "Vainiven", category = Category.WOODCUTTING, description = "Progressive Woodcutter, start at any level. Will cut tree's and willow's until 99. Start anywhere with an Iron and Rune axe in inventory or bank!", discord = "Vainven#6986", name = "V-Woodcutter", servers = {
		"Xeros" }, version = "0.3")

public class Woodcutter extends Script implements SimplePaintable {

	Antiban antiBan = new Antiban();

	// Basic Variabelen
	int TREE = 10833;
	int AXE = 1359;
	int LUMBERJACK_HAT = 10941;
	int LUMBERJACK_TORSO = 10939;
	int LUMBERJACK_LEGS = 10940;
	int LUMBERJACK_BOOTS = 10933;
	private long startTime;
	private String status;
	private int gainedLogs;
	private int tempLogs;

	// Paint Variabelen
	private final Color color1 = new Color(255, 255, 255);
	private final Font font1 = new Font("Gadugi", 1, 13);
	private final Font font2 = new Font("Gadugi", 0, 13);
	private final Image img1 = ctx.paint.getImage("https://i.ibb.co/JzN0c0v/Woodcutting.png");

	private long startExp;

	// What do I do one time when script starts
	@Override
	public boolean onExecute() {
		// Basis Variabelen
		System.out.println("Started V-Woodcutter!");
		startTime = System.currentTimeMillis();
		status = "Planting Trees";
		gainedLogs = 0;
		tempLogs = ctx.inventory.populate().filter("Willow").population();

		// Paint Variabelen
		startExp = ctx.skills.getExperience(SimpleSkills.Skill.WOODCUTTING);
		return true;

	}

	// What do I loop all the time
	@Override
	public void onProcess() {
		if (ctx.players.getLocal().getLocation().getRegionID() == 15159) {
			if (!ctx.inventory.inventoryFull() && hasItem(AXE)
					&& !(ctx.skills.getRealLevel(SimpleSkills.Skill.WOODCUTTING) >= 41 && !hasItem(1359))) {
				cutTree();
			} else {
				equipItem(LUMBERJACK_HAT);
				equipItem(LUMBERJACK_TORSO);
				equipItem(LUMBERJACK_LEGS);
				equipItem(LUMBERJACK_BOOTS);
				bank();
			}
		} else {
			ctx.teleporter.teleportStringPath("Skilling", "Skilling Island");
		}
	}

	// Check of inventory/equipment axe has or not
	public boolean hasItem(int item) {
		return (!ctx.inventory.populate().filter(item).isEmpty() || !ctx.equipment.populate().filter(item).isEmpty());
	}

	// Method Cut Tree
	public void cutTree() {
		if (!ctx.players.getLocal().isAnimating()) {
			SimpleSceneObject<?> tree = ctx.objects.populate().filter(TREE).nextNearest();
			if (tree != null) {
				tree.interact(502);
				status = "Woodcutting";
				ctx.sleepCondition(() -> !ctx.players.getLocal().isAnimating(), 5000);
			}
		} else {
			ctx.sleep(250);
			tempLogs = ctx.inventory.populate().filter(1519, 1511).population();
		}
	}

	// Method Checking Level and Equipping right Axe and Right Tree
	public void checkLevel() {
		int checkLevel = ctx.skills.getRealLevel(SimpleSkills.Skill.WOODCUTTING);
		if (checkLevel < 41) {
			AXE = 1349;
			TREE = 1276;
		} else if (checkLevel > 41 && checkLevel < 75) {
			AXE = 1359;
			TREE = 10833;
		} else {
			AXE = 1359;
			TREE = 10834;
		}
	}

	// Bank
	public void bank() {
		SimpleGameObject bank = (SimpleGameObject) ctx.objects.populate().filter(20325).nearest().next();
		if (!ctx.bank.bankOpen() && bank != null) {
			bank.interact(SimpleObjectActions.USE);
			ctx.onCondition(() -> ctx.bank.bankOpen(), 250, 30);
		}
		checkLevel();
		gainedLogs += ctx.inventory.populate().filter(1519, 1511).population();
		tempLogs = 0;
		ctx.bank.depositInventory();
		withDraw(AXE);
		withDraw(LUMBERJACK_HAT);
		withDraw(LUMBERJACK_TORSO);
		withDraw(LUMBERJACK_LEGS);
		withDraw(LUMBERJACK_BOOTS);
		ctx.bank.closeBank();
		ctx.onCondition(() -> ctx.bank.closeBank(), 250, 4);
		equipItem(LUMBERJACK_HAT);
		equipItem(LUMBERJACK_TORSO);
		equipItem(LUMBERJACK_LEGS);
		equipItem(LUMBERJACK_BOOTS);
		equipItem(AXE);

	}

	// Method Withdrawing correct equipment
	public void withDraw(int item) {
		if (ctx.equipment.populate().filter(item).isEmpty() && ctx.inventory.populate().filter(item).isEmpty()) {
			ctx.bank.withdraw(item, 1);
		}
	}

	// Method Equipping correct equipment
	public void equipItem(int equipItem) {
		if (ctx.equipment.populate().filter(equipItem).isEmpty()
				&& !ctx.inventory.populate().filter(equipItem).isEmpty()) {
			ctx.inventory.populate().filter(equipItem).next().interact(454); // equip axe
			ctx.onCondition(() -> !ctx.equipment.populate().filter(equipItem).isEmpty(), 250, 4);
		}
	}

	// What do I do when script closes
	@Override
	public void onTerminate() {

	}

	// What do I need to paint
	@Override
	public void onPaint(Graphics2D g1) {
		int woodXp = (int) (ctx.skills.getExperience(SimpleSkills.Skill.WOODCUTTING) - startExp);
		Graphics2D g = (Graphics2D) g1;
		g.drawImage(img1, 7, 345, null);
		g.setFont(font1);
		g.setColor(color1);
		g.drawString("V-WOODCUTTER", 108, 362);
		g.setFont(font2);
		g.drawString("TIME: " + ctx.paint.formatTime(System.currentTimeMillis() - startTime), 132, 380);
		g.drawString("STATUS: " + status, 133, 395);
		g.drawString(
				"CHOPPED LOGS: " + (gainedLogs + tempLogs) + " (PER HOUR: "
						+ ctx.paint.formatValue(ctx.paint.valuePerHour(gainedLogs + tempLogs, startTime)) + ")",
				230, 380);
		g.drawString("XP/PH: " + formatValue(ctx.paint.valuePerHour(woodXp, startTime)), 128, 410);
		g.drawString("XP GAINED: " + formatValue(woodXp), 110, 425);
	}

	// Configuring amount of XP to 2 decimals
	public final String formatValue(final long l) {
		return (l > 1_000_000) ? String.format("%.2fM", ((double) l / 1_000_000))
				: (l > 1000) ? String.format("%.1fK", ((double) l / 1000)) : l + "";
	}
}
