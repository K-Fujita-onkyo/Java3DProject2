import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.jogamp.vecmath.*;

import com.jogamp.opengl.util.packrect.LevelSet;

class ReadObjFile{
	
	Path objFilePath; //takes the path of the obj file
	
	String objName;
	ArrayList<Vector3f> vertices = new ArrayList<Vector3f> (); //a geometry vertex with coordinates (x,y,z)
	ArrayList<Vector2f> textures = new ArrayList<Vector2f> (); //a texture coordinates (u,v)
	ArrayList<Vector3f> normals = new ArrayList <Vector3f> (); //a face normal with coordinates (x,y,z)
	ArrayList<int[][]> faces = new ArrayList <int[][]> (); //faces
	
	Vector3f[] vecNormals;
	
	float maxVertexSize = -9999f;
	
	ReadObjFile(String objFilePath){
		this.objFilePath = Paths.get(objFilePath);
	}
	
	void setup(){

		
		String[] objFile = null;
		String[] tokens;
		
		try {
			objFile = Files.readString(objFilePath).split("\n");
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		
		for(int i=0; i<objFile.length; i++) {
			
			tokens = objFile[i].split("\\s+");
			
			if(tokens.length > 0) {
				if (tokens[0].equals("o")) {
					
					   objName = tokens[1];
					   
					   System.out.println("obj name: " + objName);
					   
				}else if(tokens[0].equals("v")) {
					
					Vector3f readV = new Vector3f(
							Float.parseFloat(tokens[1]), 
							Float.parseFloat(tokens[2]), 
							Float.parseFloat(tokens[3])
							);
					if(readV.length()>maxVertexSize) {
						
						maxVertexSize = 2*readV.length();
						
					}else if(-readV.length()>maxVertexSize) {
						
						maxVertexSize = -2*readV.length();
					}
					
					vertices.add(readV);
						
				}else if(tokens[0].equals("vt")) {
					
					Vector2f readT = new Vector2f(
							Float.parseFloat(tokens[1]),
							Float.parseFloat(tokens[2])
							);
					
					textures.add(readT);
					
				}else if(tokens[0].equals("vn")) {
					
					Vector3f readN = new Vector3f(
							Float.parseFloat(tokens[1]), 
							Float.parseFloat(tokens[2]), 
							Float.parseFloat(tokens[3])
							);
					
					normals.add(readN);
					
				}else if(tokens[0].equals("f")) {
					
					int count = tokens.length;
					
					int[][] indices = new int[count-1][3];
					
					for(int j=1; j<count; j++) {
						
						String[] facetoken = tokens[j].split("/");
						
						if(facetoken.length>2) {
							indices[j-1][0] = Integer.parseInt(facetoken[0])-1;
							indices[j-1][1] = Integer.parseInt(facetoken[1])-1;
							indices[j-1][2] = Integer.parseInt(facetoken[2])-1;
						}else {
							indices[j-1][0] = Integer.parseInt(facetoken[0])-1;
						}
					}
					
					faces.add(indices);

				}
			}
		}
		
		vecNormals = new Vector3f[faces.size()];
	}
	
	void changeSize() {
	
		System.out.println("maxLength: "+maxVertexSize);
		for(int i=0; i<vertices.size(); i++) {
				
			Vector3f changeVec = new Vector3f(
					vertices.get(i).x / maxVertexSize, 
					vertices.get(i).y / maxVertexSize, 
			 		vertices.get(i).z / maxVertexSize
					);
			
			vertices.set(i, changeVec);
		}
	
	}
	
	Point3f vertexVecToPoint(int index) {
		
		Point3f point = new Point3f(
				vertices.get(index).x, 
				vertices.get(index).y, 
				vertices.get(index).z
				);

		return point;
	}
	
	void calcVertexNormal() {
		
		if(normals.size()!=0) {
			
			System.out.println("Vertex normal has already been calculated!!");
			return;
			
		}else {
			
			for(int i=0; i<vecNormals.length;i++) {
				vecNormals[i] = new Vector3f(0.0f, 0.0f, 0.0f);
			}
			
			for(int i=0; i<faces.size(); i++) {
				
				int[][] num = faces.get(i);
				Vector3f v1 = new Vector3f();
				Vector3f v2 = new Vector3f();
				Vector3f vn = new Vector3f();
				
				v1.sub(vertices.get(num[1][0]), vertices.get(num[0][0]));
				v2.sub(vertices.get(num[2][0]), vertices.get(num[0][0]));
				vn.cross(v1, v2);
				
				vecNormals[num[0][0]].add(vn);
				vecNormals[num[1][0]].add(vn);
				vecNormals[num[2][0]].add(vn);
			}
			
			for(int i=0; i<vecNormals.length; i++) {
				vecNormals[i].normalize();
			}
		}
	}
	
	boolean showObjInfo() {
		
		if(faces.size()!=0) {
			System.out.println("******* The Mesh Info *******");
			System.out.println("The number of vertices: " + vertices.size());
			System.out.println("The number of edges: " + calcNumEdge());
			System.out.println("The number of triangles: " + faces.size());
		
		}else {
			System.out.println("******* The Point-Cloud Info *******");
			System.out.println("The number of points: " + vertices.size());
			return false;
		}
		
		return true;
	}
	
	int calcNumEdge() {
		
		int num = 0;
		boolean[][] vertex = new boolean[vertices.size()][vertices.size()];
		
		for(int i=0; i<vertices.size(); i++) {			
			for(int j=0; j<vertices.size(); j++) {
					
				vertex[i][j] = false;
			}
		}
			
		for(int i=0; i<faces.size(); i++) {
				
			int[][] n = faces.get(i);
				
			for(int j=0; j<3; j++) {
				vertex[n[j%3][0]][n[(j+1)%3][0]] = true;
				vertex[n[(j+1)%3][0]][n[j%3][0]] = true;
			}
		}
		
		for(int i=0; i<vertices.size(); i++) {			
			for(int j=0; j<vertices.size(); j++) {
					
				if(vertex[i][j]) num++;
			}
		}
			
		
		return num/2;
	}
	
	void saveObjFile(String name) throws IOException {
		
		File file = new File(name + ".obj");
		FileWriter filewriter = new FileWriter(file);
		
		filewriter.write("o " + name + "\n");
		
		for(int i=0; i<vertices.size(); i++) {
			filewriter.write("v " + vertices.get(i).x + " " + vertices.get(i).x + " " + vertices.get(i).x + "\n");
		}
		for(int i=0; i<vertices.size(); i++) {
			int[][] n = faces.get(i);
			filewriter.write("f ");
			for(int j=0; j<n.length; j++) {
				Integer nn = Integer.valueOf(n[j][0]);
				filewriter.write(nn.toString());
				if(j!=n.length-1) filewriter.write(" ");
				else filewriter.write("\n");
			}
			
		}
		
		filewriter.close();
		System.out.println("Saved!!");
	}
	
}