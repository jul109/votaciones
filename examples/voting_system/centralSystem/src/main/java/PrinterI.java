public class PrinterI implements Voting.Printer
{
    public void printString(String s, com.zeroc.Ice.Current current)
    {
        System.out.println(s);
    }
}
