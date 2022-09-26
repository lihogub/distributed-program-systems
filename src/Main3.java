import mpi.MPI;
import mpi.Request;

import java.util.Arrays;
import java.util.Random;

public class Main3 {
    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int SIZE = 100;
        int[] arrayToSort = new Random().ints(SIZE, 0, SIZE).toArray();

        if (rank == 0) {
            var result = trySplit(arrayToSort, rank + 1, size);
            System.err.println("Result is: " + Arrays.toString(result));
        } else {
            int[] buffer = new int[SIZE];
            var status = MPI.COMM_WORLD.Recv(buffer, 0, SIZE, MPI.INT, MPI.ANY_SOURCE, MPI.ANY_TAG);
            var tag = status.tag;
            var source = status.source;
            int[] receivedBuffer = Arrays.copyOfRange(buffer, 0, tag);
            var result = trySplit(receivedBuffer, rank, size);
            MPI.COMM_WORLD.Send(result, 0, result.length, MPI.INT, source, tag);
        }
        MPI.Finalize();
    }

    private static int[] trySplit(int[] source, int rank, int size) {
        if (rank * 2 > size || rank * 2 + 1 > size) {
            Arrays.sort(source);
            System.err.println("* " + rank + " -> " + source.length);
            return source;
        }
        int[] a = Arrays.copyOfRange(source, 0, source.length / 2);
        var reqA = MPI.COMM_WORLD.Isend(a, 0, a.length, MPI.INT, rank * 2, a.length);
        int[] b = Arrays.copyOfRange(source, source.length / 2, source.length);
        var reqB = MPI.COMM_WORLD.Isend(b, 0, b.length, MPI.INT, rank * 2 + 1, b.length);
        Request.Waitall(new Request[] {reqA, reqB});
        var respA = MPI.COMM_WORLD.Irecv(a, 0, a.length, MPI.INT, rank * 2, a.length);
        var respB = MPI.COMM_WORLD.Irecv(b, 0, b.length, MPI.INT, rank * 2 + 1, b.length);
        Request.Waitall(new Request[]{respA, respB});
        System.out.println("& " + rank + " -> " + (a.length + b.length));
        return merge(a, b);
    }

    private static int[] merge(int[] a, int[] b) {
        int[] c = new int[a.length + b.length];
        int ic = 0;
        int ia = 0;
        int ib = 0;
        while (ic != c.length) {
            if (ia == a.length) {
                c[ic] = b[ib];
                ib++;
            } else if (ib == b.length) {
                c[ic] = a[ia];
                ia++;
            } else {
                if (a[ia] > b[ib]) {
                    c[ic] = b[ib];
                    ib++;
                } else {
                    c[ic] = a[ia];
                    ia++;
                }
            }
            ic++;
        }
        return c;
    }
}
