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

import org.h3270.host.Screen;

/**
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id: Renderer.java,v 1.7 2006/12/13 11:50:55 spiegel Exp $
 */
public interface Renderer {

    boolean canRender(Screen s);

    boolean canRender(String screenText);

    String render(Screen s);

    String render(Screen s, String actionURL);

    String render(Screen s, String actionURL, String id);

}
