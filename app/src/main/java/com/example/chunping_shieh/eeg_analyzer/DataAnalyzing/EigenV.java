package com.example.chunping_shieh.eeg_analyzer.DataAnalyzing;

import com.example.chunping_shieh.eeg_analyzer.DataStructure.Matrix;

/**
 * Created by ChunPing-Shieh on 2015/1/8.

 */
public class EigenV extends Matrix {

    public EigenV(Matrix ScatterMatrix) {
        super(ScatterMatrix.getRaw(),1);
        double error=100;
        this.setMatrixCell(1,0,0);

        double a=Math.pow(10,-100);

        Matrix vnew = new Matrix(ScatterMatrix.getRaw(), 1);
        Matrix diff= new Matrix(ScatterMatrix.getRaw(),1);
        while (error>=a){
            vnew.MatrixCrossN(ScatterMatrix, this);
            vnew.VectorScale();
            diff.MatrixMinus(vnew,this);
            error=diff.VectorDistance();
            this.setMatrix(vnew.getMatrix());
        }

    }



}
