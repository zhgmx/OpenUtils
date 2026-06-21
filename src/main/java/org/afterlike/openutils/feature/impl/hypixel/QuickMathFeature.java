package org.afterlike.openutils.feature.impl.hypixel;

import org.afterlike.openutils.event.api.EventPhase;
import org.afterlike.openutils.event.impl.GameTickEvent;
import org.afterlike.openutils.event.impl.ReceiveChatEvent;
import org.afterlike.openutils.feature.api.FeatureCategory;
import org.afterlike.openutils.feature.api.ToggleableFeature;
import org.afterlike.openutils.util.client.ClientUtil;
import org.afterlike.openutils.util.client.TextUtil;
import org.afterlike.openutils.util.game.GameModeUtil;
import re.tsuku.confikure.annotations.Option;
import re.tsuku.confikure.annotations.Range;
import re.tsuku.fastbus.Subscribe;

public class QuickMathFeature extends ToggleableFeature {
	@Option(name = "Enable Quick Math",
			description = "Solve Hypixel quick-math chat prompts and queue the answer.", order = 0)
	public boolean enabled;
	@Option(name = "Answer delay", description = "Delay before sending the solved answer to chat.",
			order = 1)
	@Range(min = 0.0D, max = 5000.0D, step = 100.0D)
	public int delay = 1500;
	private String pendingAnswer = "";
	private long sendTime = 0;
	public QuickMathFeature() {
		super("Quick Math", FeatureCategory.HYPIXEL);
	}

	@Subscribe
	private void onChatReceived(final ReceiveChatEvent event) {
		if (!ClientUtil.notNull())
			return;
		if (!GameModeUtil.onHypixel())
			return;
		final String message = TextUtil.stripColorCodes(event.getMessage());
		if (!message.startsWith("QUICK MATHS! Solve: "))
			return;
		final String expression = message.split(": ", 2)[1];
		pendingAnswer = solve(expression);
		sendTime = System.currentTimeMillis() + delay;
	}

	@Subscribe
	private void onTick(final GameTickEvent event) {
		if (event.getPhase() != EventPhase.POST)
			return;
		if (!ClientUtil.notNull())
			return;
		if (pendingAnswer.isEmpty() || sendTime == 0)
			return;
		if (System.currentTimeMillis() < sendTime)
			return;
		ClientUtil.sendMessageAsPlayer(pendingAnswer);
		pendingAnswer = "";
		sendTime = 0;
	}

	private String solve(String expression) {
		expression = expression.replace("x", "*");
		return formatAndRound(parse(expression));
	}

	private double parse(String expr) {
		final char[] chars = expr.toCharArray();
		final int[] pos = {-1};
		final int[] ch = {nextChar(chars, pos)};
		final double result = parseExpression(chars, pos, ch);
		if (pos[0] < chars.length) {
			return 0;
		}
		return result;
	}

	private double parseExpression(char[] chars, int[] pos, int[] ch) {
		double x = parseTerm(chars, pos, ch);
		while (true) {
			if (eat(chars, pos, ch, '+')) {
				x += parseTerm(chars, pos, ch);
			} else if (eat(chars, pos, ch, '-')) {
				x -= parseTerm(chars, pos, ch);
			} else {
				return x;
			}
		}
	}

	private double parseTerm(char[] chars, int[] pos, int[] ch) {
		double x = parseFactor(chars, pos, ch);
		while (true) {
			if (eat(chars, pos, ch, '*')) {
				x *= parseFactor(chars, pos, ch);
			} else if (eat(chars, pos, ch, '/')) {
				x /= parseFactor(chars, pos, ch);
			} else {
				return x;
			}
		}
	}

	private double parseFactor(char[] chars, int[] pos, int[] ch) {
		if (eat(chars, pos, ch, '+'))
			return parseFactor(chars, pos, ch);
		if (eat(chars, pos, ch, '-'))
			return -parseFactor(chars, pos, ch);
		double x;
		final int startPos = pos[0];
		if (eat(chars, pos, ch, '(')) {
			x = parseExpression(chars, pos, ch);
			eat(chars, pos, ch, ')');
		} else if ((ch[0] >= '0' && ch[0] <= '9') || ch[0] == '.') {
			while ((ch[0] >= '0' && ch[0] <= '9') || ch[0] == '.') {
				ch[0] = nextChar(chars, pos);
			}
			x = Double.parseDouble(new String(chars, startPos, pos[0] - startPos));
		} else {
			return 0;
		}
		if (eat(chars, pos, ch, '^'))
			x = Math.pow(x, parseFactor(chars, pos, ch));
		return x;
	}

	private boolean eat(char[] chars, int[] pos, int[] ch, int charToEat) {
		while (ch[0] == ' ')
			ch[0] = nextChar(chars, pos);
		if (ch[0] == charToEat) {
			ch[0] = nextChar(chars, pos);
			return true;
		}
		return false;
	}

	private int nextChar(char[] chars, int[] pos) {
		pos[0]++;
		return (pos[0] < chars.length) ? chars[pos[0]] : -1;
	}

	private String formatAndRound(double num) {
		final double roundedVal = Math.round(num * 100.0) / 100.0;
		return (roundedVal % 1 == 0)
				? String.valueOf((int) roundedVal)
				: String.valueOf(roundedVal);
	}
}
