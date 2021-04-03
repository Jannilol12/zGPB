package console;

import main.zGPB;

import java.util.Scanner;

public class ConsoleHandler {

    public void checkInput() {
        new Thread(() -> {
            Scanner consoleScanner = new Scanner(System.in);

            while (consoleScanner.hasNextLine()) {

                String currentLine = consoleScanner.nextLine();

                if (currentLine.equals("quit"))
                    System.exit(0);
                else if (currentLine.equals("dump"))
                    zGPB.INSTANCE.databaseHandler.dumpDatabase();
                else if (currentLine.equals("dimd"))
                    zGPB.INSTANCE.gradeListener.isEnabled = false;
                else
                    System.out.println(zGPB.INSTANCE.databaseHandler.getResultFromQuery(currentLine, false));
            }

        }).start();
    }

}
