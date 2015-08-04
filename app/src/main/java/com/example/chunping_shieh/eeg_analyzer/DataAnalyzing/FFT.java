package com.example.chunping_shieh.eeg_analyzer.DataAnalyzing;

import com.example.chunping_shieh.eeg_analyzer.DataStructure.Complex;

/**
 * Created by ChunPing-Shieh on 2015/3/26.
 */



public class FFT{

    static int N;
    static int K;

    public static double[] calculateFFT(double[] input) {



        Complex w,a,b,wk,tmpC2,tmpC;
        N=input.length;
        K= (int) (Math.log(N)/Math.log(2));

        Complex[] f=bitRevOrder(input);




        w = new Complex(0,0);
        w=w.ei(-2*Math.PI/N);
        a = new Complex(0,0);
        b = new Complex(0,0);
        wk = new Complex(0,0);
        tmpC = new Complex(0,0);
        tmpC2 = new Complex(0,0);
        double k;

        for (int m=0; m<K;m++){
            for (int n=0; n<N; n+=Math.pow(2,m+1)){
                for (int p=0; p<Math.pow(2,m);p++){
                    k=p*N/Math.pow(2,m+1);
                    wk.pow(w, tmpC2, k);
                    tmpC.mlt(f[(int)(p+Math.pow(2,m)+n)],wk);
                    a.add(f[p+n],tmpC);
                    b.mns(f[p+n],tmpC);
                    f[p+n].setComplex(a);
                    f[(int)(p+Math.pow(2,m)+n)].setComplex(b);
                }
            }
        }

        double[] ans = new double[N];
        for (int i = 0; i < N; i++){
            ans[i] = Complex.abs(f[i]);
        }

        return ans;

    }

    private static Complex[] bitRevOrder(double[] input) {
        Complex[] ans = new Complex[N];
        for (int i=0;i<N;i++){
            int invOrder=0;
            int tmp=i;
            for (int j=0;j<K;j++){
                int bin=tmp%2;
                tmp=tmp/2;
                invOrder= (int) (invOrder+bin*Math.pow(2,(K-j-1)));
            }
            ans[invOrder]=new Complex(input[i],0);
        }
        return ans;
    }


}
