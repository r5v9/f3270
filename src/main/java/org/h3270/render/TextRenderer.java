package org.h3270.render;

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

import java.util.Iterator;

import org.h3270.host.Field;
import org.h3270.host.InputField;
import org.h3270.host.Screen;

/**
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id: TextRenderer.java,v 1.15 2006/12/13 11:51:40 spiegel Exp $
 */
public class TextRenderer implements Renderer {

    private boolean markIntensified = false;

    // private boolean markHidden = false;

    public TextRenderer() {
    }

    public TextRenderer(final boolean markIntensified, final boolean markHidden) {
        this.markIntensified = markIntensified;
        // this.markHidden = markHidden;
    }

    public boolean canRender(final Screen s) {
        return true;
    }

    public boolean canRender(final String screenText) {
        return true;
    }

    public String render(final Screen s, final String actionURL, final String id) {
        return this.render(s);
    }

    public String render(final Screen s, final String actionURL) {
        return this.render(s);
    }

    public String render(final Screen s) {
        final StringBuffer result = new StringBuffer();
        for (Iterator<Field> i = s.getFields().iterator(); i.hasNext();) {
            final Field f = i.next();
            result.append(f.getText());
        }

        if (markIntensified) {
            markFields(s, result, '[', ']', new FieldSelector() {
                public boolean checkField(final Field f) {
                    return !(f instanceof InputField) && f.isIntensified();
                }
            });
        }

        markFields(s, result, '{', '}', new FieldSelector() {
            public boolean checkField(final Field f) {
                return f instanceof InputField;
            }
        });

        for (int i = 0; i < result.length(); i++) {
            if (result.charAt(i) == '\u0000') {
                result.setCharAt(i, ' ');
            }
        }
        return result.toString();
    }

    /**
     * This method marks some of the Fields in a textual screen representation by replacing the control characters with
     * other characters. For example, InputFields can be surrounded by '{' and '}' to make them visible and detectable.
     * 
     * @param s
     *            the Screen on which we operate
     * @param buf
     *            a StringBuffer holding the textual representation of the screen, with individual lines separated by
     *            newline characters.
     * @param openCh
     *            the character to be used for the initial control character of a field
     * @param closeCh
     *            the character to be used for the terminating control character of the field
     * @param fs
     *            a FieldSelector that decides which of the Fields should be marked
     */
    private void markFields(final Screen s, final StringBuffer buf, final char openCh, final char closeCh,
            final FieldSelector fs) {
        for (final Iterator<Field> i = s.getFields().iterator(); i.hasNext();) {
            final Field f = i.next();
            if (!fs.checkField(f)) {
                continue;
            }
            final int startx = f.getStartX();
            final int starty = f.getStartY();
            final int endx = f.getEndX();
            final int endy = f.getEndY();
            final int width = s.getWidth();

            if (startx == 0) {
                setChar(buf, width, width - 1, starty - 1, openCh);
            } else {
                setChar(buf, width, startx - 1, starty, openCh);
            }

            if (endx == width - 1) {
                setChar(buf, width, 0, endy + 1, closeCh);
            } else {
                setChar(buf, width, endx + 1, endy, closeCh);
            }
        }
    }

    /**
     * Changes one character in the given StringBuffer. The character position is given in screen (x,y) coordinates. The
     * buffer holds the entire screen contents, with lines separated by a single newline character each. If the x and y
     * coordinates are out of range, this method silently ignores the request -- this makes the caller's code easier.
     */
    private void setChar(final StringBuffer buf, final int width, final int x, final int y, final char ch) {
        final int index = y * (width + 1) + x;
        if (index >= 0 && index < buf.length()) {
            buf.setCharAt(index, ch);
        }
    }

    /**
     * Interface for selecting Fields.
     */
    private interface FieldSelector {
        boolean checkField(final Field f);
    }

}
