package com.example.chunping_shieh.eeg_analyzer.DataStructure;


/**
 * Created by ChunPing-Shieh on 2015/1/8.
 */
public class Matrix {
    private double[][] matrix;
    private int col;
    private int raw;

    public Matrix(int Raw, int Col) {
        col = Col;
        raw = Raw;
        matrix = new double[raw][col];
        int i,j;
        for (j=1; j<=col; j++){
            for (i=1; i<=raw; i++){
                matrix[raw-1][col-1] = 0;
            }
        }
    }

    //Getters==========================================


    public int getCol() {
        return col;
    }

    public int getRaw() {
        return raw;
    }

    public double[][] getMatrix() {
        return matrix;
    }

    public double getMatrixCell(int Raw, int Col) {
        return matrix[Raw][Col];
    }



    public Matrix getColVector(int colNumber){
        Matrix colVector=new Matrix(this.getRaw(),1);
        int i;
        for (i=0;i<colVector.getRaw();i++){
            colVector.setMatrixCell(this.getMatrixCell(i,colNumber),i,0);
        }
        return colVector;
    }

    public double[] getRawDouble(int rawNumber){
        double[] rawVector=new double[this.getCol()];
        int i;
        for (i=0;i<this.getCol();i++){
            rawVector[i]=getMatrixCell(rawNumber,i);
        }
        return rawVector;
    }

    //Setters===========================================

    public void setMatrixCell(double num, int Raw, int Col) {
        matrix[Raw][Col] = num;
    }

    public void setMatrix(double[][] Matrix) {
        matrix = Matrix;
    }

    //Operators==============================================



    public Matrix MatrixCross(Matrix A, Matrix B){
        int i,j,k;
        double tmp=0;
        Matrix ans=new Matrix(A.getRaw(),B.getCol());
        for (i=0; i<A.getRaw();i++){
            for (j=0; j<B.getCol(); j++){
                for (k=0;k<A.getCol();k++){
                    tmp+=A.getMatrixCell(i, k) * B.getMatrixCell(k,j);
                }
                ans.setMatrixCell(tmp, i, j);
                tmp=0;
            }
        }
        return ans;
    }

    public void MatrixCrossN(Matrix A, Matrix B){
        int i,j,k;
        double tmp=0;
        for (i=0; i<A.getRaw();i++){
            for (j=0; j<B.getCol(); j++){
                for (k=0;k<A.getCol();k++){
                    tmp+=A.getMatrixCell(i, k) * B.getMatrixCell(k,j);
                }
                this.setMatrixCell(tmp, i, j);
                tmp=0;
            }
        }
    }



    public void VectorScale(){
        int i;
        double distance;
        distance=this.VectorDistance();
        for (i=0;i<this.getRaw();i++){
            this.setMatrixCell(this.getMatrixCell(i, 0)/distance,i,0);
        }
    }

    public void MatrixPlus(Matrix A, Matrix B){
        int i,j;
        for (i=0;i<A.getRaw();i++){
            for (j=0;j<A.getCol();j++){
                this.setMatrixCell(A.getMatrixCell(i,j)+B.getMatrixCell(i,j),i,j);
            }
        }
    }

    public void MatrixMinus(Matrix A, Matrix B){
        int i,j;
        for (i=0;i<A.getRaw();i++){
            for (j=0;j<A.getCol();j++){
                this.setMatrixCell(A.getMatrixCell(i,j)-B.getMatrixCell(i,j),i,j);
            }
        }
    }

    public Matrix Transpose(){
        int i,j;
        Matrix ans=new Matrix(this.getCol(),this.getRaw());
        for (i=0;i<this.getRaw();i++){
            for (j=0;j<this.getCol();j++){
                ans.setMatrixCell(this.getMatrixCell(i, j), j, i);
            }
        }
        return ans;
    }

    public double VectorDistance(){
        Matrix ans;
        ans=MatrixCross(this.Transpose(),this);
        return Math.sqrt(ans.getMatrixCell(0,0));
    }


    public static Matrix getScatterMatrix(Matrix X) {
        Matrix ans = new Matrix(X.getRaw(),X.getRaw());
        Matrix meanVector = new Matrix(X.getRaw(),1);
        int i,j;
        for (i=0; i< X.getRaw(); i++){
            double tmp=0;
            for (j=1; j<X.getCol(); j++){
                tmp+=X.getMatrixCell(i,j)/X.getCol();
            }
            meanVector.setMatrixCell(tmp,i,0);
        }
        Matrix tmp=new Matrix(X.getRaw(),1);
        for (i=0;i<X.getCol();i++){
            tmp.MatrixMinus(X.getColVector(i),meanVector);
            ans.MatrixPlus(ans,tmp.MatrixCross(tmp,tmp.Transpose()));
        }

        return ans;
    }


}
