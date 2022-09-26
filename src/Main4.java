import mpi.MPI;
import mpi.Status;

import java.util.Arrays;

public class Main4 {
    public static void main(String[] args) {
        int[] data = new int[1];
        int[] buf = {1,3,5};
        int count, TAG = 0;
        Status st;
        data[0] = 2016;
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        if(rank == 0)
        {
            MPI.COMM_WORLD.Send(data, 0, 1, MPI.INT, 2, TAG);
        }
        else if(rank == 1){
            MPI.COMM_WORLD.Send(buf, 0, buf.length, MPI.INT, 2, TAG);
        }
        else if(rank == 2){
            st = MPI.COMM_WORLD.Probe(0, TAG);
            count = st.Get_count(MPI.INT);
            int[] back_buf = new int[count];
            MPI.COMM_WORLD.Recv(back_buf,0,count, MPI.INT, 0, TAG);
            System.out.print("Rank = 0 ");
            print(back_buf);
            st = MPI.COMM_WORLD.Probe(1, TAG);
            count = st.Get_count(MPI.INT);
            int[] back_buf2 = new int[count];
            MPI.COMM_WORLD.Recv(back_buf2,0,count,MPI.INT,1,TAG);
            System.out.print("Rank = 1 ");
            print(back_buf2);
        }
        MPI.Finalize();
    }

    public static void print(int[] arr) {
        System.out.println(Arrays.toString(arr));
    }
}
