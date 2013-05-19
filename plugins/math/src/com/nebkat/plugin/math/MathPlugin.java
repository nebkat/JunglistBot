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

package com.nebkat.plugin.math;

import net.astesana.javaluator.DoubleEvaluator;
import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.events.EventHandler;

import java.text.DecimalFormat;

public class MathPlugin extends Plugin {
    private DoubleEvaluator mDoubleEvaluator;
    private DecimalFormat mDecimalFormatter;

    @EventHandler
    @CommandFilter("math")
    public void onMathCommand(CommandEvent e) {
        if (e.getParams().length < 1) {
            e.showUsage(getBot());
            return;
        }
        Double result;
        try {
            result = mDoubleEvaluator.evaluate(e.getRawParams());
        } catch (IllegalArgumentException iae) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error evaluating expression.");
            return;
        }

        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + mDecimalFormatter.format(result));
    }

    @EventHandler
    @CommandFilter("random")
    public void onRandomCommand(CommandEvent e) {
        int max = 1;
        int min = 0;
        try {
            if (e.getParams().length >= 1) {
                max = Integer.parseInt(e.getParams()[0]);
                if (e.getParams().length >= 2) {
                    min = Integer.parseInt(e.getParams()[2]);
                }
            }
        } catch (NumberFormatException nfe) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": <max> and <min> must be valid integers");
            return;
        }

        int random = (int)(Math.random() * ((max - min) + 1)) + min;

        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + random);
    }

    @EventHandler
    @CommandFilter("quadratic")
    public void onCommand(CommandEvent e) {
        if (e.getParams().length < 3) {
            e.showUsage(getBot());
            return;
        }
        double a;
        double b;
        double c;
        try {
            a = Double.parseDouble(e.getParams()[0]);
            b = Double.parseDouble(e.getParams()[1]);
            c = Double.parseDouble(e.getParams()[2]);
        } catch (NumberFormatException nfe) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": <a> <b> <c> must be valid numbers");
            return;
        }

        boolean complex = false;
        double discriminant = b*b - 4.0 * a * c;
        if (discriminant < 0) {
            complex = true;
            discriminant = -discriminant;
        }
        double sqroot = Math.sqrt(discriminant);

        if (!complex) {
            double root1 = (-b + sqroot) / 2.0 * a;
            double root2 = (-b - sqroot) / 2.0 * a;
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": x=" + mDecimalFormatter.format(root1) + ", x=" + mDecimalFormatter.format(root2));
        } else {
            double root = sqroot / 2.0 * a;
            double constant = -b / 2.0 * a;
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": x=" + mDecimalFormatter.format(constant) + "+" + mDecimalFormatter.format(root) + "i, x=" + mDecimalFormatter.format(constant) + "-" + mDecimalFormatter.format(root) + "i");
        }
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("math", this, "Evaluates math expression", "<math>", UserLevel.NORMAL, false, "calc", "calculator"));
        getBot().getCommandManager().registerCommand(new Command("random", this, "Returns a random integer", "[<max>] [<min>]", UserLevel.NORMAL, true, "rand"));
        getBot().getCommandManager().registerCommand(new Command("quadratic", this, "Solves a quadratic where ax^2+bx+c=0", "<a> <b> <c>", UserLevel.NORMAL, true));

        mDoubleEvaluator = new DoubleEvaluator();
        mDecimalFormatter = new DecimalFormat();
        mDecimalFormatter.setMaximumFractionDigits(340);
        mDecimalFormatter.setMaximumIntegerDigits(309);
    }
}