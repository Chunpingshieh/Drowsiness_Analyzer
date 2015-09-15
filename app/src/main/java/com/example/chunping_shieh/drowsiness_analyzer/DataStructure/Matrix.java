package com.example.chunping_shieh.drowsiness_analyzer.DataStructure;


import com.example.chunping_shieh.drowsiness_analyzer.Constants.InitialConstants;

/**
 * Created by ChunPing-Shieh on 2015/1/8.
 * Matrix contains the data structure of a Matrix and the methods
 */
public class Matrix {
    private double[][] matrix;
    private int col;
    private int raw;

    public Matrix(int Raw, int Col) {
        col = Col;
        raw = Raw;
        matrix = new double[raw][col];
        /*int i,j;
        for (j=1; j<=col; j++){
            for (i=1; i<=raw; i++){
                matrix[raw-1][col-1] = 0;
            }
        }*/
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

    /** getColVector return the No. colNumber column of the Matrix in a form of a single column Matrix (Vector) */
    public Matrix getColVector(int colNumber){
        Matrix colVector=new Matrix(this.getRaw(),1);
        int i;
        for (i=0;i<colVector.getRaw();i++){
            colVector.setMatrixCell(this.getMatrixCell(i,colNumber),i,0);
        }
        return colVector;
    }

    /** getColVector return the No. colNumber column of the Matrix in a form of a single column Matrix (Vector) */
    public void setColVector(int colNumber, Matrix vector){
        for (int i = 0; i<vector.getRaw();i++){
            this.setMatrixCell(vector.getMatrixCell(i, 0), i, colNumber);
        }
    }



    /** getRawDouble return the No. rawNumber raw of the Matrix in a form of 1*col double array */
    public double[] getRawDouble(int rawNumber){
        double[] rawVector=new double[this.getCol()];
        int i;
        for (i=0;i<this.getCol();i++){
            rawVector[i]=getMatrixCell(rawNumber,i);
        }
        return rawVector;
    }

    //Setters===========================================

    /** setMatrixCell set the single cell located in (Raw, Col) to num */
    public void setMatrixCell(double num, int Raw, int Col) {
        matrix[Raw][Col] = num;
    }

    public void setMatrix(double[][] Matrix) {
        matrix = Matrix;
    }

    //Operators==============================================



    /** MatrixCross return the Cross Product of Matrix A and B*/
    public static Matrix MatrixCross(Matrix A, Matrix B){
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

    /** MatrixCrossN put the Cross Product of Matrix A nad B into this */
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

    /** VectorScale resize the Vector length to 1*/
    public void VectorScale(){
        int i;
        double distance;
        distance=this.VectorDistance();
        for (i=0;i<this.getRaw();i++){
            this.setMatrixCell(this.getMatrixCell(i, 0)/distance,i,0);
        }
    }

    /** MartixPlus add Matrix A and B and set the answer to this*/
    public void MatrixPlus(Matrix A, Matrix B){
        int i,j;
        for (i=0;i<A.getRaw();i++){
            for (j=0;j<A.getCol();j++){
                this.setMatrixCell(A.getMatrixCell(i,j)+B.getMatrixCell(i,j),i,j);
            }
        }
    }

    /** MartixPlus subtract Matrix A and B and set the answer to this*/
    public void MatrixMinus(Matrix A, Matrix B){
        int i,j;
        for (i=0;i<A.getRaw();i++){
            for (j=0;j<A.getCol();j++){
                this.setMatrixCell(A.getMatrixCell(i, j) - B.getMatrixCell(i, j), i, j);
            }
        }
    }

    /**MatrixMlt multiply a constant to a Matrix*/
    public static Matrix MatrixMlt(double constant, Matrix matrix){
        for (int i = 0; i < matrix.getRaw(); i++){
            for (int j = 0; j < matrix.getCol(); j++){
                matrix.setMatrixCell(matrix.getMatrixCell(i, j) * constant, i, j);
            }
        }
        return matrix;
    }

    /** Transpose returns the Transpose of the Matrix*/
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

    /** VectorDistance returns the Vector's length*/
    public double VectorDistance(){
        Matrix ans;
        ans=MatrixCross(this.Transpose(),this);
        return Math.sqrt(ans.getMatrixCell(0,0));
    }

    /** getScatterMatrix returns the Scatter Matrix of the Matrix X */
    /*public static Matrix getScatterMatrix(Matrix X) {
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
        for (i=0;i<X.getCol(); i++) {
            tmp.MatrixMinus(X.getColVector(i),meanVector);
            ans.MatrixPlus(ans,MatrixCross(tmp, tmp.Transpose()));
        }

        return ans;
    }*/

    public Matrix getCovarianceMatrix(){
        Matrix A = Expectation(MatrixCross(this,this.Transpose()));
        Matrix B = MatrixCross(Expectation(this),Expectation(this).Transpose());
        A.MatrixMinus(A, B);
        return A;
    }

    private static Matrix Expectation(Matrix x){
    return MatrixMlt(1 / x.Sum(), x);
    }

    private double Sum(){
        double sum = 0;
        for (int raw = 0; raw < this.getRaw(); raw ++){
            for (int col = 0; col < this.getCol(); col++){
                sum += this.getMatrixCell(raw,col);
            }
        }
        return sum;
    }

    public void combineMatrix(Matrix matrix1, Matrix matrix2) {
        for (int j = 0; j < this.getRaw(); j++){
            for (int i = 0; i < matrix1.getCol(); i++){
                this.setMatrixCell(matrix1.getMatrixCell(j,i),j,i);
            }
            for (int i = 0; i < matrix1.getRaw(); i++ ){
                this.setMatrixCell(matrix2.getMatrixCell(j,i),j,matrix1.getRaw());
            }
        }
    }


}
