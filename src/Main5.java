import mpi.MPI;
import mpi.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Main5 {
    static int TAG_A = 0;
    static int TAG_B = 1;
    static int TAG_C = 2;


    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();


        int N = 2;
        int M = 2;
        int K = 2;

        if (rank == 0) {
            double[][] A = generate(N, M);
            double[][] B = generate(M, K);

            double[][] BT = transpose(B);

            double[] flattenBT = slice(BT, 0, BT.length);
            double[] flattenA = slice(A, 0, A.length);

            List<Request> requests = new ArrayList<>();

            int T = 2;


            if (M % T == 0) {
                int dT = M / T;
                for (int i = 0; i < T; i++) {
                    Request request1 = MPI.COMM_WORLD.Isend(flattenBT, i * dT, dT, MPI.DOUBLE, i + 1, TAG_A);
                    Request request2 = MPI.COMM_WORLD.Isend(flattenA, i * dT, dT, MPI.DOUBLE, i + 1, TAG_B);
                    requests.add(request1);
                    requests.add(request2);
                }
            }

            Request.Waitall(requests.toArray(new Request[0]));

            for (int i = 0; i < T; i++) {
                double[] arrC = getResult(i+1);
                System.out.println(i + " res=" + Arrays.toString(arrC));
            }



            double[][] res = multiply(A, B);
            print2D(res);
        } else {
            int sizeA = MPI.COMM_WORLD.Probe(0, TAG_A).Get_count(MPI.DOUBLE);
            double[] arrA = new double[sizeA];
            MPI.COMM_WORLD.Recv(arrA,0, sizeA, MPI.DOUBLE, 0, TAG_A);

            int sizeB = MPI.COMM_WORLD.Probe(0, TAG_B).Get_count(MPI.DOUBLE);
            double[] arrB = new double[sizeB];
            MPI.COMM_WORLD.Recv(arrB,0, sizeA, MPI.DOUBLE, 0, TAG_B);

            int _N = M / sizeA;
            int _M = M / sizeB;
            double[] C = new double[_N*_M];
            for (int i = 0; i < _N; i++) {
                for (int j = 0; j < _M; j++) {
                    C[i*_N + j] = scalar(arrA, arrB, i*M, j*M, M);
                }
            }
            MPI.COMM_WORLD.Send(C, 0, C.length, MPI.DOUBLE, 0, TAG_C);
        }
        MPI.Finalize();
    }

    private static double[] getResult(int source) {
        int sizeC = MPI.COMM_WORLD.Probe(source, TAG_C).Get_count(MPI.DOUBLE);
        double[] arrC = new double[sizeC];
        MPI.COMM_WORLD.Recv(arrC, 0, sizeC, MPI.DOUBLE, source, TAG_C);
        return arrC;
    }

    private static double[][] transpose(double[][] arr) {
        double[][] newArr = new double[arr[0].length][arr.length];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                newArr[i][j] = arr[j][i];
            }
        }
        return newArr;
    }

    private static double scalar(double[] arrA, double[] arrB, int offsetA, int offsetB, int size) {

        double s = 0.0;
        for (int i = 0; i < size; i++) {
            s += arrA[offsetA + i]*arrB[offsetB + i];
        }
//        System.err.println(Arrays.toString(arrA) + " " + Arrays.toString(arrB) + " " + offsetA + " " + offsetB + " " + size + " " + s);

        return s;
    }

    private static double[] slice(double[][] arr, int offset, int size) {
        double[] newArr = arr[offset];
        for (int i = 1; i < size; i++) {
            newArr = concat(newArr, arr[offset + i]);
        }
        return newArr;
    }

    private static double[] concat(double[] first, double[] second) {
        double[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private static double[][] generate(int rows, int cols) {
        double[][] arr = new double[rows][cols];
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                arr[i][j] = random.nextInt(10) - 5;
            }
        }
        return arr;
    }

    private static double[][] multiply(double[][] A, double[][] B) {
        double[][] C = new double[A.length][B.length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < B[0].length; j++) {
                C[i][j] = 0.0;
                for (int k = 0; k < B.length; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return C;
    }

    public static void print2D(double[][] mat)
    {
        for (int i = 0; i < mat.length; i++) {
            for (int j = 0; j < mat[i].length; j++) {
                System.err.print(mat[i][j] + " ");
            }
            System.err.println("");
        }
    }
}
