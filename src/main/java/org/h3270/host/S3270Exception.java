/*
 * Copyright (C) 2003-2007 akquinet framework solutions
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

package org.h3270.host;

/**
 * An exception that indicates something went wrong in the S3270 subprocess. Subclasses represent more concrete failure
 * scenarios.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id: S3270Exception.java,v 1.1 2007/02/19 15:40:50 spiegel Exp $
 */
public class S3270Exception extends RuntimeException {

    private static final long serialVersionUID = 2941839322531181864L;

    public S3270Exception(final String message) {
        super(message);
    }

}
