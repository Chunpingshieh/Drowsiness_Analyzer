package com.example.chunping_shieh.eeg_analyzer.DataAnalyzing;

import com.example.chunping_shieh.eeg_analyzer.DataStructure.Matrix;

/**
 * Created by ChunPing-Shieh on 2015/1/8.
 * EigenV calculate the Eigen Vector of a ScatterMatrix through a Fast PCA approach
 * The vector will be Crossed by the ScatterMatrix and rescale to 1 until the procedure put little change on the Vector(< errThresh)
 */
public class EigenV extends Matrix {

    public EigenV(Matrix ScatterMatrix) {
        super(ScatterMatrix.getRaw(), 1);
        double error=100;
        this.setMatrixCell(1, 0, 0);

        double errThresh=Math.pow(10,-100);

        Matrix newVector = new Matrix(ScatterMatrix.getRaw(), 1);
        Matrix diff= new Matrix(ScatterMatrix.getRaw(),1);
        while (error>=errThresh){
            newVector.MatrixCrossN(ScatterMatrix, this);
            newVector.VectorScale();
            diff.MatrixMinus(newVector, this);
            error=diff.VectorDistance();
            this.setMatrix(newVector.getMatrix());
        }

    }



}
