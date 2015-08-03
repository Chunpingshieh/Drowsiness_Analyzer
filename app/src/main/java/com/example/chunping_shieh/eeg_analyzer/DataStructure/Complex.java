package com.example.chunping_shieh.eeg_analyzer.DataStructure;

/**
 * Created by ChunPing-Shieh on 2015/3/26.
 */
public class Complex {
    private double real;
    private double imagine;




    public Complex(double real, double imagine) {
        this.real = real;
        this.imagine = imagine;
    }



    public void add(Complex a, Complex b){
        this.real=a.real+b.real;
        this.imagine=a.imagine+b.imagine;
    }

    public void mns(Complex a, Complex b){
        this.real=a.real-b.real;
        this.imagine=a.imagine-b.imagine;
    }

    public void mlt(Complex a, Complex b){//a and b can not be this.
        this.real=a.real*b.real-a.imagine*b.imagine;
        this.imagine=a.real*b.imagine+a.imagine*b.real;
    }

    public void pow(Complex a, Complex tmp, double b){
        int i;
        this.setRI(1,0);
        for (i=0;i<b;i++){
            tmp.setComplex(this);
            this.mlt(tmp,a);
        }
    }

    public Complex ei(double a){
        Complex ans=new Complex(1,0);
        ans.real=Math.cos(a);
        ans.imagine=-Math.sin(a);
        return ans;
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


    public static double abs(Complex complex) {
        return Math.sqrt(Math.pow(complex.real,2)+Math.pow(complex.imagine,2));
    }
}
