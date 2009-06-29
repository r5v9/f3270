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
 * Indicates that the host could not be reached by the s3270 process. This is most likely due to firewall problems.
 * 
 * @author Andre Spiegel spiegel@gnu.org
 * @version $Id: HostUnreachableException.java,v 1.2 2007/03/02 09:35:23 spiegel Exp $
 */
public class HostUnreachableException extends S3270Exception {

    private static final long serialVersionUID = 3815053284627149828L;

    private String host;
    private String reason;

    public HostUnreachableException(final String host, final String reason) {
        super("Host " + host + " cannot be reached: " + reason);
        this.host = host;
        this.reason = reason;
    }

    /**
     * Returns the name of the host that could not be reached.
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the reason why the host could not be reached, as reported by s3270.
     */
    public String getReason() {
        return reason;
    }

}
