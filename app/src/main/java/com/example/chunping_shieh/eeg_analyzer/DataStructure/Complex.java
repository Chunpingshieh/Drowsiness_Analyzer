package com.example.chunping_shieh.eeg_analyzer.DataStructure;

/**
 * Created by ChunPing-Shieh on 2015/3/26.
 * This class define the data structure of Complex and the method
 */
public class Complex {
    private double real;
    private double imagine;

    /**Constructor */
    public Complex(double real, double imagine) {
        this.real = real;
        this.imagine = imagine;
    }

    /** This method add up Complex a, b and save the answer to this*/
    public void add(Complex a, Complex b){
        this.real=a.real+b.real;
        this.imagine=a.imagine+b.imagine;
    }

    /** This method subtract Complex b from a and save the answer to this */
    public void mns(Complex a, Complex b){
        this.real=a.real-b.real;
        this.imagine=a.imagine-b.imagine;
    }

    /** This method multiply Complex a and b, and save the answer to this */
    public void mlt(Complex a, Complex b){//a and b can not be this.
        this.setRI(a.real*b.real-a.imagine*b.imagine, a.real*b.imagine+a.imagine*b.real);
    }

    /** This method calculate the bth power of Complex a and save the answer to this*/
    public void pow(Complex complex, int power){
        int i;
        this.setRI(1,0);
        for (i = 0; i < power; i++){
            this.mlt(this, complex);
        }
    }

    /** This method return the (i*a)th power of Constant e */
    public static Complex ei(double a){
        Complex ans=new Complex(1,0);
        ans.real=Math.cos(a);
        ans.imagine=-Math.sin(a);
        return ans;
    }

    /** abs() return the absolute value of the complex*/
    public static double abs(Complex complex) {
        return Math.sqrt(Math.pow(complex.real,2)+Math.pow(complex.imagine,2));
    }

    public void setComplex(Complex a) {
        this.real = a.real;
        this.imagine=a.imagine;
    }

    public double getReal() {
        return real;
    }

    public double getImagine() {
        return imagine;
    }


    public void setRI(double r,double i){
        this.real=r;
        this.imagine=i;
    }



}
