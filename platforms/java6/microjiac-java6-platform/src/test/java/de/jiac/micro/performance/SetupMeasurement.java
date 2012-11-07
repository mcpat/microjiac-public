/*
 * MicroJIAC - A Lightweight Agent Framework
 * This file is part of MicroJIAC Java6-Platform.
 *
 * Copyright (c) 2007-2012 DAI-Labor, Technische Universität Berlin
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
package de.jiac.micro.performance;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * This class measures the duration of the setup procedure of a
 * spring-based application (e.g. agent node). Additionally the number of threads,
 * the used heap and non-heap memory and the number of loaded classes are measured
 * for the running application.
 * 
 * @author Jan Keiser
 */
public class SetupMeasurement {

	/**
	 * Starts and measures an spring-based application.
	 * @param args the filename of the spring configuration
	 */
	public static void main(String[] args) throws Exception {
		// start application and measure duration
		long startTime = System.nanoTime();		
		if (args.length > 0) {
		    TestLauncherWithCompilation.execute(args);
		}
		long duration = System.nanoTime() - startTime;

		// get heap and non-heap memory usage
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		long heap = memoryBean.getHeapMemoryUsage().getUsed();
		long nonHeap = memoryBean.getNonHeapMemoryUsage().getUsed();
		
		// get number of loaded classes
		int classes = ManagementFactory.getClassLoadingMXBean().getLoadedClassCount();
		
		// get number of live threads
		int threads = ManagementFactory.getThreadMXBean().getThreadCount();
		
		// print all data to console
		System.out.println("Duration (nanos): " + duration);
		System.out.println("Heap size(bytes): " + heap);
		System.out.println("Non-heap (bytes): " + nonHeap);
		System.out.println("Threads (number): " + threads);
		System.out.println("Classes (number): " + classes);
		
		System.exit(0);
	}
		
}
