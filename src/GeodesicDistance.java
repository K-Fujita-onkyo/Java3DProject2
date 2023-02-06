import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.*;

import org.jogamp.vecmath.*;

public class GeodesicDistance {
	
	Path colorMapPath;
	CoolWarmColor[] colorMap;
	float[] dist;
	int[][] nearFaceList;
	float[][] weight;

	float maxDist;
	
	float INFTY = 99999999f;
	
	int WHITE = 1;
	int GRAY = 2;
	int BLACK = 3;
			
	GeodesicDistance() {
		colorMapPath = Paths.get("./default.csv");
	}
	
	GeodesicDistance(String colorMapPath) {
		this.colorMapPath = Paths.get(colorMapPath);
	}
	
	public void setUp() {
		//open csv file
		try {
		
			String[] csvStrFile = Files.readString(colorMapPath).split("\n");
			colorMap = new CoolWarmColor[csvStrFile.length - 1];

			for(int i = 0; i < colorMap.length; i++){
				String[] colorData = csvStrFile[i+1].split(",");
				colorMap[i] = new CoolWarmColor(
						Float.parseFloat(colorData[0]), 
						Float.parseFloat(colorData[1]), 
						Float.parseFloat(colorData[2]), 
						Float.parseFloat(colorData[3]));
			}
		
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}
	
	
	public Color3f[] getColor(ReadObjFile obj, int pointNum) {
		
		CoolWarmColor cwc;
		Color3f[] pointColor = new Color3f[obj.vertices.size()];
		Vector3f v = new Vector3f();
		dist = new float[obj.vertices.size()];
		weight = new float[obj.vertices.size()][obj.vertices.size()];
		
		maxDist = -INFTY;
		
		//init weight and dist
		for(int i=0; i<obj.vertices.size(); i++) {
			
			dist[i] = INFTY;
			
			for(int j=0; j<obj.vertices.size(); j++) {
				
				weight[i][j] = INFTY;
			}
		}
		
		for(int i=0; i<obj.faces.size(); i++) {
			
			int[][] n = obj.faces.get(i);
			
			for(int j=0; j<3; j++) {
				
				v.sub(obj.vertices.get(n[j%3][0]), obj.vertices.get(n[(j+1)%3][0]));
				weight[n[j%3][0]][n[(j+1)%3][0]] = v.length();
				weight[n[(j+1)%3][0]][n[j%3][0]] = v.length();
				
			}
			
		}
		
		calcGeodesticDistance(obj, pointNum);
		
		for(int i=0; i<dist.length; i++) {
			if(maxDist<dist[i]) maxDist = dist[i];
		}
		
		for(int i=0; i<pointColor.length; i++) {
			
			dist[i] /= maxDist;
			cwc = new CoolWarmColor(dist[i]);
			cwc = findCWColor(dist[i]);
			pointColor[i] = new Color3f(cwc.R, cwc.G, cwc.B);
			
		}
		
		return pointColor;
		
	}
	
	public Color3f[] getPointCloudColor(ReadObjFile obj, int pointNum) {
		
		int k = 3;
		CoolWarmColor cwc;
		Color3f[] pointColor = new Color3f[obj.vertices.size()];
		Vector3f v = new Vector3f();
		dist = new float[obj.vertices.size()];
		weight = new float[obj.vertices.size()][obj.vertices.size()];
		ArrayList<VecList> list = new ArrayList<VecList> ();
		
		maxDist = -INFTY;
		
		
		for(int i=0; i<obj.vertices.size(); i++) {
			
			dist[i] = INFTY;
			
			for(int j=0; j<obj.vertices.size(); j++) {
				
				weight[i][j] = INFTY;
			}
		}
		
		for(int i=0; i<obj.vertices.size(); i++) {
			
			list.clear();
			
			for(int j=0; j<obj.vertices.size(); j++) {
				if(i==j) continue;
				v.sub(obj.vertices.get(i), obj.vertices.get(j));
				list.add(new VecList(v.length(), j));
			}
			Collections.sort(list, new Comparator<VecList>() {
				@Override
				public int compare(VecList vFirst, VecList vSecond) {
					return Float.compare(vFirst.getValue(), vSecond.getValue());
				}
			});
			
			for(int j=0; j<k; j++) {
				weight[i][list.get(j).getIndex()] = list.get(j).getValue();
				weight[list.get(j).getIndex()][i] = list.get(j).getValue();
			}
			 
			
		}
		
		calcGeodesticDistance(obj, pointNum);
		
		for(int i=0; i<dist.length; i++) {
			//System.out.println("dist["+i+"]="+dist[i]);
			if(maxDist<dist[i]) maxDist = dist[i];
		}
		
		for(int i=0; i<pointColor.length; i++) {
			
			dist[i] /= maxDist;
			cwc = new CoolWarmColor(dist[i]);
			cwc = findCWColor(dist[i]);
			pointColor[i] = new Color3f(cwc.R, cwc.G, cwc.B);
			//System.out.println("c: " + pointColor[i].x);
			
		}
		
		
		return pointColor;
	}
	
	public void calcGeodesticDistance(ReadObjFile obj, int pointNum) {
		
		int[] verFlag = new int[obj.vertices.size()];
		
		//init dist[]
		for(int i=0; i<dist.length; i++) {
			verFlag[i] = WHITE;
		}
		
		dist[pointNum] = 0.0f;
		verFlag[pointNum] = GRAY;
		int flag=0;
		while(true) {
			int u = -1;
			float minDist = INFTY;
			
			for(int i=0; i<dist.length; i++) {
				if(minDist > dist[i] && verFlag[i] != BLACK) {
					u = i;
					minDist = dist[i];
				}
			}
			
			if(u == -1) break;
			verFlag[u] = BLACK;
			
			for(int i=0; i<dist.length; i++) {
				
				if(verFlag[i] != BLACK && weight[u][i] != INFTY) {
					if(dist[i] > dist[u] + weight[u][i]) {
						dist[i] = dist[u] + weight[u][i];
						verFlag[i] = GRAY;
					}
				}
			}
			
		}
		
		
	}
	
	public CoolWarmColor findCWColor(float dist) {
		
		CoolWarmColor color = new CoolWarmColor(dist);
		
		for(int i=0; i<colorMap.length-1; i++) {
			
			if(colorMap[0].scalar > color.scalar) {
				color = colorMap[0];
				break;
			}else if(colorMap[i].scalar < color.scalar && colorMap[i+1].scalar > color.scalar) {
				color.interpolationColor(colorMap[i], colorMap[i+1]);
				break;
			}else if(colorMap[colorMap.length-1].scalar < color.scalar) {
				color = colorMap[colorMap.length-1];
				break;
			}
		}
		return color;
	}
			
	
}

class VecList{
	
	float value;
	int index;
	
	public VecList(float value, int index) {
		this.value = value;
		this.index = index;
	}
	
	public float getValue() {
		return value;
	}
	
	public int getIndex() {
		return index;
	}
}
