public class CoolWarmColor{
	
	public float scalar;
	public float R;
	public float G;
	public float B;

	CoolWarmColor(float scalar){
		this.scalar = scalar;
	}

	CoolWarmColor(float scalar, float R, float G, float B){
		this.scalar = scalar;
		this.R = R;
		this.G = G;
		this.B = B;
	}

	void interpolationColor(CoolWarmColor color1, CoolWarmColor color2){
		float ratio = (scalar - color2.scalar) / (color2.scalar - color1.scalar);
		R = interpolation(color1.R, color2.R, ratio);
		G = interpolation(color1.G, color2.G, ratio);
		B = interpolation(color1.B, color2.B, ratio);
	}

	float interpolation(float a, float b, float ratio){
		float ans = a + ratio*(b - a);
		if(ans<1)return ans;
		else return 0.99999f;
	}

}