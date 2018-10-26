/*
 * Copyright (C) 2018 Chan Chung Kwong
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.chungkwong.mathocr.preprocess.skew;
import com.github.chungkwong.mathocr.Registry;
/**
 *
 * @author Chan Chung Kwong
 */
public class SkewDetectors{
	public static final Registry<SkewDetector> REGISTRY=new Registry<>("SKEW_DETECT_METHOD",(img)->0.0);
	static{
		HierarchyStrategy hierarchyStrategy=new HierarchyStrategy();
		REGISTRY.register("PP",new PPDetector(hierarchyStrategy));
		REGISTRY.register("PPA",new PPADetector());
		REGISTRY.register("PCP",new PCPDetector(hierarchyStrategy));
		REGISTRY.register("TC",new TCDetector(hierarchyStrategy));
		REGISTRY.register("HT",new HTDetector(hierarchyStrategy));
		REGISTRY.register("CC",new CCDetector());
		REGISTRY.register("NN",new NNDetector());
		REGISTRY.register("S",new SDetector());
	}
}
