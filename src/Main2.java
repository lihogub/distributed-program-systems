import mpi.*;

public class Main2 {
    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int[] sendBuffer = {rank};
        int[] receiveBuffer = new int[1];

        if(rank != 0) {
            MPI.COMM_WORLD.Recv(receiveBuffer,0, 1, MPI.INT, rank - 1, 0);
            System.out.println("Process " + rank + " got "+ receiveBuffer[0]);

            if (rank != 1) {
                sendBuffer[0] += receiveBuffer[0];
            }
        }

        if (rank == 0) {
            MPI.COMM_WORLD.Sendrecv(sendBuffer, 0, 1, MPI.INT, (rank + 1) % size, 0, receiveBuffer,0, 1, MPI.INT, size-1, 0);
            System.out.println("Total sum: " + receiveBuffer[0]);
        } else {
            MPI.COMM_WORLD.Rsend(sendBuffer, 0, 1, MPI.INT, (rank + 1) % size, 0);
        }


        MPI.Finalize();
    }
}
