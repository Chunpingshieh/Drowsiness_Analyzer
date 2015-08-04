package com.example.chunping_shieh.eeg_analyzer.DataAnalyzing;

import com.example.chunping_shieh.eeg_analyzer.DataStructure.Complex;

/**
 * Created by ChunPing-Shieh on 2015/3/26.
 * This class Calculate FFT
 */

public final class FFT{

    static int N;
    static int K;
    static Complex[] f;

    public static double[] calculateFFT(double[] input) {



        N=input.length;
        K= (int) (Math.log(N)/Math.log(2));

        bitRevOrder(input);

        Complex w = Complex.ei(-2 * Math.PI / N);
        Complex a = new Complex(0,0);
        Complex b = new Complex(0,0);
        Complex wk = new Complex(0,0);
        Complex tmpC = new Complex(0,0);
        int k;

        for (int m=0; m<K;m++){
            for (int n=0; n<N; n+=Math.pow(2,m+1)){
                for (int p=0; p<Math.pow(2,m);p++){
                    k= (int) (p*N/Math.pow(2,m+1));
                    wk.pow(w, k);
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

    /** bitRevOrder Rearrange the input double array in bit Reverse order and put in a new Complex array */
    private static void bitRevOrder(double[] input) {
        f = new Complex[N];
        for (int i=0;i<N;i++){
            int invOrder=0;
            int tmp=i;
            for (int j=0;j<K;j++){
                int bin=tmp%2;
                tmp=tmp/2;
                invOrder= (int) (invOrder+bin*Math.pow(2,(K-j-1)));
            }
            f[invOrder]=new Complex(input[i],0);
        }
    }


}
