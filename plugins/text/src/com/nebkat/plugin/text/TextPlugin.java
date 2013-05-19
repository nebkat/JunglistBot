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

package com.nebkat.plugin.text;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.BinaryCodec;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import com.nebkat.junglist.bot.command.Command;
import com.nebkat.junglist.bot.command.CommandEvent;
import com.nebkat.junglist.bot.command.CommandFilter;
import com.nebkat.junglist.bot.command.UserLevel;
import com.nebkat.junglist.bot.plugin.Plugin;
import com.nebkat.junglist.irc.Irc;
import com.nebkat.junglist.irc.events.EventHandler;
import com.nebkat.junglist.irc.utils.Utils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class TextPlugin extends Plugin {
    private static final Map<String, String> UPSIDE_DOWN_MAP = new HashMap<String, String>() {
        {
            // Letters (lower case)
            put("a", "ɐ");
            put("b", "q");
            put("c", "ɔ");
            put("d", "p");
            put("e", "ǝ");
            put("f", "ɟ");
            put("g", "ƃ");
            put("h", "ɥ");
            put("i", "ı");
            put("j", "ɾ");
            put("k", "ʞ");
            put("l", "l");
            put("m", "ɯ");
            put("n", "u");
            put("o", "o");
            put("p", "d");
            put("q", "b");
            put("r", "ɹ");
            put("s", "s");
            put("t", "ʇ");
            put("u", "n");
            put("v", "ʌ");
            put("w", "ʍ");
            put("x", "x");
            put("y", "ʎ");
            put("z", "z");

            // Letters (upper case)
            put("A", "∀");
            put("B", "B");
            put("C", "Ɔ");
            put("D", "ᗡ");
            put("E", "Ǝ");
            put("F", "Ⅎ");
            put("G", "⅁");
            put("H", "H");
            put("I", "I");
            put("J", "ſ");
            put("K", "⋊");
            put("L", "˥");
            put("M", "W");
            put("N", "N");
            put("O", "O");
            put("P", "Ԁ");
            put("Q", "Ό");
            put("R", "ᴚ");
            put("S", "S");
            put("T", "⊥");
            put("U", "∩");
            put("V", "Λ");
            put("W", "M");
            put("X", "X");
            put("Y", "⅄");
            put("Z", "Z");

            // Numbers
            put("0", "0");
            put("1", "Ɩ");
            put("2", "\u1105");
            put("3", "Ɛ");
            put("4", "ㄣ");
            put("5", "ގ");
            put("6", "9");
            put("7", "ㄥ");
            put("8", "8");
            put("9", "6");

            // Symbols
            put(",", "'");
            put(".", "˙");
            put("?", "¿");
            put("¿", "?");
            put("\"", "„");
            put("'", ",");
            put("`", ",");
            put(";", "؛");
            put("(", ")");
            put(")", "(");
            put("[", "]");
            put("]", "[");
            put("{", "}");
            put("}", "{");
            put("<", ">");
            put(">", "<");
            put("&", "⅋");
            put("!", "¡");
            put("_", "‾");
        }
    };
    private static final Map<String, String> BUBBLE_MAP = new HashMap<String, String>() {
        {
            // Letters (lower case)
            put("a", "ⓐ");
            put("b", "ⓑ");
            put("c", "ⓒ");
            put("d", "ⓓ");
            put("e", "ⓔ");
            put("f", "ⓕ");
            put("g", "ⓖ");
            put("h", "ⓗ");
            put("i", "ⓘ");
            put("j", "ⓙ");
            put("k", "ⓚ");
            put("l", "ⓛ");
            put("m", "ⓜ");
            put("n", "ⓝ");
            put("o", "ⓞ");
            put("p", "ⓟ");
            put("q", "ⓠ");
            put("r", "ⓡ");
            put("s", "ⓢ");
            put("t", "ⓣ");
            put("u", "ⓤ");
            put("v", "ⓥ");
            put("w", "ⓦ");
            put("x", "ⓧ");
            put("y", "ⓨ");
            put("z", "ⓩ");

            // Letters (upper case)
            put("A", "Ⓐ");
            put("B", "Ⓑ");
            put("C", "Ⓒ");
            put("D", "Ⓓ");
            put("E", "Ⓔ");
            put("F", "Ⓕ");
            put("G", "Ⓖ");
            put("H", "Ⓗ");
            put("I", "Ⓘ");
            put("J", "Ⓙ");
            put("K", "Ⓚ");
            put("L", "Ⓛ");
            put("M", "Ⓜ");
            put("N", "Ⓝ");
            put("O", "Ⓞ");
            put("P", "Ⓟ");
            put("Q", "Ⓠ");
            put("R", "Ⓡ");
            put("S", "Ⓢ");
            put("T", "Ⓣ");
            put("U", "Ⓤ");
            put("V", "Ⓥ");
            put("W", "Ⓦ");
            put("X", "Ⓧ");
            put("Y", "Ⓨ");
            put("Z", "Ⓩ");
        }
    };

    @EventHandler
    @CommandFilter("text")
    public void onCommand(CommandEvent e) {
        if (e.getParams().length < 2) {
            e.showUsage(getBot());
            return;
        }
        String action = e.getParams()[0];
        String text = e.getRawParams().substring(action.length() + 1);
        String result = "Algorithm unsupported";
        if (action.equalsIgnoreCase("invert")) {
            result = new StringBuilder(text).reverse().toString();
        } else if (action.equalsIgnoreCase("wordinvert")) {
            String[] words = text.split(" ");
            for (int i = 0; i < words.length; i++) {
                words[i] = new StringBuilder(words[i]).reverse().toString();
            }
            result = Utils.implode(words, " ");
        } else if (action.equalsIgnoreCase("upsidedown")) {
            StringBuilder upside = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                String character = text.substring(i, i + 1);
                character = UPSIDE_DOWN_MAP.getOrDefault(character, character);
                upside.insert(0, character);
            }
            result = upside.toString();
        } else if (action.equalsIgnoreCase("bubble")) {
            StringBuilder bubble = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                String character = text.substring(i, i + 1);
                character = BUBBLE_MAP.getOrDefault(character, character);
                bubble.append(character);
            }
            result = bubble.toString();
        } else if (action.equals("uppercase") || action.equals("upper")) {
            result = text.toUpperCase();
        } else if (action.equals("lowercase") || action.equals("lower")) {
            result = text.toLowerCase();
        } else if (action.equals("randcase")) {
            StringBuilder randcase = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                randcase.append(Math.random() < 0.5 ? text.substring(i, i + 1).toLowerCase() : text.substring(i, i + 1).toUpperCase());
            }
            result = randcase.toString();
        } else if (action.equalsIgnoreCase("switchcase")) {
            StringBuilder switchcase = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                switchcase.append(Character.isUpperCase(text.charAt(i)) ? text.substring(i, i + 1).toLowerCase() : text.substring(i, i + 1).toUpperCase());
            }
            result = switchcase.toString();
        } else if (action.equalsIgnoreCase("wordcapitalise")) {
            String[] words = text.split(" ");
            for (int i = 0; i < words.length; i++) {
                if (words[i].length() > 0) {
                    words[i] = words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase();
                }
            }
            result = Utils.implode(words, " ");
            Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + Utils.implode(words, " "));
        }
        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + result);
    }

    @EventHandler
    @CommandFilter("encode")
    public void onEncodeCommand(CommandEvent e) {
        if (e.getParams().length < 2) {
            e.showUsage(getBot());
            return;
        }
        String algorithm = e.getParams()[0];
        String text = e.getRawParams().substring(algorithm.length() + 1);
        boolean decode = e.getCommandString().equals("decode");
        byte[] bytes = text.getBytes(Charset.defaultCharset());
        String result = "Algorithm unsupported";
        if (algorithm.equalsIgnoreCase("base64")) {
            result = !decode ? Base64.encodeBase64String(bytes) : new String(Base64.decodeBase64(text), Charset.defaultCharset());
        } else if (algorithm.equals("hex")) {
            if (!decode) {
                result = Hex.encodeHexString(bytes);
            } else {
                try {
                    result = new String(Hex.decodeHex(text.toCharArray()), Charset.defaultCharset());
                } catch (DecoderException de) {
                    result = "Invalid hex input";
                }
            }
        } else if (algorithm.equalsIgnoreCase("binary")) {
            result = !decode ? BinaryCodec.toAsciiString(bytes) : new String(BinaryCodec.fromAscii(text.toCharArray()), Charset.defaultCharset());
        } else if (algorithm.equalsIgnoreCase("morse")) {
            result = !decode ? Morse.stringToMorse(text) : Morse.stringFromMorse(text);
        } else if (algorithm.equalsIgnoreCase("vigenere")) {
            String key = e.getParams()[1];
            result = !decode ? Vigenere.encrypt(e.getRawParamsAfter(2), key) : Vigenere.decrypt(e.getRawParamsAfter(2), key);
        } else if (algorithm.equalsIgnoreCase("rot")) {
            try {
                result = Rot.encrypt(e.getRawParamsAfter(2).toUpperCase(), (!decode ? 1 : -1) * Integer.parseInt(e.getParams()[1]));
            } catch (NumberFormatException nfe) {
                result = "Invalid rot input";

            }
        }
        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + result);
    }

    @EventHandler
    @CommandFilter("hash")
    public void onHashCommand(CommandEvent e) {
        if (e.getParams().length < 2) {
            e.showUsage(getBot());
            return;
        }
        String algorithm = e.getParams()[0];
        String text = e.getRawParams().substring(algorithm.length() + 1);
        String result = "Algorithm unsupported";
        if (algorithm.equalsIgnoreCase("md5")) {
            result = DigestUtils.md5Hex(text);
        } else if (algorithm.equalsIgnoreCase("md2")) {
            result = DigestUtils.md2Hex(text);
        } else if (algorithm.equalsIgnoreCase("sha1") || algorithm.equalsIgnoreCase("sha")) {
            result = DigestUtils.sha1Hex(text);
        } else if (algorithm.equalsIgnoreCase("sha256")) {
            result = DigestUtils.sha256Hex(text);
        } else if (algorithm.equalsIgnoreCase("sha384")) {
            result = DigestUtils.sha384Hex(text);
        } else if (algorithm.equalsIgnoreCase("sha512")) {
            result = DigestUtils.sha512Hex(text);
        }
        Irc.message(e.getSession(), e.getTarget(), e.getSource().getNick() + ": " + result);
    }

    @Override
    public void onCreate() {
        getBot().getCommandManager().registerCommand(new Command("text", this, "Text utilities", "invert/wordinvert/upsidedown/bubble/uppercase/lowercase/randcase/switchcase/wordcapitalise <text>", UserLevel.NORMAL, false));
        getBot().getCommandManager().registerCommand(new Command("hash", this, "Hash utility", "<algo> <params>", UserLevel.NORMAL, false));
        getBot().getCommandManager().registerCommand(new Command("encode", this, "Encoding utility", "<algo> <params>", UserLevel.NORMAL, false, "decode"));
    }
}
