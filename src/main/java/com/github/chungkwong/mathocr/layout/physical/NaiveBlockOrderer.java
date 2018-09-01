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
package com.github.chungkwong.mathocr.layout.physical;
import java.util.*;
/**
 * Naive implementation of BlockOrderer
 *
 * @author Chan Chung Kwong
 */
public class NaiveBlockOrderer implements BlockOrderer{
	public static final String NAME="NAIVE";
	@Override
	public List<PhysicalBlock> order(List<PhysicalBlock> blocks){
		int n=blocks.size();
		boolean[][] adj=new boolean[n][n];
		int[] deg=new int[n];
		LinkedList<Integer> avail=new LinkedList<Integer>();
		ListIterator<PhysicalBlock> iter2=blocks.listIterator();
		for(int i=0;i<n;i++){
			adj[i][i]=true;
		}
		while(iter2.hasNext()){
			PhysicalBlock block2=iter2.next();
			int ind2=iter2.previousIndex();
			ListIterator<PhysicalBlock> iter1=blocks.listIterator();
			while(iter1.hasNext()){
				PhysicalBlock block1=iter1.next();
				int ind1=iter1.previousIndex();
				if(block2.getBox().getLeft()<=block1.getBox().getRight()
						&&block1.getBox().getLeft()<=block2.getBox().getRight()
						&&block1.getBox().getTop()<block2.getBox().getTop()
						&&!adj[ind1][ind2]){
					adj[ind1][ind2]=true;
					++deg[ind2];
					for(int i=0;i<n;i++){
						for(int j=0;j<n;j++){
							if(adj[i][ind1]&&adj[ind2][j]&&!adj[i][j]){
								adj[i][j]=true;
								++deg[j];
							}
						}
					}
				}
			}
		}
		iter2=blocks.listIterator();
		while(iter2.hasNext()){
			PhysicalBlock block2=iter2.next();
			int ind2=iter2.previousIndex();
			ListIterator<PhysicalBlock> iter1=blocks.listIterator();
			while(iter1.hasNext()){
				PhysicalBlock block1=iter1.next();
				int ind1=iter1.previousIndex();
				if(!adj[ind1][ind2]&&!adj[ind2][ind1]&&block1.getBox().getRight()<block2.getBox().getLeft()){
					adj[ind1][ind2]=true;
					++deg[ind2];
					for(int i=0;i<n;i++){
						for(int j=0;j<n;j++){
							if(adj[i][ind1]&&adj[ind2][j]&&!adj[i][j]){
								adj[i][j]=true;
								++deg[j];
							}
						}
					}
				}
			}
			if(deg[ind2]==0){
				avail.addFirst(ind2);
			}
		}
		List<PhysicalBlock> newblocks=new ArrayList<>();
		int countdown=n;
		while(!avail.isEmpty()){
			int ind=avail.removeLast();
			newblocks.add(blocks.get(ind));
			for(int i=0;i<n;i++){
				if(adj[ind][i]){
					adj[ind][i]=false;
					if(--deg[i]==0){
						avail.addFirst(i);
					}
				}
			}
			--countdown;
		}
		if(countdown==0){
			return newblocks;
		}else{
			throw new RuntimeException("Reading order sort failed, please report this bug");
		}
	}
}
