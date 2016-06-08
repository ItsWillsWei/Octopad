
public class Vector {
	private double x;
	private double y;
	
	Vector(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	double getX(){
		return x;
	}
	
	double getY(){
		return y;
	}
	
	void setX(double value){
		x = value;
	}
	
	void setY(double value){
		y = value;
	}
}
