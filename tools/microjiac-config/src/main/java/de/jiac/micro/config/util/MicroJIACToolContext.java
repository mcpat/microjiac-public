/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Config.
 *
 * Copyright (c) 2007-2011 DAI-Labor, Technische Universität Berlin
 *
 * This library includes software developed at DAI-Labor, Technische
 * Universität Berlin (http://www.dai-labor.de)
 *
 * This library is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * $Id$
 */
package de.jiac.micro.config.util;

import de.dailab.jiac.common.aamm.FrameworkType;
import de.dailab.jiac.common.aamm.tools.AdaptableObjectFactory;
import de.dailab.jiac.common.aamm.tools.CheckerToolContext;
import de.dailab.jiac.common.aamm.tools.ICheckerParams;
import de.jiac.micro.config.analysis.ConventionEnforcer;

/**
 * Specific context implementation for MicroJIAC. We enhance the default AAMM model
 * to ease the resolution and checking steps. Thus we specify an implementation path
 * which contains subclasses of the default model and also further interfaces.
 * <p>
 * Enhancing the model is supported via the {@link AdaptableObjectFactory} which is part
 * of AAMM.
 * </p>
 * 
 * @author Marcel Patzlaff
 * @version $Revision$
 */
public class MicroJIACToolContext extends CheckerToolContext {
    private final static ICheckerParams PARAMS= new ICheckerParams() {
        public String getAgentClassName() {
            return "de.jiac.micro.core.IAgent";
        }

        public String getAgentElementClassName() {
            return "de.jiac.micro.agent.IAgentElement";
        }

        public String getNodeClassName() {
            return "de.jiac.micro.core.INode";
        }

        public String getObjectClassName() {
            return "java.lang.Object";
        }
    };
    
    public MicroJIACToolContext(ClassLoader loader) {
        super(FrameworkType.MICRO_JIAC, loader, PARAMS);
    }
    
    public final ConventionEnforcer createEnforcer() {
        return new ConventionEnforcer(this);
    }
}
