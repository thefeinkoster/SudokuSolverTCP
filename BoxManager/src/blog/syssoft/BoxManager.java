package blog.syssoft;

import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BoxManager {

    public static void main(String[] args) {
        System.out.println("Sudoku Solver - blog.syssoft.BoxManager");

        if (args.length != 1) {
            System.err.println("Usage: blog.syssoft.BoxManager <port>");
            System.exit(-1);
        }

        String[] boxnames = {
                "BOX_A1", "BOX_D1", "BOX_G1",
                "BOX_A4", "BOX_D4", "BOX_G4",
                "BOX_A7", "BOX_D7", "BOX_G7",
                "BOX_UL", "BOX_UM", "BOX_UR",
                "BOX_ML", "BOX_MM", "BOX_MR",
                "BOX_LL", "BOX_LM", "BOX_LR",
                "TEST"
        };
        Set<String> possibleBoxnames = Set.of(boxnames);

        int port = Integer.parseInt(args[0]);

        TCPServer server = new TCPServer(port);

        int nBoxesConnected = 0;

        while (nBoxesConnected < 9) {
            Socket c = server.acceptClient();
            ClientProxy cp = new ClientProxy(c,boxMap,possibleBoxnames);
            clients.put(c,cp);
            if (cp.isGoodclient()) {
                System.out.printf("Box %s at %s and port %s registered\n",
                        cp.getName(), cp.getAddress(), cp.getPort());
                boxMap.put(cp.getName(), cp);
                nBoxesConnected++;
            }
            else {
                System.out.println("Client with inappropriate initial message or test message tried to connect");
                cp.close();
            }
        }

        System.out.println("All 9 boxes are running ... start answering queries");
        for (Map.Entry<String,ClientProxy> b : boxMap.entrySet()) {
            b.getValue().start();
        }

        System.out.println("And the results are:");
        for (Map.Entry<String,ClientProxy> b : boxMap.entrySet()) {
            ClientProxy cp = b.getValue();
            cp.join();
            String[] resString = cp.getResult().split(",", 3);

            // resolve result string to values
            int[] values = new int[9];
            for (int i=0; i < resString[2].length(); i++)
                values[i] = resString[2].charAt(i) - '0';

            System.out.printf("" +
                            "##### " + resString[1] + " #####\n" +
                            "╭─────┬─────┬─────╮\n" +
                            "│  %d  │  %d  │  %d  │\n" +
                            "├─────┼─────┼─────┤\n" +
                            "│  %d  │  %d  │  %d  │\n" +
                            "├─────┼─────┼─────┤\n" +
                            "│  %d  │  %d  │  %d  │\n" +
                            "╰─────┴─────┴─────╯\n"
                    , values[0], values[1], values[2], values[3], values[4], values[5], values[6], values[7], values[8]);
        }
    }

    private static Map<Socket,ClientProxy> clients = new HashMap<Socket,ClientProxy>();
    private static Map<String,ClientProxy> boxMap = new HashMap<String,ClientProxy>();
}
