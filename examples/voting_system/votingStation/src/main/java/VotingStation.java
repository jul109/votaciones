public class VotingStation
{
    public static void main(String[] args)
    {
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args))
        {
            com.zeroc.Ice.ObjectPrx base = communicator.stringToProxy("SimplePrinter:default -h localhost -p 10000");
            Voting.PrinterPrx printer = Voting.PrinterPrx.checkedCast(base);
            if(printer == null)
            {
                throw new Error("Invalid proxy");
            }
            printer.printString("Hello World!");
        }
    }
}
