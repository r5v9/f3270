package org.h3270.host;

/*
 * Copyright (C) 2003-2006 akquinet framework solutions
 *
 * This file is part of h3270.
 *
 * h3270 is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * h3270 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with h3270; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, 
 * MA 02110-1301 USA
 */

import java.util.List;

/**
 * Represents the contents of a 3270 screen. A screen is made up of characters arranged in rows and columns. Some
 * portions of the screen are fields that allow user input. These are represented by objects of their own (see class
 * 
 * @link{Field ).
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id: Screen.java,v 1.7 2006/10/25 11:20:09 spiegel Exp $
 */
public interface Screen {

    /**
     * Returns the width (number of columns) of this screen.
     */
    int getWidth();

    /**
     * Returns the height (number of rows) of this screen.
     */
    int getHeight();

    /**
     * Returns the character at the given position. x and y start in the upper left hand corner, which is position
     * (0,0). Control characters are returned as blanks.
     */
    char charAt(int x, int y);

    /**
     * Returns the contents of a region on this screen.
     * 
     * @param startx
     *            x coordinate of the starting point (inclusive)
     * @param starty
     *            y coordinate of the starting point
     * @param endx
     *            x coordinate of the end point (inclusive)
     * @param endy
     *            y coordinate of the end point
     * @return the region as a String, with line breaks (newline characters) inserted
     */
    String substring(int startx, int starty, int endx, int endy);

    /**
     * Returns a part of a row on this screen, as a string.
     * 
     * @param startx
     *            x coordinate of the starting point (inclusive)
     * @param endx
     *            x coordinate of the end point (inclusive)
     * @param y
     *            number of the row
     */
    String substring(int startx, int endx, int y);

    /**
     * Returns a single row of this screen.
     * 
     * @param y
     *            the row number
     * @return the row as a String, without a terminating newline
     */
    String substring(int y);

    /**
     * Returns a list of all Fields on this screen. If there are no fields, this method returns an empty list.
     */
    List<Field> getFields();

    /**
     * Returns a Field object representing the input field at position (x,y). If there is no input field at this
     * position, this method returns null. A field begins with the character <i>after</i> the first control character,
     * and ends with the character <i>before</i> the terminating control character. Thus, for the positions of the
     * control characters themselves, this method always returns null.
     * 
     * x and y start in the upper left hand corner, which is position (0,0).
     */
    InputField getInputFieldAt(int x, int y);

    /**
     * Returns true if there is an input field at position (x, y) on this screen. Fields do not include the control
     * characters that delimit them, see {@link #getInputFieldAt getFieldAt()}.
     */
    boolean isInputField(int x, int y);

    /**
     * Gets the InputField in which the cursor is currently, or null if the cursor is not in an InputField.
     */
    InputField getFocusedField();

    /**
     * Returns true if this Screen is formatted.
     */
    boolean isFormatted();

}
