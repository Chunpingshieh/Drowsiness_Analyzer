package com.example.chunping_shieh.drowsiness_analyzer.DataAnalyzing;

import com.example.chunping_shieh.drowsiness_analyzer.Constants.InitialConstants;
import com.example.chunping_shieh.drowsiness_analyzer.DataStructure.Matrix;

/**
 * Created by ChunPing-Shieh on 2015/1/8.
 * EigenV calculate the Eigen Vector of a ScatterMatrix through a Fast PCA approach
 * The vector will be Crossed by the ScatterMatrix and rescale to 1 until the procedure put little change on the Vector(< errThresh)
 */
public class EigenV extends Matrix {

    public EigenV() {
        super(InitialConstants.Channel, InitialConstants.Channel);
    }

    public EigenV(Matrix CovarianceMatrix) {
        super(CovarianceMatrix.getRaw(), InitialConstants.Channel);
        for (int i = 0; i < InitialConstants.Channel; i++) {
            this.setMatrixCell(Math.random(), 0, i);
        }

        //double errThresh = Math.pow(10,-300);

        for (int eigenvector_No = 0; eigenvector_No < 8; eigenvector_No++) {
            Matrix newVector = new Matrix(CovarianceMatrix.getRaw(), 1);
            Matrix diff = new Matrix(CovarianceMatrix.getRaw(), 1);
            Matrix tmpCell;
            double error=100;
            while (error > 0) {
                newVector.MatrixCrossN(CovarianceMatrix, this.getColVector(eigenvector_No));

                for (int j = 0; j < eigenvector_No; j++){
                    tmpCell = MatrixCross(newVector.Transpose(), this.getColVector(j));
                    newVector.MatrixMinus(newVector,MatrixMlt(tmpCell.getMatrixCell(0, 0),this.getColVector(j)));
                }

                newVector.VectorScale();
                /*diff.MatrixMinus(newVector, this.getColVector(eigenvector_No));
                error = diff.VectorDistance();*/
                tmpCell = MatrixCross(newVector.Transpose(),this.getColVector(eigenvector_No));
                error = Math.abs(tmpCell.getMatrixCell(0,0))-1;
                this.setColVector(eigenvector_No, newVector);
            }
        }

    }



}
