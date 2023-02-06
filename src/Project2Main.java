import java.awt.*;
import java.io.IOException;
import java.util.Scanner;

import org.jogamp.java3d.*;
import org.jogamp.vecmath.*;
import org.jogamp.java3d.utils.geometry.*;
import org.jogamp.java3d.utils.universe.*;

import org.jogamp.java3d.utils.behaviors.vp.*; 

public class Project2Main {
	
	private static BranchGroup createShading(ReadObjFile obj, int mode){
		
		// Make a scene graph branch
		BranchGroup branch = new BranchGroup();
		
		//Initialize
		Point3f[] coord = new Point3f[3];
		Point3f[] coords = new Point3f[3*obj.faces.size()];
		Vector3f[] normalArray = new Vector3f[3*obj.faces.size()];
		
		//TriangleArray
		TriangleArray tarr = new TriangleArray(
				3 *obj.faces.size(), 
				GeometryArray.COORDINATES | GeometryArray.NORMALS
				);
		
		//Calculate for all polygons
		for(int i=0; i<obj.faces.size(); i++) {
			
			//Vertex numbers
			int[][] num = obj.faces.get(i);
			
			//Vertex for polygon
			coord[0]  = new Point3f(obj.vertexVecToPoint(num[0][0]));
			coord[1]  = new Point3f(obj.vertexVecToPoint(num[1][0]));
			coord[2]  = new Point3f(obj.vertexVecToPoint(num[2][0]));
			
			Shape3D shape = new Shape3D();
			
			//
			if(mode != 4) {
				
				GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);
				gi.setCoordinates(coord);
				NormalGenerator nGenerator = new NormalGenerator();
				nGenerator.generateNormals(gi);
				shape.setGeometry(gi.getGeometryArray());
				shape.setAppearance(createAppearance(mode));
				branch.addChild(shape);
				
			}else if(mode == 4) {
				
				coords[i*3] = new Point3f(coord[0]);
				coords[i*3+1] = new Point3f(coord[1]); 
				coords[i*3+2] = new Point3f(coord[2]); 
				
				normalArray[i*3] = new Vector3f();
				normalArray[i*3] = obj.vecNormals[num[0][0]];
				normalArray[i*3+1] = new Vector3f();
				normalArray[i*3+1] = obj.vecNormals[num[1][0]];
				normalArray[i*3+2] = new Vector3f();
				normalArray[i*3+2] = obj.vecNormals[num[2][0]];
			}
		}
		
		if(mode == 4) {
			Shape3D shape = new Shape3D();
			tarr.setCoordinates(0, coords);
			tarr.setNormals(0, normalArray);
			shape.setGeometry(tarr);
			shape.setAppearance(createAppearance(3));
			
			branch.addChild(shape);
		}
			
