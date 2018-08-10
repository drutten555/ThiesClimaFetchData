package se.chalmers.LivingLab;

import java.io.*;
import java.net.Socket;

public class ThiesClimaFetchData
{
    public static final String hostname = "192.168.1.10";
    public static final int port = 23;
    public static final String command = "MM";
    public static final String STX = new String(new byte[] { (byte)0x02 });
    public static final String ETX = new String(new byte[] { (byte)0x03 });

    private	Socket mySocket;
    private	BufferedReader input;
    private	PrintStream output;


    public static void main(String[] args)
    {
        new ThiesClimaFetchData();
    }

    public ThiesClimaFetchData()
    {
        try
        {
            setUpConnection();
            gatherData();
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
        finally
        {
            try { cleanUpAndClose(); } catch (IOException e) {}
        }
    }

    private void setUpConnection() throws IOException
    {
        mySocket = new Socket(hostname, port);
        input = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
        output = new PrintStream(mySocket.getOutputStream());
    }

    private void gatherData() throws IOException
    {
        String responseLine;

        output.println(STX + command + ETX);
        output.flush();

        while ((responseLine = input.readLine()) != null)
        {
            // System.out.println(responseLine);
            parseOutPut(responseLine);
            if (responseLine.startsWith("END") || responseLine.equals("Command not processed"))
                break;
        }
    }

    private void parseOutPut(String line)
    {
        String name;
        String value;
        String unit = "";
        int colonPos = line.indexOf(':');
        int i = 0;

        if (colonPos == -1)
            return;

        if (line.indexOf('?') >= 0) // Ignore null values.
            return;

        if (line.indexOf('!') >= 0) // Warn about errors.
        {
            System.err.println("Measurement error:" + line);
            return;
        }

        name = line.substring(0, colonPos).trim();
        value = line.substring(colonPos + 1, line.length()).trim();

        if (value.length() == 0)
            return;

        if (name.equals("Date / Time"))
        {
            // Fix time format?
            unit = "time";
        }
        else
        {
            while (i < value.length() && ((value.charAt(i) >= '0' && value.charAt(i) <= '9') || value.charAt(i) == '.' || value.charAt(i) == '-'))
                i++;

            if (i < value.length())
            {
                unit = value.substring(i, value.length());
                value = value.substring(0, i);
            }
        }

        System.out.println(name + "\t" + value + "\t" + unit);
    }

    private void cleanUpAndClose() throws IOException
    {
        if (output != null)
            output.close();

        if (input != null)
            input.close();

        if (mySocket != null)
            mySocket.close();
    }
}
