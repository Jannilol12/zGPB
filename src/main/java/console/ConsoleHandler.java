package console;

import main.JADB;

import java.util.Scanner;

public class ConsoleHandler {

    public void checkInput() {

        Scanner consoleScanner = new Scanner(System.in);

        while (consoleScanner.hasNextLine()) {

            String currentLine = consoleScanner.nextLine();

            if (currentLine.equals("quit"))
                System.exit(0);
            else if (currentLine.equals("dump"))
                JADB.INSTANCE.databaseHandler.dumpDatabase();
            else if(currentLine.equals("dimd"))
                JADB.INSTANCE.gradeListener.isEnabled = false;
            else
                System.out.println(JADB.INSTANCE.databaseHandler.getResultFromQuery(currentLine, false));
        }

    }

}