		return branch;
	}
	
	static BranchGroup createPointCloud(ReadObjFile obj) {

		BranchGroup branch = new BranchGroup( );
		
		Shape3D shape = new Shape3D();

		// Make a changeable 3D transform
		TransformGroup trans = new TransformGroup( );
		trans.setCapability( TransformGroup.ALLOW_TRANSFORM_WRITE );
		branch.addChild( trans );

		// Make a shape
		//trans.addChild( demo );
		Point3f[] coord = new Point3f[obj.vertices.size()];
		for(int i=0; i<obj.vertices.size(); i++) {
			
			coord[i] = new Point3f(
					obj.vertices.get(i).x, 
					obj.vertices.get(i).y, 
					obj.vertices.get(i).z
					);		
		}

		//Make a point array
		PointArray pa = new PointArray(coord.length, GeometryArray.COORDINATES);
		PointAttributes pat = new PointAttributes();
		pat.setPointSize(3.0f); 
		pat.setPointAntialiasingEnable(true);

		pa.setCoordinates(0, coord);

		Appearance appear = new Appearance();
		appear.setPointAttributes(pat);
		
		 Color3f diffColor = new Color3f( 0.34f, 0.0f, 0.34f );
	     Color3f specColor = new Color3f( 0.89f, 0.0f, 0.0f );
	     Color3f black = new Color3f( 0.0f, 0.0f, 0.0f );
	     Material material = new Material();
	        
	     material.setDiffuseColor(diffColor);
	     material.setAmbientColor(black);
	     material.setSpecularColor(specColor);
	     material.setShininess(15.0f);
	     material.setLightingEnable(true);

		ColoringAttributes cAttributes = new ColoringAttributes();
		cAttributes.setColor(0.2f, 0f, 0f);
		appear.setColoringAttributes(cAttributes);
		appear.setMaterial(material);
		
		shape.setGeometry(pa);
		shape.setAppearance(appear);
		branch.addChild(shape);
		
		return branch;
	}
	
    static Appearance createAppearance(int mode) {

        Appearance appear = new Appearance();
        
        //Define  Material color
        Color3f diffColor = new Color3f( 0.34f, 0.0f, 0.34f );
        Color3f specColor = new Color3f( 0.89f, 0.0f, 0.0f );
        Color3f black = new Color3f( 0.0f, 0.0f, 0.0f );
        Material material = new Material();
        
        material.setDiffuseColor(diffColor);
        material.setAmbientColor(black);
        material.setSpecularColor(specColor);
        material.setShininess(15.0f);
        material.setLightingEnable(true);
	
        // assign PolygonAttributes to the Appearance.
	    PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setBackFaceNormalFlip(true);	
		
		switch (mode) {
		
		//Point
		case 1:
			
			appear.setMaterial(material);
			polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_POINT);
			appear.setPolygonAttributes(polyAttrib);
			
			PointAttributes pat = new PointAttributes();
			pat.setPointSize(5.0f); 
			pat.setPointAntialiasingEnable(true);
			appear.setPointAttributes(pat);
			
			break;
		
		//WireFrame
		case 2:
			
			appear.setMaterial(material);
			polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_LINE);
			appear.setPolygonAttributes(polyAttrib);
			
			LineAttributes la = new LineAttributes();
			la.setLineWidth(2.0f); 
			la.setLineAntialiasingEnable(true); 
			la.setLinePattern(LineAttributes.PATTERN_DASH);
			appear.setLineAttributes(la);
			
		//Flat and Smooth Shading
		case 3:
			
			appear.setMaterial(material);
			appear.setPolygonAttributes(polyAttrib);
			
			break;
		
		//WireFrame with Filled Shading	
		case 5:
			
		    material.setDiffuseColor(new Color3f( 0.0f, 0.5f, 0.5f ));
		    material.setAmbientColor(new Color3f( 0.0f, 0.3f, 0.3f ));
		    material.setSpecularColor(new Color3f( 0.0f, 1.0f, 1.0f ));
		    material.setShininess(15.0f);
		    material.setLightingEnable(true);
			
		    appear.setMaterial(material);
			polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_LINE);
			appear.setPolygonAttributes(polyAttrib);
			
			LineAttributes la1 = new LineAttributes();
			la1.setLineWidth(2.0f); 
			la1.setLineAntialiasingEnable(true); 
			la1.setLinePattern(LineAttributes.PATTERN_DASH);
			appear.setLineAttributes(la1);
			
			break;
			
		default:
			throw new IllegalArgumentException("Unexpected value: " + mode);
		}
			
	
	    return appear;
	
	}
	
	public static void addLights( BranchGroup bg ) {

        // create the color for the light
        Color3f color1 = new Color3f( 1.0f,1.0f,1.0f );

        // create a vector that describes the direction that
        // the light is shining.
        Vector3f direction1  = new Vector3f( -1.0f,-1.0f,-1.0f );

        // create the directional light with the color and direction
        DirectionalLight light1 = new DirectionalLight( color1, direction1 );
        

        // set the volume of influence of the light.
        // Only objects within the Influencing Bounds
        // will be illuminated.
        light1.setInfluencingBounds(  new BoundingSphere( new Point3d( 0.0,0.0,0.0 ), 200.0 ) );

        // add the light to the BranchGroup
        bg.addChild( light1 );
        
        Color3f color2 = new Color3f( 1.0f,1.0f,1.0f );

        // create a vector that describes the direction that
        // the light is shining.
        Vector3f direction2 = new Vector3f( 1.0f,1.0f,1.0f );

        // create the directional light with the color and direction
        DirectionalLight light2 = new DirectionalLight( color2, direction2 );
        

        // set the volume of influence of the light.
        // Only objects within the Influencing Bounds
        // will be illuminated.
        light2.setInfluencingBounds(  new BoundingSphere( new Point3d( 1,1,1 ), 200.0 ) );

        // add the light to the BranchGroup
        bg.addChild( light2 );

    }
	
	
	private static BranchGroup createProcessShading(ReadObjFile obj, Color3f[] color){
		
		// Make a scene graph branch
		BranchGroup branch = new BranchGroup();
		
		Point3f[] coord = new Point3f[3];
		Vector3f[] normal = new Vector3f[3];

		for(int i=0; i<obj.faces.size(); i++) {
			

			TriangleArray tarr = new TriangleArray(3, GeometryArray.COORDINATES | GeometryArray.NORMALS);
			
			//Vertex numbers
			int[][] num = obj.faces.get(i);
			
			//Vertex for polygon
			coord[0]  = new Point3f(obj.vertexVecToPoint(num[0][0]));
			coord[1]  = new Point3f(obj.vertexVecToPoint(num[1][0]));
			coord[2]  = new Point3f(obj.vertexVecToPoint(num[2][0]));
			
			normal[0] = new Vector3f(obj.vecNormals[num[0][0]]);
			normal[1] = new Vector3f(obj.vecNormals[num[1][0]]);
			normal[2] = new Vector3f(obj.vecNormals[num[2][0]]);
			
			Color3f nowColor = new Color3f();
			nowColor.add(color[num[0][0]], color[num[1][0]]);
			nowColor.add(color[num[2][0]]);
			nowColor.scale(1.0f/3.0f);
			
			Color3f diffColor = new Color3f( nowColor.x, nowColor.y, nowColor.z);
		    Color3f specColor = new Color3f( 0.85f, 0.85f, 0.85f );
		    Color3f black = new Color3f( nowColor.x/5.0f, nowColor.y/5.0f, nowColor.z/5.0f );
		    Material material = new Material();
			
			Shape3D shape = new Shape3D();
			

			GeometryInfo gi = new GeometryInfo(GeometryInfo.TRIANGLE_ARRAY);

			tarr.setCoordinates(0, coord);
			tarr.setNormals(0, normal);
			shape.setGeometry(tarr);
			shape.setAppearance(createAppearance(3));
		        
		     material.setDiffuseColor(diffColor);
		     material.setAmbientColor(black);
		     material.setSpecularColor(specColor);
		     material.setShininess(15.0f);
		     material.setLightingEnable(true);
		     
		     Appearance appear = new Appearance();
		     
			
		        // assign PolygonAttributes to the Appearance.
			 PolygonAttributes polyAttrib = new PolygonAttributes();
			 polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
			 polyAttrib.setBackFaceNormalFlip(true);
			 appear.setMaterial(material);
			 shape.setAppearance(appear);
			 branch.addChild(shape);
	
		}
			
		return branch;
	}
	

	private static BranchGroup createProcessPoint(ReadObjFile obj, Color3f[] color){
		
		// Make a scene graph branch
		BranchGroup branch = new BranchGroup();

			
		Point3f[] coord = new Point3f[obj.vertices.size()];
		for(int i=0; i<obj.vertices.size(); i++) {
				
			coord[i] = new Point3f(
					obj.vertices.get(i).x, 
					obj.vertices.get(i).y, 
					obj.vertices.get(i).z
					);

			//System.out.println("color" + i + " = " +  color[i].x + " " +  color[i].y + " " +  color[i].z + " ");
			
			PointArray pa = new PointArray(1, GeometryArray.COORDINATES);
			pa.setCoordinate(0, coord[i]);
			Shape3D shape = new Shape3D();

		    Appearance appear = new Appearance();
		    
			ColoringAttributes cAttributes = new ColoringAttributes();
			cAttributes.setColor(color[i]);
			appear.setColoringAttributes(cAttributes);
			
			PointAttributes pat = new PointAttributes();
			pat.setPointSize(5.0f); 
			pat.setPointAntialiasingEnable(true);
			appear.setPointAttributes(pat);
			
			shape.setGeometry(pa);
			shape.setAppearance(appear);
			branch.addChild(shape); 
		}
		return branch;
	}
	

	
	public static void main(String[] args) {
    	
		int refPointNum = 0;
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter your obj file name!!");
		ReadObjFile objFile = new ReadObjFile(scanner.nextLine());
		objFile.setup();
		System.out.println("obj name:" + objFile.objName);
       
		objFile.changeSize();
		objFile.calcVertexNormal();
		boolean mesh = objFile.showObjInfo();
		
		
		System.out.println("Please select \"rendering modes\"!! ");
		if(mesh) {
			System.out.println("\n  Points: 1\n  Wireframe: 2\n  Flat shading: 3\n  Smooth shading: 4\n  Wireframe with filled polygon: 5\n  GeodesicDistance for Mesh: 6\n");
		}else {
			System.out.println("\n  Points: 1\n  GeodesicDistance for Point-Cloud: 2\n");
		}
		String mode;
		mode = scanner.nextLine();
 
		if(Integer.parseInt(mode)>5) {
			
			System.out.println("Please enter point number (" + 0 + "~" + (objFile.vertices.size()-1)+")");
			refPointNum = Integer.parseInt(scanner.nextLine()); 
			
		}else if(!mesh && Integer.parseInt(mode)==2) {
			System.out.println("Please enter point number(" + 0 + "~" + (objFile.vertices.size()-1) +")");
			refPointNum = Integer.parseInt(scanner.nextLine()); 
		}
			
			Frame frame = new Frame();
			frame.setSize(700, 700);
			frame.setLayout(new BorderLayout());
       
		GraphicsConfiguration cf = SimpleUniverse.getPreferredConfiguration();
       
       //add Canvas3d
       Canvas3D canvas = new Canvas3D(cf);
       frame.add( "Center", canvas );
       
       //set universe
       SimpleUniverse univ = new SimpleUniverse( canvas );
       
       BranchGroup objRoot = new BranchGroup( );
       
       if(mesh) {
	       if(Integer.parseInt(mode)<5) {
	    	   objRoot.addChild(createShading(objFile, Integer.parseInt(mode)));
	       } else if(Integer.parseInt(mode)==5){
	    	   objRoot.addChild(createShading(objFile, 3));
	    	   objRoot.addChild(createShading(objFile, 5));
	       }else {
	    	   //Geodesic Distance Shading
	    	   GeodesicDistance gd = new GeodesicDistance();
	    	   gd.setUp();
	    	   Color3f[] color = gd.getColor(objFile, refPointNum);
	    	   objRoot.addChild(createProcessShading(objFile, color));
	    	   Point3f[] point = { new Point3f(objFile.vertices.get(refPointNum).x, objFile.vertices.get(refPointNum).y, objFile.vertices.get(refPointNum).z) };
	    	   PointArray pa = new PointArray(point.length, GeometryArray.COORDINATES);
	    	   pa.setCoordinates(0, point);
	
	    	   PointAttributes pat = new PointAttributes();
	   			pat.setPointSize(20.0f); 
	   			pat.setPointAntialiasingEnable(true);
	   			Appearance appear = new Appearance();
	   			appear.setPointAttributes(pat);
	   			ColoringAttributes cAttributes = new ColoringAttributes();
	   			cAttributes.setColor(1.0f, 1.0f, 0f);
	   			appear.setColoringAttributes(cAttributes);
	    	   Shape3D shape3d = new Shape3D(pa, appear);
	    	   objRoot.addChild(shape3d);
	       }
       }else {
    	   if(Integer.parseInt(mode)==1) {
    		   objRoot.addChild(createPointCloud(objFile));
    	   }else {
    		   System.out.println("OK");
    		   GeodesicDistance gd = new GeodesicDistance();
	    	   gd.setUp();
	    	   Color3f[] color = gd.getPointCloudColor(objFile, refPointNum);
	    	   
	    	   
	    	   objRoot.addChild(createProcessPoint(objFile, color));
	    	   Point3f[] point = { new Point3f(objFile.vertices.get(refPointNum).x, objFile.vertices.get(refPointNum).y, objFile.vertices.get(refPointNum).z) };
	    	   PointArray pa = new PointArray(point.length, GeometryArray.COORDINATES);
	    	   pa.setCoordinates(0, point);
	    	   
	    	   PointAttributes pat = new PointAttributes();
	   			pat.setPointSize(20.0f); 
	   			pat.setPointAntialiasingEnable(true);
	   			Appearance appear = new Appearance();
	   			appear.setPointAttributes(pat);
	   			ColoringAttributes cAttributes = new ColoringAttributes();
	   			cAttributes.setColor(1.0f, 1.0f, 0f);
	   			appear.setColoringAttributes(cAttributes);
	    	   Shape3D shape3d = new Shape3D(pa, appear);
	    	   objRoot.addChild(shape3d);
	    	   
    	   }
       }
       
       univ.getViewingPlatform( ).setNominalViewingTransform();
       
       OrbitBehavior ob = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ALL);
       BoundingSphere bs = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
       ob.setSchedulingBounds(bs);
       
       univ.getViewingPlatform().setViewPlatformBehavior(ob);
       
       addLights(objRoot);
       
       Background bgNode = new Background(1f,1f,1f);
       bgNode.setApplicationBounds( new BoundingSphere( new Point3d( 0.0,0.0,0.0 ), 200.0 ) );
       objRoot.addChild(bgNode);
       
       ViewingPlatform viewingPlatform = univ.getViewingPlatform(); 
       viewingPlatform.setNominalViewingTransform();
       OrbitBehavior orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ALL);
       BoundingSphere bounds = new BoundingSphere(new Point3d(0.,0.,0.),100.0);
       orbit.setSchedulingBounds(bounds);
       viewingPlatform.setViewPlatformBehavior(orbit);
       
       univ.addBranchGraph( objRoot );
		
       frame.setVisible(true);
       
       System.out.println("Please enter save file name!!");
       String name =  scanner.nextLine();
       try {
    	   objFile.saveObjFile(name);
       } catch (IOException e) {
    	   e.printStackTrace();
       }
    }
}


