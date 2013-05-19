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

package com.nebkat.plugin.money;

import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.Log;
import com.nebkat.junglist.irc.events.EventHandler;
import org.openexchangerates.Currency;
import org.openexchangerates.OpenExchangeRatesClient;
import org.openexchangerates.exceptions.UnavailableExchangeRateException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;

public class MoneyPlugin extends Plugin<MoneyPlugin.Config> {
    public static final String TAG = "MoneyPlugin";

    @EventHandler
    @CommandFilter("money")
    public void onMoneyCommand(CommandEvent e) {
        if (e.getParams().length < 3) {
            e.showUsage(getBot());
            return;
        }
        float amount;
        try {
            amount = Float.parseFloat(e.getParams()[0]);
        } catch (NumberFormatException nfe) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Exchange currency amount must be a valid number.");
            return;
        }

        String fromCode = e.getParams()[1];
        String toCode = e.getParams()[2];
        if (fromCode.length() != 3 || toCode.length() != 3) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": From/to currencies must be written in 3 character currency code.");
            return;
        }

        Currency from;
        Currency to;
        try {
            from = Currency.valueOf(fromCode.toUpperCase());
            to = Currency.valueOf(toCode.toUpperCase());
        } catch (IllegalArgumentException iae) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Unknown currencies.");
            return;
        }

        OpenExchangeRatesClient ratesClient = new OpenExchangeRatesClient(mConfig.key);
        Map<Currency, BigDecimal> rates;
        try {
            rates = ratesClient.getLatest();
        } catch (UnavailableExchangeRateException ex) {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error while fetching exchange rates.");
            Log.e(TAG, "Could not fetch exchange rates", ex);
            return;
        }
        if (rates.containsKey(from) && rates.containsKey(to)) {
            DecimalFormat df = new DecimalFormat("0.00");
            float result = amount / rates.get(from).floatValue() * rates.get(to).floatValue();
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + df.format(amount) + " " + from.name() + " = " + df.format(result) + " " + to.name());
        } else {
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": Error while fetching exchange rates.");
        }
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("money", this, "Gives operator privileges to a user", "<amount> <from> <to>", UserLevel.NORMAL, false));
        saveConfig();
    }

    public class Config {
        public String key;
    }
}