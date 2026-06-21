package org.afterlike.openutils.feature.impl.hypixel;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.event.impl.ReceiveChatEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.client.TextUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Range;
import re.tsuku.confikure.annotations.Text;
import re.tsuku.fastbus.Subscribe;

public class AutoGGFeature extends ToggleableFeature {
	@Option(name = "Enable Auto GG",
			description = "Send your GG message automatically when a Hypixel game ends.", order = 0)
	public boolean enabled;
	@Option(name = "Send delay",
			description = "Delay before sending the GG message after a game ends.", order = 1)
	@Range(min = 0.0D, max = 5000.0D, step = 100.0D)
	public int delay = 1000;
	@Option(name = "GG message", description = "Message sent when Auto GG triggers.", order = 2)
	@Text
	public String content = "gg";
	private static final List<Pattern> GAME_END_PATTERNS = new ArrayList<>();
	static {
		String[] regexes = new String[]{
				"^ +1st Killer - ?\\[?\\w*\\+*\\]? \\w+ - \\d+(?: Kills?)?$",
				"^ *1st (?:Place ?)?(?:-|:)? ?\\[?\\w*\\+*\\]? \\w+(?: : \\d+| - \\d+(?: Points?)?| - \\d+(?: x .)?| \\(\\w+ .{1,6}\\) - \\d+ Kills?|: \\d+:\\d+| - \\d+ (?:Zombie )?(?:Kills?|Blocks? Destroyed)| - \\[LINK\\])?$",
				"^ +Winn(?:er #1 \\(\\d+ Kills\\): \\w+ \\(\\w+\\)|er(?::| - )(?:Hiders|Seekers|Defenders|Attackers|PLAYERS?|MURDERERS?|Red|Blue|RED|BLU|\\w+)(?: Team)?|ers?: ?\\[?\\w*\\+*\\]? \\w+(?:, ?\\[?\\w*\\+*\\]? \\w+)?|ing Team ?[\\:-] (?:Animals|Hunters|Red|Green|Blue|Yellow|RED|BLU|Survivors|Vampires))$",
				"^ +Alpha Infected: \\w+ \\(\\d+ infections?\\)$",
				"^ +Murderer: \\w+ \\(\\d+ Kills?\\)$", "^ +You survived \\d+ rounds!$",
				"^ +(?:UHC|SkyWars|Bridge|Sumo|Classic|OP|MegaWalls|Bow|NoDebuff|Blitz|Combo|Bow Spleef|Boxing|Hypixel) (?:Duel|Doubles|Teams|Deathmatch|2v2v2v2|3v3v3v3|Parkour)? ?- \\d+:\\d+$",
				"^ +They captured all wools!$", "^ +Game over!$",
				"^ +[\\d\\.]+k?/[\\d\\.]+k? \\w+$", "^ +(?:Criminal|Cop)s won the game!$",
				"^ +\\[?\\w*\\+*\\]? \\w+ - \\d+ Final Kills$",
				"^ +Zombies - \\d*:?\\d+:\\d+ \\(Round \\d+\\)$", "^ +. YOUR STATISTICS .$",
				"^ {35,36}Winner(s?)$", "^ {21}Bridge CTF [a-zA-Z]+ - \\d\\d:\\d\\d$",
				"^ {35}GAME OVER!$", "^ {32}Party Games$", "^ +\\w+ won the game!$",
				"^ +GAME OVER!$", "^ +Player of the match: \\w+ \\(\\d+ Goals\\)$",
				"^\\s*?.+ WINNER!  .+$", "^\\s*?.+ Bridge [a-zA-Z0-9]* - .+$",
				"^ {6}#1 (?:\\[.+] )?(?:.{1,16}) \\(\\d+:\\d+:\\d+\\)$"};
		for (String r : regexes) {
			GAME_END_PATTERNS.add(Pattern.compile(r));
		}
	}
	private int ticksRemaining = -1;
	public AutoGGFeature() {
		super("Auto GG", FeatureCategory.HYPIXEL);
	}

	@Subscribe
	private void onChat(final ReceiveChatEvent event) {
		String msg = event.getMessage();
		String stripped = TextUtil.stripColorCodes(msg);
		for (Pattern p : GAME_END_PATTERNS) {
			if (p.matcher(stripped).matches()) {
				handleGameEnd();
				return;
			}
		}
	}

	@Subscribe
	public void onTick(GameTickEvent event) {
		if (event.getPhase() != EventPhase.POST)
			return;
		if (ticksRemaining < 0)
			return;
		if (--ticksRemaining <= 0) {
			ClientUtil.sendMessageAsPlayer("/ac " + content);
			ticksRemaining = -1;
		}
	}

	private void handleGameEnd() {
		if (ticksRemaining >= 0)
			return;
		ticksRemaining = msToTicks(delay);
	}

	private static int msToTicks(int ms) {
		return Math.max(1, ms / 50);
	}
}
