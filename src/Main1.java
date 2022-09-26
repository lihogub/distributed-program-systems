import mpi.*;

public class Main1 {
    public static void main(String[] args) {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int[] sendBuffer = {rank};
        int[] receiveBuffer = new int[1];

        if ((rank % 2) == 0) {
            if((rank + 1) != size) {
                MPI.COMM_WORLD.Send(sendBuffer, 0, 1, MPI.INT, rank + 1, 0);
            }
        } else {
            MPI.COMM_WORLD.Recv(receiveBuffer, 0, 1, MPI.INT, rank - 1, 0);
            System.out.printf("Received: %d from %d to %d\n", receiveBuffer[0], rank - 1, rank);
        }

        MPI.Finalize();
    }
}