/*
 * Copyright 2013 Nebojsa Cvetkovic. All rights reserved.
 *
 * This file is part of JunglistIRC.
 *
 * JunglistIRC is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JunglistIRC is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JunglistIRC.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.nebkat.plugin.bomb;

import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Channel;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.Session;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.events.irc.IRCEvent;
import com.nebkat.junglist.irc.events.irc.MessageEvent;
import com.nebkat.junglist.irc.events.irc.NickEvent;
import com.nebkat.junglist.irc.events.irc.PartEvent;
import com.nebkat.junglist.irc.events.irc.QuitEvent;
import com.nebkat.junglist.irc.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

public class BombPlugin extends Plugin {

    private enum BombColor {
        RED, GREEN, BLUE, YELLOW, ORANGE, PURPLE, BROWN, WHITE, BLACK, GREY, AQUA, AZURE, BEIGE, CHOCOLATE,
        CRIMSON, CYAN, FUCHSIA, GOLD, INDIGO, IVORY, KHAKI, LAVENDER, LIME, LINEN, MAGENTA, MAROON, NAVY,
        OLIVE, ORCHID, PERU, PINK, PLUM, SALMON, SIENNA, SILVER, SNOW, TAN, TEAL, TOMATO, TURQUOISE,
        VIOLET, WHEAT
    }

    private final static String ADMIN_CORRECT_ANSWER = "42";

    private final static List<BombColor> BOMB_LEVEL_EASY = new ArrayList<>();
    private final static List<BombColor> BOMB_LEVEL_MEDIUM = new ArrayList<>();
    private final static List<BombColor> BOMB_LEVEL_HARD = new ArrayList<>();
    private final static List<BombColor> BOMB_LEVEL_DEATH = new ArrayList<>();

    static {
        BOMB_LEVEL_EASY.add(BombColor.RED);
        BOMB_LEVEL_EASY.add(BombColor.GREEN);
        BOMB_LEVEL_EASY.add(BombColor.BLUE);

        BOMB_LEVEL_MEDIUM.add(BombColor.RED);
        BOMB_LEVEL_MEDIUM.add(BombColor.GREEN);
        BOMB_LEVEL_MEDIUM.add(BombColor.BLUE);
        BOMB_LEVEL_MEDIUM.add(BombColor.YELLOW);
        BOMB_LEVEL_MEDIUM.add(BombColor.PURPLE);

        BOMB_LEVEL_HARD.add(BombColor.RED);
        BOMB_LEVEL_HARD.add(BombColor.GREEN);
        BOMB_LEVEL_HARD.add(BombColor.BLUE);
        BOMB_LEVEL_HARD.add(BombColor.YELLOW);
        BOMB_LEVEL_HARD.add(BombColor.ORANGE);
        BOMB_LEVEL_HARD.add(BombColor.PURPLE);
        BOMB_LEVEL_HARD.add(BombColor.BROWN);
        BOMB_LEVEL_HARD.add(BombColor.WHITE);
        BOMB_LEVEL_HARD.add(BombColor.BLACK);
        BOMB_LEVEL_HARD.add(BombColor.GREY);

        BOMB_LEVEL_DEATH.add(BombColor.RED);
        BOMB_LEVEL_DEATH.add(BombColor.GREEN);
        BOMB_LEVEL_DEATH.add(BombColor.BLUE);
        BOMB_LEVEL_DEATH.add(BombColor.YELLOW);
        BOMB_LEVEL_DEATH.add(BombColor.ORANGE);
        BOMB_LEVEL_DEATH.add(BombColor.PURPLE);
        BOMB_LEVEL_DEATH.add(BombColor.BROWN);
        BOMB_LEVEL_DEATH.add(BombColor.WHITE);
        BOMB_LEVEL_DEATH.add(BombColor.BLACK);
        BOMB_LEVEL_DEATH.add(BombColor.GREY);
        BOMB_LEVEL_DEATH.add(BombColor.AQUA);
        BOMB_LEVEL_DEATH.add(BombColor.AZURE);
        BOMB_LEVEL_DEATH.add(BombColor.BEIGE);
        BOMB_LEVEL_DEATH.add(BombColor.CHOCOLATE);
        BOMB_LEVEL_DEATH.add(BombColor.CRIMSON);
        BOMB_LEVEL_DEATH.add(BombColor.CYAN);
        BOMB_LEVEL_DEATH.add(BombColor.FUCHSIA);
        BOMB_LEVEL_DEATH.add(BombColor.GOLD);
        BOMB_LEVEL_DEATH.add(BombColor.INDIGO);
        BOMB_LEVEL_DEATH.add(BombColor.IVORY);
        BOMB_LEVEL_DEATH.add(BombColor.KHAKI);
        BOMB_LEVEL_DEATH.add(BombColor.LAVENDER);
        BOMB_LEVEL_DEATH.add(BombColor.LIME);
        BOMB_LEVEL_DEATH.add(BombColor.LINEN);
        BOMB_LEVEL_DEATH.add(BombColor.MAGENTA);
        BOMB_LEVEL_DEATH.add(BombColor.MAROON);
        BOMB_LEVEL_DEATH.add(BombColor.NAVY);
        BOMB_LEVEL_DEATH.add(BombColor.OLIVE);
        BOMB_LEVEL_DEATH.add(BombColor.ORCHID);
        BOMB_LEVEL_DEATH.add(BombColor.PERU);
        BOMB_LEVEL_DEATH.add(BombColor.PINK);
        BOMB_LEVEL_DEATH.add(BombColor.PLUM);
        BOMB_LEVEL_DEATH.add(BombColor.SALMON);
        BOMB_LEVEL_DEATH.add(BombColor.SIENNA);
        BOMB_LEVEL_DEATH.add(BombColor.SILVER);
        BOMB_LEVEL_DEATH.add(BombColor.SNOW);
        BOMB_LEVEL_DEATH.add(BombColor.TAN);
        BOMB_LEVEL_DEATH.add(BombColor.TEAL);
        BOMB_LEVEL_DEATH.add(BombColor.TOMATO);
        BOMB_LEVEL_DEATH.add(BombColor.TURQUOISE);
        BOMB_LEVEL_DEATH.add(BombColor.VIOLET);
        BOMB_LEVEL_DEATH.add(BombColor.WHEAT);

    }

    private boolean mBombActive;
    private Session mBombSession;
    private Channel mBombTarget;
    private String mBombNick;
    private boolean mBombIsNuclear;
    private BombColor mBombColor;

    private Random mRandom;
    private Timer mTimer;
    private TickTimerTask mTickTimerTask = new TickTimerTask();
    private List<TimerTask> mPendingTimerTasks = new ArrayList<>();

    private int mTimeout;

    @EventHandler
    @CommandFilter("bomb")
    public void onBombCommand(CommandEvent e) {
        if (e.getParams().length < 1) {
            e.showUsage(getBot());
            return;
        }
        if (!(e.getTarget() instanceof Channel)) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Bomb must be executed in a channel.");
            return;
        }
        if (mBombActive) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Bomb already in progress!");
            return;
        }
        List<BombColor> bombLevel = BOMB_LEVEL_EASY;
        String level = e.getParamOrDefault(1, null);
        if (level != null) {
            if (level.equalsIgnoreCase("easy") || level.equals("1")) {
                bombLevel = BOMB_LEVEL_EASY;
            } else if (level.equalsIgnoreCase("medium") || level.equals("2")) {
                bombLevel = BOMB_LEVEL_MEDIUM;
            } else if (level.equalsIgnoreCase("hard") || level.equals("3")) {
                bombLevel = BOMB_LEVEL_HARD;
            } else if (level.equalsIgnoreCase("death") || level.equals("9")) {
                bombLevel = BOMB_LEVEL_DEATH;
            }
        }
        int random = mRandom.nextInt(bombLevel.size());
        mBombColor = bombLevel.get(random);
        mBombNick = e.getParams()[0];
        mBombSession = e.getSession();
        mBombTarget = (Channel) e.getTarget();
        mBombIsNuclear = e.getCommand().getName().equals("nuclearbomb");
        String wiresString = bombLevel.stream()
                .map((color) -> color.name().toUpperCase())
                .collect(Collectors.toStringJoiner(", ")).toString();
        Irc.message(mBombSession, mBombTarget, mBombNick + ": You have been challenged! Choose which wire to cut (" + wiresString + ") before time runs out!");
        mTimeout = 10;
        mTickTimerTask = new TickTimerTask();
        mTimer.schedule(mTickTimerTask, 1000, 1000);
    }

    @EventHandler
    public void onMessage(MessageEvent e) {
        if (!mBombActive) {
            return;
        }
        if (mBombNick.equalsIgnoreCase(e.getSource().getNick()) &&
                mBombTarget.getName().equalsIgnoreCase(e.getTarget().getName()) &&
                mBombSession.equals(e.getSession())) {
            String message = e.getMessage().trim();
            boolean isAdmin = getBot().getCommandManager().getLevelForHost(e.getSource().getRaw()).getLevel() >= UserLevel.ADMIN.getLevel();
            if (message.equalsIgnoreCase(mBombColor.name()) || (isAdmin && message.equalsIgnoreCase(ADMIN_CORRECT_ANSWER))) {
                Irc.message(mBombSession, mBombTarget, mBombNick + ": Correct wire! Bomb disarmed.");
            } else {
                if (mBombIsNuclear) {
                    Irc.ban(mBombSession, mBombTarget, mBombNick);
                }
                Irc.kick(mBombSession, mBombTarget, mBombNick, "Wrong wire! You failed to disarm the bomb! Correct wire was " + mBombColor.name().toLowerCase() + ".");
            }
            mTickTimerTask.cancel();
            mBombActive = false;
        }
    }

    @EventHandler
    public void onNick(NickEvent e) {
        if (!mBombActive) {
            return;
        }
        if (mBombNick.equals(e.getSource().getNick()) &&
                mBombSession.equals(e.getSession())) {
            mBombNick = e.getNick();
            Irc.message(mBombSession, mBombTarget, mBombNick + ": You can run but you can't hide!");
        }
    }

    @EventHandler
    public void onPart(PartEvent e) {
        if (e.getTarget().equals(mBombTarget)) {
            onLeave(e);
        }
    }

    @EventHandler
    public void onQuit(QuitEvent e) {
        onLeave(e);
    }

    private void onLeave(IRCEvent e) {
        if (!mBombActive) {
            return;
        }
        if (mBombSession.equals(e.getSession()) &&
                mBombNick.equals(e.getSource().getNick())) {
            final Session bombSession = mBombSession;
            final Channel bombTarget = mBombTarget;
            final String bombNick = mBombNick;
            if (!mBombIsNuclear) {
                Irc.message(mBombSession, mBombTarget, "Bitch too afraid to play the game. Banned for 30s. Correct wire was " + mBombColor.name().toLowerCase() + ".");
                Irc.ban(bombSession, bombTarget, bombNick);
                TimerTask unbanTimerTask = new TimerTask() {
                    @Override
                    public void run() {
                        Irc.unban(bombSession, bombTarget, bombNick);
                    }
                };
                mPendingTimerTasks.add(unbanTimerTask);
                mTimer.schedule(unbanTimerTask, 1000 * 30);
            } else {
                Irc.message(mBombSession, mBombTarget, "Nobody escapes the nuclear bomb. Nobody.");
                Irc.ban(bombSession, bombTarget, bombNick);
            }

            cancelBomb();
        }
    }

    private void onTick() {
        if (mTimeout > 0) {
            mBombActive = true;
            Irc.message(mBombSession, mBombTarget, mBombNick + ": " + mTimeout);
            mTimeout--;
        } else {
            if (mBombIsNuclear) {
                Irc.ban(mBombSession, mBombTarget, mBombNick);
            }
            Irc.kick(mBombSession, mBombTarget, mBombNick, "You failed to disarm the bomb! Correct wire was " + mBombColor.name().toLowerCase() + ".");
            mTickTimerTask.cancel();
            mBombActive = false;
        }
    }

    private void cancelBomb() {
        mBombActive = false;
        mBombColor = null;
        mBombIsNuclear = false;
        mBombNick = null;
        mBombSession = null;
        mBombTarget = null;
        mTickTimerTask.cancel();
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("bomb", this, "Bombs a user", "<user>", UserLevel.ADMIN, false));
        getBot().getCommandManager().registerCommand(new Command("nuclearbomb", this, "Bombs a user and bans if fail", "<user>", UserLevel.ADMIN, true));

        mTimer = new Timer();
        mRandom = new Random();
    }

    @Override
    public void onDisable() {
        cancelBomb();

        mTimer.cancel();
        mPendingTimerTasks.forEach(TimerTask::run);
        mPendingTimerTasks.clear();
    }

    private class TickTimerTask extends TimerTask {
        @Override
        public void run() {
            onTick();
        }
    }
}