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
                else if (currentLine.equals("dimd"))
                    zGPB.INSTANCE.gradeListener.isEnabled = false;
                else if(currentLine.equals("backup"))
                    zGPB.INSTANCE.databaseHandler.executeStatement("BACKUP TO data.db");
                else
                    zGPB.INSTANCE.databaseHandler.executeStatement(currentLine);
            }

        }).start();
    }

}
