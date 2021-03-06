/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2004-11 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package processing.app;

import java.awt.*;
import java.io.*;
import java.util.*;

import processing.core.*;


/**
 * Storage class for theme settings. This was separated from the Preferences
 * class for 1.0 so that the coloring wouldn't conflict with previous releases
 * and to make way for future ability to customize.
 */
public class Settings {
  /** 
   * Copy of the defaults in case the user mangles a preference. 
   * It's necessary to keep a copy of the defaults around, because the user may
   * have mangled a setting on their own. In the past, we used to load the 
   * defaults, then replace those with what was in the user's preferences file.
   * Problem is, if something like a font entry in the user's file no longer 
   * parses properly, we need to be able to get back to a clean version of that
   * setting so we can recover.
   */
  HashMap<String,String> defaults;
  
  /** Table of attributes/values. */
  HashMap<String,String> table = new HashMap<String,String>();;
  
  /** Associated file for this settings data. */
  File file;


  public Settings(File file) throws IOException {
    this.file = file;
    
    if (file.exists()) {
      load();
    }
    
    // clone the hash table
    defaults = (HashMap<String,String>) table.clone();
  }


  public void load() {
    String[] lines = PApplet.loadStrings(file);
    for (String line : lines) {
      if ((line.length() == 0) ||
          (line.charAt(0) == '#')) continue;

      // this won't properly handle = signs being in the text
      int equals = line.indexOf('=');
      if (equals != -1) {
        String key = line.substring(0, equals).trim();
        String value = line.substring(equals + 1).trim();
        table.put(key, value);
      }
    }

    // check for platform-specific properties in the defaults
    String platformExt = "." + Base.getPlatformName();
    int platformExtLength = platformExt.length();
    for (String key : table.keySet()) {
      if (key.endsWith(platformExt)) {
        // this is a key specific to a particular platform
        String actualKey = key.substring(0, key.length() - platformExtLength);
        String value = get(key);
        table.put(actualKey, value);
      }
    }
  }


  public void save() {
    PrintWriter writer = PApplet.createWriter(file);

    for (String key : table.keySet()) {
      writer.println(key + "=" + table.get(key));
    }

    writer.flush();
    writer.close();
  }


  public String get(String attribute) {
    return table.get(attribute);
  }


  public String getDefault(String attribute) {
    return defaults.get(attribute);
  }


  public void set(String attribute, String value) {
    table.put(attribute, value);
  }


  public boolean getBoolean(String attribute) {
    String value = get(attribute);
    if (value == null) {
      System.err.println("Boolean not found: " + attribute);
      return false;
    }
    return Boolean.parseBoolean(value);
  }


  public void setBoolean(String attribute, boolean value) {
    set(attribute, value ? "true" : "false");
  }


  public int getInteger(String attribute) {
    String value = get(attribute);
    if (value == null) {
      System.err.println("Integer not found: " + attribute);
      return 0;
    }
    return Integer.parseInt(value);
  }


  public void setInteger(String key, int value) {
    set(key, String.valueOf(value));
  }


  public Color getColor(String attribute) {
    Color parsed = null;
    String s = get(attribute);
    if ((s != null) && (s.indexOf("#") == 0)) {
      try {
        int v = Integer.parseInt(s.substring(1), 16);
        parsed = new Color(v);
      } catch (Exception e) {
      }
    }
    return parsed;
  }


  public void setColor(String attr, Color what) {
    set(attr, "#" + PApplet.hex(what.getRGB() & 0xffffff, 6));
  }


  public Font getFont(String attr) {
    boolean replace = false;
    String value = get(attr);
    if (value == null) {
      //System.out.println("reset 1");
      value = getDefault(attr);
      replace = true;
    }

    String[] pieces = PApplet.split(value, ',');
    if (pieces.length != 3) {
      value = getDefault(attr);
      //System.out.println("reset 2 for " + attr);
      pieces = PApplet.split(value, ',');
      //PApplet.println(pieces);
      replace = true;
    }

    String name = pieces[0];
    int style = Font.PLAIN;  // equals zero
    if (pieces[1].indexOf("bold") != -1) {
      style |= Font.BOLD;
    }
    if (pieces[1].indexOf("italic") != -1) {
      style |= Font.ITALIC;
    }
    int size = PApplet.parseInt(pieces[2], 12);
    Font font = new Font(name, style, size);

    // replace bad font with the default
    if (replace) {
      //System.out.println(attr + " > " + value);
      //setString(attr, font.getName() + ",plain," + font.getSize());
      set(attr, value);
    }

    return font;
  }
}